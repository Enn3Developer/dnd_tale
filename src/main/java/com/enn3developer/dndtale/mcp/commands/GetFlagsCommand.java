package com.enn3developer.dndtale.mcp.commands;

import com.enn3developer.dndtale.mcp.CommandTier;
import com.enn3developer.dndtale.mcp.McpCommand;
import com.enn3developer.dndtale.mcp.McpCommandContext;
import com.hypixel.hytale.builtin.gameflags.GameFlagsResource;

import javax.annotation.Nonnull;
import java.util.Map;
import java.util.TreeMap;

public class GetFlagsCommand extends McpCommand {

    public GetFlagsCommand() {
        super("get_flags", "Read Campaign Flags",
            "Read every campaign flag currently set, so you can recall what the party has already done.",
            CommandTier.PERCEPTION);
    }

    @Nonnull
    @Override
    protected String execute(@Nonnull McpCommandContext context) {
        GameFlagsResource flags = GameFlagsResource.get();
        if (flags == null) {
            throw new IllegalStateException("The GameFlags module is not available");
        }

        Map<String, Integer> snapshot = new TreeMap<>(flags.snapshot());
        if (snapshot.isEmpty()) {
            return "No campaign flags are set.";
        }

        StringBuilder out = new StringBuilder(snapshot.size() + " flag(s):\n");
        for (Map.Entry<String, Integer> entry : snapshot.entrySet()) {
            out.append("- ").append(entry.getKey()).append(" = ").append(entry.getValue()).append('\n');
        }
        return out.toString();
    }
}
