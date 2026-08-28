package com.enn3developer.dndtale.config;

import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;

import javax.annotation.Nonnull;
import java.util.LinkedHashSet;
import java.util.Set;

public class DndTaleConfig {

    public static final BuilderCodec<DndTaleConfig> CODEC = BuilderCodec.builder(DndTaleConfig.class, DndTaleConfig::new)
        .append(
            new KeyedCodec<>("AllowedTools", Codec.STRING_ARRAY),
            (config, value, extraInfo) -> config.allowedTools = value,
            (config, extraInfo) -> config.allowedTools
        )
        .add()
        .append(
            new KeyedCodec<>("Model", Codec.STRING),
            (config, value, extraInfo) -> config.model = value,
            (config, extraInfo) -> config.model
        )
        .add()
        .append(
            new KeyedCodec<>("ClaudeBinary", Codec.STRING),
            (config, value, extraInfo) -> config.claudeBinary = value,
            (config, extraInfo) -> config.claudeBinary
        )
        .add()
        .build();

    private String[] allowedTools = {"PERCEPTION"};
    private String model = "claude-opus-5";
    private String claudeBinary = "claude";

    private DndTaleConfig() {
    }

    @Nonnull
    public Set<String> getAllowedTools() {
        return new LinkedHashSet<>(Set.of(allowedTools == null ? new String[0] : allowedTools));
    }

    @Nonnull
    public String getModel() {
        return model == null || model.isBlank() ? "claude-opus-5" : model;
    }

    @Nonnull
    public String getClaudeBinary() {
        return claudeBinary == null || claudeBinary.isBlank() ? "claude" : claudeBinary;
    }
}
