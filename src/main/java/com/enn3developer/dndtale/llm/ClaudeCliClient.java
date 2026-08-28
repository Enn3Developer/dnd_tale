package com.enn3developer.dndtale.llm;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public final class ClaudeCliClient implements LlmClient {

    public static final String BACKEND = "claude-cli";
    private static final String TOKEN_ENV = "CLAUDE_CODE_OAUTH_TOKEN";
    private static final long PROBE_TIMEOUT_SECONDS = 15L;

    private final String binary;
    private final String model;
    private final long promptTimeoutSeconds;
    private final ExecutorService executor;

    public ClaudeCliClient(@Nonnull String binary, @Nonnull String model, long promptTimeoutSeconds) {
        this.binary = binary;
        this.model = model;
        this.promptTimeoutSeconds = promptTimeoutSeconds;
        this.executor = Executors.newVirtualThreadPerTaskExecutor();
    }

    @Nonnull
    @Override
    public String backendName() {
        return BACKEND;
    }

    @Nonnull
    @Override
    public CompletableFuture<LlmReadiness> probe() {
        return CompletableFuture.supplyAsync(this::probeBlocking, executor);
    }

    @Nonnull
    @Override
    public CompletableFuture<LlmResponse> prompt(@Nonnull String prompt, @Nullable String sessionId) {
        return CompletableFuture.supplyAsync(() -> promptBlocking(prompt, sessionId), executor);
    }

    @Nonnull
    private LlmReadiness probeBlocking() {
        Path resolved = resolveBinary();
        if (resolved == null) {
            return LlmReadiness.notReady(BACKEND, "binary '" + binary + "' not found on PATH");
        }

        String version;
        try {
            ProcessOutput out = run(List.of(resolved.toString(), "--version"), null, PROBE_TIMEOUT_SECONDS);
            if (out.exitCode() != 0) {
                return LlmReadiness.notReady(BACKEND, "'" + binary + " --version' exited " + out.exitCode());
            }
            version = out.stdout().trim();
        } catch (Exception e) {
            return LlmReadiness.notReady(BACKEND, "failed to run '" + binary + "': " + e.getMessage());
        }

        if (!hasCredential()) {
            return LlmReadiness.notReady(BACKEND, "no " + TOKEN_ENV + " in the server environment and no stored login");
        }
        return LlmReadiness.ready(BACKEND, resolved.toString(), version);
    }

    @Nonnull
    private LlmResponse promptBlocking(@Nonnull String prompt, @Nullable String sessionId) {
        Path resolved = resolveBinary();
        if (resolved == null) {
            throw new IllegalStateException("claude binary '" + binary + "' not found on PATH");
        }

        List<String> command = new ArrayList<>();
        command.add(resolved.toString());
        command.add("--print");
        command.add("--output-format");
        command.add("json");
        command.add("--model");
        command.add(model);
        if (sessionId != null && !sessionId.isBlank()) {
            command.add("--resume");
            command.add(sessionId);
        }

        ProcessOutput out;
        try {
            out = run(command, prompt, promptTimeoutSeconds);
        } catch (Exception e) {
            throw new IllegalStateException("claude invocation failed: " + e.getMessage(), e);
        }

        if (out.exitCode() != 0) {
            throw new IllegalStateException("claude exited " + out.exitCode() + ": " + firstLine(out.stderr()));
        }
        return parse(out.stdout());
    }

    @Nonnull
    private static LlmResponse parse(@Nonnull String stdout) {
        JsonObject root = JsonParser.parseString(stdout).getAsJsonObject();
        if (root.has("is_error") && root.get("is_error").getAsBoolean()) {
            throw new IllegalStateException("claude reported an error: " + firstLine(textOf(root, "result")));
        }
        String text = textOf(root, "result");
        String sessionId = root.has("session_id") && !root.get("session_id").isJsonNull()
            ? root.get("session_id").getAsString()
            : null;
        double cost = root.has("total_cost_usd") && !root.get("total_cost_usd").isJsonNull()
            ? root.get("total_cost_usd").getAsDouble()
            : 0.0d;
        return new LlmResponse(text, sessionId, cost);
    }

    @Nonnull
    private static String textOf(@Nonnull JsonObject root, @Nonnull String key) {
        return root.has(key) && !root.get(key).isJsonNull() ? root.get(key).getAsString() : "";
    }

    @Nonnull
    private static String firstLine(@Nonnull String value) {
        int newline = value.indexOf('\n');
        String line = newline < 0 ? value : value.substring(0, newline);
        return line.length() > 200 ? line.substring(0, 200) + "..." : line;
    }

    private static boolean hasCredential() {
        String token = System.getenv(TOKEN_ENV);
        if (token != null && !token.isBlank()) {
            return true;
        }
        String home = System.getProperty("user.home");
        return home != null && Files.isReadable(Paths.get(home, ".claude", ".credentials.json"));
    }

    @Nullable
    private Path resolveBinary() {
        Path direct = Paths.get(binary);
        if (direct.isAbsolute()) {
            return Files.isExecutable(direct) ? direct : null;
        }
        String path = System.getenv("PATH");
        if (path == null) {
            return null;
        }
        for (String entry : path.split(java.io.File.pathSeparator)) {
            if (entry.isBlank()) {
                continue;
            }
            Path candidate = Paths.get(entry).resolve(binary);
            if (Files.isExecutable(candidate)) {
                return candidate;
            }
        }
        return null;
    }

    @Nonnull
    private static ProcessOutput run(@Nonnull List<String> command, @Nullable String stdin, long timeoutSeconds)
        throws IOException, InterruptedException {
        Process process = new ProcessBuilder(command).start();
        if (stdin != null) {
            try (OutputStreamWriter writer = new OutputStreamWriter(process.getOutputStream(), StandardCharsets.UTF_8)) {
                writer.write(stdin);
            }
        } else {
            process.getOutputStream().close();
        }

        String stdout = new String(process.getInputStream().readAllBytes(), StandardCharsets.UTF_8);
        String stderr = new String(process.getErrorStream().readAllBytes(), StandardCharsets.UTF_8);

        if (!process.waitFor(timeoutSeconds, TimeUnit.SECONDS)) {
            process.destroyForcibly();
            throw new IOException("timed out after " + timeoutSeconds + "s");
        }
        return new ProcessOutput(process.exitValue(), stdout, stderr);
    }

    @Override
    public void close() {
        executor.shutdownNow();
    }

    private record ProcessOutput(int exitCode, @Nonnull String stdout, @Nonnull String stderr) {
    }
}
