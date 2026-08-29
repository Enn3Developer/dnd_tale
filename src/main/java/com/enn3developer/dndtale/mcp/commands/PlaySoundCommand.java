package com.enn3developer.dndtale.mcp.commands;

import com.enn3developer.dndtale.mcp.Assets;
import com.enn3developer.dndtale.mcp.CommandTier;
import com.enn3developer.dndtale.mcp.McpArgument;
import com.enn3developer.dndtale.mcp.McpArgumentType;
import com.enn3developer.dndtale.mcp.McpCommand;
import com.enn3developer.dndtale.mcp.McpCommandContext;
import com.enn3developer.dndtale.mcp.Worlds;
import com.hypixel.hytale.protocol.SoundCategory;
import com.hypixel.hytale.server.core.asset.type.soundevent.config.SoundEvent;
import com.hypixel.hytale.server.core.universe.world.SoundUtil;
import com.hypixel.hytale.server.core.universe.world.World;

import javax.annotation.Nonnull;
import java.util.Locale;

public class PlaySoundCommand extends McpCommand {

    private final McpArgument<String> sound = declare(McpArgument.required("sound", McpArgumentType.STRING,
        "Sound event asset id. Call with an invalid id to learn what this server accepts."));

    private final McpArgument<Double> x = declare(McpArgument.required("x", McpArgumentType.NUMBER,
        "X coordinate to play the sound at."));

    private final McpArgument<Double> y = declare(McpArgument.required("y", McpArgumentType.NUMBER,
        "Y coordinate to play the sound at."));

    private final McpArgument<Double> z = declare(McpArgument.required("z", McpArgumentType.NUMBER,
        "Z coordinate to play the sound at."));

    private final McpArgument<String> category = declare(McpArgument.optional("category", McpArgumentType.STRING,
        "One of: Music, Ambient, SFX, UI, Voice. Defaults to SFX.", "SFX"));

    private final McpArgument<String> world = declare(McpArgument.optional("world", McpArgumentType.STRING,
        "World name. Defaults to the main world."));

    public PlaySoundCommand() {
        super("play_sound", "Play Sound",
            "Play a sound at a point in the world, heard by nearby players. Use it for atmosphere and cues.",
            CommandTier.SCENE);
    }

    @Nonnull
    @Override
    protected String execute(@Nonnull McpCommandContext context) {
        String soundId = context.get(sound);
        int index = SoundEvent.getAssetMap().getIndex(soundId);
        if (index < 0) {
            throw Assets.unknown("sound event", soundId, SoundEvent.getAssetMap().getAssetMap().keySet());
        }

        String requested = context.get(category);
        SoundCategory soundCategory = switch (requested.toLowerCase(Locale.ROOT)) {
            case "music" -> SoundCategory.Music;
            case "ambient" -> SoundCategory.Ambient;
            case "sfx" -> SoundCategory.SFX;
            case "ui" -> SoundCategory.UI;
            case "voice" -> SoundCategory.Voice;
            default -> throw new IllegalArgumentException(
                "'category' must be one of: Music, Ambient, SFX, UI, Voice (got '" + requested + "')");
        };

        double px = context.get(x);
        double py = context.get(y);
        double pz = context.get(z);

        World target = Worlds.resolve(context.getOrNull(world));
        Worlds.call(target, () -> {
            SoundUtil.playSoundEvent3d(index, soundCategory, px, py, pz, target.getEntityStore().getStore());
            return null;
        });

        return "Played '" + soundId + "' at " + px + ", " + py + ", " + pz + " in '" + target.getName() + "'.";
    }
}
