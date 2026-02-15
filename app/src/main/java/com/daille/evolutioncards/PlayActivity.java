package com.daille.evolutioncards;

import android.os.Bundle;
import android.text.SpannableString;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.style.ForegroundColorSpan;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.button.MaterialButton;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.HashSet;
import java.util.Set;
import java.util.Locale;
import java.util.Map;
import java.util.Random;

public class PlayActivity extends AppCompatActivity {

    private static final int PLAYER_COUNT = 3;
    private static final int HAND_TARGET = 5;
    private static final int MAX_SPECIES = 3;
    private static final int ATTRIBUTE_MIN = 0;
    private static final int ATTRIBUTE_MAX = 10;

    private final Random random = new Random();
    private final List<PlayerState> players = new ArrayList<>();
    private final List<GameCard> biomeDeck = new ArrayList<>();
    private final List<SpeciesRef> forageParticipants = new ArrayList<>();
    private final List<SpeciesRef> attackParticipants = new ArrayList<>();

    private TextView turnLabel;
    private TextView biomeLabel;
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
    private TextView[] humanHandCardViews;

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
        humanHandCardViews = new TextView[]{
                findViewById(R.id.humanHandCard1),
                findViewById(R.id.humanHandCard2),
                findViewById(R.id.humanHandCard3),
                findViewById(R.id.humanHandCard4),
                findViewById(R.id.humanHandCard5)
        };

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

        setupHandInteractions();
        setupSpeciesInteractions();

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
            if (!ref.species.canForage()) {
                continue;
            }
            int gain = Math.min(Math.max(1, ref.species.getAttack()), foragePool);
            ref.species.food += gain;
            foragePool -= gain;
            forageParticipants.add(ref);
            renderForageTokens(foragePool);
            String feedMessage = getSpeciesLabel(ref) + " forrajea +" + gain + " comida (quedan " + foragePool + ").";
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
            if (!attacker.canAttack()) {
                continue;
            }
            SpeciesRef target = chooseTarget(attackerRef);
            attackParticipants.add(attackerRef);
            if (target == null) {
                String noTargetMessage = getSpeciesLabel(attackerRef) + " no tiene objetivos válidos para depredar.";
                appendLog(noTargetMessage);
                showMessage(noTargetMessage);
                continue;
            }

