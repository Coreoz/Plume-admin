package com.coreoz.plume.admin;

import com.coreoz.plume.admin.services.logapi.LogInterceptApiBean;
import com.google.common.collect.ImmutableList;
import lombok.SneakyThrows;
import okhttp3.MediaType;
import okhttp3.Protocol;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okhttp3.ResponseBody;
import okio.Buffer;
import org.assertj.core.internal.bytebuddy.utility.RandomString;
import org.junit.Assert;
import org.junit.Test;

import java.io.IOException;

public class OkHttpLogEntryTransformerTest {

    @Test
    public void transformer_must_return_response_if_no_filters() {
        LogEntryTransformer transformer = new OkHttpLogEntryTransformerBuilder().build();
        Request request = generatePostRequest("/hello/world", 30);
        Response response = generateResponse(request, "header", "value", 30);

        LogInterceptApiBean generatedTrace = generatedTrace(request, response);
        String bodyRequest = generatedTrace.getBodyRequest();
        String bodyResponse = generatedTrace.getBodyResponse();

        LogInterceptApiBean transformedTrace = transformer.transform(request, response, generatedTrace);
        Assert.assertEquals(bodyRequest, transformedTrace.getBodyRequest());
        Assert.assertEquals(bodyResponse, transformedTrace.getBodyResponse());
    }

    @Test
    public void transformer_must_transform_if_limit_and_for_all() {
        int limit = 20;
        LogEntryTransformer transformer = new OkHttpLogEntryTransformerBuilder().forAll().forMaxBodyChar(limit).build();
        Request request = generatePostRequest("/hello/world", 30);
        Response response = generateResponse(request, "header", "value", 30);

        LogInterceptApiBean generatedTrace = generatedTrace(request, response);
        String bodyRequest = generatedTrace.getBodyRequest();
        String bodyResponse = generatedTrace.getBodyResponse();

        LogInterceptApiBean transformedTrace = transformer.transform(request, response, generatedTrace);
        Assert.assertEquals(bodyRequest.substring(0, limit), transformedTrace.getBodyRequest());
        Assert.assertEquals(bodyResponse.substring(0, limit), transformedTrace.getBodyResponse());
    }

    @Test
    public void transformer_must_not_transform_if_limit_but_not_filtered() {
        int limit = 20;
        LogEntryTransformer transformer = new OkHttpLogEntryTransformerBuilder().forEndpoint("/bye/world").forMaxBodyChar(limit).build();
        Request request = generatePostRequest("/hello/world", 30);
        Response response = generateResponse(request, "header", "value", 30);

        LogInterceptApiBean generatedTrace = generatedTrace(request, response);
        String bodyRequest = generatedTrace.getBodyRequest();
        String bodyResponse = generatedTrace.getBodyResponse();

        LogInterceptApiBean transformedTrace = transformer.transform(request, response, generatedTrace);
        Assert.assertEquals(bodyRequest, transformedTrace.getBodyRequest());
        Assert.assertEquals(bodyResponse, transformedTrace.getBodyResponse());
    }

    @Test
    public void transformer_must_transform_if_limit_and_filtered_by_endpoint() {
        int limit = 20;
        LogEntryTransformer transformer = new OkHttpLogEntryTransformerBuilder().forEndpoint("/hello/world").forMaxBodyChar(limit).build();
        Request request = generatePostRequest("/hello/world", 30);
        Response response = generateResponse(request, "header", "value", 30);

        LogInterceptApiBean generatedTrace = generatedTrace(request, response);
        String bodyRequest = generatedTrace.getBodyRequest();
        String bodyResponse = generatedTrace.getBodyResponse();

        LogInterceptApiBean transformedTrace = transformer.transform(request, response, generatedTrace);
        Assert.assertEquals(bodyRequest.substring(0, limit), transformedTrace.getBodyRequest());
        Assert.assertEquals(bodyResponse.substring(0, limit), transformedTrace.getBodyResponse());
    }

