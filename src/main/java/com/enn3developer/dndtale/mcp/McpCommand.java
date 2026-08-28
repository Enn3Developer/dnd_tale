package com.enn3developer.dndtale.mcp;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.List;

public abstract class McpCommand {

    private final String name;
    private final String title;
    private final String description;
    private final CommandTier tier;
    private final List<McpArgument<?>> arguments = new ArrayList<>();

    protected McpCommand(@Nonnull String name, @Nonnull String title, @Nonnull String description,
                         @Nonnull CommandTier tier) {
        this.name = name;
        this.title = title;
        this.description = description;
        this.tier = tier;
    }

    @Nonnull
    protected <T> McpArgument<T> declare(@Nonnull McpArgument<T> argument) {
        for (McpArgument<?> existing : arguments) {
            if (existing.name().equals(argument.name())) {
                throw new IllegalStateException("Duplicate argument '" + argument.name() + "' on " + name);
            }
        }
        arguments.add(argument);
        return argument;
    }

    @Nonnull
    public String name() {
        return name;
    }

    @Nonnull
    public String title() {
        return title;
    }

    @Nonnull
    public String description() {
        return description;
    }

    @Nonnull
    public CommandTier tier() {
        return tier;
    }

    @Nonnull
    public List<McpArgument<?>> arguments() {
        return List.copyOf(arguments);
    }

    @Nonnull
    public final JsonObject inputSchema() {
        JsonObject schema = new JsonObject();
        schema.addProperty("type", "object");
        if (arguments.isEmpty()) {
            schema.addProperty("additionalProperties", false);
            return schema;
        }

        JsonObject properties = new JsonObject();
        JsonArray required = new JsonArray();
        for (McpArgument<?> argument : arguments) {
            JsonObject field = new JsonObject();
            field.addProperty("type", argument.type().jsonType());
            field.addProperty("description", argument.description());
            properties.add(argument.name(), field);
            if (argument.isRequired()) {
                required.add(argument.name());
            }
        }
        schema.add("properties", properties);
        schema.add("required", required);
        schema.addProperty("additionalProperties", false);
        return schema;
    }

    @Nonnull
    public final String invoke(@Nonnull JsonObject arguments) throws Exception {
        return execute(McpCommandContext.parse(this, arguments));
    }

    @Nonnull
    protected abstract String execute(@Nonnull McpCommandContext context) throws Exception;
}
