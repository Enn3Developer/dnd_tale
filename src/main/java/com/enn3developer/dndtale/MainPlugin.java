package com.enn3developer.dndtale;

import com.enn3developer.dndtale.commands.DndTaleCommand;
import com.enn3developer.dndtale.config.DndTaleConfig;
import com.enn3developer.dndtale.llm.ClaudeCliClient;
import com.enn3developer.dndtale.llm.LlmClient;
import com.enn3developer.dndtale.mcp.McpServer;
import com.enn3developer.dndtale.tools.ListPlayersTool;
import com.enn3developer.dndtale.tools.NarrateTool;
import com.hypixel.hytale.logger.HytaleLogger;
import com.hypixel.hytale.server.core.plugin.JavaPlugin;
import com.hypixel.hytale.server.core.plugin.JavaPluginInit;
import com.hypixel.hytale.server.core.util.Config;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class MainPlugin extends JavaPlugin {

    private static final HytaleLogger LOGGER = HytaleLogger.forEnclosingClass();
    private static final long PROMPT_TIMEOUT_SECONDS = 180L;

    private final Config<DndTaleConfig> config;
    @Nullable
    private McpServer mcpServer;
    @Nullable
    private LlmClient llmClient;

    public MainPlugin(@Nonnull JavaPluginInit init) {
        super(init);
        this.config = this.withConfig("dndtale", DndTaleConfig.CODEC);
    }

    @Override
    protected void setup() {
        this.config.save();
        DndTaleConfig settings = this.config.get();

        String mcpUrl = null;
        String mcpToken = null;
        try {
            McpServer server = new McpServer(settings.getAllowedTools());
            server.register(new ListPlayersTool());
            server.register(new NarrateTool());
            this.mcpServer = server;
            mcpUrl = server.url();
            mcpToken = server.token();
            LOGGER.atInfo().log("DnD Tale MCP server listening on %s", mcpUrl);
        } catch (Exception e) {
            LOGGER.atSevere().withCause(e).log("DnD Tale MCP server failed to start; the DM will have no tools");
        }

        this.llmClient = new ClaudeCliClient(
            settings.getClaudeBinary(), settings.getModel(), PROMPT_TIMEOUT_SECONDS, mcpUrl, mcpToken);
        this.getCommandRegistry().registerCommand(new DndTaleCommand(this.llmClient));
    }

    @Nonnull
    public LlmClient getLlmClient() {
        if (this.llmClient == null) {
            throw new IllegalStateException("DnD Tale is not set up yet");
        }
        return this.llmClient;
    }
}
