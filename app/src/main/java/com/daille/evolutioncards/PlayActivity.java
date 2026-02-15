package com.daille.evolutioncards;

import android.os.Bundle;
import android.util.TypedValue;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.button.MaterialButton;

import java.util.ArrayDeque;
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
    private final List<SpeciesRef> forageParticipants = new ArrayList<>();
    private final List<SpeciesRef> attackParticipants = new ArrayList<>();

    private TextView turnLabel;
    private TextView biomeLabel;
    private TextView humanHandLabel;
    private TextView humanSpecies1Label;
    private TextView humanSpecies2Label;
    private TextView humanSpecies3Label;
    private TextView humanDeckLabel;
    private TextView bot1DeckLabel;
    private TextView bot2DeckLabel;
    private TextView bot1Species1Label;
    private TextView bot1Species2Label;
    private TextView bot1Species3Label;
    private TextView bot2Species1Label;
    private TextView bot2Species2Label;
    private TextView bot2Species3Label;
    private TextView logLabel;
    private LinearLayout forageTokensContainer;
    private LinearLayout messageOverlay;
    private TextView messageOverlayText;
    private MaterialButton buttonContinueMessage;

    private final ArrayDeque<String> pendingMessages = new ArrayDeque<>();
    private boolean messageVisible = false;

    private int currentPlayer = 0;
    private int round = 1;
    private GameCard activeBiome;
    private Phase currentPhase = Phase.PLAYER_ACTION;

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
        humanSpecies1Label = findViewById(R.id.humanSpecies1Label);
        humanSpecies2Label = findViewById(R.id.humanSpecies2Label);
        humanSpecies3Label = findViewById(R.id.humanSpecies3Label);
        humanDeckLabel = findViewById(R.id.humanDeckLabel);
        bot1DeckLabel = findViewById(R.id.bot1DeckLabel);
        bot2DeckLabel = findViewById(R.id.bot2DeckLabel);
        bot1Species1Label = findViewById(R.id.bot1Species1Label);
        bot1Species2Label = findViewById(R.id.bot1Species2Label);
        bot1Species3Label = findViewById(R.id.bot1Species3Label);
        bot2Species1Label = findViewById(R.id.bot2Species1Label);
        bot2Species2Label = findViewById(R.id.bot2Species2Label);
        bot2Species3Label = findViewById(R.id.bot2Species3Label);
        logLabel = findViewById(R.id.logLabel);
        forageTokensContainer = findViewById(R.id.forageTokensContainer);
        messageOverlay = findViewById(R.id.messageOverlay);
        messageOverlayText = findViewById(R.id.messageOverlayText);
        buttonContinueMessage = findViewById(R.id.buttonContinueMessage);

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

        buttonContinueMessage.setOnClickListener(v -> showNextPendingMessage());

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

        beginPhase(Phase.PLAYER_ACTION, "Ronda 1 lista. Elige una acción: crear especie, reemplazar carta o pasar.");
        appendLog("Partida iniciada. Se generaron 3 mazos aleatorios (1 humano + 2 bots).");
    }

    private void endHumanTurn() {
        beginPhase(Phase.PLAYER_ACTION, "Turno de bots: observa sus acciones.");
        currentPlayer = 1;
        runBotsUntilRoundEnds();
        resolveEndOfRound();
        currentPlayer = 0;
        round++;
        beginPhase(Phase.PLAYER_ACTION, "Tu turno: elige una acción.");
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
            String msg = bot.name + " crea una nueva especie.";
            appendLog(msg);
            showMessage(msg);
            return;
        }
        boolean replaced = tryReplaceCard(bot);
        if (replaced) {
            String msg = bot.name + " reemplaza una carta de adaptación.";
            appendLog(msg);
            showMessage(msg);
            return;
        }
        String msg = bot.name + " pasa su acción.";
        appendLog(msg);
        showMessage(msg);
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
        forageParticipants.clear();
        attackParticipants.clear();

        beginPhase(Phase.FORAGE, "Se reparte comida en la zona de forraje y comen por velocidad.");
        int foragePool = random.nextInt(10) + 1;
        renderForageTokens(foragePool);
        String diceMessage = "Resultado del dado: " + foragePool + ". Se agregan fichas de alimento a la zona de forraje.";
        appendLog(diceMessage);
        showMessage(diceMessage);

        List<SpeciesRef> allSpecies = collectSpecies();
        allSpecies.sort(Comparator
                .comparingInt((SpeciesRef ref) -> ref.species.getSpeed())
                .thenComparingInt(ref -> ref.species.getTotalAttributes())
                .reversed());

        for (SpeciesRef ref : allSpecies) {
            if (foragePool <= 0) {
                break;
            }
            int gain = Math.min(Math.max(1, ref.species.getAttack()), foragePool);
            ref.species.food += gain;
            foragePool -= gain;
            forageParticipants.add(ref);
            renderForageTokens(foragePool);
            String feedMessage = ref.player.name + " forrajea +" + gain + " comida (quedan " + foragePool + ").";
            appendLog(feedMessage);
            showMessage(feedMessage);
        }
        if (forageParticipants.isEmpty()) {
            appendLog("Ninguna especie logró comer en forrajeo.");
            showMessage("Ninguna especie logró comer en forrajeo.");
        }

        beginPhase(Phase.PREDATION, "Las especies con ataque intentan cazar.");
        for (SpeciesRef attackerRef : allSpecies) {
            SpeciesState attacker = attackerRef.species;
            if (attacker.getAttack() <= 0) {
                continue;
            }
            SpeciesRef target = chooseTarget(attackerRef);
            attackParticipants.add(attackerRef);
            if (target == null) {
                String noTargetMessage = attackerRef.player.name + " no tiene objetivos válidos para depredar.";
                appendLog(noTargetMessage);
                showMessage(noTargetMessage);
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
                String attackMessage = attackerRef.player.name + " depreda a " + target.player.name + " por " + damage + " de daño.";
                appendLog(attackMessage);
                showMessage(attackMessage);
            } else {
                String failMessage = attackerRef.player.name + " falla ataque sobre " + target.player.name + ".";
                appendLog(failMessage);
                showMessage(failMessage);
            }
        }

        beginPhase(Phase.RESOLUTION, "Cada especie consume metabolismo y se resuelven bajas.");
        for (PlayerState player : players) {
            for (int i = player.species.size() - 1; i >= 0; i--) {
                SpeciesState species = player.species.get(i);
                String speciesName = "Especie " + (i + 1) + " de " + player.name;
                int metabolism = species.getMetabolism(activeBiome);
                species.food -= metabolism;
                appendLog(speciesName + " consume " + metabolism + " comida por metabolismo.");

                boolean starvation = species.food < 1;
                boolean lowHealth = species.health < 1;
                if (lowHealth || starvation) {
                    species.individuals -= 1;
                    species.food = Math.max(0, species.food);
                    species.health = Math.max(1, species.individuals);
                    String lossMessage;
                    if (starvation) {
                        lossMessage = speciesName + " pierde 1 individuo por falta de alimentación.";
                    } else {
                        lossMessage = speciesName + " pierde 1 individuo por daño.";
                    }
                    appendLog(lossMessage);
                    showMessage(lossMessage);
                }

                if (species.individuals < 1) {
                    player.species.remove(i);
                    String extinctionMessage;
                    if (starvation) {
                        extinctionMessage = speciesName + " falleció por falta de alimentación.";
                    } else {
                        extinctionMessage = speciesName + " se extinguió por daño acumulado.";
                    }
                    appendLog(extinctionMessage);
                    showMessage(extinctionMessage);
                    continue;
                }

                species.health = Math.max(species.health, species.individuals);
            }
        }

        beginPhase(Phase.REPRODUCTION, "Las especies con comida suficiente se reproducen.");
        for (PlayerState player : players) {
            for (int i = 0; i < player.species.size(); i++) {
                SpeciesState species = player.species.get(i);
                int fertility = Math.max(1, species.individuals);
                if (species.food >= fertility) {
                    species.food -= fertility;
                    species.individuals += 1;
                    species.health = Math.max(species.health, species.individuals);
                    player.score += 5;
                    String reproductionMessage = "Especie " + (i + 1) + " de " + player.name + " se reprodujo (+1 individuo).";
                    appendLog(reproductionMessage);
                    showMessage(reproductionMessage);
                }
            }
        }

        beginPhase(Phase.REPLENISHMENT, "Se reponen manos y se actualiza puntaje.");
        for (PlayerState player : players) {
            for (SpeciesState species : player.species) {
                player.score += species.food;
                player.score += species.cards.size();
            }
            int previousHand = player.hand.size();
            player.drawTo(HAND_TARGET);
            appendLog(player.name + " repone mano: " + previousHand + " → " + player.hand.size() + " cartas. Puntaje: " + player.score + ".");
        }

        maybeAdvanceBiome();
        refreshUi();
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
            if (wasInForageOrAttack(ref.species)) {
                validTargets.add(ref);
            }
        }
        if (validTargets.isEmpty()) {
            return null;
        }
        return validTargets.get(random.nextInt(validTargets.size()));
    }

    private boolean wasInForageOrAttack(SpeciesState species) {
        for (SpeciesRef ref : forageParticipants) {
            if (ref.species == species) {
                return true;
            }
        }
        for (SpeciesRef ref : attackParticipants) {
            if (ref.species == species) {
                return true;
            }
        }
        return false;
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

    private void beginPhase(Phase phase, String instruction) {
        currentPhase = phase;
        clearLog();
        appendLog("--- Ronda " + round + " · " + phase.label + " ---");
        appendLog(instruction);
        showMessage(phase.label + ": " + instruction);
        if (phase != Phase.FORAGE) {
            renderForageTokens(0);
        }
        refreshUi();
    }

    private void clearLog() {
        logLabel.setText("");
    }

    private void showMessage(String message) {
        pendingMessages.add(message);
        if (!messageVisible) {
            showNextPendingMessage();
        }
    }

    private void showNextPendingMessage() {
        String nextMessage = pendingMessages.poll();
        if (nextMessage == null) {
            messageVisible = false;
            messageOverlay.setVisibility(View.GONE);
            return;
        }
        messageVisible = true;
        messageOverlayText.setText(nextMessage);
        messageOverlay.setVisibility(View.VISIBLE);
    }

    private void renderForageTokens(int tokenCount) {
        if (forageTokensContainer == null) {
            return;
        }
        forageTokensContainer.removeAllViews();
        for (int i = 0; i < tokenCount; i++) {
            View tokenView = new View(this);
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(0, dpToPx(22), 1f);
            params.setMargins(dpToPx(2), dpToPx(2), dpToPx(2), dpToPx(2));
            tokenView.setLayoutParams(params);
            tokenView.setBackgroundResource(R.drawable.bg_food_token);
            forageTokensContainer.addView(tokenView);
        }
    }

    private int dpToPx(int dp) {
        return (int) TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP,
                dp,
                getResources().getDisplayMetrics()
        );
    }

    private void appendLog(String message) {
        CharSequence current = logLabel.getText();
        String next = (current == null || current.length() == 0) ? message : current + "\n" + message;
        logLabel.setText(next);
    }

    private void refreshUi() {
        turnLabel.setText(getString(R.string.play_turn_phase, round, currentPhase.label));
        biomeLabel.setText(getString(R.string.play_forage_zone, activeBiome == null ? "N/D" : activeBiome.name));

        PlayerState human = players.get(0);
        PlayerState bot1 = players.get(1);
        PlayerState bot2 = players.get(2);

        humanDeckLabel.setText(formatHumanDeck(human));
        humanHandLabel.setText(formatHand(human));

        humanSpecies1Label.setText(formatSpeciesAt(human, 0));
        humanSpecies2Label.setText(formatSpeciesAt(human, 1));
        humanSpecies3Label.setText(formatSpeciesAt(human, 2));

        bot1DeckLabel.setText(formatBotDeck(bot1));
        bot2DeckLabel.setText(formatBotDeck(bot2));

        bot1Species1Label.setText(formatSpeciesAt(bot1, 0));
        bot1Species2Label.setText(formatSpeciesAt(bot1, 1));
        bot1Species3Label.setText(formatSpeciesAt(bot1, 2));

        bot2Species1Label.setText(formatSpeciesAt(bot2, 0));
        bot2Species2Label.setText(formatSpeciesAt(bot2, 1));
        bot2Species3Label.setText(formatSpeciesAt(bot2, 2));
    }

    private String formatHumanDeck(PlayerState player) {
        return String.format(Locale.US, "Mazo de\njugador\n\n%d", player.deck.size());
    }

    private String formatBotDeck(PlayerState player) {
        return String.format(Locale.US, "Mazo de\n%s\n\n%d", player.name.toLowerCase(Locale.US), player.deck.size());
    }

    private String formatHand(PlayerState player) {
        StringBuilder builder = new StringBuilder();
        builder.append(getString(R.string.play_hand_title)).append("\n");
        if (player.hand.isEmpty()) {
            builder.append("- sin cartas");
            return builder.toString();
        }
        for (int i = 0; i < player.hand.size(); i++) {
            GameCard card = player.hand.get(i);
            String compactName = card.name.length() > 16 ? card.name.substring(0, 16) + "…" : card.name;
            builder.append(i + 1)
                    .append(") ")
                    .append(compactName)
                    .append("\n");
        }
        return builder.toString();
    }

    private String formatSpeciesAt(PlayerState player, int index) {
        if (index >= player.species.size()) {
            return "Sin especie";
        }
        SpeciesState species = player.species.get(index);
        return String.format(Locale.US,
                "F:%d I:%d H:%d\nAT:%d VE:%d\nAR:%d PE:%d",
                species.cards.size(),
                species.individuals,
                species.health,
                species.getAttack(),
                species.getSpeed(),
                species.getArmor(),
                species.getPerception());
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

        int getTotalAttributes() {
            return getAttack() + getSpeed() + getArmor() + getPerception();
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

    private enum Phase {
        PLAYER_ACTION("Fase del jugador"),
        FORAGE("Fase de forrajeo"),
        PREDATION("Fase de depredación"),
        RESOLUTION("Fase de resolución"),
        REPRODUCTION("Fase de reproducción"),
        REPLENISHMENT("Fase de reposición");

        final String label;

        Phase(String label) {
            this.label = label;
        }
    }
}
