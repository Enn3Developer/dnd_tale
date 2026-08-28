package com.enn3developer.dndtale.mcp.commands;

import com.enn3developer.dndtale.mcp.CommandTier;
import com.enn3developer.dndtale.mcp.McpArgument;
import com.enn3developer.dndtale.mcp.McpArgumentType;
import com.enn3developer.dndtale.mcp.McpCommand;
import com.enn3developer.dndtale.mcp.McpCommandContext;
import com.enn3developer.dndtale.mcp.Worlds;
import com.hypixel.hytale.server.core.modules.time.WorldTimeResource;
import com.hypixel.hytale.server.core.universe.world.World;

import javax.annotation.Nonnull;
import java.util.Locale;

public class SetTimeCommand extends McpCommand {

    private final McpArgument<String> time = declare(McpArgument.required("time", McpArgumentType.STRING,
        "Time of day: midnight, dawn, midday or dusk."));

    private final McpArgument<String> world = declare(McpArgument.optional("world", McpArgumentType.STRING,
        "World name. Defaults to the main world."));

    public SetTimeCommand() {
        super("set_time", "Set Time of Day",
            "Set the time of day in a world, for pacing and atmosphere.", CommandTier.SCENE);
    }

    @Nonnull
    @Override
    protected String execute(@Nonnull McpCommandContext context) {
        String requested = context.get(time).toLowerCase(Locale.ROOT);
        double fraction = switch (requested) {
            case "midnight" -> 0.0d;
            case "dawn" -> 0.25d;
            case "midday", "noon" -> 0.5d;
            case "dusk" -> 0.75d;
            default -> throw new IllegalArgumentException(
                "'time' must be one of: midnight, dawn, midday, dusk (got '" + requested + "')");
        };

        World target = Worlds.resolve(context.getOrNull(world));
        Worlds.call(target, () -> {
            var store = target.getEntityStore().getStore();
            store.getResource(WorldTimeResource.getResourceType()).setDayTime(fraction, target, store);
            return null;
        });
        return "Time in '" + target.getName() + "' set to " + requested + ".";
    }
}
