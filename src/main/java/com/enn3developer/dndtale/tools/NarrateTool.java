package com.enn3developer.dndtale.tools;

import com.enn3developer.dndtale.mcp.McpTool;
import com.enn3developer.dndtale.mcp.Schemas;
import com.enn3developer.dndtale.mcp.ToolTier;
import com.google.gson.JsonObject;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.Universe;

import javax.annotation.Nonnull;

public class NarrateTool implements McpTool {

    @Nonnull
    @Override
    public String name() {
        return "narrate";
    }

    @Nonnull
    @Override
    public String title() {
        return "Narrate";
    }

    @Nonnull
    @Override
    public String description() {
        return "Send narration text to the players as the Dungeon Master. "
            + "Optionally target a single player by username; otherwise every connected player sees it.";
    }

    @Nonnull
    @Override
    public ToolTier tier() {
        return ToolTier.PERCEPTION;
    }

    @Nonnull
    @Override
    public JsonObject inputSchema() {
        JsonObject schema = Schemas.object();
        Schemas.property(schema, "text", "string", "The narration to deliver.", true);
        Schemas.property(schema, "player", "string", "Optional username to address privately.", false);
        return schema;
    }

    @Nonnull
    @Override
    public String call(@Nonnull JsonObject arguments) {
        if (!arguments.has("text") || arguments.get("text").getAsString().isBlank()) {
            throw new IllegalArgumentException("'text' is required and must not be blank");
        }
        String text = arguments.get("text").getAsString();
        String target = arguments.has("player") && !arguments.get("player").isJsonNull()
            ? arguments.get("player").getAsString()
            : null;

        Message message = Message.raw(text);
        int delivered = 0;
        for (PlayerRef player : Universe.get().getPlayers()) {
            if (!player.isValid()) {
                continue;
            }
            if (target != null && !target.equalsIgnoreCase(player.getUsername())) {
                continue;
            }
            player.sendMessage(message);
            delivered++;
        }
        if (target != null && delivered == 0) {
            throw new IllegalArgumentException("No connected player named '" + target + "'");
        }
        return "Narrated to " + delivered + " player(s).";
    }
}
