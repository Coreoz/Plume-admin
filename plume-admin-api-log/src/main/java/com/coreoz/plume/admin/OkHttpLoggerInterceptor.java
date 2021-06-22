package com.coreoz.plume.admin;

import java.io.EOFException;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;

import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.coreoz.plume.admin.services.logapi.HttpHeader;
import com.coreoz.plume.admin.services.logapi.LogApiService;
import com.coreoz.plume.admin.services.logapi.LogInterceptApiBean;
import com.google.common.base.Throwables;
import com.google.common.collect.ImmutableList;

import lombok.NonNull;
import okhttp3.Connection;
import okhttp3.Headers;
import okhttp3.Interceptor;
import okhttp3.MediaType;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okhttp3.ResponseBody;
import okhttp3.internal.http.HttpHeaders;
import okio.Buffer;
import okio.BufferedSource;
import okio.GzipSource;

public class OkHttpLoggerInterceptor implements Interceptor {
    private static final Logger logger = LoggerFactory.getLogger("api.http");

    private final String apiName;
    private final LogApiService logApiService;
    private final Predicate<Request> requestFilterPredicate;
    private final LogEntryTransformer logEntryTransformer;

    public OkHttpLoggerInterceptor(
        String apiName,
        LogApiService logApiService,
        Predicate<Request> requestFilterPredicate,
        LogEntryTransformer logEntryTransformer
    ) {
        this.apiName = apiName;
        this.logApiService = logApiService;
        this.requestFilterPredicate = requestFilterPredicate;
        this.logEntryTransformer = logEntryTransformer;
    }

    public OkHttpLoggerInterceptor(String apiName, LogApiService logApiService) {
        this(apiName, logApiService, request -> true, (request, response, trace) -> trace);
    }

    public OkHttpLoggerInterceptor(String apiName, LogApiService logApiService, RequestPredicate requestFilterPredicate) {
    	this(apiName, logApiService, requestFilterPredicate, (request, response, trace) -> trace);
    }

    public OkHttpLoggerInterceptor(String apiName, LogApiService logApiService, LogEntryTransformer logEntryTransformer) {
        this(apiName, logApiService, request -> true, logEntryTransformer);
    }

    @NotNull
    @Override
    public Response intercept(Chain chain) throws IOException {
        Request request = chain.request();
        Connection connection = chain.connection();

        if (!this.requestFilterPredicate.test(request)) {
            logger.debug("--> Request {} is marked as filtered", request.url());
            return chain.proceed(request);
        }

        Headers chainRequestHeaders = request.headers();
        List<HttpHeader> requestHeaders = readHeaders(chainRequestHeaders);
        RequestBody requestBody = request.body();
        String requestMethod = request.method();
        String requestBodyString = null;

        String requestStartMessage = String.format("--> %s %s%s", request.method(), request.url(), (connection != null ? " " + connection.protocol() : ""));
        if (requestBody != null) {
            requestStartMessage = String.format("%s (%s-byte body)", requestStartMessage, requestBody.contentLength());
        }
        logger.debug(requestStartMessage);

        if (requestBody != null) {
            requestBodyString = logRequestBody(requestBody, requestMethod, bodyHasUnknownEncoding(chainRequestHeaders));
        } else {
            logger.debug("--> END {}", requestMethod);
        }

        long startMs = System.currentTimeMillis();
        Response response = executeRequest(
            chain,
            request,
            requestBodyString,
            requestHeaders
        );
        long tookMs = System.currentTimeMillis() - startMs;

        Headers chainResponseHeaders = response.headers();
        List<HttpHeader> responseHeaders = readHeaders(chainResponseHeaders);
        ResponseBody responseBody = response.body();
        String responseBodyString = null;

        long contentLength;
        String responseMessage = "-- no message --";
        String bodySize = "unknown-length";

        if (responseBody != null) {
            contentLength = responseBody.contentLength();
            bodySize = contentLength != -1L ? contentLength + "-byte" : "unknown-length";
            responseMessage = response.message().isEmpty() ? "-- no message --" : response.message();

            Buffer buffer = responseBodyToBuffer(responseBody);
            if (!isPlaintext(buffer)) {
                logger.debug("");
                logger.debug("<-- END HTTP (binary {}-byte body omitted)", buffer.size());
                return response;
            }
            if (HttpHeaders.promisesBody(response)) {
                responseBodyString = readAndLogResponseBody(responseBody, chainResponseHeaders, buffer);
            } else {
                logger.debug("<-- END HTTP");
            }
            if (contentLength != -1L) {
                logger.debug(responseBodyString);
            }
        } else {
            logger.debug("--> END {}", requestMethod);
        }

        logger.debug("<-- {} {} {} ({} ms, {} body)",
            response.code(),
            responseMessage,
            response.request().url(),
            tookMs,
            bodySize
        );

        LogInterceptApiBean logInterceptApiBean = new LogInterceptApiBean(
            request.url().toString(),
            request.method(),
            response.code(),
            requestBodyString,
            responseBodyString,
            requestHeaders,
            responseHeaders,
            this.apiName
        );

        logApiService.saveLog(this.logEntryTransformer.transform(request, response, logInterceptApiBean));
        return response;
    }

