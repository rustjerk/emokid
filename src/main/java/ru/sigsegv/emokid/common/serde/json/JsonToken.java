package ru.sigsegv.emokid.common.serde.json;

/**
 * A JSON token
 */
public enum JsonToken {
    NULL,
    TRUE,
    FALSE,
    COMMA,
    COLON,
    L_BRACKET,
    R_BRACKET,
    L_BRACE,
    R_BRACE,
    NUMBER,
    STRING
}
