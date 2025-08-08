package com.minhyung.schedule.testsupport;

import com.fasterxml.jackson.databind.ObjectMapper;

public final class TestObjectMapper {
    private TestObjectMapper() {}

    private static class TestObjectMapperHolder {
        private static final ObjectMapper INSTANCE = new ObjectMapper();
    }

    public static ObjectMapper getInstance() {
        return TestObjectMapperHolder.INSTANCE;
    }
}
