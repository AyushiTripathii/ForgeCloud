package dev.forgecloud.common;

public class BadRequestException extends IllegalArgumentException {
    public BadRequestException(String message) { super(message); }
}

