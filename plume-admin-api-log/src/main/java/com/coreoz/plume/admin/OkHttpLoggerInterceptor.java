package com.coreoz.plume.admin;

import com.coreoz.plume.admin.services.logApi.HttpHeader;
import com.coreoz.plume.admin.services.logApi.LogApiService;
import com.coreoz.plume.admin.services.logApi.LogInterceptApiBean;
import com.google.common.base.Throwables;
import com.google.common.collect.ImmutableList;
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
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.EOFException;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.function.BiPredicate;

public class OkHttpLoggerInterceptor implements Interceptor {

    private static final Logger logger = LoggerFactory.getLogger("api.http");
    private static final BiPredicate<Request, Response> ALWAYS_FALSE_BI_PREDICATE = (request, response) -> false;

    private final String apiName;
    private final LogApiService logApiService;
    private final BiPredicate<Request, Response> loggingFilter;

    public OkHttpLoggerInterceptor(String apiName, LogApiService logApiService, BiPredicate<Request, Response> okHttpLoggerFiltersFunction) {
        this.apiName = apiName;
        this.logApiService = logApiService;
        this.loggingFilter = okHttpLoggerFiltersFunction;
    }

    public OkHttpLoggerInterceptor(String apiName, LogApiService logApiService) {
        this(apiName, logApiService, ALWAYS_FALSE_BI_PREDICATE);
    }

    @NotNull
    @Override
    public Response intercept(Chain chain) throws IOException {
        Request request = chain.request();
        Connection connection = chain.connection();

        Headers chainRequestHeaders = request.headers();
        List<HttpHeader> requestHeaders = getAllHeaders(chainRequestHeaders);
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

        Response response = executeRequestAndGetResponse(
            chain,
            request,
            requestBodyString,
            requestHeaders
        );

        if (this.loggingFilter.test(request, response)) {
            logger.debug("--> {} is mark as filtered", request.url());
            logger.debug("--> END {}", requestMethod);
            return response;
        }

        Headers chainResponseHeaders = response.headers();
        List<HttpHeader> responseHeaders = getAllHeaders(chainResponseHeaders);
        ResponseBody responseBody = response.body();
        String responseBodyString = null;

        long startNs = System.nanoTime();
        long tookMs = TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - startNs);
        long contentLength = -1L;
        String responseMessage = "-- no message --";
        String bodySize = "unknown-length";

        if (responseBody != null) {
            contentLength = responseBody.contentLength();
            bodySize = contentLength != -1L ? contentLength + "-byte" : "unknown-length";
            responseMessage = response.message().isEmpty() ? "-- no message --" : response.message();

            Buffer buffer = getResponseBodyBuffer(responseBody);
            if (buffer == null) {
                logger.debug("<-- END HTTP (body response omitted)");
                return response;
            }
            if (!isPlaintext(buffer)) {
                logger.debug("");
                logger.debug("<-- END HTTP (binary {}-byte body omitted)", buffer.size());
                return response;
            }
            if (HttpHeaders.promisesBody(response)) {
                responseBodyString = logResponseBody(responseBody, chainResponseHeaders, buffer);
            } else {
                logger.debug("<-- END HTTP");
            }
            if (contentLength != -1L) {
                logger.debug("");
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
        logApiService.saveLog(logInterceptApiBean);
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

    private Response executeRequestAndGetResponse(
        Chain chain,
        Request request,
        String requestBodyString,
        List<HttpHeader> requestHeaders
    ) throws IOException {
        try {
            return chain.proceed(request);
        } catch (Exception e) {
            logger.debug("<-- HTTP FAILED: ", e);

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

    private static String logRequestBody(RequestBody requestBody, String method, boolean hasUnknownEncoding) throws IOException {
        String requestBodyString;
        Buffer requestBodyBuffer = getRequestBodyBuffer(requestBody);
        MediaType contentType = requestBody.contentType();

        if (contentType != null) {
            logger.debug("Content-Type: {}", requestBody.contentType());
            logger.debug("Content-Length: {}", requestBody.contentLength());
        }

        if (!hasUnknownEncoding) {
            requestBodyString = getStringFromBuffer(requestBodyBuffer, StandardCharsets.UTF_8);
            logger.debug("--> END {} (encoded body omitted)", method);
        } else {
            Charset charset = StandardCharsets.UTF_8;
            if (contentType != null) {
                charset = contentType.charset(StandardCharsets.UTF_8);
            }
            requestBodyString = getStringFromBuffer(requestBodyBuffer, charset);

            if (isPlaintext(requestBodyBuffer)) {
                logger.debug(requestBodyString);
                logger.debug("--> END {} ({}-byte body)", method, requestBody.contentLength());
            } else {
                logger.debug("--> END {} (binary {}-byte body omitted)", method, requestBody.contentLength());
            }
        }

        return requestBodyString;
    }

    private static String logResponseBody(ResponseBody responseBody, Headers chainResponseHeaders, Buffer buffer) throws IOException {
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

            responseBodyString = getStringFromBuffer(buffer, charset);
        }
        return responseBodyString;
    }

    private static List<HttpHeader> getAllHeaders(Headers headers) {
        List<HttpHeader> httpHeaders = new ArrayList<>();
        for (int count = 0; count < headers.size(); count++) {
            String name = headers.name(count);
            httpHeaders.add(new HttpHeader(headers.name(count), headers.value(count)));
            logger.debug("{}: {}", name, headers.value(count));
        }
        return httpHeaders;
    }

    private static String getStringFromBuffer(Buffer buffer, Charset charset) {
        if (buffer == null) {
            return null;
        }
        return buffer.clone().readString(charset);
    }

    private static Buffer getRequestBodyBuffer(RequestBody requestBody) throws IOException {
        if (requestBody == null) {
            return null;
        }
        Buffer bufferRq = new Buffer();
        requestBody.writeTo(bufferRq);
        return bufferRq;
    }

    private static Buffer getResponseBodyBuffer(ResponseBody responseBody) throws IOException {
        if (responseBody == null) {
            return null;
        }
        final BufferedSource source = responseBody.source();
        source.request(Long.MAX_VALUE);
        return source.getBuffer();
    }

}
