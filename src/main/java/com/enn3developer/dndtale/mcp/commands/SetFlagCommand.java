package com.enn3developer.dndtale.mcp.commands;

import com.enn3developer.dndtale.mcp.CommandTier;
import com.enn3developer.dndtale.mcp.McpArgument;
import com.enn3developer.dndtale.mcp.McpArgumentType;
import com.enn3developer.dndtale.mcp.McpCommand;
import com.enn3developer.dndtale.mcp.McpCommandContext;
import com.hypixel.hytale.builtin.gameflags.GameFlagsResource;

import javax.annotation.Nonnull;

public class SetFlagCommand extends McpCommand {

    private final McpArgument<String> key = declare(McpArgument.required("key", McpArgumentType.STRING,
        "Flag name, for example 'met_the_innkeeper' or 'act_two'."));

    private final McpArgument<Integer> value = declare(McpArgument.required("value", McpArgumentType.INTEGER,
        "Integer value to store for this flag."));

    public SetFlagCommand() {
        super("set_flag", "Set Campaign Flag",
            "Record persistent campaign state as a named integer flag. "
                + "Use it to remember what the party has done across sessions.", CommandTier.CONSEQUENCE);
    }

    @Nonnull
    @Override
    protected String execute(@Nonnull McpCommandContext context) {
        String name = context.get(key);
        if (name.isBlank()) {
            throw new IllegalArgumentException("'key' must not be blank");
        }
        int level = context.get(value);

        GameFlagsResource flags = GameFlagsResource.get();
        if (flags == null) {
            throw new IllegalStateException("The GameFlags module is not available");
        }
        flags.set(name, level);
        return "Flag '" + name + "' set to " + level + ".";
    }
}
