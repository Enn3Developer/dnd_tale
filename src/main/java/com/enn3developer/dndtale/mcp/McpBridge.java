package com.enn3developer.dndtale.mcp;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.hypixel.hytale.logger.HytaleLogger;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.IOException;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.UUID;
import java.util.concurrent.Executors;

public final class McpBridge implements AutoCloseable {

    private static final HytaleLogger LOGGER = HytaleLogger.forEnclosingClass();
    private static final String PATH = "/mcp";
    private static final String PROTOCOL_VERSION = "2025-11-25";
    private static final String SERVER_NAME = "dndtale";

    private final McpCommandRegistry registry;
    private final HttpServer http;
    private final String token;

    public McpBridge(@Nonnull McpCommandRegistry registry) throws IOException {
        this.registry = registry;
        this.token = UUID.randomUUID().toString();
        this.http = HttpServer.create(new InetSocketAddress(InetAddress.getLoopbackAddress(), 0), 0);
        this.http.setExecutor(Executors.newVirtualThreadPerTaskExecutor());
        this.http.createContext(PATH, this::handle);
        this.http.start();
    }

    @Nonnull
    public String url() {
        return "http://127.0.0.1:" + http.getAddress().getPort() + PATH;
    }

    @Nonnull
    public String token() {
        return token;
    }

    @Nonnull
    public String serverName() {
        return SERVER_NAME;
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

            JsonObject request;
            try {
                String body = new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);
                request = JsonParser.parseString(body).getAsJsonObject();
            } catch (Exception e) {
                respond(exchange, error(null, -32700, "Parse error"));
                return;
            }

            String method = request.has("method") ? request.get("method").getAsString() : "";
            JsonElement id = request.get("id");
            if (method.startsWith("notifications/") || id == null) {
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
        return switch (method) {
            case "initialize" -> success(id, initializeResult());
            case "tools/list" -> success(id, toolsListResult());
            case "tools/call" -> callResult(id, request);
            default -> error(id, -32601, "Method not found: " + method);
        };
    }

    @Nonnull
    private static JsonObject initializeResult() {
        JsonObject tools = new JsonObject();
        tools.addProperty("listChanged", false);
        JsonObject capabilities = new JsonObject();
        capabilities.add("tools", tools);

        JsonObject info = new JsonObject();
        info.addProperty("name", SERVER_NAME);
        info.addProperty("title", "DnD Tale");
        info.addProperty("version", "0.0.0");

        JsonObject result = new JsonObject();
        result.addProperty("protocolVersion", PROTOCOL_VERSION);
        result.add("capabilities", capabilities);
        result.add("serverInfo", info);
        return result;
    }

    @Nonnull
    private JsonObject toolsListResult() {
        JsonArray list = new JsonArray();
        for (McpCommand command : registry.exposed()) {
            JsonObject entry = new JsonObject();
            entry.addProperty("name", command.name());
            entry.addProperty("title", command.title());
            entry.addProperty("description", command.description());
            entry.add("inputSchema", command.inputSchema());
            list.add(entry);
        }
        JsonObject result = new JsonObject();
        result.add("tools", list);
        return result;
    }

    @Nonnull
    private JsonObject callResult(@Nonnull JsonElement id, @Nonnull JsonObject request) {
        JsonObject params = request.has("params") && request.get("params").isJsonObject()
            ? request.getAsJsonObject("params")
            : new JsonObject();
        String name = params.has("name") ? params.get("name").getAsString() : "";
        JsonObject arguments = params.has("arguments") && params.get("arguments").isJsonObject()
            ? params.getAsJsonObject("arguments")
            : new JsonObject();

        McpCommand command = registry.resolve(name);
        if (command == null) {
            return error(id, -32602, "Unknown tool: " + name);
        }

        LOGGER.atInfo().log("DM command %s [%s] %s", command.name(), command.tier(), arguments);
        try {
            return success(id, content(command.invoke(arguments), false));
        } catch (IllegalArgumentException e) {
            LOGGER.atInfo().log("DM command '%s' rejected: %s", command.name(), e.getMessage());
            return success(id, content(String.valueOf(e.getMessage()), true));
        } catch (Exception e) {
            LOGGER.atWarning().withCause(e).log("DM command '%s' failed", command.name());
            String detail = e.getMessage() == null ? e.getClass().getSimpleName() : e.getMessage();
            return success(id, content(detail, true));
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
        JsonObject details = new JsonObject();
        details.addProperty("code", code);
        details.addProperty("message", message);
        JsonObject response = new JsonObject();
        response.addProperty("jsonrpc", "2.0");
        if (id != null) {
            response.add("id", id);
        }
        response.add("error", details);
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