    @Test
    public void transformer_must_transform_if_limit_and_filtered_by_response_header() {
        int limit = 20;
        LogEntryTransformer transformer = new OkHttpLogEntryTransformerBuilder()
            .forResponseHeaderValue("header", "value")
            .forMaxBodyChar(limit)
            .build();
        Request request = generatePostRequest("/hello/world", 30);
        Response response = generateResponse(request, "header", "value", 30);

        LogInterceptApiBean generatedTrace = generatedTrace(request, response);
        String bodyRequest = generatedTrace.getBodyRequest();
        String bodyResponse = generatedTrace.getBodyResponse();

        LogInterceptApiBean transformedTrace = transformer.transform(request, response, generatedTrace);
        Assert.assertEquals(bodyRequest.substring(0, limit), transformedTrace.getBodyRequest());
        Assert.assertEquals(bodyResponse.substring(0, limit), transformedTrace.getBodyResponse());
    }

    @Test
    public void transformer_must_transform_if_limit_and_filtered_by_method() {
        int limit = 20;
        LogEntryTransformer transformer = new OkHttpLogEntryTransformerBuilder()
            .forMethod(HttpMethod.POST)
            .forMaxBodyChar(limit)
            .build();
        Request request = generatePostRequest("/hello/world", 30);
        Response response = generateResponse(request, "header", "value", 30);

        LogInterceptApiBean generatedTrace = generatedTrace(request, response);
        String bodyRequest = generatedTrace.getBodyRequest();
        String bodyResponse = generatedTrace.getBodyResponse();

        LogInterceptApiBean transformedTrace = transformer.transform(request, response, generatedTrace);
        Assert.assertEquals(bodyRequest.substring(0, limit), transformedTrace.getBodyRequest());
        Assert.assertEquals(bodyResponse.substring(0, limit), transformedTrace.getBodyResponse());
    }

    @Test
    public void transformer_must_not_be_transformed_if_limit_but_not_filtered() {
        int limit = 20;
        LogEntryTransformer transformer = new OkHttpLogEntryTransformerBuilder().forMaxBodyChar(limit).build();
        Request request = generatePostRequest("/hello/world", 30);
        Response response = generateResponse(request, "header", "value", 30);

        LogInterceptApiBean generatedTrace = generatedTrace(request, response);
        String bodyRequest = generatedTrace.getBodyRequest();
        String bodyResponse = generatedTrace.getBodyResponse();

        LogInterceptApiBean transformedTrace = transformer.transform(request, response, generatedTrace);
        Assert.assertEquals(bodyRequest, transformedTrace.getBodyRequest());
        Assert.assertEquals(bodyResponse, transformedTrace.getBodyResponse());
    }

    @Test
    public void transformer_must_not_fail_if_limit_superior_than_body_length() {
        int limit = 100;
        LogEntryTransformer transformer = new OkHttpLogEntryTransformerBuilder().forMaxBodyChar(limit).build();
        Request request = generatePostRequest("/hello/world", 30);
        Response response = generateResponse(request, "header", "value", 30);

        LogInterceptApiBean generatedTrace = generatedTrace(request, response);
        String bodyRequest = generatedTrace.getBodyRequest();
        String bodyResponse = generatedTrace.getBodyResponse();

        LogInterceptApiBean transformedTrace = transformer.transform(request, response, generatedTrace);
        Assert.assertEquals(bodyRequest, transformedTrace.getBodyRequest());
        Assert.assertEquals(bodyResponse, transformedTrace.getBodyResponse());
    }

    private static Request generatePostRequest(String endpoint, int length) {
        RequestBody body = RequestBody.create(RandomString.make(length), MediaType.parse("text/plain"));
        return new Request.Builder().post(body).url("https://test.coco.com" + endpoint).build();
    }

    private static Response generateResponse(Request request, String headerName, String headerValue, int length) {
        ResponseBody body = ResponseBody.create(RandomString.make(length), MediaType.parse("text/plain"));
        return new Response.Builder().body(body).header(headerName, headerValue).request(request).code(200)
            .protocol(Protocol.HTTP_1_1).message("").build();
    }

    @SneakyThrows
    private static LogInterceptApiBean generatedTrace(Request request, Response response) {
        return new LogInterceptApiBean(
            request.url().toString(),
            request.method(),
            response.code(),
            requestBodyToBuffer(request.body()),
            response.body().string(),
            ImmutableList.of(),
            ImmutableList.of(),
            "TEST"
        );
    }

    private static String requestBodyToBuffer(RequestBody requestBody) throws IOException {
        if (requestBody == null) {
            return null;
        }
        Buffer bufferRq = new Buffer();
        requestBody.writeTo(bufferRq);
        return bufferRq.readUtf8();
    }
}
