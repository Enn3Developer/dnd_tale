package com.enn3developer.dndtale.mcp.commands;

import com.enn3developer.dndtale.mcp.CommandTier;
import com.enn3developer.dndtale.mcp.McpArgument;
import com.enn3developer.dndtale.mcp.McpArgumentType;
import com.enn3developer.dndtale.mcp.McpCommand;
import com.enn3developer.dndtale.mcp.McpCommandContext;
import com.enn3developer.dndtale.mcp.Worlds;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.math.vector.Rotation3f;
import com.hypixel.hytale.server.core.modules.entity.component.TransformComponent;
import com.hypixel.hytale.server.core.modules.entity.teleport.Teleport;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.Universe;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import org.joml.Vector3d;

import javax.annotation.Nonnull;

public class TeleportPlayerCommand extends McpCommand {

    private final McpArgument<String> player = declare(McpArgument.required("player", McpArgumentType.STRING,
        "Username of the player to move."));

    private final McpArgument<Double> x = declare(McpArgument.required("x", McpArgumentType.NUMBER,
        "Destination X coordinate."));

    private final McpArgument<Double> y = declare(McpArgument.required("y", McpArgumentType.NUMBER,
        "Destination Y coordinate."));

    private final McpArgument<Double> z = declare(McpArgument.required("z", McpArgumentType.NUMBER,
        "Destination Z coordinate."));

    public TeleportPlayerCommand() {
        super("teleport_player", "Teleport Player",
            "Move a player to exact coordinates in their current world.", CommandTier.SCENE);
    }

    @Nonnull
    @Override
    protected String execute(@Nonnull McpCommandContext context) {
        String username = context.get(player);
        PlayerRef target = null;
        for (PlayerRef candidate : Universe.get().getPlayers()) {
            if (candidate.isValid() && username.equalsIgnoreCase(candidate.getUsername())) {
                target = candidate;
                break;
            }
        }
        if (target == null) {
            throw new IllegalArgumentException("No connected player named '" + username + "'");
        }

        Ref<EntityStore> ref = target.getReference();
        if (ref == null || !ref.isValid()) {
            throw new IllegalStateException("Player '" + username + "' is not currently in a world");
        }

        var store = ref.getStore();
        var world = store.getExternalData().getWorld();
        Vector3d destination = new Vector3d(context.get(x), context.get(y), context.get(z));

        Worlds.call(world, () -> {
            TransformComponent transform = store.getComponent(ref, TransformComponent.getComponentType());
            if (transform == null) {
                throw new IllegalStateException("Player '" + username + "' has no transform");
            }
            Rotation3f rotation = new Rotation3f(transform.getRotation());
            store.addComponent(ref, Teleport.getComponentType(),
                Teleport.createExact(destination, rotation, rotation));
            return null;
        });

        return "Teleported " + target.getUsername() + " to "
            + destination.x + ", " + destination.y + ", " + destination.z + ".";
    }
}
