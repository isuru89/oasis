package io.github.oasis.core.services.api.infrastructure.repository.dto;

import io.github.oasis.core.model.GameState;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter(autoApply = true)
public class GameStateConverter implements AttributeConverter<GameState, Integer> {
    @Override
    public Integer convertToDatabaseColumn(GameState attribute) {
        if (attribute == null) {
            return null;
        }
        return attribute.getId();
    }

    @Override
    public GameState convertToEntityAttribute(Integer dbData) {
        if (dbData == null) {
            return GameState.UNKNOWN;
        }
        return GameState.of(dbData).orElse(GameState.UNKNOWN);
    }
}
