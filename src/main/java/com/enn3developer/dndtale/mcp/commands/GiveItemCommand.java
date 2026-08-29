package com.enn3developer.dndtale.mcp.commands;

import com.enn3developer.dndtale.mcp.Assets;
import com.enn3developer.dndtale.mcp.CommandTier;
import com.enn3developer.dndtale.mcp.McpArgument;
import com.enn3developer.dndtale.mcp.McpArgumentType;
import com.enn3developer.dndtale.mcp.McpCommand;
import com.enn3developer.dndtale.mcp.McpCommandContext;
import com.enn3developer.dndtale.mcp.Players;
import com.enn3developer.dndtale.mcp.Worlds;
import com.hypixel.hytale.server.core.asset.type.item.config.Item;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.inventory.ItemStack;
import com.hypixel.hytale.server.core.universe.PlayerRef;

import javax.annotation.Nonnull;
import java.util.Set;

public class GiveItemCommand extends McpCommand {

    private static final int MAX_QUANTITY = 512;

    private final McpArgument<String> player = declare(McpArgument.required("player", McpArgumentType.STRING,
        "Username of the player receiving the item."));

    private final McpArgument<String> item = declare(McpArgument.required("item", McpArgumentType.STRING,
        "Item asset id. Call with an invalid id to see what this server accepts."));

    private final McpArgument<Integer> quantity = declare(McpArgument.optional("quantity", McpArgumentType.INTEGER,
        "How many to give, 1 to " + MAX_QUANTITY + ". Defaults to 1.", 1));

    public GiveItemCommand() {
        super("give_item", "Give Item",
            "Place an item into a player's inventory, as loot or a reward.", CommandTier.CONSEQUENCE);
    }

    @Nonnull
    @Override
    protected String execute(@Nonnull McpCommandContext context) {
        String itemId = context.get(item);
        Set<String> known = Item.getAssetMap().getAssetMap().keySet();
        if (!known.contains(itemId)) {
            throw Assets.unknown("item", itemId, known);
        }

        int count = context.get(quantity);
        if (count < 1 || count > MAX_QUANTITY) {
            throw new IllegalArgumentException("'quantity' must be between 1 and " + MAX_QUANTITY + ", got " + count);
        }

        PlayerRef target = Players.require(context.get(player));
        var ref = Players.requireReference(target);
        var store = ref.getStore();
        var world = store.getExternalData().getWorld();

        String leftover = Worlds.call(world, () -> {
            var transaction = Player.giveItem(new ItemStack(itemId, count), ref, store);
            var remainder = transaction.getRemainder();
            return remainder == null || remainder.isEmpty() ? null : "inventory full";
        });

        return leftover == null
            ? "Gave " + count + " x " + itemId + " to " + target.getUsername() + "."
            : "Gave what fitted of " + count + " x " + itemId + " to " + target.getUsername()
                + "; the rest did not fit (" + leftover + ").";
    }
}
