package com.proyecto_it.mercado_oficio.Domain.ValueObjects;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

import java.util.Map;

@Converter(autoApply = true)
public class DisponibilidadConverter implements AttributeConverter<Disponibilidad, String> {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public String convertToDatabaseColumn(Disponibilidad attribute) {
        if (attribute == null) return null;
        try {
            return objectMapper.writeValueAsString(attribute.getHorarios());
        } catch (JsonProcessingException e) {
            throw new IllegalArgumentException("Error convirtiendo Disponibilidad a JSON", e);
        }
    }

    @Override
    public Disponibilidad convertToEntityAttribute(String dbData) {
        if (dbData == null) return null;
        try {
            Map<String, String> map = objectMapper.readValue(dbData, new TypeReference<Map<String, String>>() {});
            return new Disponibilidad(map);
        } catch (JsonProcessingException e) {
            throw new IllegalArgumentException("Error convirtiendo JSON a Disponibilidad", e);
        }
    }
}
