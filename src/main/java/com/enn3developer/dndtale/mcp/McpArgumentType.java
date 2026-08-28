package com.enn3developer.dndtale.mcp;

import com.google.gson.JsonElement;

import javax.annotation.Nonnull;

public interface McpArgumentType<T> {

    McpArgumentType<String> STRING = new McpArgumentType<>() {
        @Nonnull
        @Override
        public String jsonType() {
            return "string";
        }

        @Nonnull
        @Override
        public String parse(@Nonnull String name, @Nonnull JsonElement element) {
            if (!element.isJsonPrimitive() || !element.getAsJsonPrimitive().isString()) {
                throw new IllegalArgumentException("'" + name + "' must be a string");
            }
            return element.getAsString();
        }
    };

    McpArgumentType<Integer> INTEGER = new McpArgumentType<>() {
        @Nonnull
        @Override
        public String jsonType() {
            return "integer";
        }

        @Nonnull
        @Override
        public Integer parse(@Nonnull String name, @Nonnull JsonElement element) {
            if (!element.isJsonPrimitive() || !element.getAsJsonPrimitive().isNumber()) {
                throw new IllegalArgumentException("'" + name + "' must be an integer");
            }
            return element.getAsInt();
        }
    };

    McpArgumentType<Double> NUMBER = new McpArgumentType<>() {
        @Nonnull
        @Override
        public String jsonType() {
            return "number";
        }

        @Nonnull
        @Override
        public Double parse(@Nonnull String name, @Nonnull JsonElement element) {
            if (!element.isJsonPrimitive() || !element.getAsJsonPrimitive().isNumber()) {
                throw new IllegalArgumentException("'" + name + "' must be a number");
            }
            return element.getAsDouble();
        }
    };

    McpArgumentType<Boolean> BOOLEAN = new McpArgumentType<>() {
        @Nonnull
        @Override
        public String jsonType() {
            return "boolean";
        }

        @Nonnull
        @Override
        public Boolean parse(@Nonnull String name, @Nonnull JsonElement element) {
            if (!element.isJsonPrimitive() || !element.getAsJsonPrimitive().isBoolean()) {
                throw new IllegalArgumentException("'" + name + "' must be a boolean");
            }
            return element.getAsBoolean();
        }
    };

    @Nonnull
    String jsonType();

    @Nonnull
    T parse(@Nonnull String name, @Nonnull JsonElement element);
}
