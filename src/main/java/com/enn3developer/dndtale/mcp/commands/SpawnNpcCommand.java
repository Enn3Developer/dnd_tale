package com.enn3developer.dndtale.mcp.commands;

import com.enn3developer.dndtale.mcp.Assets;
import com.enn3developer.dndtale.mcp.CommandTier;
import com.enn3developer.dndtale.mcp.McpArgument;
import com.enn3developer.dndtale.mcp.McpArgumentType;
import com.enn3developer.dndtale.mcp.McpCommand;
import com.enn3developer.dndtale.mcp.McpCommandContext;
import com.enn3developer.dndtale.mcp.Worlds;
import com.hypixel.hytale.math.vector.Rotation3f;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.npc.NPCPlugin;
import com.hypixel.hytale.server.spawning.SpawnTestResult;
import org.joml.Vector3d;

import javax.annotation.Nonnull;

public class SpawnNpcCommand extends McpCommand {

    private final McpArgument<String> role = declare(McpArgument.required("role", McpArgumentType.STRING,
        "NPC role template name. Call with an invalid name to see what this server accepts."));

    private final McpArgument<Double> x = declare(McpArgument.required("x", McpArgumentType.NUMBER,
        "X coordinate to spawn at."));

    private final McpArgument<Double> y = declare(McpArgument.required("y", McpArgumentType.NUMBER,
        "Y coordinate to spawn at."));

    private final McpArgument<Double> z = declare(McpArgument.required("z", McpArgumentType.NUMBER,
        "Z coordinate to spawn at."));

    private final McpArgument<String> world = declare(McpArgument.optional("world", McpArgumentType.STRING,
        "World name. Defaults to the main world."));

    public SpawnNpcCommand() {
        super("spawn_npc", "Spawn NPC",
            "Spawn an NPC of a given role at a position. The spawn is refused if the space is blocked.",
            CommandTier.SCENE);
    }

    @Nonnull
    @Override
    protected String execute(@Nonnull McpCommandContext context) {
        NPCPlugin npc = NPCPlugin.get();
        if (npc == null) {
            throw new IllegalStateException("The NPC module is not available");
        }

        String roleName = context.get(role);
        if (npc.getIndex(roleName) < 0) {
            throw Assets.unknown("NPC role", roleName, npc.getRoleTemplateNames(true));
        }

        Vector3d position = new Vector3d(context.get(x), context.get(y), context.get(z));
        World target = Worlds.resolve(context.getOrNull(world));

        SpawnTestResult result = Worlds.call(target, () ->
            npc.spawnNPCWithSpaceValidation(target.getEntityStore().getStore(), roleName, null,
                position, new Rotation3f(0f, 0f, 0f)));

        if (result != SpawnTestResult.TEST_OK) {
            String reason = switch (result) {
                case FAIL_INVALID_POSITION ->
                    "there is not enough room there, or that chunk is not loaded (spawn near a player)";
                case FAIL_NOT_SPAWNABLE -> "that role is not spawnable";
                default -> result.toString();
            };
            throw new IllegalStateException("Could not spawn '" + roleName + "' at "
                + position.x + ", " + position.y + ", " + position.z + ": " + reason);
        }
        return "Spawned '" + roleName + "' at " + position.x + ", " + position.y + ", " + position.z
            + " in '" + target.getName() + "'.";
    }
}
