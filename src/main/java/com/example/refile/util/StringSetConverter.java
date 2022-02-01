package com.example.refile.util;

import javax.persistence.AttributeConverter;
import javax.persistence.Converter;
import java.util.Set;

import static java.util.Collections.emptySet;

@Converter
public class StringSetConverter implements AttributeConverter<Set<String>, String> {
    private static final String SPLIT_CHAR = ",";

    @Override
    public String convertToDatabaseColumn(Set<String> stringSet) {
        return stringSet != null ? String.join(SPLIT_CHAR, stringSet) : "";
    }

    @Override
    public Set<String> convertToEntityAttribute(String string) {
        return string != null ? Set.of(string.split(SPLIT_CHAR)) : emptySet();
    }
}
