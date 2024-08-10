package io.github.oasis.core.services.api.infrastructure.repository.configs;

import io.github.oasis.core.model.UserGender;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

import java.util.Arrays;

@Converter(autoApply = true)
public class GenderConverter implements AttributeConverter<UserGender, Integer> {
    @Override
    public Integer convertToDatabaseColumn(UserGender attribute) {
        return attribute.getId();
    }

    @Override
    public UserGender convertToEntityAttribute(Integer dbData) {
        return Arrays.stream(UserGender.values()).filter(g -> g.getId() == dbData)
                .findFirst()
                .orElse(UserGender.UNKNOWN);
    }
}
