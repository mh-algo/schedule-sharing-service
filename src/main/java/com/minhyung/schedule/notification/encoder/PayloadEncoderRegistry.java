package com.minhyung.schedule.notification.encoder;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.minhyung.schedule.notification.domain.MessageEnvelope;
import com.minhyung.schedule.notification.event.NotificationEvent;
import org.springframework.stereotype.Component;

import java.io.UncheckedIOException;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;

@Component
public class PayloadEncoderRegistry {
    private final Map<Class<?>, PayloadEncoder<?>> encoderMap;
    private final ObjectMapper objectMapper;

    public PayloadEncoderRegistry(List<PayloadEncoder<?>> encoders, ObjectMapper objectMapper) {
        this.encoderMap = encoders.stream().collect(Collectors.toMap(PayloadEncoder::supports, Function.identity()));
        this.objectMapper = objectMapper;
    }

    public <T extends NotificationEvent> String encode(T event) {
        @SuppressWarnings("unchecked")
        PayloadEncoder<T> encoder = (PayloadEncoder<T>) Objects.requireNonNull(
                encoderMap.get(event.getClass()), "No encoder for " + event.getClass());
        MessageEnvelope envelope = encoder.toEnvelope(event);
        try {
            return objectMapper.writeValueAsString(envelope);
        } catch (JsonProcessingException e) {
            throw new UncheckedIOException(e);
        }
    }
}
