package com.enn3developer.dndtale.llm;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.concurrent.CompletableFuture;

public interface LlmClient extends AutoCloseable {

    @Nonnull
    String backendName();

    @Nonnull
    CompletableFuture<LlmReadiness> probe();

    @Nonnull
    CompletableFuture<LlmResponse> prompt(@Nonnull String prompt, @Nullable String sessionId);

    @Override
    void close();
}
