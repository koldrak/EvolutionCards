package com.daille.evolutioncards;

import android.content.Context;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

public final class CardDesignDetails {
    private static final String FILE_NAME = "cartas_adaptacion_2_0.csv";
    private static final Map<String, DesignCardInfo> CACHE = new HashMap<>();
    private static boolean loaded;

    private CardDesignDetails() {
    }

    public static DesignCardInfo findByGameCard(Context context, GameCard card) {
        ensureLoaded(context);
        if (card == null) {
            return null;
        }

        String numericId = extractNumericId(card.id);
        if (numericId != null) {
            DesignCardInfo byId = CACHE.get("id:" + numericId);
            if (byId != null) {
                return byId;
            }
        }
        return CACHE.get("name:" + normalize(card.name));
    }

    private static synchronized void ensureLoaded(Context context) {
        if (loaded) {
            return;
        }
        try (InputStream input = context.getAssets().open(FILE_NAME);
             BufferedReader reader = new BufferedReader(new InputStreamReader(input, StandardCharsets.UTF_8))) {
            reader.readLine();
            String line;
            while ((line = reader.readLine()) != null) {
                String[] values = splitCsvLine(line);
                if (values.length < 14) {
                    continue;
                }
                DesignCardInfo info = new DesignCardInfo(
                        values[0], values[1], values[2], values[3], values[4],
                        values[5], values[6], values[7], values[8], values[9],
                        values[10], values[11], values[12], values[13]
                );
                CACHE.put("id:" + info.id, info);
                CACHE.put("name:" + normalize(info.name), info);
            }
        } catch (IOException ignored) {
            // Si no está disponible, la UI mostrará los datos base de GameCard.
        }
        loaded = true;
    }

    private static String extractNumericId(String cardId) {
        if (cardId == null || cardId.length() < 2) {
            return null;
        }
        String candidate = cardId.substring(1);
        for (int i = 0; i < candidate.length(); i++) {
            if (!Character.isDigit(candidate.charAt(i))) {
                return null;
            }
        }
        return candidate;
    }

    private static String normalize(String value) {
        return value == null ? "" : value.trim().toLowerCase();
    }

    private static String[] splitCsvLine(String line) {
        String[] values = new String[14];
        StringBuilder current = new StringBuilder();
        boolean inQuotes = false;
        int index = 0;
        for (int i = 0; i < line.length(); i++) {
            char c = line.charAt(i);
            if (c == '"') {
                if (inQuotes && i + 1 < line.length() && line.charAt(i + 1) == '"') {
                    current.append('"');
                    i++;
                } else {
                    inQuotes = !inQuotes;
                }
            } else if (c == ',' && !inQuotes) {
                if (index < values.length) {
                    values[index++] = current.toString().trim();
                }
                current.setLength(0);
            } else {
                current.append(c);
            }
        }
        if (index < values.length) {
            values[index] = current.toString().trim();
        }
        for (int i = 0; i < values.length; i++) {
            if (values[i] == null) {
                values[i] = "";
            }
        }
        return values;
    }

    public static final class DesignCardInfo {
        public final String id;
        public final String name;
        public final String type;
        public final String feedingType;
        public final String attackTarget;
        public final String attack;
        public final String armor;
        public final String health;
        public final String speed;
        public final String perception;
        public final String fertility;
        public final String metabolism;
        public final String temperature;
        public final String ability;

        private DesignCardInfo(
                String id,
                String name,
                String type,
                String feedingType,
                String attackTarget,
                String attack,
                String armor,
                String health,
                String speed,
                String perception,
                String fertility,
                String metabolism,
                String temperature,
                String ability
        ) {
            this.id = id;
            this.name = name;
            this.type = type;
            this.feedingType = feedingType;
            this.attackTarget = attackTarget;
            this.attack = attack;
            this.armor = armor;
            this.health = health;
            this.speed = speed;
            this.perception = perception;
            this.fertility = fertility;
            this.metabolism = metabolism;
            this.temperature = temperature;
            this.ability = ability;
        }
    }
}
