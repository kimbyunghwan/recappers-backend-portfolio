package com.sch.capstone.backend.enums;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonValue;

@JsonFormat(shape = JsonFormat.Shape.STRING)
public enum QuestionType {
    OBJECTIVE("객관식", "objective", "mcq"),
    DESCRIPTIVE("서술형", "descriptive"),
    SUBJECTIVE("주관식", "subjective");

    private final String[] aliases;

    QuestionType(String... aliases) {
        this.aliases = aliases;
    }

    @JsonCreator
    public static QuestionType from(String raw) {
        if (raw == null) return null;
        String normalized = raw.trim().toLowerCase();
        for (QuestionType qt : values()) {
            for (String alias : qt.aliases) {
                if (alias.equalsIgnoreCase(normalized)) {
                    return qt;
                }
            }
        }
        throw new IllegalArgumentException("Unknown QuestionType: " + raw);
    }

    @JsonValue
    public String toValue() {
        return name();
    }
}
