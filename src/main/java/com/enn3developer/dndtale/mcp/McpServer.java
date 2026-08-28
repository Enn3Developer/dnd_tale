package com.enn3developer.dndtale.mcp;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;
import com.hypixel.hytale.logger.HytaleLogger;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.IOException;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.Executors;

public final class McpServer implements AutoCloseable {

    private static final HytaleLogger LOGGER = HytaleLogger.forEnclosingClass();
    private static final String PATH = "/mcp";
    private static final String PROTOCOL_VERSION = "2025-11-25";

    private final HttpServer http;
    private final String token;
    private final Map<String, McpTool> tools = new LinkedHashMap<>();
    private final Set<String> allowed;

    public McpServer(@Nonnull Set<String> allowed) throws IOException {
        this.allowed = allowed;
        this.token = UUID.randomUUID().toString();
        this.http = HttpServer.create(new InetSocketAddress(InetAddress.getLoopbackAddress(), 0), 0);
        this.http.setExecutor(Executors.newVirtualThreadPerTaskExecutor());
        this.http.createContext(PATH, this::handle);
        this.http.start();
    }

    public void register(@Nonnull McpTool tool) {
        tools.put(tool.name(), tool);
    }

    @Nonnull
    public String url() {
        return "http://127.0.0.1:" + http.getAddress().getPort() + PATH;
    }

    @Nonnull
    public String token() {
        return token;
    }

    public boolean isAllowed(@Nonnull McpTool tool) {
        return allowed.contains(tool.tier().name()) || allowed.contains(tool.name());
    }

    private void handle(@Nonnull HttpExchange exchange) throws IOException {
        try (exchange) {
            if (!"POST".equals(exchange.getRequestMethod())) {
                exchange.sendResponseHeaders(405, -1);
                return;
            }
            String auth = exchange.getRequestHeaders().getFirst("Authorization");
            if (auth == null || !auth.equals("Bearer " + token)) {
                exchange.sendResponseHeaders(401, -1);
                return;
            }

            String body = new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);
            JsonObject request;
            try {
                request = JsonParser.parseString(body).getAsJsonObject();
            } catch (Exception e) {
                respond(exchange, error(null, -32700, "Parse error"));
                return;
            }

            String method = request.has("method") ? request.get("method").getAsString() : "";
            JsonElement id = request.get("id");

            if (method.startsWith("notifications/")) {
                exchange.sendResponseHeaders(202, -1);
                return;
            }
            if (id == null) {
                exchange.sendResponseHeaders(202, -1);
                return;
            }
            respond(exchange, dispatch(method, id, request));
        } catch (Exception e) {
            LOGGER.atWarning().withCause(e).log("MCP request failed");
        }
    }

    @Nonnull
    private JsonObject dispatch(@Nonnull String method, @Nonnull JsonElement id, @Nonnull JsonObject request) {
        switch (method) {
            case "initialize" -> {
                JsonObject caps = new JsonObject();
                JsonObject toolsCap = new JsonObject();
                toolsCap.addProperty("listChanged", false);
                caps.add("tools", toolsCap);

                JsonObject info = new JsonObject();
                info.addProperty("name", "dndtale");
                info.addProperty("title", "DnD Tale");
                info.addProperty("version", "0.0.0");

                JsonObject result = new JsonObject();
                result.addProperty("protocolVersion", PROTOCOL_VERSION);
                result.add("capabilities", caps);
                result.add("serverInfo", info);
                return success(id, result);
            }
            case "tools/list" -> {
                JsonArray list = new JsonArray();
                for (McpTool tool : tools.values()) {
                    if (!isAllowed(tool)) {
                        continue;
                    }
                    JsonObject entry = new JsonObject();
                    entry.addProperty("name", tool.name());
                    entry.addProperty("title", tool.title());
                    entry.addProperty("description", tool.description());
                    entry.add("inputSchema", tool.inputSchema());
                    list.add(entry);
                }
                JsonObject result = new JsonObject();
                result.add("tools", list);
                return success(id, result);
            }
            case "tools/call" -> {
                JsonObject params = request.has("params") ? request.getAsJsonObject("params") : new JsonObject();
                String name = params.has("name") ? params.get("name").getAsString() : "";
                JsonObject args = params.has("arguments") && params.get("arguments").isJsonObject()
                    ? params.getAsJsonObject("arguments")
                    : new JsonObject();

                McpTool tool = tools.get(name);
                if (tool == null || !isAllowed(tool)) {
                    return error(id, -32602, "Unknown tool: " + name);
                }
                LOGGER.atInfo().log("DM tool call: %s %s", name, args);
                try {
                    return success(id, content(tool.call(args), false));
                } catch (Exception e) {
                    LOGGER.atWarning().withCause(e).log("DM tool '%s' failed", name);
                    String detail = e.getMessage() == null ? e.getClass().getSimpleName() : e.getMessage();
                    return success(id, content(detail, true));
                }
            }
            default -> {
                return error(id, -32601, "Method not found: " + method);
            }
        }
    }

    @Nonnull
    private static JsonObject content(@Nonnull String text, boolean isError) {
        JsonObject block = new JsonObject();
        block.addProperty("type", "text");
        block.addProperty("text", text);
        JsonArray blocks = new JsonArray();
        blocks.add(block);
        JsonObject result = new JsonObject();
        result.add("content", blocks);
        result.addProperty("isError", isError);
        return result;
    }

    @Nonnull
    private static JsonObject success(@Nonnull JsonElement id, @Nonnull JsonObject result) {
        JsonObject response = new JsonObject();
        response.addProperty("jsonrpc", "2.0");
        response.add("id", id);
        response.add("result", result);
        return response;
    }

    @Nonnull
    private static JsonObject error(@Nullable JsonElement id, int code, @Nonnull String message) {
        JsonObject err = new JsonObject();
        err.addProperty("code", code);
        err.addProperty("message", message);
        JsonObject response = new JsonObject();
        response.addProperty("jsonrpc", "2.0");
        if (id != null) {
            response.add("id", id);
        }
        response.add("error", err);
        return response;
    }

    private static void respond(@Nonnull HttpExchange exchange, @Nonnull JsonObject payload) throws IOException {
        byte[] bytes = payload.toString().getBytes(StandardCharsets.UTF_8);
        exchange.getResponseHeaders().add("Content-Type", "application/json");
        exchange.sendResponseHeaders(200, bytes.length);
        try (OutputStream out = exchange.getResponseBody()) {
            out.write(bytes);
        }
    }

    @Override
    public void close() {
        http.stop(0);
    }
}
