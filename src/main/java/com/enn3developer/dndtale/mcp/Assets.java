package com.enn3developer.dndtale.mcp;

import com.hypixel.hytale.common.util.StringUtil;

import javax.annotation.Nonnull;
import java.util.Collection;
import java.util.List;

public final class Assets {

    private static final int SUGGESTIONS = 12;

    private Assets() {
    }

    @Nonnull
    public static IllegalArgumentException unknown(@Nonnull String kind, @Nonnull String input,
                                                   @Nonnull Collection<String> known) {
        StringBuilder message = new StringBuilder("Unknown ").append(kind).append(" '").append(input).append("'.");
        if (known.isEmpty()) {
            message.append(" This server has none registered.");
            return new IllegalArgumentException(message.toString());
        }

        List<String> closest = StringUtil.sortByFuzzyDistance(input, known, SUGGESTIONS);
        message.append(" Closest matches: ").append(String.join(", ", closest));
        message.append(" (").append(known.size()).append(" registered in total).");
        return new IllegalArgumentException(message.toString());
    }
}
