package com.enn3developer.dndtale.commands;

import com.enn3developer.dndtale.llm.LlmClient;
import com.hypixel.hytale.server.core.command.system.basecommands.AbstractCommandCollection;
import com.hypixel.hytale.server.core.permissions.provider.HytalePermissionsProvider;

import javax.annotation.Nonnull;

public class LlmCommand extends AbstractCommandCollection {

    public LlmCommand(@Nonnull final LlmClient client) {
        super("llm", "Configure the Dungeon Master's language model");
        setPermissionGroups(HytalePermissionsProvider.GROUP_ADMIN);
        addSubCommand(new LlmSetupCommand(client));
        addSubCommand(new LlmStatusCommand(client));
    }
}
