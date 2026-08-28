package com.enn3developer.dndtale.commands;

import com.enn3developer.dndtale.llm.LlmClient;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.command.system.CommandContext;
import com.hypixel.hytale.server.core.command.system.basecommands.AbstractAsyncCommand;
import com.hypixel.hytale.server.core.permissions.provider.HytalePermissionsProvider;

import javax.annotation.Nonnull;
import java.util.concurrent.CompletableFuture;

public class LlmStatusCommand extends AbstractAsyncCommand {

    private final LlmClient client;

    public LlmStatusCommand(@Nonnull final LlmClient client) {
        super("status", "Report whether DnD Tale can reach Claude");
        setPermissionGroups(HytalePermissionsProvider.GROUP_ADMIN);
        this.client = client;
    }

    @Nonnull
    @Override
    protected CompletableFuture<Void> executeAsync(@Nonnull final CommandContext context) {
        return client.probe().thenAccept(readiness -> {
            StringBuilder text = new StringBuilder();
            text.append("DnD Tale - LLM status\n");
            text.append("  Backend: ").append(client.backendName()).append('\n');
            text.append("  Ready:   ").append(readiness.ready() ? "yes" : "no").append('\n');
            if (readiness.version() != null) {
                text.append("  Version: ").append(readiness.version()).append('\n');
            }
            text.append("  Detail:  ").append(readiness.detail());
            if (!readiness.ready()) {
                text.append("\n\nRun '/dndtale llm setup' for instructions.");
            }
            context.sendMessage(Message.raw(text.toString()));
        });
    }
}
