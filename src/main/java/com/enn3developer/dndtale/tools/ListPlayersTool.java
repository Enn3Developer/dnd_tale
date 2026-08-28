package com.enn3developer.dndtale.tools;

import com.enn3developer.dndtale.mcp.McpTool;
import com.enn3developer.dndtale.mcp.Schemas;
import com.enn3developer.dndtale.mcp.ToolTier;
import com.google.gson.JsonObject;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.Universe;

import javax.annotation.Nonnull;

public class ListPlayersTool implements McpTool {

    @Nonnull
    @Override
    public String name() {
        return "list_players";
    }

    @Nonnull
    @Override
    public String title() {
        return "List Players";
    }

    @Nonnull
    @Override
    public String description() {
        return "List the players currently connected to the server, with their usernames and ids.";
    }

    @Nonnull
    @Override
    public ToolTier tier() {
        return ToolTier.PERCEPTION;
    }

    @Nonnull
    @Override
    public JsonObject inputSchema() {
        return Schemas.noArguments();
    }

    @Nonnull
    @Override
    public String call(@Nonnull JsonObject arguments) {
        StringBuilder out = new StringBuilder();
        int count = 0;
        for (PlayerRef player : Universe.get().getPlayers()) {
            if (!player.isValid()) {
                continue;
            }
            out.append("- ").append(player.getUsername()).append(" (").append(player.getUuid()).append(")\n");
            count++;
        }
        return count == 0 ? "No players are connected." : count + " player(s) connected:\n" + out;
    }
}
