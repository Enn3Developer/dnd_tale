package com.enn3developer.dndtale.mcp.commands;

import com.enn3developer.dndtale.mcp.CommandTier;
import com.enn3developer.dndtale.mcp.McpArgument;
import com.enn3developer.dndtale.mcp.McpArgumentType;
import com.enn3developer.dndtale.mcp.McpCommand;
import com.enn3developer.dndtale.mcp.McpCommandContext;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.Universe;

import javax.annotation.Nonnull;

public class NarrateCommand extends McpCommand {

    private final McpArgument<String> text = declare(
        McpArgument.required("text", McpArgumentType.STRING, "The narration to deliver."));

    private final McpArgument<String> player = declare(
        McpArgument.optional("player", McpArgumentType.STRING, "Optional username to address privately."));

    public NarrateCommand() {
        super("narrate", "Narrate",
            "Send narration text to the players as the Dungeon Master. "
                + "Optionally target a single player by username; otherwise every connected player sees it.",
            CommandTier.PERCEPTION);
    }

    @Nonnull
    @Override
    protected String execute(@Nonnull McpCommandContext context) {
        String narration = context.get(text);
        if (narration.isBlank()) {
            throw new IllegalArgumentException("'text' must not be blank");
        }
        String target = context.getOrNull(player);

        Message message = Message.raw(narration);
        int delivered = 0;
        for (PlayerRef ref : Universe.get().getPlayers()) {
            if (!ref.isValid()) {
                continue;
            }
            if (target != null && !target.equalsIgnoreCase(ref.getUsername())) {
                continue;
            }
            ref.sendMessage(message);
            delivered++;
        }
        if (target != null && delivered == 0) {
            throw new IllegalArgumentException("No connected player named '" + target + "'");
        }
        return "Narrated to " + delivered + " player(s).";
    }
}
