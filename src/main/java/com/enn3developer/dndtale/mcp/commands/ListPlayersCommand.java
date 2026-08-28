package com.enn3developer.dndtale.mcp.commands;

import com.enn3developer.dndtale.mcp.CommandTier;
import com.enn3developer.dndtale.mcp.McpCommand;
import com.enn3developer.dndtale.mcp.McpCommandContext;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.Universe;

import javax.annotation.Nonnull;

public class ListPlayersCommand extends McpCommand {

    public ListPlayersCommand() {
        super("list_players", "List Players",
            "List the players currently connected to the server, with their usernames and ids.",
            CommandTier.PERCEPTION);
    }

    @Nonnull
    @Override
    protected String execute(@Nonnull McpCommandContext context) {
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
