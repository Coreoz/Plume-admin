package com.coreoz.plume.admin;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.junit.Assert;
import org.junit.Test;

import com.coreoz.plume.admin.services.logapi.LogInterceptApiBean;
import com.google.common.collect.ImmutableList;

import lombok.SneakyThrows;
import net.bytebuddy.utility.RandomString;
import okhttp3.MediaType;
import okhttp3.Protocol;
import okhttp3.Request;
import okhttp3.Request.Builder;
import okhttp3.RequestBody;
import okhttp3.Response;
import okhttp3.ResponseBody;
import okio.Buffer;

public class OkHttpLogEntryTransformerTest {

    @Test
    public void transformer_must_return_all_with_hidden_keys() {
        LogEntryTransformer transformer = LogEntryTransformer.hideJsonFields(List.of("password"), "****");
        Request request = generatePostRequest("/hello/world", 30);
        Response response = generateResponse(request, "header", "value", 30);

        LogInterceptApiBean generatedTrace = generatedTrace(request, response);
        generatedTrace.setBodyRequest(generateJsonObjectString());
        generatedTrace.setBodyResponse(generateJsonObjectString()
        );

        LogInterceptApiBean transformedTrace = transformer.transform(request, response, generatedTrace);
        Assert.assertEquals(transformedTrace.getBodyRequest(), "{\"id\":\"123456\",\"password\":\"****\",\"detail\":\"Détail\"}");
        Assert.assertEquals(transformedTrace.getBodyResponse(), "{\"id\":\"123456\",\"password\":\"****\",\"detail\":\"Détail\"}");
    }

    @Test
    public void transformer_must_return_all_with_hidden_keys_list() {
        LogEntryTransformer transformer = LogEntryTransformer.hideJsonFields(List.of("password", "detail"), "****");
        Request request = generatePostRequest("/hello/world", 30);
        Response response = generateResponse(request, "header", "value", 30);

        LogInterceptApiBean generatedTrace = generatedTrace(request, response);
        generatedTrace.setBodyRequest(generateJsonObjectString());
        generatedTrace.setBodyResponse(generateJsonObjectString());

        LogInterceptApiBean transformedTrace = transformer.transform(request, response, generatedTrace);
        Assert.assertEquals(transformedTrace.getBodyRequest(), "{\"id\":\"123456\",\"password\":\"****\",\"detail\":\"****\"}");
        Assert.assertEquals(transformedTrace.getBodyResponse(), "{\"id\":\"123456\",\"password\":\"****\",\"detail\":\"****\"}");
    }

    @Test
    public void transformer_must_return_all_unmodified_when_key_is_absent() {
        LogEntryTransformer transformer = LogEntryTransformer.hideJsonFields(List.of("test"), "****");
        Request request = generatePostRequest("/hello/world", 30);
        Response response = generateResponse(request, "header", "value", 30);

        LogInterceptApiBean generatedTrace = generatedTrace(request, response);
        generatedTrace.setBodyRequest(generateJsonObjectString());
        generatedTrace.setBodyResponse(generateJsonObjectString());

        String bodyRequest = generatedTrace.getBodyRequest();
        String bodyResponse = generatedTrace.getBodyResponse();

        LogInterceptApiBean transformedTrace = transformer.transform(request, response, generatedTrace);
        Assert.assertEquals(transformedTrace.getBodyRequest(), bodyRequest);
        Assert.assertEquals(transformedTrace.getBodyResponse(), bodyResponse);
    }

    @Test
    public void transformer_must_return_all_unmodified_when_keys_is_empty() {
        LogEntryTransformer transformer = LogEntryTransformer.hideJsonFields(new ArrayList<>(), "****");
        Request request = generatePostRequest("/hello/world", 30);
        Response response = generateResponse(request, "header", "value", 30);

        LogInterceptApiBean generatedTrace = generatedTrace(request, response);
        generatedTrace.setBodyRequest(generateJsonObjectString());
        generatedTrace.setBodyResponse(generateJsonObjectString());

        String bodyRequest = generatedTrace.getBodyRequest();
        String bodyResponse = generatedTrace.getBodyResponse();

        LogInterceptApiBean transformedTrace = transformer.transform(request, response, generatedTrace);
        Assert.assertEquals(transformedTrace.getBodyRequest(), bodyRequest);
        Assert.assertEquals(transformedTrace.getBodyResponse(), bodyResponse);
    }

