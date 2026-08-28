package com.enn3developer.dndtale.mcp;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import javax.annotation.Nonnull;

public final class Schemas {

    private Schemas() {
    }

    @Nonnull
    public static JsonObject noArguments() {
        JsonObject schema = new JsonObject();
        schema.addProperty("type", "object");
        schema.addProperty("additionalProperties", false);
        return schema;
    }

    @Nonnull
    public static JsonObject object() {
        JsonObject schema = new JsonObject();
        schema.addProperty("type", "object");
        schema.add("properties", new JsonObject());
        schema.add("required", new JsonArray());
        return schema;
    }

    @Nonnull
    public static JsonObject property(@Nonnull JsonObject schema, @Nonnull String name, @Nonnull String type,
                                      @Nonnull String description, boolean required) {
        JsonObject field = new JsonObject();
        field.addProperty("type", type);
        field.addProperty("description", description);
        schema.getAsJsonObject("properties").add(name, field);
        if (required) {
            schema.getAsJsonArray("required").add(name);
        }
        return schema;
    }
}
