package com.coreoz.plume.admin;

import com.coreoz.plume.admin.services.logApi.LogApiService;
import com.coreoz.plume.admin.services.logApi.LogInterceptApiBean;
import com.coreoz.plume.admin.services.logApi.LogInterceptHeaderBean;
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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.EOFException;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;


public class OkHttpLoggerInterceptor implements Interceptor {

    private static final Charset UTF8 = Charset.forName("UTF-8");

    private final String apiName;
    private final LogApiService logApiService;
    private final Logger logger;

    public OkHttpLoggerInterceptor(String apiName, LogApiService logApiService) {
        this.apiName = apiName;
        this.logApiService = logApiService;
        this.logger = LoggerFactory.getLogger("api.http");
    }

    @Override
    public Response intercept(Chain chain) throws IOException {
        Request request = chain.request();
        List<LogInterceptHeaderBean> requestHeaders = new ArrayList<>();
        List<LogInterceptHeaderBean> responseHeaders = new ArrayList<>();
        if (logger.isDebugEnabled()) {
            RequestBody requestBody = request.body();
            boolean hasRequestBody = requestBody != null;
            Connection connection = chain.connection();
            String requestStartMessage = "--> " + request.method() + ' ' + request.url() + (connection != null ? " " + connection.protocol() : "");
            if (hasRequestBody) {
                requestStartMessage = requestStartMessage + " (" + requestBody.contentLength() + "-byte body)";
            }
            this.logger.info(requestStartMessage);
            if (hasRequestBody) {
                if (requestBody.contentType() != null) {
                    LogInterceptHeaderBean logInterceptHeaderBean = new LogInterceptHeaderBean("Content-Type", requestBody.contentType().toString());
                    requestHeaders.add(logInterceptHeaderBean);
                    this.logger.info("Content-Type: " + requestBody.contentType());
                }

                if (requestBody.contentLength() != -1L) {
                    this.logger.info("Content-Length: " + requestBody.contentLength());
                }

                Headers headers = request.headers();
                int i = 0;

                for (int count = headers.size(); i < count; ++i) {
                    String name = headers.name(i);
                    if (!"Content-Type".equalsIgnoreCase(name) && !"Content-Length".equalsIgnoreCase(name)) {
                        LogInterceptHeaderBean logInterceptHeaderBean = new LogInterceptHeaderBean(headers.name(i), headers.value(i));
                        requestHeaders.add(logInterceptHeaderBean);
                        this.logger.info(name + ": " + headers.value(i));
                    }
                }
                if (this.bodyHasUnknownEncoding(request.headers())) {
                    this.logger.info("--> END " + request.method() + " (encoded body omitted)");
                } else {
                    Buffer buffer = new Buffer();
                    requestBody.writeTo(buffer);
                    Charset charset = UTF8;
                    MediaType contentType = requestBody.contentType();
                    if (contentType != null) {
                        charset = contentType.charset(UTF8);
                    }

                    this.logger.info("");
                    if (isPlaintext(buffer)) {
                        this.logger.info(buffer.readString(charset));
                        this.logger.info("--> END " + request.method() + " (" + requestBody.contentLength() + "-byte body)");
                    } else {
                        this.logger.info("--> END " + request.method() + " (binary " + requestBody.contentLength() + "-byte body omitted)");
                    }
                }
            } else {
                this.logger.info("--> END " + request.method());
            }

            long startNs = System.nanoTime();

            Response response;
            try {
                response = chain.proceed(request);
            } catch (Exception var27) {
                this.logger.info("<-- HTTP FAILED: " + var27);
                throw var27;
            }

            Buffer bufferRs = new Buffer();

            long tookMs = TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - startNs);
            ResponseBody responseBody = response.body();

            long contentLength = responseBody.contentLength();
            String bodySize = contentLength != -1L ? contentLength + "-byte" : "unknown-length";
            this.logger.info("<-- " + response.code() + (response.message().isEmpty() ? "" : ' ' + response.message()) + ' ' + response.request().url() + " (" + tookMs + "ms" + ", " + bodySize + " body" + ')');

            Headers headers = response.headers();
            int i = 0;

            for (int count = headers.size(); i < count; ++i) {
                LogInterceptHeaderBean logInterceptHeaderBean = new LogInterceptHeaderBean(headers.name(i), headers.value(i));
                responseHeaders.add(logInterceptHeaderBean);
                this.logger.info(headers.name(i) + ": " + headers.value(i));
            }

            if (HttpHeaders.hasBody(response)) {
                if (this.bodyHasUnknownEncoding(response.headers())) {
                    this.logger.info("<-- END HTTP (encoded body omitted)");
                } else {
                    BufferedSource source = responseBody.source();
                    source.request(9223372036854775807L);
                    Buffer buffer = source.buffer();
                    bufferRs = source.buffer();
                    Long gzippedLength = null;
                    if ("gzip".equalsIgnoreCase(headers.get("Content-Encoding"))) {
                        gzippedLength = buffer.size();
                        GzipSource gzippedResponseBody = null;

                        try {
                            gzippedResponseBody = new GzipSource(buffer.clone());
                            buffer = new Buffer();
                            buffer.writeAll(gzippedResponseBody);
                            bufferRs.writeAll(gzippedResponseBody);
                        } finally {
                            if (gzippedResponseBody != null) {
                                gzippedResponseBody.close();
                            }
                        }
                    }

                    Charset charset = UTF8;
                    MediaType contentType = responseBody.contentType();
                    if (contentType != null) {
                        charset = contentType.charset(UTF8);
                    }

                    if (!isPlaintext(buffer)) {
                        this.logger.info("");
                        this.logger.info("<-- END HTTP (binary " + buffer.size() + "-byte body omitted)");
                        return response;
                    }

                    if (contentLength != 0L) {
                        this.logger.info("");
                        this.logger.info(buffer.clone().readString(charset));
                    }

                    if (gzippedLength != null) {
                        this.logger.info("<-- END HTTP (" + buffer.size() + "-byte, " + gzippedLength + "-gzipped-byte body)");
                    } else {
                        this.logger.info("<-- END HTTP (" + buffer.size() + "-byte body)");
                    }
                }
            } else {
                this.logger.info("<-- END HTTP");
            }
            Buffer bufferRq = new Buffer();
            if (hasRequestBody) {
                requestBody.writeTo(bufferRq);
            }

            LogInterceptApiBean logInterceptApiBean = new LogInterceptApiBean(
                request.url().toString(),
                request.method(),
                String.valueOf(response.code()),
                bufferRq.clone().readString(UTF8),
                bufferRs.clone().readString(UTF8),
                requestHeaders,
                responseHeaders,
                this.apiName
            );
            logApiService.saveLog(logInterceptApiBean);
            return response;
        }


        return null;
    }

    static boolean isPlaintext(Buffer buffer) {
        try {
            Buffer prefix = new Buffer();
            long byteCount = buffer.size() < 64L ? buffer.size() : 64L;
            buffer.copyTo(prefix, 0L, byteCount);

            for(int i = 0; i < 16 && !prefix.exhausted(); ++i) {
                int codePoint = prefix.readUtf8CodePoint();
                if (Character.isISOControl(codePoint) && !Character.isWhitespace(codePoint)) {
                    return false;
                }
            }

            return true;
        } catch (EOFException var6) {
            return false;
        }
    }

    private boolean bodyHasUnknownEncoding(Headers headers) {
        String contentEncoding = headers.get("Content-Encoding");
        return contentEncoding != null && !contentEncoding.equalsIgnoreCase("identity") && !contentEncoding.equalsIgnoreCase("gzip");
    }


}
