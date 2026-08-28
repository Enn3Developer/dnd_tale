package com.enn3developer.dndtale.mcp;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public final class McpArgument<T> {

    private final String name;
    private final String description;
    private final McpArgumentType<T> type;
    private final boolean required;
    @Nullable
    private final T fallback;

    private McpArgument(@Nonnull String name, @Nonnull String description, @Nonnull McpArgumentType<T> type,
                        boolean required, @Nullable T fallback) {
        this.name = name;
        this.description = description;
        this.type = type;
        this.required = required;
        this.fallback = fallback;
    }

    @Nonnull
    public static <T> McpArgument<T> required(@Nonnull String name, @Nonnull McpArgumentType<T> type,
                                              @Nonnull String description) {
        return new McpArgument<>(name, description, type, true, null);
    }

    @Nonnull
    public static <T> McpArgument<T> optional(@Nonnull String name, @Nonnull McpArgumentType<T> type,
                                              @Nonnull String description) {
        return new McpArgument<>(name, description, type, false, null);
    }

    @Nonnull
    public static <T> McpArgument<T> optional(@Nonnull String name, @Nonnull McpArgumentType<T> type,
                                              @Nonnull String description, @Nonnull T fallback) {
        return new McpArgument<>(name, description, type, false, fallback);
    }

    @Nonnull
    public String name() {
        return name;
    }

    @Nonnull
    public String description() {
        return description;
    }

    @Nonnull
    public McpArgumentType<T> type() {
        return type;
    }

    public boolean isRequired() {
        return required;
    }

    @Nullable
    public T fallback() {
        return fallback;
    }
}
