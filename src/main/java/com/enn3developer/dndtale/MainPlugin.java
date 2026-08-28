package com.enn3developer.dndtale;

import com.enn3developer.dndtale.commands.DndTaleCommand;
import com.enn3developer.dndtale.llm.ClaudeCliClient;
import com.enn3developer.dndtale.llm.LlmClient;
import com.hypixel.hytale.server.core.plugin.JavaPlugin;
import com.hypixel.hytale.server.core.plugin.JavaPluginInit;

import javax.annotation.Nonnull;

public class MainPlugin extends JavaPlugin {

    private LlmClient llmClient;

    public MainPlugin(@Nonnull JavaPluginInit init) {
        super(init);
    }

    @Override
    protected void setup() {
        this.llmClient = new ClaudeCliClient("claude", "claude-opus-5", 180L);
        this.getCommandRegistry().registerCommand(new DndTaleCommand(this.llmClient));
    }

    @Nonnull
    public LlmClient getLlmClient() {
        return this.llmClient;
    }
}
