package com.enn3developer.dndtale.mcp;

import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.Universe;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.List;

public final class Players {

    private Players() {
    }

    @Nonnull
    public static PlayerRef require(@Nonnull String username) {
        List<String> online = new ArrayList<>();
        for (PlayerRef candidate : Universe.get().getPlayers()) {
            if (!candidate.isValid()) {
                continue;
            }
            if (username.equalsIgnoreCase(candidate.getUsername())) {
                return candidate;
            }
            online.add(candidate.getUsername());
        }
        throw new IllegalArgumentException(online.isEmpty()
            ? "No connected player named '" + username + "'; nobody is online"
            : "No connected player named '" + username + "'; online: " + String.join(", ", online));
    }

    @Nonnull
    public static Ref<EntityStore> requireReference(@Nonnull PlayerRef player) {
        Ref<EntityStore> ref = player.getReference();
        if (ref == null || !ref.isValid()) {
            throw new IllegalStateException("Player '" + player.getUsername() + "' is not currently in a world");
        }
        return ref;
    }
}
