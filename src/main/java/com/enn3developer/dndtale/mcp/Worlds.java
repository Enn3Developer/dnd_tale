package com.enn3developer.dndtale.mcp;

import com.hypixel.hytale.server.core.universe.Universe;
import com.hypixel.hytale.server.core.universe.world.World;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.concurrent.Callable;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

public final class Worlds {

    private static final long WORLD_THREAD_TIMEOUT_SECONDS = 10L;

    private Worlds() {
    }

    @Nonnull
    public static World resolve(@Nullable String name) {
        World world = name == null || name.isBlank()
            ? Universe.get().getDefaultWorld()
            : Universe.get().getWorld(name);
        if (world == null) {
            throw new IllegalArgumentException("No world named '" + name + "'");
        }
        return world;
    }

    public static <T> T call(@Nonnull World world, @Nonnull Callable<T> task) {
        CompletableFuture<T> future = new CompletableFuture<>();
        world.execute(() -> {
            try {
                future.complete(task.call());
            } catch (Throwable t) {
                future.completeExceptionally(t);
            }
        });
        try {
            return future.get(WORLD_THREAD_TIMEOUT_SECONDS, TimeUnit.SECONDS);
        } catch (ExecutionException e) {
            Throwable cause = e.getCause();
            if (cause instanceof RuntimeException runtime) {
                throw runtime;
            }
            throw new IllegalStateException(cause == null ? e.getMessage() : cause.getMessage(), cause);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("Interrupted waiting for the world thread");
        } catch (Exception e) {
            throw new IllegalStateException("World thread did not respond in time");
        }
    }
}
