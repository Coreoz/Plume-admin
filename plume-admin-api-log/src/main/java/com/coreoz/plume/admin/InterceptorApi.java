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
import okhttp3.internal.platform.Platform;
import okio.Buffer;
import okio.BufferedSource;
import okio.GzipSource;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.io.EOFException;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

@Singleton
public class InterceptorApi implements Interceptor {
    private static final Charset UTF8 = Charset.forName("UTF-8");
    private final InterceptorApi.Logger logger;
    private volatile InterceptorApi.Level level;
    private String apiName;
    private LogInterceptApiBean logInterceptApiBean;
    private LogApiService logApiService;

    @Inject
    public InterceptorApi(LogApiService logApiService) {
        this(Logger.DEFAULT);
        this.logApiService = logApiService;
    }

    public InterceptorApi(InterceptorApi.Logger logger) {
        this.level = InterceptorApi.Level.NONE;
        this.logger = logger;
    }

    public InterceptorApi setLevel(InterceptorApi.Level level) {
        if (level == null) {
            throw new NullPointerException("level == null. Use Level.NONE instead.");
        } else {
            this.level = level;
            return this;
        }
    }

    public LogInterceptApiBean getLogInterceptApiBean() {
        return this.logInterceptApiBean;
    }

    public InterceptorApi.Level getLevel() {
        return this.level;
    }

    public Response intercept(Chain chain) throws IOException {
        InterceptorApi.Level level = this.level;
        Request request = chain.request();

        List<LogInterceptHeaderBean> requestHeaders = new ArrayList<>();
        List<LogInterceptHeaderBean> responseHeaders= new ArrayList<>();

        if (level == InterceptorApi.Level.NONE) {
            return chain.proceed(request);
        } else {
            boolean logBody = level == InterceptorApi.Level.BODY;
            boolean logHeaders = logBody || level == InterceptorApi.Level.HEADERS;
            RequestBody requestBody = request.body();
            boolean hasRequestBody = requestBody != null;
            Connection connection = chain.connection();
            String requestStartMessage = "--> " + request.method() + ' ' + request.url() + (connection != null ? " " + connection.protocol() : "");
            if (!logHeaders && hasRequestBody) {
                requestStartMessage = requestStartMessage + " (" + requestBody.contentLength() + "-byte body)";
            }

            this.logger.log(requestStartMessage);
            if (logHeaders) {
                if (hasRequestBody) {
                    if (requestBody.contentType() != null) {
                        LogInterceptHeaderBean logInterceptHeaderBean = new LogInterceptHeaderBean("Content-Type", requestBody.contentType().toString());
                        requestHeaders.add(logInterceptHeaderBean);
                        this.logger.log("Content-Type: " + requestBody.contentType());
                    }

                    if (requestBody.contentLength() != -1L) {
                        this.logger.log("Content-Length: " + requestBody.contentLength());
                    }
                }

                Headers headers = request.headers();
                int i = 0;

                for(int count = headers.size(); i < count; ++i) {
                    String name = headers.name(i);
                    if (!"Content-Type".equalsIgnoreCase(name) && !"Content-Length".equalsIgnoreCase(name)) {
                        LogInterceptHeaderBean logInterceptHeaderBean = new LogInterceptHeaderBean(headers.name(i), headers.value(i));
                        requestHeaders.add(logInterceptHeaderBean);
                        this.logger.log(name + ": " + headers.value(i));
                    }
                }

                if (logBody && hasRequestBody) {
                    if (this.bodyHasUnknownEncoding(request.headers())) {
                        this.logger.log("--> END " + request.method() + " (encoded body omitted)");
                    } else {
                        Buffer buffer = new Buffer();
                        requestBody.writeTo(buffer);
                        Charset charset = UTF8;
                        MediaType contentType = requestBody.contentType();
                        if (contentType != null) {
                            charset = contentType.charset(UTF8);
                        }

                        this.logger.log("");
                        if (isPlaintext(buffer)) {
                            this.logger.log(buffer.readString(charset));
                            this.logger.log("--> END " + request.method() + " (" + requestBody.contentLength() + "-byte body)");
                        } else {
                            this.logger.log("--> END " + request.method() + " (binary " + requestBody.contentLength() + "-byte body omitted)");
                        }
                    }
                } else {
                    this.logger.log("--> END " + request.method());
                }
            }

            long startNs = System.nanoTime();

            Response response;
            try {
                response = chain.proceed(request);
            } catch (Exception var27) {
                this.logger.log("<-- HTTP FAILED: " + var27);
                throw var27;
            }

            Buffer bufferRs = new Buffer();

            long tookMs = TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - startNs);
            ResponseBody responseBody = response.body();

            long contentLength = responseBody.contentLength();
            String bodySize = contentLength != -1L ? contentLength + "-byte" : "unknown-length";
            this.logger.log("<-- " + response.code() + (response.message().isEmpty() ? "" : ' ' + response.message()) + ' ' + response.request().url() + " (" + tookMs + "ms" + (!logHeaders ? ", " + bodySize + " body" : "") + ')');
            if (logHeaders) {
                Headers headers = response.headers();
                int i = 0;

                for(int count = headers.size(); i < count; ++i) {
                    LogInterceptHeaderBean logInterceptHeaderBean = new LogInterceptHeaderBean(headers.name(i), headers.value(i));
                    responseHeaders.add(logInterceptHeaderBean);
                    this.logger.log(headers.name(i) + ": " + headers.value(i));
                }

                if (logBody && HttpHeaders.hasBody(response)) {
                    if (this.bodyHasUnknownEncoding(response.headers())) {
                        this.logger.log("<-- END HTTP (encoded body omitted)");
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
                            this.logger.log("");
                            this.logger.log("<-- END HTTP (binary " + buffer.size() + "-byte body omitted)");
                            return response;
                        }

                        if (contentLength != 0L) {
                            this.logger.log("");
                            this.logger.log(buffer.clone().readString(charset));
                        }

                        if (gzippedLength != null) {
                            this.logger.log("<-- END HTTP (" + buffer.size() + "-byte, " + gzippedLength + "-gzipped-byte body)");
                        } else {
                            this.logger.log("<-- END HTTP (" + buffer.size() + "-byte body)");
                        }
                    }
                } else {
                    this.logger.log("<-- END HTTP");
                }
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

    public void setApiName(String apiName) {
        this.apiName = apiName;
    }

    public interface Logger {
        InterceptorApi.Logger DEFAULT = message -> Platform.get().log(4, message, (Throwable)null);

        void log(String var1);
    }

    public static enum Level {
        NONE,
        BASIC,
        HEADERS,
        BODY;

        private Level() {
        }
    }
}
