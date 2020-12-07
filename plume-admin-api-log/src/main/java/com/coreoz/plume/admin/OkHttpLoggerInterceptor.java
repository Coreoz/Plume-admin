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
    private final BiPredicate<Request, Response> okHttpLoggerFiltersFunction;

    public OkHttpLoggerInterceptor(String apiName, LogApiService logApiService, BiPredicate<Request, Response> okHttpLoggerFiltersFunction) {
        this.apiName = apiName;
        this.logApiService = logApiService;
        this.okHttpLoggerFiltersFunction = okHttpLoggerFiltersFunction;
    }

    public OkHttpLoggerInterceptor(String apiName, LogApiService logApiService) {
        this(apiName, logApiService, ALWAYS_FALSE_BI_PREDICATE);
    }

    @NotNull
    @Override
    public Response intercept(Chain chain) throws IOException {
        Request request = chain.request();
        Connection connection = chain.connection();

        List<HttpHeader> requestHeaders;
        List<HttpHeader> responseHeaders;
        RequestBody requestBody = request.body();
        Buffer requestBodyBuffer = getRequestBodyBuffer(requestBody);
        String requestBodyString = getStringFromBuffer(requestBodyBuffer);

        String requestStartMessage = "--> " + request.method() + ' ' + request.url() + (connection != null ? " " + connection.protocol() : "");
        if (requestBody != null) {
            requestStartMessage = requestStartMessage + " (" + requestBody.contentLength() + "-byte body)";
        }
        logger.debug(requestStartMessage);

        Headers chainRequestHeaders = request.headers();
        requestHeaders = getAllHeaders(chainRequestHeaders, false);

        if (requestBody != null) {
            MediaType contentType = requestBody.contentType();
            if (contentType != null) {
                requestHeaders.add(getContentTypeHeader(requestBody));
            }

            if (this.bodyHasUnknownEncoding(request.headers())) {
                logger.debug("--> END {} (encoded body omitted)", request.method());
            } else {
                Charset charset = StandardCharsets.UTF_8;
                if (contentType != null) {
                    charset = contentType.charset(StandardCharsets.UTF_8);
                }

                if (isPlaintext(requestBodyBuffer)) {
                    logger.debug(requestBodyBuffer.readString(charset));
                    logger.debug("--> END {} ({}-byte body)", request.method(), requestBody.contentLength());
                } else {
                    logger.debug("--> END {} (binary {}-byte body omitted)", request.method(), requestBody.contentLength());
                }
            }
        } else {
            logger.debug("--> END {}", request.method());
        }

        long startNs = System.nanoTime();

        Response response = this.executeRequestAndGetResponse(chain, request, requestBodyString, requestHeaders);

        if (this.okHttpLoggerFiltersFunction.test(request, response)) {
            logger.debug("--> OkHttpRequest is filtered");
            logger.debug("--> END {}", request.method());
            return response;
        }

        long tookMs = TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - startNs);
        ResponseBody responseBody = response.body();
        String responseBodyString = null;

        long contentLength = responseBody.contentLength();
        String bodySize = contentLength != -1L ? contentLength + "-byte" : "unknown-length";
        String responseMessage = response.message().isEmpty() ? "-- no message --" : response.message();
        logger.debug("<-- {} {} {} ({} ms, {} body)",
            response.code(),
            responseMessage,
            response.request().url(),
            tookMs,
            bodySize
        );

        Headers chainResponseHeaders = response.headers();
        responseHeaders = getAllHeaders(chainResponseHeaders, true);

        if (HttpHeaders.promisesBody(response)) {
            if (this.bodyHasUnknownEncoding(response.headers())) {
                logger.debug("<-- END HTTP (encoded body omitted)");
            } else {
                BufferedSource source = responseBody.source().peek();
                source.request(Long.MAX_VALUE);
                Buffer buffer = source.getBuffer();
                responseBodyString = getStringFromBuffer(buffer);

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

                if (!isPlaintext(buffer)) {
                    logger.debug("");
                    logger.debug("<-- END HTTP (binary {}-byte body omitted)", buffer.size());
                    return response;
                }

                if (contentLength != 0L) {
                    logger.debug("");
                    logger.debug(buffer.clone().readString(charset));
                }
            }
        } else {
            logger.debug("<-- END HTTP");
        }

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

    private boolean bodyHasUnknownEncoding(Headers headers) {
        String contentEncoding = headers.get("Content-Encoding");
        return contentEncoding != null && !contentEncoding.equalsIgnoreCase("identity") && !contentEncoding.equalsIgnoreCase("gzip");
    }

    private static HttpHeader getContentTypeHeader(RequestBody requestBody) throws IOException {
        logger.debug("Content-Type: {}", requestBody.contentType());
        logger.debug("Content-Length: {}", requestBody.contentLength());
        return new HttpHeader("Content-Type", requestBody.contentType().toString());
    }

    private static List<HttpHeader> getAllHeaders(Headers headers, boolean withContentType) {
        List<HttpHeader> httpHeaders = new ArrayList<>();
        for (int count = 0; count < headers.size(); count++) {
            String name = headers.name(count);
            if (withContentType || !"Content-Type".equalsIgnoreCase(name) && !"Content-Length".equalsIgnoreCase(name)) {
                httpHeaders.add(new HttpHeader(headers.name(count), headers.value(count)));
                logger.debug("{}: {}", name, headers.value(count));
            }
        }
        return httpHeaders;
    }

    private static String getStringFromBuffer(Buffer buffer) {
        if (buffer == null) {
            return null;
        }
        return buffer.clone().readString(StandardCharsets.UTF_8);
    }

    private static Buffer getRequestBodyBuffer(RequestBody requestBody) throws IOException {
        if (requestBody == null) {
            return null;
        }
        Buffer bufferRq = new Buffer();
        requestBody.writeTo(bufferRq);
        return bufferRq;
    }

}
