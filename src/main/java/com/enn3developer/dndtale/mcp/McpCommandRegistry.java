package com.enn3developer.dndtale.mcp;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public final class McpCommandRegistry {

    private final Map<String, McpCommand> commands = new LinkedHashMap<>();
    private final Set<String> allowed;

    public McpCommandRegistry(@Nonnull Set<String> allowed) {
        this.allowed = Set.copyOf(allowed);
    }

    public void register(@Nonnull McpCommand command) {
        McpCommand previous = commands.putIfAbsent(command.name(), command);
        if (previous != null) {
            throw new IllegalStateException("Duplicate MCP command name: " + command.name());
        }
    }

    public boolean isExposed(@Nonnull McpCommand command) {
        return allowed.contains(command.tier().name()) || allowed.contains(command.name());
    }

    @Nonnull
    public Collection<McpCommand> exposed() {
        return commands.values().stream().filter(this::isExposed).toList();
    }

    @Nonnull
    public Collection<McpCommand> all() {
        return List.copyOf(commands.values());
    }

    @Nullable
    public McpCommand resolve(@Nonnull String name) {
        McpCommand command = commands.get(name);
        return command != null && isExposed(command) ? command : null;
    }
}
