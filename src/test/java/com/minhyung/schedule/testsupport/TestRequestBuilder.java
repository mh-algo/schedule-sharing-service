package com.minhyung.schedule.testsupport;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpServletRequest;

import java.nio.charset.StandardCharsets;
import java.util.Map;

public final class TestRequestBuilder {
    private String method = HttpMethod.POST.name();
    private String uri = "/";
    private final HttpHeaders headers = new HttpHeaders();
    private byte[] content = new byte[0];

    private TestRequestBuilder() {}

    public static TestRequestBuilder json() {
        return new TestRequestBuilder();
    }

    public TestRequestBuilder method(HttpMethod method) {
        this.method = method.name();
        return this;
    }

    public TestRequestBuilder uri(String uri) {
        this.uri = uri;
        return this;
    }

    public TestRequestBuilder post(String uri) {
        this.method = HttpMethod.POST.name();
        this.uri = uri;
        return this;
    }

    public TestRequestBuilder get(String uri) {
        this.method = HttpMethod.GET.name();
        this.uri = uri;
        return this;
    }

    public TestRequestBuilder header(String name, String value) {
        this.headers.set(name, value);
        return this;
    }

    public TestRequestBuilder headers(Map<String, String> headers) {
        this.headers.setAll(headers);
        return this;
    }

    public TestRequestBuilder body(String json) {
        this.content = json == null ? new byte[0] : json.getBytes(StandardCharsets.UTF_8);
        return this;
    }

    public TestRequestBuilder body(Object payload, ObjectMapper mapper) {
        try {
            this.content = mapper.writeValueAsBytes(payload);
            return this;
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    public MockHttpServletRequest build() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setMethod(this.method);
        request.setRequestURI(this.uri);
        request.setContentType(MediaType.APPLICATION_JSON_VALUE);
        request.setCharacterEncoding(StandardCharsets.UTF_8.name());
        headers.forEach(request::addHeader);
        request.setContent(this.content);
        return request;
    }
}
