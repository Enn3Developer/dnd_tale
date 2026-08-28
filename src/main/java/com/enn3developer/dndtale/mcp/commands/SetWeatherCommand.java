package com.enn3developer.dndtale.mcp.commands;

import com.enn3developer.dndtale.mcp.CommandTier;
import com.enn3developer.dndtale.mcp.McpArgument;
import com.enn3developer.dndtale.mcp.McpArgumentType;
import com.enn3developer.dndtale.mcp.McpCommand;
import com.enn3developer.dndtale.mcp.McpCommandContext;
import com.enn3developer.dndtale.mcp.Worlds;
import com.hypixel.hytale.builtin.weather.resources.WeatherResource;
import com.hypixel.hytale.server.core.asset.type.weather.config.Weather;
import com.hypixel.hytale.server.core.universe.world.World;

import javax.annotation.Nonnull;
import java.util.Set;
import java.util.TreeSet;

public class SetWeatherCommand extends McpCommand {

    private final McpArgument<String> weather = declare(McpArgument.required("weather", McpArgumentType.STRING,
        "Weather asset id to force. Call it with an invalid value to see the list this server accepts."));

    private final McpArgument<String> world = declare(McpArgument.optional("world", McpArgumentType.STRING,
        "World name. Defaults to the main world."));

    public SetWeatherCommand() {
        super("set_weather", "Set Weather",
            "Force the weather in a world. Use it to set the mood of a scene.", CommandTier.SCENE);
    }

    @Nonnull
    @Override
    protected String execute(@Nonnull McpCommandContext context) {
        String forced = context.get(weather);
        if (forced.isBlank()) {
            throw new IllegalArgumentException("'weather' must not be blank");
        }

        Set<String> known = new TreeSet<>(Weather.getAssetMap().getAssetMap().keySet());
        if (!known.contains(forced)) {
            throw new IllegalArgumentException(
                "Unknown weather '" + forced + "'. This server accepts: " + String.join(", ", known));
        }

        World target = Worlds.resolve(context.getOrNull(world));
        Worlds.call(target, () -> {
            var store = target.getEntityStore().getStore();
            store.getResource(WeatherResource.getResourceType()).setForcedWeather(forced);
            var config = target.getWorldConfig();
            config.setForcedWeather(forced);
            config.markChanged();
            return null;
        });
        return "Weather in '" + target.getName() + "' forced to '" + forced + "'.";
    }
}
