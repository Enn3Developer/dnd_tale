package com.enn3developer.dndtale.mcp;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.IdentityHashMap;
import java.util.Map;

public final class McpCommandContext {

    private final Map<McpArgument<?>, Object> values;

    private McpCommandContext(@Nonnull Map<McpArgument<?>, Object> values) {
        this.values = values;
    }

    @Nonnull
    static McpCommandContext parse(@Nonnull McpCommand command, @Nonnull JsonObject arguments) {
        Map<McpArgument<?>, Object> values = new IdentityHashMap<>();
        for (McpArgument<?> argument : command.arguments()) {
            JsonElement element = arguments.get(argument.name());
            if (element == null || element.isJsonNull()) {
                if (argument.isRequired()) {
                    throw new IllegalArgumentException("Missing required argument '" + argument.name() + "'");
                }
                if (argument.fallback() != null) {
                    values.put(argument, argument.fallback());
                }
                continue;
            }
            values.put(argument, argument.type().parse(argument.name(), element));
        }
        return new McpCommandContext(values);
    }

    public boolean provided(@Nonnull McpArgument<?> argument) {
        return values.containsKey(argument);
    }

    @Nonnull
    public <T> T get(@Nonnull McpArgument<T> argument) {
        T value = getOrNull(argument);
        if (value == null) {
            throw new IllegalStateException("Argument '" + argument.name() + "' was not provided");
        }
        return value;
    }

    @Nullable
    @SuppressWarnings("unchecked")
    public <T> T getOrNull(@Nonnull McpArgument<T> argument) {
        return (T) values.get(argument);
    }
}
