package com.enn3developer.dndtale.commands;

import com.enn3developer.dndtale.llm.LlmClient;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.command.system.CommandContext;
import com.hypixel.hytale.server.core.command.system.basecommands.AbstractAsyncCommand;
import com.hypixel.hytale.server.core.permissions.provider.HytalePermissionsProvider;

import javax.annotation.Nonnull;
import java.util.concurrent.CompletableFuture;

public class LlmSetupCommand extends AbstractAsyncCommand {

    private final LlmClient client;

    public LlmSetupCommand(@Nonnull final LlmClient client) {
        super("setup", "Explain how to connect DnD Tale to Claude");
        setPermissionGroups(HytalePermissionsProvider.GROUP_ADMIN);
        this.client = client;
    }

    @Nonnull
    @Override
    protected CompletableFuture<Void> executeAsync(@Nonnull final CommandContext context) {
        return client.probe().thenAccept(readiness -> {
            StringBuilder text = new StringBuilder();
            text.append("DnD Tale - Claude setup (backend: ").append(client.backendName()).append(")\n");

            if (readiness.ready()) {
                text.append("\nAlready connected. ").append(readiness.version())
                    .append(" at ").append(readiness.detail())
                    .append("\nNothing to do. Run '/dndtale llm status' to re-check.");
                context.sendMessage(Message.raw(text.toString()));
                return;
            }

            text.append("Not connected: ").append(readiness.detail()).append("\n");
            text.append("\nOn your own machine, not the server:\n");
            text.append("  1. Install Claude Code:  npm install -g @anthropic-ai/claude-code\n");
            text.append("  2. Sign in:              claude\n");
            text.append("  3. Mint a long-lived token for this server:\n");
            text.append("       claude setup-token\n");
            text.append("\nOn the server host:\n");
            text.append("  4. Install Claude Code the same way.\n");
            text.append("  5. Put the token in the server process environment:\n");
            text.append("       CLAUDE_CODE_OAUTH_TOKEN=<token>\n");
            text.append("  6. Restart the server, then run '/dndtale llm status'.\n");
            text.append("\nDo not paste the token into chat or into a DnD Tale config file.\n");
            text.append("DnD Tale never reads or stores it; the claude binary reads it itself.");

            context.sendMessage(Message.raw(text.toString()));
        });
    }
}
