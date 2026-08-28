package com.enn3developer.dndtale.llm;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public record LlmReadiness(boolean ready, @Nonnull String backend, @Nonnull String detail, @Nullable String version) {

    public static LlmReadiness notReady(@Nonnull String backend, @Nonnull String detail) {
        return new LlmReadiness(false, backend, detail, null);
    }

    public static LlmReadiness ready(@Nonnull String backend, @Nonnull String detail, @Nonnull String version) {
        return new LlmReadiness(true, backend, detail, version);
    }
}
