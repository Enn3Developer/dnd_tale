package com.enn3developer.dndtale.commands;

import com.enn3developer.dndtale.llm.LlmClient;
import com.hypixel.hytale.server.core.command.system.basecommands.AbstractCommandCollection;
import com.hypixel.hytale.server.core.permissions.provider.HytalePermissionsProvider;

import javax.annotation.Nonnull;

public class DndTaleCommand extends AbstractCommandCollection {

    public DndTaleCommand(@Nonnull final LlmClient client) {
        super("dndtale", "DnD Tale administration");
        setPermissionGroups(HytalePermissionsProvider.GROUP_ADMIN);
        addSubCommand(new LlmCommand(client));
    }
}
