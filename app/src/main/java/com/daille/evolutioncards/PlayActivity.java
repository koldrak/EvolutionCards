package com.daille.evolutioncards;

import android.os.Bundle;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.button.MaterialButton;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Random;

public class PlayActivity extends AppCompatActivity {

    private static final int PLAYER_COUNT = 3;
    private static final int HAND_TARGET = 5;
    private static final int MAX_SPECIES = 3;

    private final Random random = new Random();
    private final List<PlayerState> players = new ArrayList<>();
    private final List<GameCard> biomeDeck = new ArrayList<>();

    private TextView turnLabel;
    private TextView biomeLabel;
    private TextView humanHandLabel;
    private TextView humanSpeciesLabel;
    private TextView scoreboardLabel;
    private TextView logLabel;

    private int currentPlayer = 0;
    private int round = 1;
    private GameCard activeBiome;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_play);
        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }

        turnLabel = findViewById(R.id.turnLabel);
        biomeLabel = findViewById(R.id.biomeLabel);
        humanHandLabel = findViewById(R.id.humanHandLabel);
        humanSpeciesLabel = findViewById(R.id.humanSpeciesLabel);
        scoreboardLabel = findViewById(R.id.scoreboardLabel);
        logLabel = findViewById(R.id.logLabel);

        MaterialButton createSpeciesButton = findViewById(R.id.buttonCreateSpecies);
        MaterialButton replaceCardButton = findViewById(R.id.buttonReplaceCard);
        MaterialButton passButton = findViewById(R.id.buttonPass);

        createSpeciesButton.setOnClickListener(v -> {
            showCreateSpeciesDialog();
        });

        replaceCardButton.setOnClickListener(v -> {
            humanActionReplaceCard();
            endHumanTurn();
        });

        passButton.setOnClickListener(v -> {
            appendLog("Humano pasa su acción.");
            endHumanTurn();
        });

        setupGame();
        refreshUi();
    }

    private void setupGame() {
        List<GameCard> adaptationPool = new ArrayList<>();
        for (GameCard card : CardRepository.getAllCards()) {
            if ("Bioma".equalsIgnoreCase(card.type)) {
                biomeDeck.add(card);
            } else {
                adaptationPool.add(card);
            }
        }

        Collections.shuffle(biomeDeck, random);
        activeBiome = biomeDeck.isEmpty() ? null : biomeDeck.get(0);

        for (int i = 0; i < PLAYER_COUNT; i++) {
            List<GameCard> draft = new ArrayList<>(adaptationPool);
            Collections.shuffle(draft, random);
            List<GameCard> deck = new ArrayList<>(draft.subList(0, Math.min(40, draft.size())));
            PlayerState state = new PlayerState(i == 0 ? "Humano" : "Bot " + i, i == 0, deck);
            state.drawTo(HAND_TARGET);
            players.add(state);
        }

        appendLog("Partida iniciada. Se generaron 3 mazos aleatorios (1 humano + 2 bots).");
    }

    private void endHumanTurn() {
        currentPlayer = 1;
        runBotsUntilRoundEnds();
        resolveEndOfRound();
        currentPlayer = 0;
        round++;
        refreshUi();
    }

    private void runBotsUntilRoundEnds() {
        for (int i = 1; i < PLAYER_COUNT; i++) {
            runBotAction(players.get(i));
        }
    }

    private void runBotAction(PlayerState bot) {
        boolean created = tryCreateSpecies(bot);
        if (created) {
            appendLog(bot.name + " crea una nueva especie.");
            return;
        }
        boolean replaced = tryReplaceCard(bot);
        if (replaced) {
            appendLog(bot.name + " reemplaza una carta de adaptación.");
            return;
        }
        appendLog(bot.name + " pasa su acción.");
    }

    private void humanActionCreateSpecies() {
        if (!tryCreateSpecies(players.get(0))) {
            appendLog("No se pudo crear especie (se necesitan 2 cartas, incluyendo 1 mandíbula, y cupo disponible).");
        } else {
            appendLog("Humano crea una especie nueva.");
        }
    }

    private void showCreateSpeciesDialog() {
        PlayerState human = players.get(0);
        if (human.species.size() >= MAX_SPECIES) {
            appendLog("No puedes crear más especies (máximo 3).");
            return;
        }
        if (human.hand.size() < 2) {
            appendLog("No tienes suficientes cartas para crear una especie.");
            return;
        }

        CharSequence[] labels = new CharSequence[human.hand.size()];
        boolean[] selected = new boolean[human.hand.size()];
        for (int i = 0; i < human.hand.size(); i++) {
            GameCard card = human.hand.get(i);
            labels[i] = card.id + " · " + card.name + " [" + card.type + "]";
        }

        new AlertDialog.Builder(this)
                .setTitle(R.string.play_create_species_dialog_title)
                .setMultiChoiceItems(labels, selected, (dialog, which, isChecked) -> {
                    selected[which] = isChecked;
                    int count = 0;
                    for (boolean flag : selected) {
                        if (flag) {
                            count++;
                        }
                    }
                    if (count > 2) {
                        selected[which] = false;
                        ((AlertDialog) dialog).getListView().setItemChecked(which, false);
                    }
                })
                .setNegativeButton(R.string.play_cancel, null)
                .setPositiveButton(R.string.play_confirm, (dialog, which) -> {
                    List<Integer> indexes = new ArrayList<>();
                    for (int i = 0; i < selected.length; i++) {
                        if (selected[i]) {
                            indexes.add(i);
                        }
                    }
                    if (indexes.size() != 2) {
                        appendLog("Debes seleccionar exactamente 2 cartas.");
                        return;
                    }

                    GameCard c1 = human.hand.get(indexes.get(0));
                    GameCard c2 = human.hand.get(indexes.get(1));
                    if (!isJaw(c1) && !isJaw(c2)) {
                        appendLog("La especie necesita al menos 1 carta de tipo mandíbula.");
                        return;
                    }

                    int idxA = Math.max(indexes.get(0), indexes.get(1));
                    int idxB = Math.min(indexes.get(0), indexes.get(1));
                    GameCard first = human.hand.remove(idxA);
                    GameCard second = human.hand.remove(idxB);

                    SpeciesState species = new SpeciesState();
                    species.cards.add(first);
                    species.cards.add(second);
                    species.individuals = 1;
                    species.food = 1;
                    species.health = 1;
                    human.species.add(species);

                    appendLog("Humano crea una especie nueva (selección manual).");
                    endHumanTurn();
                })
                .show();
    }

    private void humanActionReplaceCard() {
        if (!tryReplaceCard(players.get(0))) {
            appendLog("No se pudo reemplazar carta (requiere especie con carta no mandíbula y carta de mano no mandíbula).");
        } else {
            appendLog("Humano reemplaza una carta en una especie.");
        }
    }

    private boolean tryCreateSpecies(PlayerState player) {
        if (player.species.size() >= MAX_SPECIES || player.hand.size() < 2) {
            return false;
        }

        List<Integer> jawIndexes = new ArrayList<>();
        for (int i = 0; i < player.hand.size(); i++) {
            if (isJaw(player.hand.get(i))) {
                jawIndexes.add(i);
            }
        }
        if (jawIndexes.isEmpty()) {
            return false;
        }

        int jawHandIndex = jawIndexes.get(random.nextInt(jawIndexes.size()));
        int secondIndex = random.nextInt(player.hand.size());
        while (secondIndex == jawHandIndex && player.hand.size() > 1) {
            secondIndex = random.nextInt(player.hand.size());
        }

        GameCard first = player.hand.get(Math.max(jawHandIndex, secondIndex));
        GameCard second = player.hand.get(Math.min(jawHandIndex, secondIndex));
        player.hand.remove(first);
        player.hand.remove(second);

        SpeciesState species = new SpeciesState();
        species.cards.add(first);
        species.cards.add(second);
        species.individuals = 1;
        species.food = 1;
        species.health = 1;
        player.species.add(species);
        return true;
    }

    private boolean tryReplaceCard(PlayerState player) {
        if (player.hand.isEmpty() || player.species.isEmpty()) {
            return false;
        }

        List<GameCard> handCandidates = new ArrayList<>();
        for (GameCard card : player.hand) {
            if (!isJaw(card)) {
                handCandidates.add(card);
            }
        }
        if (handCandidates.isEmpty()) {
            return false;
        }

        List<SpeciesState> speciesCandidates = new ArrayList<>();
        for (SpeciesState species : player.species) {
            if (species.getNonJawCardCount() > 0) {
                speciesCandidates.add(species);
            }
        }
        if (speciesCandidates.isEmpty()) {
            return false;
        }

        SpeciesState targetSpecies = speciesCandidates.get(random.nextInt(speciesCandidates.size()));
        GameCard replacement = handCandidates.get(random.nextInt(handCandidates.size()));
        if (!targetSpecies.replaceRandomNonJaw(replacement, random)) {
            return false;
        }
        player.hand.remove(replacement);
        return true;
    }

    private void resolveEndOfRound() {
        appendLog("--- Ronda " + round + ": forrajeo ---");
        int foragePool = random.nextInt(10) + 1;
        appendLog("Zona de forraje: " + foragePool + " comida.");

        List<SpeciesRef> allSpecies = collectSpecies();
        allSpecies.sort(Comparator.comparingInt((SpeciesRef ref) -> ref.species.getSpeed()).reversed());

        for (SpeciesRef ref : allSpecies) {
            if (foragePool <= 0) {
                break;
            }
            int gain = Math.min(Math.max(1, ref.species.getAttack()), foragePool);
            ref.species.food += gain;
            foragePool -= gain;
            appendLog(ref.player.name + " forrajea +" + gain + " comida.");
        }

        appendLog("--- Depredación ---");
        for (SpeciesRef attackerRef : allSpecies) {
            SpeciesState attacker = attackerRef.species;
            if (attacker.getAttack() <= 0) {
                continue;
            }
            SpeciesRef target = chooseTarget(attackerRef);
            if (target == null) {
                continue;
            }

            int attackerAwareness = attacker.getPerception();
            int defenderAwareness = target.species.getPerception();
            boolean attackSuccess;
            if (attackerAwareness > defenderAwareness) {
                attackSuccess = random.nextBoolean() || attacker.getSpeed() >= target.species.getSpeed();
            } else {
                attackSuccess = attacker.getSpeed() > target.species.getSpeed();
            }

            if (attackSuccess) {
                int damage = Math.max(1, attacker.getAttack() - (target.species.getArmor() / 2));
                target.species.health -= damage;
                attacker.food += attacker.getAttack();
                attackerRef.player.score += attacker.getAttack();
                appendLog(attackerRef.player.name + " depreda a " + target.player.name + " por " + damage + " de daño.");
            } else {
                appendLog(attackerRef.player.name + " falla ataque sobre " + target.player.name + ".");
            }
        }

        appendLog("--- Resolución y reproducción ---");
        for (PlayerState player : players) {
            for (int i = player.species.size() - 1; i >= 0; i--) {
                SpeciesState species = player.species.get(i);
                int metabolism = species.getMetabolism(activeBiome);
                species.food -= metabolism;

                if (species.health < 1 || species.food < 1) {
                    species.individuals -= 1;
                    species.food = Math.max(0, species.food);
                    species.health = Math.max(1, species.individuals);
                    appendLog(player.name + " pierde 1 individuo por daño/hambre.");
                }

                if (species.individuals < 1) {
                    player.species.remove(i);
                    appendLog(player.name + " pierde una especie (extinta).");
                    continue;
                }

                int fertility = Math.max(1, species.individuals);
                if (species.food >= fertility) {
                    species.food -= fertility;
                    species.individuals += 1;
                    species.health = Math.max(species.health, species.individuals);
                    player.score += 5;
                    appendLog(player.name + " reproduce una especie (+1 individuo).");
                }

                player.score += species.food;
                player.score += species.cards.size();
            }
            player.drawTo(HAND_TARGET);
        }

        maybeAdvanceBiome();
    }

    private void maybeAdvanceBiome() {
        int topScore = 0;
        for (PlayerState player : players) {
            topScore = Math.max(topScore, player.score);
        }
        if (biomeDeck.size() >= 3) {
            if (topScore >= 100) {
                activeBiome = biomeDeck.get(2);
            } else if (topScore >= 50) {
                activeBiome = biomeDeck.get(1);
            } else {
                activeBiome = biomeDeck.get(0);
            }
        }
    }

    private SpeciesRef chooseTarget(SpeciesRef attacker) {
        List<SpeciesRef> validTargets = new ArrayList<>();
        for (SpeciesRef ref : collectSpecies()) {
            if (ref.player == attacker.player) {
                continue;
            }
            if (ref.species.food > 0) {
                validTargets.add(ref);
            }
        }
        if (validTargets.isEmpty()) {
            return null;
        }
        return validTargets.get(random.nextInt(validTargets.size()));
    }

    private List<SpeciesRef> collectSpecies() {
        List<SpeciesRef> refs = new ArrayList<>();
        for (PlayerState player : players) {
            for (SpeciesState species : player.species) {
                refs.add(new SpeciesRef(player, species));
            }
        }
        return refs;
    }

    private boolean isJaw(GameCard card) {
        return "Mandíbula".equalsIgnoreCase(card.type);
    }

    private void appendLog(String message) {
        CharSequence current = logLabel.getText();
        String next = (current == null || current.length() == 0) ? message : current + "\n" + message;
        logLabel.setText(next);
    }

    private void refreshUi() {
        turnLabel.setText(getString(R.string.play_turn, round));
        biomeLabel.setText(getString(R.string.play_biome, activeBiome == null ? "N/D" : activeBiome.name));

        PlayerState human = players.get(0);
        humanHandLabel.setText(formatHand(human));
        humanSpeciesLabel.setText(formatSpecies(human));
        scoreboardLabel.setText(formatScoreboard());
    }

    private String formatHand(PlayerState player) {
        StringBuilder builder = new StringBuilder();
        builder.append(getString(R.string.play_hand_title)).append("\n");
        if (player.hand.isEmpty()) {
            builder.append("- sin cartas");
            return builder.toString();
        }
        for (GameCard card : player.hand) {
            builder.append("• ").append(card.id).append(" ").append(card.name)
                    .append(" [").append(card.type).append("]\n");
        }
        return builder.toString();
    }

    private String formatSpecies(PlayerState player) {
        StringBuilder builder = new StringBuilder();
        builder.append(getString(R.string.play_species_title)).append("\n");
        if (player.species.isEmpty()) {
            builder.append("- aún no tienes especies");
            return builder.toString();
        }
        for (int i = 0; i < player.species.size(); i++) {
            SpeciesState species = player.species.get(i);
            builder.append(String.format(Locale.US,
                    "• Especie %d | Cartas:%d Ind:%d Com:%d Atk:%d Vel:%d Arm:%d Per:%d\n",
                    i + 1,
                    species.cards.size(),
                    species.individuals,
                    species.food,
                    species.getAttack(),
                    species.getSpeed(),
                    species.getArmor(),
                    species.getPerception()));
        }
        return builder.toString();
    }

    private String formatScoreboard() {
        StringBuilder builder = new StringBuilder();
        builder.append(getString(R.string.play_scoreboard_title)).append("\n");
        for (PlayerState player : players) {
            builder.append("• ").append(player.name)
                    .append(": ").append(player.score)
                    .append(" pts | mazo ").append(player.deck.size())
                    .append(" | mano ").append(player.hand.size())
                    .append("\n");
        }
        return builder.toString();
    }

    private static class SpeciesRef {
        final PlayerState player;
        final SpeciesState species;

        SpeciesRef(PlayerState player, SpeciesState species) {
            this.player = player;
            this.species = species;
        }
    }

    private static class PlayerState {
        final String name;
        final boolean human;
        final List<GameCard> deck;
        final List<GameCard> hand = new ArrayList<>();
        final List<SpeciesState> species = new ArrayList<>();
        int score = 0;

        PlayerState(String name, boolean human, List<GameCard> deck) {
            this.name = name;
            this.human = human;
            this.deck = deck;
        }

        void drawTo(int target) {
            while (hand.size() < target && !deck.isEmpty()) {
                hand.add(deck.remove(0));
            }
        }
    }

    private static class SpeciesState {
        final List<GameCard> cards = new ArrayList<>();
        int individuals;
        int food;
        int health;

        int getAttack() {
            int base = 0;
            for (GameCard card : cards) {
                if ("Mandíbula".equalsIgnoreCase(card.type)) {
                    base += 1;
                }
            }
            return Math.max(0, base);
        }

        int getSpeed() {
            int speed = 0;
            for (GameCard card : cards) {
                if ("Extremidades".equalsIgnoreCase(card.type) || "Alas".equalsIgnoreCase(card.type)) {
                    speed += 1;
                }
            }
            return speed;
        }

        int getArmor() {
            int armor = 0;
            for (GameCard card : cards) {
                if ("Pelaje".equalsIgnoreCase(card.type)) {
                    armor += 1;
                }
            }
            return armor;
        }

        int getPerception() {
            int perception = 0;
            for (GameCard card : cards) {
                if ("Sentido".equalsIgnoreCase(card.type)) {
                    perception += 1;
                }
            }
            return perception;
        }

        int getMetabolism(GameCard biome) {
            int metabolism = Math.max(1, individuals);
            int biomeTemp = 0;
            if (biome != null && biome.description != null) {
                int idx = biome.description.indexOf("Temperatura:");
                if (idx >= 0) {
                    String sub = biome.description.substring(idx).replace("Temperatura:", "").trim();
                    String value = sub.split("\\|")[0].trim();
                    try {
                        biomeTemp = Integer.parseInt(value);
                    } catch (NumberFormatException ignored) {
                        biomeTemp = 0;
                    }
                }
            }
            if (biomeTemp >= 5 || biomeTemp <= -5) {
                metabolism += 2;
            } else if (biomeTemp >= 3 || biomeTemp <= -3) {
                metabolism += 1;
            }
            return metabolism;
        }

        int getNonJawCardCount() {
            int count = 0;
            for (GameCard card : cards) {
                if (!"Mandíbula".equalsIgnoreCase(card.type)) {
                    count++;
                }
            }
            return count;
        }

        boolean replaceRandomNonJaw(GameCard replacement, Random random) {
            List<Integer> replaceable = new ArrayList<>();
            for (int i = 0; i < cards.size(); i++) {
                if (!"Mandíbula".equalsIgnoreCase(cards.get(i).type)) {
                    replaceable.add(i);
                }
            }
            if (replaceable.isEmpty()) {
                return false;
            }
            int idx = replaceable.get(random.nextInt(replaceable.size()));
            cards.set(idx, replacement);
            return true;
        }
    }
}
