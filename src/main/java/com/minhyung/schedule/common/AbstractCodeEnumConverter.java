package com.minhyung.schedule.common;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

import java.util.Arrays;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;

@Converter
public abstract class AbstractCodeEnumConverter<T extends Enum<T> & CodeEnum<C>, C> implements AttributeConverter<T, C> {
    private final Class<T> clazz;
    private final Map<C, T> codeEnumMap;

    protected AbstractCodeEnumConverter(Class<T> clazz) {
        this.clazz = clazz;
        this.codeEnumMap = Arrays.stream(clazz.getEnumConstants())
                .collect(Collectors.toUnmodifiableMap(T::getCode, Function.identity()));
    }

    @Override
    public C convertToDatabaseColumn(T type) {
        return Objects.requireNonNull(type, clazz.getSimpleName() + " is null").getCode();
    }

    @Override
    public T convertToEntityAttribute(C code) {
        Objects.requireNonNull(code, "code is null");
        return Objects.requireNonNull(codeEnumMap.get(code), "Invalid code! Not Found " + clazz.getSimpleName());
    }
}
