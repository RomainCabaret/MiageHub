package com.GlassFishJSF.converters;

import jakarta.faces.convert.Converter;
import jakarta.faces.convert.FacesConverter;
import jakarta.faces.component.UIComponent;
import jakarta.faces.context.FacesContext;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

import java.time.format.DateTimeFormatter;

@FacesConverter("localDateConverter")
public class LocalDateConverter implements Converter {

    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ISO_LOCAL_DATE; // yyyy-MM-dd

    @Override
    public Object getAsObject(FacesContext context, UIComponent component, String value) {
        if (value == null || value.trim().isEmpty()) return null;
        try {
            return LocalDate.parse(value, FORMATTER);
        } catch (DateTimeParseException e) {
            System.err.println("LocalDateConverter parse error: " + e.getMessage());
            return null;
        }
    }

    @Override
    public String getAsString(FacesContext context, UIComponent component, Object value) {
        if (value == null) return "";
        if (value instanceof LocalDate) {
            return ((LocalDate) value).format(FORMATTER);
        }
        return value.toString();
    }
}