            AttackResolution resolution = resolveAttack(attackerRef, target);
            if (resolution.success) {
                target.species.health -= resolution.damage;
                attacker.food += attacker.getAttack();
                attackerRef.player.score += attacker.getAttack();
                String attackMessage = getSpeciesLabel(attackerRef)
                        + " ataca por " + resolution.modeLabel + " a " + getSpeciesLabel(target)
                        + ". Resultado ataque exitoso " + getSpeciesLabel(target)
                        + " pierde " + resolution.damage + " de salud y "
                        + getSpeciesLabel(attackerRef) + " recibe " + attacker.getAttack() + " fichas de comida.";
                appendLog(attackMessage);
                showMessage(attackMessage);
            } else {
                String failMessage = getSpeciesLabel(attackerRef)
                        + " ataca por " + resolution.modeLabel + " a " + getSpeciesLabel(target)
                        + ". Resultado ataque fallido.";
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
            if (ref.species == attacker.species) {
                continue;
            }
            if (wasInForageOrAttack(ref.species)) {
                validTargets.add(ref);
            }
        }
        if (validTargets.isEmpty()) {
            return null;
        }
        TargetRule rule = attacker.species.getTargetRule();
        return chooseTargetByRule(validTargets, rule);
    }


    private SpeciesRef chooseTargetByRule(List<SpeciesRef> validTargets, TargetRule rule) {
        if (rule == TargetRule.HIGHEST_ARMOR) {
            return validTargets.stream()
                    .max(Comparator.comparingInt(ref -> ref.species.getArmor()))
                    .orElse(validTargets.get(random.nextInt(validTargets.size())));
        }
        if (rule == TargetRule.LOWEST_SPEED) {
            return validTargets.stream()
                    .min(Comparator.comparingInt((SpeciesRef ref) -> ref.species.getSpeed())
                            .thenComparingInt(ref -> ref.species.getTotalAttributes()))
                    .orElse(validTargets.get(random.nextInt(validTargets.size())));
        }
        if (rule == TargetRule.LOWEST_PERCEPTION) {
            return validTargets.stream()
                    .min(Comparator.comparingInt((SpeciesRef ref) -> ref.species.getPerception())
                            .thenComparingInt(ref -> ref.species.getTotalAttributes()))
                    .orElse(validTargets.get(random.nextInt(validTargets.size())));
        }
        if (rule == TargetRule.LOWEST_DEFENSE) {
            return validTargets.stream()
                    .min(Comparator.comparingInt((SpeciesRef ref) -> ref.species.getArmor())
                            .thenComparingInt(ref -> ref.species.getTotalAttributes()))
                    .orElse(validTargets.get(random.nextInt(validTargets.size())));
        }
        return validTargets.get(random.nextInt(validTargets.size()));
    }

    private String getSpeciesLabel(SpeciesRef ref) {
        int index = ref.player.species.indexOf(ref.species);
        return "Especie " + (index + 1) + " de " + ref.player.name;
    }

    private AttackResolution resolveAttack(SpeciesRef attackerRef, SpeciesRef defenderRef) {
        SpeciesState attacker = attackerRef.species;
        SpeciesState defender = defenderRef.species;
        String modeLabel;
        boolean success;

        if (attacker.getPerception() > defender.getPerception()) {
            if (random.nextBoolean()) {
                modeLabel = "emboscada";
                success = true;
            } else {
                modeLabel = "huida con ventaja";
                success = attacker.getSpeed() > defender.getSpeed() + 2;
            }
        } else {
            modeLabel = "huida";
            success = attacker.getSpeed() > defender.getSpeed();
        }

        int damage = success ? Math.max(0, attacker.getAttack() - (defender.getArmor() / 2)) : 0;
        return new AttackResolution(modeLabel, success, damage);
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
            int tokenSize = dpToPx(14);
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(tokenSize, tokenSize);
            params.setMargins(dpToPx(3), dpToPx(3), dpToPx(3), dpToPx(3));
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
        renderHumanHandSlots(human);

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

    private void setupHandInteractions() {
        for (int i = 0; i < humanHandCardViews.length; i++) {
            TextView cardView = humanHandCardViews[i];
            final int cardIndex = i;
            cardView.setOnClickListener(v -> {
                PlayerState human = players.get(0);
                if (cardIndex >= human.hand.size()) {
                    return;
                }
                showCardDetailPanel(human.hand.get(cardIndex));
            });
        }
    }

    private void setupSpeciesInteractions() {
        setupSpeciesClick(humanSpecies1Label, 0, 0);
        setupSpeciesClick(humanSpecies2Label, 0, 1);
        setupSpeciesClick(humanSpecies3Label, 0, 2);
        setupSpeciesClick(bot1Species1Label, 1, 0);
        setupSpeciesClick(bot1Species2Label, 1, 1);
        setupSpeciesClick(bot1Species3Label, 1, 2);
        setupSpeciesClick(bot2Species1Label, 2, 0);
        setupSpeciesClick(bot2Species2Label, 2, 1);
        setupSpeciesClick(bot2Species3Label, 2, 2);
    }

    private void setupSpeciesClick(TextView view, int playerIndex, int speciesIndex) {
        view.setOnClickListener(v -> {
            PlayerState player = players.get(playerIndex);
            if (speciesIndex >= player.species.size()) {
                return;
            }
            showSpeciesDetailPanel(player, speciesIndex, player.species.get(speciesIndex));
        });
    }

    private String formatHumanDeck(PlayerState player) {
        return String.format(Locale.US, "Mazo de\njugador\n\n%d", player.deck.size());
    }

    private String formatBotDeck(PlayerState player) {
        return String.format(Locale.US, "Mazo de\n%s\n\n%d", player.name.toLowerCase(Locale.US), player.deck.size());
    }

    private void renderHumanHandSlots(PlayerState player) {
        for (int i = 0; i < humanHandCardViews.length; i++) {
            TextView slot = humanHandCardViews[i];
            if (i < player.hand.size()) {
                GameCard card = player.hand.get(i);
                String compactName = card.name.length() > 22 ? card.name.substring(0, 22) + "…" : card.name;
                slot.setText((i + 1) + "\n" + compactName);
                slot.setAlpha(1f);
            } else {
                slot.setText("-");
                slot.setAlpha(0.45f);
            }
        }
    }

    private void showCardDetailPanel(GameCard card) {
        View panel = LayoutInflater.from(this).inflate(R.layout.panel_card_detail, null, false);
        TextView title = panel.findViewById(R.id.detailCardTitle);
        TextView type = panel.findViewById(R.id.detailCardType);
        TextView rarity = panel.findViewById(R.id.detailCardRarity);
        TextView description = panel.findViewById(R.id.detailCardDescription);

        title.setText(card.id + " · " + card.name);
        type.setText("Tipo: " + card.type);
        rarity.setText("Rareza: " + (card.metadata == null || card.metadata.trim().isEmpty() ? "Sin dato" : card.metadata));
        description.setText("Descripción: " + (card.description == null || card.description.trim().isEmpty() ? "Sin descripción" : card.description));

        new AlertDialog.Builder(this)
                .setTitle("Detalle de carta")
                .setView(panel)
                .setPositiveButton(R.string.play_confirm, null)
                .show();
    }

    private void showSpeciesDetailPanel(PlayerState player, int speciesIndex, SpeciesState species) {
        View panel = LayoutInflater.from(this).inflate(R.layout.panel_species_detail, null, false);
        TextView title = panel.findViewById(R.id.detailSpeciesTitle);
        TextView summary = panel.findViewById(R.id.detailSpeciesSummary);
        TextView stats = panel.findViewById(R.id.detailSpeciesStats);
        TextView cards = panel.findViewById(R.id.detailSpeciesCards);

        title.setText("Especie " + (speciesIndex + 1) + " · " + player.name);
        summary.setText("Individuos: " + species.individuals + " | Comida: " + species.food + " | Salud base: " + species.health);
        stats.setText(buildSpeciesBarsText(species));
        cards.setText(buildSpeciesCardsText(species));

        new AlertDialog.Builder(this)
                .setTitle("Detalle de especie")
                .setView(panel)
                .setPositiveButton(R.string.play_confirm, null)
                .show();
    }

    private String buildSpeciesBarsText(SpeciesState species) {
        StringBuilder builder = new StringBuilder();
        builder.append("Salud ").append(buildBar(clamp(species.health, ATTRIBUTE_MIN, ATTRIBUTE_MAX))).append("\n");
        builder.append("Ataque ").append(buildBar(clamp(species.getAttack(), ATTRIBUTE_MIN, ATTRIBUTE_MAX))).append("\n");
        builder.append("Armadura ").append(buildBar(clamp(species.getArmor(), ATTRIBUTE_MIN, ATTRIBUTE_MAX))).append("\n");
        builder.append("Velocidad ").append(buildBar(clamp(species.getSpeed(), ATTRIBUTE_MIN, ATTRIBUTE_MAX))).append("\n");
        builder.append("Percepción ").append(buildBar(clamp(species.getPerception(), ATTRIBUTE_MIN, ATTRIBUTE_MAX))).append("\n");
        builder.append("Metabolismo ").append(buildBar(clamp(species.getMetabolism(activeBiome), ATTRIBUTE_MIN, ATTRIBUTE_MAX)));
        return builder.toString();
    }

    private String buildSpeciesCardsText(SpeciesState species) {
        if (species.cards.isEmpty()) {
            return "Sin cartas de adaptación.";
        }
        StringBuilder builder = new StringBuilder();
        for (GameCard card : species.cards) {
            builder.append("• ")
                    .append(card.id)
                    .append(" · ")
                    .append(card.name)
                    .append(" (")
                    .append(card.type)
                    .append(")\n")
                    .append("   ")
                    .append(card.description)
                    .append("\n");
        }
        return builder.toString().trim();
    }

    private CharSequence formatSpeciesAt(PlayerState player, int index) {
        if (index >= player.species.size()) {
            return "Sin especie";
        }
        SpeciesState species = player.species.get(index);

        SpannableStringBuilder builder = new SpannableStringBuilder();
        builder.append("Individuos: ");
        builder.append(createTokenDots(species.individuals, 0xFF000000));
        builder.append("\nAlimentación: ");
        builder.append(createTokenDots(species.food, 0xFF2E7D32));
        builder.append("\n\n");

        appendAttributeLine(builder, "Salud", species.health);
        appendAttributeLine(builder, "Ataque", species.getAttack());
        appendAttributeLine(builder, "Armadura", species.getArmor());
        appendAttributeLine(builder, "Velocidad", species.getSpeed());
        appendAttributeLine(builder, "Percepción", species.getPerception());
        appendAttributeLine(builder, "Metabolismo", species.getMetabolism(activeBiome));

        int fertility = species.individuals;
        appendAttributeLine(builder, "Fertilidad", fertility);

        appendAttributeLine(builder, "Temperatura", getBiomeTemperatureValue(activeBiome));
        return builder;
    }

    private void appendAttributeLine(SpannableStringBuilder builder, String label, int rawValue) {
        int value = clamp(rawValue, ATTRIBUTE_MIN, ATTRIBUTE_MAX);
        builder.append(label)
                .append("=")
                .append(String.valueOf(value))
                .append(" ")
                .append(buildBar(value));
        if (!"Temperatura".contentEquals(label)) {
            builder.append("\n");
        }
    }

    private int clamp(int value, int min, int max) {
        return Math.max(min, Math.min(max, value));
    }

    private String buildBar(int value) {
        StringBuilder bar = new StringBuilder(ATTRIBUTE_MAX + 2);
        bar.append("[");
        for (int i = 0; i < ATTRIBUTE_MAX; i++) {
            bar.append(i < value ? "█" : "░");
        }
        bar.append("]");
        return bar.toString();
    }

    private CharSequence createTokenDots(int amount, int color) {
        int dots = Math.max(0, amount);
        if (dots == 0) {
            return "-";
        }
        SpannableStringBuilder dotsBuilder = new SpannableStringBuilder();
        for (int i = 0; i < dots; i++) {
            SpannableString dot = new SpannableString("●");
            dot.setSpan(new ForegroundColorSpan(color), 0, dot.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            dotsBuilder.append(dot);
            if (i < dots - 1) {
                dotsBuilder.append(" ");
            }
        }
        return dotsBuilder;
    }

    private int getBiomeTemperatureValue(GameCard biome) {
        if (biome == null || biome.description == null) {
            return 0;
        }
        int idx = biome.description.indexOf("Temperatura:");
        if (idx < 0) {
            return 0;
        }
        String sub = biome.description.substring(idx).replace("Temperatura:", "").trim();
        String value = sub.split("\\|")[0].trim();
        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException ignored) {
            return 0;
        }
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
        private static final Set<String> HERBIVORE_JAWS = new HashSet<>(Arrays.asList(
                "A11", "A102", "A103", "A104", "A105", "A106", "A107", "A108",
                "A110", "A111", "A112", "A113", "A114", "A115", "A116"
        ));
        private static final Set<String> OMNIVORE_JAWS = new HashSet<>(Arrays.asList("A15", "A109"));

        final List<GameCard> cards = new ArrayList<>();
        int individuals;
        int food;
        int health;

        DietType getDietType() {
            boolean hasCarnivoreJaw = false;
            boolean hasHerbivoreJaw = false;
            boolean hasOmnivoreJaw = false;
            for (GameCard card : cards) {
                if (!"Mandíbula".equalsIgnoreCase(card.type)) {
                    continue;
                }
                String id = card.id == null ? "" : card.id.toUpperCase(Locale.US);
                if (OMNIVORE_JAWS.contains(id)) {
                    hasOmnivoreJaw = true;
                } else if (HERBIVORE_JAWS.contains(id)) {
                    hasHerbivoreJaw = true;
                } else {
                    hasCarnivoreJaw = true;
                }
            }

            if (hasOmnivoreJaw || (hasCarnivoreJaw && hasHerbivoreJaw)) {
                return DietType.OMNIVORE;
            }
            if (hasCarnivoreJaw) {
                return DietType.CARNIVORE;
            }
            return DietType.HERBIVORE;
        }

        boolean canForage() {
            return getDietType() != DietType.CARNIVORE;
        }

        boolean canAttack() {
            return getAttack() > 0 && getDietType() != DietType.HERBIVORE;
        }

        TargetRule getTargetRule() {
            String jawId = getPrimaryJawId();
            if (jawId == null) {
                return TargetRule.RANDOM;
            }
            Map<String, TargetRule> rules = JawTargetRules.RULES;
            if (rules.containsKey(jawId)) {
                return rules.get(jawId);
            }
            return TargetRule.RANDOM;
        }

        private String getPrimaryJawId() {
            for (GameCard card : cards) {
                if ("Mandíbula".equalsIgnoreCase(card.type)) {
                    return card.id == null ? null : card.id.toUpperCase(Locale.US);
                }
            }
            return null;
        }

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

    private static class JawTargetRules {
        static final Map<String, TargetRule> RULES = new java.util.HashMap<>();

        static {
            RULES.put("A1", TargetRule.LOWEST_SPEED);
            RULES.put("A2", TargetRule.HIGHEST_ARMOR);
            RULES.put("A3", TargetRule.LOWEST_SPEED);
            RULES.put("A4", TargetRule.LOWEST_SPEED);
            RULES.put("A5", TargetRule.LOWEST_PERCEPTION);
            RULES.put("A6", TargetRule.LOWEST_PERCEPTION);
            RULES.put("A7", TargetRule.LOWEST_PERCEPTION);
            RULES.put("A8", TargetRule.LOWEST_SPEED);
            RULES.put("A9", TargetRule.LOWEST_PERCEPTION);
            RULES.put("A10", TargetRule.LOWEST_SPEED);
            RULES.put("A12", TargetRule.LOWEST_PERCEPTION);
            RULES.put("A13", TargetRule.LOWEST_PERCEPTION);
            RULES.put("A14", TargetRule.LOWEST_DEFENSE);
            RULES.put("A15", TargetRule.LOWEST_DEFENSE);
        }
    }

    private static class AttackResolution {
        final String modeLabel;
        final boolean success;
        final int damage;

        AttackResolution(String modeLabel, boolean success, int damage) {
            this.modeLabel = modeLabel;
            this.success = success;
            this.damage = damage;
        }
    }

    private enum TargetRule {
        LOWEST_SPEED,
        HIGHEST_ARMOR,
        LOWEST_PERCEPTION,
        LOWEST_DEFENSE,
        RANDOM
    }

    private enum DietType {
        HERBIVORE,
        CARNIVORE,
        OMNIVORE
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
