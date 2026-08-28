package com.enn3developer.dndtale.mcp;

import com.google.gson.JsonObject;

import javax.annotation.Nonnull;

public interface McpTool {

    @Nonnull
    String name();

    @Nonnull
    String title();

    @Nonnull
    String description();

    @Nonnull
    ToolTier tier();

    @Nonnull
    JsonObject inputSchema();

    @Nonnull
    String call(@Nonnull JsonObject arguments) throws Exception;
}
