package com.enn3developer.dndtale.llm;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public record LlmResponse(@Nonnull String text, @Nullable String sessionId, double costUsd) {
}