    static boolean isPlaintext(Buffer buffer) {
        try {
            Buffer prefix = new Buffer();
            long byteCount = Math.min(buffer.size(), 64L);
            buffer.copyTo(prefix, 0L, byteCount);

            for (int i = 0; i < 16 && !prefix.exhausted(); ++i) {
                int codePoint = prefix.readUtf8CodePoint();
                if (Character.isISOControl(codePoint) && !Character.isWhitespace(codePoint)) {
                    return false;
                }
            }

            return true;
        } catch (EOFException eofException) {
            return false;
        }
    }

    private Response executeRequest(
        Chain chain,
        Request request,
        String requestBodyString,
        List<HttpHeader> requestHeaders
    ) throws IOException {
        try {
            return chain.proceed(request);
        } catch (Exception e) {
            logger.debug("<-- HTTP FAILED : {}", e.getMessage());

            LogInterceptApiBean logInterceptApiBean = new LogInterceptApiBean(
                request.url().toString(),
                request.method(),
                500,
                requestBodyString,
                Throwables.getStackTraceAsString(e),
                requestHeaders,
                ImmutableList.of(),
                this.apiName
            );
            logApiService.saveLog(logInterceptApiBean);

            throw e;
        }
    }

    private static boolean bodyHasUnknownEncoding(Headers headers) {
        String contentEncoding = headers.get("Content-Encoding");
        return contentEncoding != null
            && !contentEncoding.equalsIgnoreCase("identity")
            && !contentEncoding.equalsIgnoreCase("gzip");
    }

    private static String logRequestBody(@NonNull RequestBody requestBody, String method, boolean hasUnknownEncoding) throws IOException {
        String requestBodyString;
        Buffer requestBodyBuffer = requestBodyToBuffer(requestBody);
        MediaType contentType = requestBody.contentType();

        if (contentType != null) {
            logger.debug("Content-Type: {}", requestBody.contentType());
            logger.debug("Content-Length: {}", requestBody.contentLength());
        }

        if (!hasUnknownEncoding) {
            requestBodyString = bufferToString(requestBodyBuffer, StandardCharsets.UTF_8);
            logger.debug("--> END {} (encoded body omitted)", method);
        } else {
            Charset charset = StandardCharsets.UTF_8;
            if (contentType != null) {
                charset = contentType.charset(StandardCharsets.UTF_8);
            }
            requestBodyString = bufferToString(requestBodyBuffer, charset);

            if (isPlaintext(requestBodyBuffer)) {
                logger.debug(requestBodyString);
                logger.debug("--> END {} ({}-byte body)", method, requestBody.contentLength());
            } else {
                logger.debug("--> END {} (binary {}-byte body omitted)", method, requestBody.contentLength());
            }
        }

        return requestBodyString;
    }

    private static String readAndLogResponseBody(ResponseBody responseBody, Headers chainResponseHeaders, Buffer buffer) throws IOException {
        String responseBodyString = null;
        if (bodyHasUnknownEncoding(chainResponseHeaders)) {
            logger.debug("<-- END HTTP (encoded body omitted)");
        } else {
            if ("gzip".equalsIgnoreCase(chainResponseHeaders.get("Content-Encoding"))) {
                long gzippedLength = buffer.size();
                try (GzipSource gzippedResponseBody = new GzipSource(buffer.clone())) {
                    buffer = new Buffer();
                    buffer.writeAll(gzippedResponseBody);
                }
                logger.debug("<-- END HTTP ({}-byte, {}-gzipped-byte body)", buffer.size(), gzippedLength);
            } else {
                logger.debug("<-- END HTTP ({}-byte body)", buffer.size());
            }

            Charset charset = StandardCharsets.UTF_8;
            MediaType contentType = responseBody.contentType();
            if (contentType != null) {
                charset = contentType.charset(StandardCharsets.UTF_8);
            }

            responseBodyString = bufferToString(buffer, charset);
        }
        return responseBodyString;
    }

    private static List<HttpHeader> readHeaders(Headers headers) {
        List<HttpHeader> httpHeaders = new ArrayList<>();
        for (int count = 0; count < headers.size(); count++) {
            String name = headers.name(count);
            String headerValue = headers.value(count);
            httpHeaders.add(new HttpHeader(headers.name(count), headerValue));
            logger.debug("{}: {}", name, headerValue);
        }
        return httpHeaders;
    }

    private static String bufferToString(Buffer buffer, Charset charset) {
        if (buffer == null) {
            return null;
        }
        return buffer.clone().readString(charset);
    }

    private static Buffer requestBodyToBuffer(RequestBody requestBody) throws IOException {
        if (requestBody == null) {
            return null;
        }
        Buffer bufferRq = new Buffer();
        requestBody.writeTo(bufferRq);
        return bufferRq;
    }

    private static Buffer responseBodyToBuffer(ResponseBody responseBody) throws IOException {
        if (responseBody == null) {
            return null;
        }
        final BufferedSource source = responseBody.source();
        source.request(Long.MAX_VALUE);
        return source.getBuffer();
    }

}
