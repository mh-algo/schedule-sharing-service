package com.minhyung.schedule.testsupport;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

public final class TestObjectMapper {
    private TestObjectMapper() {}

    private static class TestObjectMapperHolder {
        private static final ObjectMapper INSTANCE;

        static {
            INSTANCE = new ObjectMapper();
            INSTANCE.registerModule(new JavaTimeModule());
        }
    }

    public static ObjectMapper getInstance() {
        return TestObjectMapperHolder.INSTANCE;
    }
}