    private static String generateJsonObjectString() {
        return "{\"id\":\"123456\",\"password\":\"TEST\",\"detail\":\"Détail\"}";
    }

    @Test
    public void transformer_must_return_response_if_no_filters() {
        LogEntryTransformer transformer = LogEntryTransformer.limitBodySizeTransformer(-1);
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
        LogEntryTransformer transformer = LogEntryTransformer.limitBodySizeTransformer(limit);
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
        LogEntryTransformer transformer = LogEntryTransformer.limitBodySizeTransformer(limit)
            .applyOnlyToRequests(
                RequestPredicate.alwaysTrue().filterEndpointStartsWith("/bye/world")
            );
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
        LogEntryTransformer transformer = LogEntryTransformer.limitBodySizeTransformer(limit)
            .applyOnlyToRequests(
                RequestPredicate.alwaysTrue().filterEndpointStartsWith("/hello/world")
            );
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
        LogEntryTransformer transformer = LogEntryTransformer.limitBodySizeTransformer(limit)
            .applyOnlyToResponsesWithHeader("header", "value");
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
        LogEntryTransformer transformer = LogEntryTransformer.limitBodySizeTransformer(limit)
            .applyOnlyToRequests(RequestPredicate.alwaysTrue().filterMethod(HttpMethod.POST));

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
    public void transformer_must_be_transformed_if_limit_but_not_filtered() {
        int limit = 20;
        LogEntryTransformer transformer = LogEntryTransformer.limitBodySizeTransformer(limit);
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
    public void transformer_must_not_fail_if_limit_superior_than_body_length() {
        int limit = 100;
        LogEntryTransformer transformer = LogEntryTransformer.limitBodySizeTransformer(limit);
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
    public void transformer_must_not_fail_if_body_is_empty() {
        int limit = 100;
        LogEntryTransformer transformer = LogEntryTransformer.limitBodySizeTransformer(limit);
        Request request = generatePostRequest("/hello/world", 0);
        Response response = generateResponse(request, "header", "value", 0);

        LogInterceptApiBean generatedTrace = generatedTrace(request, response);
        String bodyRequest = generatedTrace.getBodyRequest();
        String bodyResponse = generatedTrace.getBodyResponse();

        LogInterceptApiBean transformedTrace = transformer.transform(request, response, generatedTrace);
        Assert.assertEquals(bodyRequest, transformedTrace.getBodyRequest());
        Assert.assertEquals(bodyResponse, transformedTrace.getBodyResponse());
    }

    @Test
    public void transformer_must_apply_second_transformer() {
        int limit = 100;
        String dummyText = "Dummy Text";
        LogEntryTransformer transformer = LogEntryTransformer.limitBodySizeTransformer(limit)
            .andApply((request, response, trace) -> {
                trace.setBodyResponse(dummyText);
                trace.setBodyRequest(dummyText);
                return trace;
            });
        Request request = generatePostRequest("/hello/world", 30);
        Response response = generateResponse(request, "header", "value", 30);

        LogInterceptApiBean generatedTrace = generatedTrace(request, response);

        LogInterceptApiBean transformedTrace = transformer.transform(request, response, generatedTrace);
        Assert.assertEquals(dummyText, transformedTrace.getBodyRequest());
        Assert.assertEquals(dummyText, transformedTrace.getBodyResponse());
    }

    private static Request generatePostRequest(String endpoint, int length) {
        Builder request = new Request.Builder().url("https://test.coco.com" + endpoint);
        if (length == 0) {
            return request.get().build();
        }
        RequestBody body = RequestBody.create(RandomString.make(length), MediaType.parse("text/plain"));
        return request.post(body).build();
    }

    private static Response generateResponse(Request request, String headerName, String headerValue, int length) {
        okhttp3.Response.Builder response = new Response.Builder().header(headerName, headerValue).request(request).code(200)
                .protocol(Protocol.HTTP_1_1).message("");
        if (length == 0) {
            return response.build();
        }
        ResponseBody body = ResponseBody.create(RandomString.make(length), MediaType.parse("text/plain"));
        return response.body(body).build();
    }

    @SneakyThrows
    private static LogInterceptApiBean generatedTrace(Request request, Response response) {
        return new LogInterceptApiBean(
            request.url().toString(),
            request.method(),
            response.code(),
            requestBodyToBuffer(request.body()),
            response.body() == null ? null : response.body().string(),
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
