package com.daille.evolutioncards;

import android.os.Bundle;
import android.graphics.Color;
import android.text.SpannableString;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.style.ForegroundColorSpan;
import android.text.style.StyleSpan;
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
import java.text.Normalizer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class PlayActivity extends AppCompatActivity {

    public static final String EXTRA_PLAYER_COUNT = "player_count";
    private static final int DEFAULT_PLAYER_COUNT = 3;
    private static final int MIN_PLAYER_COUNT = 2;
    private static final int MAX_PLAYER_COUNT = 3;
    private static final int HAND_TARGET = 5;
    private static final int MAX_SPECIES = 3;
    private static final int ATTRIBUTE_MIN = 0;
    private static final int ATTRIBUTE_MAX = 10;
    private static final int STAT_LABEL_WIDTH = "Metabolismo".length();

    private final Random random = new Random();
    private final List<PlayerState> players = new ArrayList<>();
    private final List<GameCard> biomeDeck = new ArrayList<>();
    private final List<GameCard> revealedBiomes = new ArrayList<>();
    private final List<SpeciesRef> forageParticipants = new ArrayList<>();
    private final List<SpeciesRef> attackParticipants = new ArrayList<>();

    private TextView biomeLabel;
    private TextView biomeEffectsLabel;
    private TextView biomeRevealedLabel;
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
    private final ArrayDeque<PhaseStep> pendingRoundPhases = new ArrayDeque<>();
    private boolean messageVisible = false;
    private Runnable onMessagesDrained;
    private int playerCount = DEFAULT_PLAYER_COUNT;
    private boolean gameOver = false;

    private int currentPlayer = 0;
    private int round = 1;
    private GameCard activeBiome;
    private Phase currentPhase = Phase.PLAYER_ACTION;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_play);
        int requestedPlayerCount = getIntent().getIntExtra(EXTRA_PLAYER_COUNT, DEFAULT_PLAYER_COUNT);
        playerCount = clamp(requestedPlayerCount, MIN_PLAYER_COUNT, MAX_PLAYER_COUNT);
        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }

        biomeLabel = findViewById(R.id.biomeLabel);
        biomeEffectsLabel = findViewById(R.id.biomeEffectsLabel);
        biomeRevealedLabel = findViewById(R.id.biomeRevealedLabel);
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
            if (gameOver) {
                return;
            }
            showCreateSpeciesDialog();
        });

        replaceCardButton.setOnClickListener(v -> {
            if (gameOver) {
                return;
            }
            showReplaceCardDialog();
        });

        passButton.setOnClickListener(v -> {
            if (gameOver) {
                return;
            }
            humanActionDiscardHand();
            endHumanTurn();
        });

        buttonContinueMessage.setOnClickListener(v -> showNextPendingMessage());

        setupHandInteractions();
        setupSpeciesInteractions();
        if (playerCount == 2) {
            hideBotTwoColumn();
        }

        setupGame();
        refreshUi();
    }

    private void hideBotTwoColumn() {
        bot2DeckLabel.setVisibility(View.INVISIBLE);
        bot2Species1Label.setVisibility(View.INVISIBLE);
        bot2Species2Label.setVisibility(View.INVISIBLE);
        bot2Species3Label.setVisibility(View.INVISIBLE);
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
        revealedBiomes.clear();
        for (int i = 0; i < Math.min(3, biomeDeck.size()); i++) {
            revealedBiomes.add(biomeDeck.get(i));
        }
        activeBiome = revealedBiomes.isEmpty() ? null : revealedBiomes.get(0);

        for (int i = 0; i < playerCount; i++) {
            List<GameCard> draft = new ArrayList<>(adaptationPool);
            Collections.shuffle(draft, random);
            List<GameCard> deck = new ArrayList<>(draft.subList(0, Math.min(40, draft.size())));
            PlayerState state = new PlayerState(i == 0 ? "Humano" : "Bot " + i, i == 0, deck);
            state.drawTo(HAND_TARGET);
            players.add(state);
        }

        beginPhase(Phase.PLAYER_ACTION, "Ronda 1 lista. Elige una acción: crear especie, reemplazar carta o descartar mano.");
        appendLog("Partida iniciada. Se generaron " + playerCount + " mazos aleatorios.");
    }

    private void endHumanTurn() {
        if (gameOver) {
            return;
        }
        beginPhase(Phase.PLAYER_ACTION, "Turno de bots: observa sus acciones.");
        currentPlayer = 1;
        runBotsUntilRoundEnds();
        resolveEndOfRound();
    }

    private void runBotsUntilRoundEnds() {
        for (int i = 1; i < playerCount; i++) {
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

        int discardedCards = discardHandAndDraw(bot);
        String msg = bot.name + " descarta su mano (" + discardedCards + " cartas) y roba "
                + bot.hand.size() + " cartas nuevas.";
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
                    species.individuals = 1;
                    species.food = 1;
                    species.addCard(first, random);
                    species.addCard(second, random);
                    species.health = species.getAdaptationHealth();
                    applyOnPlayCardEffects(human, species, first);
                    applyOnPlayCardEffects(human, species, second);
                    human.species.add(species);

                    appendLog("Humano crea una especie nueva (selección manual).");
                    endHumanTurn();
                })
                .show();
    }

    private void showReplaceCardDialog() {
        PlayerState human = players.get(0);
        if (human.hand.isEmpty() || human.species.isEmpty()) {
            appendLog("No se pudo reemplazar carta (requiere especie y cartas en mano).");
            endHumanTurn();
            return;
        }

        List<GameCard> handCandidates = new ArrayList<>();
        for (GameCard card : human.hand) {
            if (!isJaw(card)) {
                handCandidates.add(card);
            }
        }
        if (handCandidates.isEmpty()) {
            appendLog("No se pudo reemplazar carta (requiere carta de mano no mandíbula).");
            endHumanTurn();
            return;
        }

        CharSequence[] handLabels = new CharSequence[handCandidates.size()];
        for (int i = 0; i < handCandidates.size(); i++) {
            GameCard card = handCandidates.get(i);
            handLabels[i] = card.id + " · " + card.name + " [" + card.type + "]";
        }

        final int[] selectedHandCardIndex = {-1};
        new AlertDialog.Builder(this)
                .setTitle("Selecciona la carta de tu mano")
                .setSingleChoiceItems(handLabels, -1, (dialog, which) -> selectedHandCardIndex[0] = which)
                .setNegativeButton(R.string.play_cancel, null)
                .setPositiveButton(R.string.play_confirm, (dialog, which) -> {
                    if (selectedHandCardIndex[0] < 0) {
                        appendLog("Debes seleccionar una carta de tu mano para reemplazar.");
                        return;
                    }
                    GameCard selectedCard = handCandidates.get(selectedHandCardIndex[0]);
                    showReplaceSpeciesDialog(human, selectedCard);
                })
                .show();
    }

    private void showReplaceSpeciesDialog(PlayerState human, GameCard selectedCard) {
        CharSequence[] speciesLabels = new CharSequence[human.species.size()];
        for (int i = 0; i < human.species.size(); i++) {
            SpeciesState species = human.species.get(i);
            speciesLabels[i] = "Especie " + (i + 1)
                    + " (no mandíbula: " + species.getNonJawCardCount()
                    + "/" + species.individuals + " individuos)";
        }

        final int[] selectedSpeciesIndex = {-1};
        new AlertDialog.Builder(this)
                .setTitle("Selecciona la especie objetivo")
                .setSingleChoiceItems(speciesLabels, -1, (dialog, which) -> selectedSpeciesIndex[0] = which)
                .setNegativeButton(R.string.play_cancel, null)
                .setPositiveButton(R.string.play_confirm, (dialog, which) -> {
                    if (selectedSpeciesIndex[0] < 0) {
                        appendLog("Debes seleccionar una especie.");
                        return;
                    }

                    SpeciesState targetSpecies = human.species.get(selectedSpeciesIndex[0]);
                    if (targetSpecies.getNonJawCardCount() < targetSpecies.individuals) {
                        targetSpecies.addCard(selectedCard, random);
                        applyOnPlayCardEffects(human, targetSpecies, selectedCard);
                        human.hand.remove(selectedCard);
                        appendLog("Humano agrega una carta a la especie " + (selectedSpeciesIndex[0] + 1) + ".");
                        endHumanTurn();
                        return;
                    }

                    showReplaceTargetCardDialog(human, selectedCard, selectedSpeciesIndex[0], targetSpecies);
                })
                .show();
    }

    private void showReplaceTargetCardDialog(PlayerState human, GameCard selectedCard, int speciesIndex, SpeciesState targetSpecies) {
        List<Integer> replaceableIndexes = new ArrayList<>();
        List<GameCard> replaceableCards = new ArrayList<>();
        for (int i = 0; i < targetSpecies.cards.size(); i++) {
            GameCard card = targetSpecies.cards.get(i);
            if (!isJaw(card)) {
                replaceableIndexes.add(i);
                replaceableCards.add(card);
            }
        }
        if (replaceableCards.isEmpty()) {
            appendLog("No se pudo reemplazar carta (la especie seleccionada no tiene cartas no mandíbula).");
            endHumanTurn();
            return;
        }

        CharSequence[] replaceLabels = new CharSequence[replaceableCards.size()];
        for (int i = 0; i < replaceableCards.size(); i++) {
            GameCard card = replaceableCards.get(i);
            replaceLabels[i] = card.id + " · " + card.name + " [" + card.type + "]";
        }

        final int[] selectedReplaceIndex = {-1};
        new AlertDialog.Builder(this)
                .setTitle("Selecciona la carta a reemplazar")
                .setSingleChoiceItems(replaceLabels, -1, (dialog, which) -> selectedReplaceIndex[0] = which)
                .setNegativeButton(R.string.play_cancel, null)
                .setPositiveButton(R.string.play_confirm, (dialog, which) -> {
                    if (selectedReplaceIndex[0] < 0) {
                        appendLog("Debes seleccionar una carta a reemplazar.");
                        return;
                    }

                    int cardIndex = replaceableIndexes.get(selectedReplaceIndex[0]);
                    if (!targetSpecies.replaceNonJawAt(cardIndex, selectedCard, random)) {
                        appendLog("No se pudo completar el reemplazo en la especie seleccionada.");
                        endHumanTurn();
                        return;
                    }
                    human.hand.remove(selectedCard);
                    appendLog("Humano reemplaza una carta en la especie " + (speciesIndex + 1) + ".");
                    endHumanTurn();
                })
                .show();
    }

    private void humanActionDiscardHand() {
        PlayerState human = players.get(0);
        int discardedCards = discardHandAndDraw(human);
        appendLog("Humano descarta su mano (" + discardedCards + " cartas) y roba " + human.hand.size() + " cartas nuevas.");
    }

    private int discardHandAndDraw(PlayerState player) {
        int discardedCards = player.hand.size();
        player.hand.clear();
        player.drawTo(HAND_TARGET);
        return discardedCards;
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
        species.individuals = 1;
        species.food = 1;
        species.addCard(first, random);
        species.addCard(second, random);
        species.health = species.getAdaptationHealth();
        applyOnPlayCardEffects(player, species, first);
        applyOnPlayCardEffects(player, species, second);
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

        List<SpeciesState> addCandidates = new ArrayList<>();
        for (SpeciesState species : player.species) {
            if (species.getNonJawCardCount() < species.individuals) {
                addCandidates.add(species);
            }
        }
        if (!addCandidates.isEmpty()) {
            SpeciesState targetSpecies = addCandidates.get(random.nextInt(addCandidates.size()));
            GameCard addition = handCandidates.get(random.nextInt(handCandidates.size()));
            targetSpecies.addCard(addition, random);
            applyOnPlayCardEffects(player, targetSpecies, addition);
            player.hand.remove(addition);
            return true;
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
        clearRoundCombatMarkers();

        pendingRoundPhases.clear();
        pendingRoundPhases.add(new PhaseStep(Phase.FORAGE,
                "Se reparte comida en la zona de forraje y comen por velocidad.",
                this::resolveForagePhase));
        pendingRoundPhases.add(new PhaseStep(Phase.PREDATION,
                "Las especies con ataque intentan cazar.",
                this::resolvePredationPhase));
        pendingRoundPhases.add(new PhaseStep(Phase.RESOLUTION,
                "Cada especie consume metabolismo y se resuelven bajas.",
                this::resolveResolutionPhase));
        pendingRoundPhases.add(new PhaseStep(Phase.REPRODUCTION,
                "Las especies con comida suficiente se reproducen.",
                this::resolveReproductionPhase));
        pendingRoundPhases.add(new PhaseStep(Phase.REPLENISHMENT,
                "Se reponen manos y se actualiza puntaje.",
                this::resolveReplenishmentPhase));
        advanceRoundResolution();
    }

    private void advanceRoundResolution() {
        PhaseStep nextPhase = pendingRoundPhases.poll();
        if (nextPhase == null) {
            maybeAdvanceBiome();
            checkGameOver();
            refreshUi();
            if (gameOver) {
                return;
            }
            currentPlayer = 0;
            round++;
            beginPhase(Phase.PLAYER_ACTION, "Tu turno: elige una acción.");
            refreshUi();
            return;
        }

        beginPhase(nextPhase.phase, nextPhase.instruction);
        onMessagesDrained = () -> {
            nextPhase.resolver.run();
            refreshUi();
            onMessagesDrained = this::advanceRoundResolution;
            if (!messageVisible && pendingMessages.isEmpty()) {
                Runnable callback = onMessagesDrained;
                onMessagesDrained = null;
                if (callback != null) {
                    callback.run();
                }
            }
        };
    }

    private void resolveForagePhase() {
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
            ref.species.forageSuccessThisRound = true;
            renderForageTokens(foragePool);
            String feedMessage = getSpeciesLabel(ref) + " forrajea +" + gain + " comida (quedan " + foragePool + ").";
            appendLog(feedMessage);
            showMessage(feedMessage);
        }
        applyPostForageAbilities();
        if (forageParticipants.isEmpty()) {
            appendLog("Ninguna especie logró comer en forrajeo.");
            showMessage("Ninguna especie logró comer en forrajeo.");
        }
    }

    private void resolvePredationPhase() {
        List<SpeciesRef> allSpecies = collectSpecies();
        allSpecies.sort(Comparator
                .comparingInt((SpeciesRef ref) -> ref.species.getSpeed())
                .thenComparingInt(ref -> ref.species.getTotalAttributes())
                .reversed());

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
                if (resolution.damage > 0) {
                    target.species.tookPredationDamageThisRound = true;
                    target.species.lastAttacker = attackerRef.player;
                }
                attacker.food += resolution.attackPower;
                attackerRef.player.score += resolution.attackPower;
                maybeApplyAttackStatus(attacker, target.species);
                maybeApplyDefenseTriggersOnSuccessfulAttack(attackerRef, target, resolution);
                String attackMessage = getSpeciesLabel(attackerRef)
                        + " ataca por " + resolution.modeLabel + " a " + getSpeciesLabel(target)
                        + ". Resultado ataque exitoso " + getSpeciesLabel(target)
                        + " pierde " + resolution.damage + " de salud y "
                        + getSpeciesLabel(attackerRef) + " recibe " + resolution.attackPower + " fichas de comida.";
                appendLog(attackMessage);
                showMessage(attackMessage);
            } else {
                String failMessage = getSpeciesLabel(attackerRef)
                        + " ataca por " + resolution.modeLabel + " a " + getSpeciesLabel(target)
                        + ". Resultado ataque fallido.";
                maybeApplyFailedAttackDefenderBonus(target.species, target);
                maybeApplyFailedAttackTriggers(attackerRef, target);
                appendLog(failMessage);
                showMessage(failMessage);
            }
        }
    }

    private void maybeApplyDefenseTriggersOnSuccessfulAttack(SpeciesRef attackerRef, SpeciesRef defenderRef, AttackResolution resolution) {
        SpeciesState attacker = attackerRef.species;
        SpeciesState defender = defenderRef.species;
        if (resolution.damage > 0 && defender.hasAbilityText("si es dañado por un ataque el atacante queda envenenado")) {
            attacker.applyStatus(Status.ENVENENADO);
        }
        if (resolution.damage > 0 && defender.hasAbilityText("si es dañado por un ataque el atacante recibe 1 de daño")) {
            attacker.health -= 1;
        }
        if (resolution.damage <= 0 && defender.hasAbilityText("si recibe 0 de daño tras un ataque el atacante queda aturdido")) {
            attacker.applyStatus(Status.PARALIZADO);
        }
    }

    private void maybeApplyFailedAttackTriggers(SpeciesRef attackerRef, SpeciesRef defenderRef) {
        SpeciesState attacker = attackerRef.species;
        SpeciesState defender = defenderRef.species;
        if (defender.hasAbilityText("si un rival te ataca") && defender.hasAbilityText("falla")
                && defender.hasAbilityText("confundido")) {
            attacker.applyStatus(Status.CONFUNDIDO);
        }
    }

    private void resolveResolutionPhase() {
        for (PlayerState player : players) {
            for (int i = player.species.size() - 1; i >= 0; i--) {
                SpeciesState species = player.species.get(i);
                String speciesName = "Especie " + (i + 1) + " de " + player.name;

                if (species.hasStatus(Status.ENVENENADO)) {
                    species.health -= 1;
                    appendLog(speciesName + " sufre 1 daño por estado Envenenado.");
                }

                int metabolism = species.getMetabolism(activeBiome);
                if (species.hasAbilityText("puede ignorar 1 consumo de alimento por ronda")
                        && !species.ignoredFoodConsumptionThisRound
                        && metabolism > 0) {
                    metabolism -= 1;
                    species.ignoredFoodConsumptionThisRound = true;
                }
                species.food -= metabolism;
                appendLog(speciesName + " consume " + metabolism + " comida por metabolismo.");

                boolean starvation = species.food < 1;
                boolean lowHealth = species.health < 1;
                if (lowHealth && species.hasCard("A66")) {
                    GameCard reviveCard = null;
                    for (GameCard card : species.cards) {
                        if ("A66".equalsIgnoreCase(card.id)) {
                            reviveCard = card;
                            break;
                        }
                    }
                    if (reviveCard != null) {
                        species.cards.remove(reviveCard);
                        species.health = species.getAdaptationHealth();
                        lowHealth = false;
                        appendLog(speciesName + " activa su habilidad de supervivencia: descarta una adaptación para evitar perder individuo.");
                    }
                }
                if (lowHealth || starvation) {
                    species.individuals -= 1;
                    grantScavengerFoodBonus();
                    species.food = Math.max(0, species.food);
                    species.health = species.getAdaptationHealth();
                    species.clearStatuses();
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
                    if (species.lastAttacker != null && species.tookPredationDamageThisRound) {
                        species.lastAttacker.score += 4;
                    }
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

                species.trimNonJawToIndividuals(random);
                species.health = species.getAdaptationHealth();
            }
        }
    }

    private void grantScavengerFoodBonus() {
        for (SpeciesRef ref : collectSpecies()) {
            if (ref.species.hasAbilityText("obtiene mas 1 de alimento cada vez que un individuo es eliminado")
                    || ref.species.hasAbilityText("obtiene más 1 de alimento cada vez que un individuo es eliminado")) {
                ref.species.food += 1;
                String message = getSpeciesLabel(ref)
                        + " activa Mandíbula Trituradora y obtiene +1 comida por eliminación de individuo.";
                appendLog(message);
                showMessage(message);
            }
        }
    }

    private void resolveReproductionPhase() {
        for (PlayerState player : players) {
            for (int i = 0; i < player.species.size(); i++) {
                SpeciesState species = player.species.get(i);
                int fertility = species.getFertility();
                if (species.food >= fertility) {
                    species.food -= fertility;
                    species.individuals += 1;
                    species.health = species.getAdaptationHealth();
                    player.score += 5;
                    String reproductionMessage = "Especie " + (i + 1) + " de " + player.name + " se reprodujo (+1 individuo).";
                    appendLog(reproductionMessage);
                    showMessage(reproductionMessage);
                }
            }
        }
    }

    private void resolveReplenishmentPhase() {
        for (PlayerState player : players) {
            for (SpeciesState species : player.species) {
                player.score += species.food;
                player.score += species.cards.size();
            }
            player.score += player.species.size() * 10;
            int previousHand = player.hand.size();
            player.drawTo(HAND_TARGET);
            appendLog(player.name + " repone mano: " + previousHand + " → " + player.hand.size() + " cartas. Puntaje: " + player.score + ".");
        }

        showMessage(buildScoreboardOverlayMessage());
    }

    private String buildScoreboardOverlayMessage() {
        StringBuilder builder = new StringBuilder();
        builder.append("📊 Puntajes al final de la ronda ").append(round).append("\n\n");
        for (int i = 0; i < players.size(); i++) {
            PlayerState player = players.get(i);
            builder.append(i + 1)
                    .append(") ")
                    .append(player.name)
                    .append(": ")
                    .append(player.score)
                    .append(" pts");
            if (i < players.size() - 1) {
                builder.append("\n");
            }
        }
        return builder.toString();
    }

    private void clearRoundCombatMarkers() {
        for (PlayerState player : players) {
            for (SpeciesState species : player.species) {
                species.tookPredationDamageThisRound = false;
                species.forageSuccessThisRound = false;
                species.failedAttackFoodBonusAppliedThisRound = false;
                species.wasFirstAttackDefenseUsedThisRound = false;
                species.ignoredFoodConsumptionThisRound = false;
                species.paidStatusProtectionThisRound = false;
                species.lastAttacker = null;
            }
        }
    }

    private void checkGameOver() {
        if (gameOver) {
            return;
        }
        PlayerState winner = null;
        for (PlayerState player : players) {
            if (winner == null || player.score > winner.score) {
                winner = player;
            }
        }
        boolean scoreLimitReached = winner != null && winner.score >= 150;
        boolean deckExhausted = false;
        for (PlayerState player : players) {
            if (player.deck.isEmpty()) {
                deckExhausted = true;
                break;
            }
        }
        if (!scoreLimitReached && !deckExhausted) {
            return;
        }

        gameOver = true;
        String reason = scoreLimitReached
                ? "Fin de partida: " + winner.name + " alcanzó 150 puntos."
                : "Fin de partida: se agotó al menos un mazo.";
        appendLog(reason);
        showMessage(reason);
    }

    private void maybeAdvanceBiome() {
        int topScore = 0;
        for (PlayerState player : players) {
            topScore = Math.max(topScore, player.score);
        }
        if (revealedBiomes.size() >= 3) {
            if (topScore >= 100) {
                activeBiome = revealedBiomes.get(2);
            } else if (topScore >= 50) {
                activeBiome = revealedBiomes.get(1);
            } else {
                activeBiome = revealedBiomes.get(0);
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

        int attackPower = attacker.getAttack();
        if (attacker.hasCard("A15") || attacker.hasAbilityText("al atacar obtiene +1 de ataque")) {
            attackPower += 1;
        }
        int effectiveArmor = defender.getArmor();
        if (attacker.hasCard("A2") || attacker.hasAbilityText("ignora la armadura de la presa")) {
            effectiveArmor = 0;
        }
        if (defender.hasAbilityText("la primera vez que esta especie sea atacada cada ronda")
                && !defender.wasFirstAttackDefenseUsedThisRound) {
            defender.wasFirstAttackDefenseUsedThisRound = true;
            attackPower = Math.max(0, attackPower - 1);
        }
        if ("emboscada".equals(modeLabel)
                && defender.hasAbilityText("si es atacado por emboscada recibe un bono de +2 de velocidad")) {
            success = (attacker.getSpeed() > defender.getSpeed() + 2);
        }

        int damage = success ? Math.max(0, attackPower - (effectiveArmor / 2)) : 0;
        return new AttackResolution(modeLabel, success, damage, attackPower);
    }

    private void maybeApplyAttackStatus(SpeciesState attacker, SpeciesState defender) {
        if (attacker.hasAbilityText("si golpea a la presa está queda paralizada")
                || attacker.hasAbilityText("si ataca con éxito el defensor queda paralizado")) {
            defender.applyStatus(Status.PARALIZADO);
            return;
        }
        if (attacker.hasAbilityText("el objetivo queda envenenado")) {
            defender.applyStatus(Status.ENVENENADO);
            return;
        }
        if (attacker.hasAbilityText("aplica un estado aleatorio a la especie objetivo")) {
            Status[] rollable = new Status[]{
                    Status.ENVENENADO,
                    Status.PARALIZADO,
                    Status.CONFUNDIDO,
                    Status.ENFERMEDAD,
                    Status.TERROR
            };
            defender.applyStatus(rollable[random.nextInt(rollable.length)]);
            return;
        }
        String jawId = attacker.getPrimaryJawId();
        if (jawId == null) {
            return;
        }
        if ("A5".equals(jawId)) {
            defender.applyStatus(Status.ENVENENADO);
            return;
        }
        if ("A6".equals(jawId) || "A12".equals(jawId)) {
            defender.applyStatus(Status.PARALIZADO);
            return;
        }
        if ("A14".equals(jawId)) {
            Status[] rollable = new Status[]{
                    Status.ENVENENADO,
                    Status.PARALIZADO,
                    Status.CONFUNDIDO,
                    Status.ENFERMEDAD,
                    Status.TERROR
            };
            defender.applyStatus(rollable[random.nextInt(rollable.length)]);
        }
    }

    private void applyPostForageAbilities() {
        for (SpeciesRef ref : collectSpecies()) {
            SpeciesState species = ref.species;
            String primaryJawId = species.getPrimaryJawId();
            if ("A8".equals(primaryJawId) && !species.forageSuccessThisRound) {
                species.food += 1;
                String message = getSpeciesLabel(ref) + " activa Mandíbula Filtradora y obtiene +1 comida fuera de la zona de forraje.";
                appendLog(message);
                showMessage(message);
            }
            if ("A106".equals(primaryJawId) && !species.forageSuccessThisRound) {
                species.food += 1;
                String message = getSpeciesLabel(ref) + " activa Mandíbula de Broteo y obtiene +1 comida al no forrajear.";
                appendLog(message);
                showMessage(message);
            }
            if (species.forageSuccessThisRound
                    && species.isInAnyBiome("pradera", "estepa")
                    && species.hasAbilityText("si forrajeas en pradera/estepa")) {
                species.food += 1;
                String message = getSpeciesLabel(ref) + " obtiene +1 comida adicional por habilidad de forrajeo en Pradera/Estepa.";
                appendLog(message);
                showMessage(message);
            }
            if (species.forageSuccessThisRound
                    && (species.hasCard("A108")
                    || species.hasAbilityText("puedes repartir la comida obtenida entre 2 especies aliadas herbivoras adyacentes"))) {
                applyAdjacentHerbivoreForageShare(ref);
            }
        }
    }

    private void applyAdjacentHerbivoreForageShare(SpeciesRef sourceRef) {
        PlayerState owner = sourceRef.player;
        int sourceIndex = owner.species.indexOf(sourceRef.species);
        if (sourceIndex < 0) {
            return;
        }

        int shared = 0;
        int[] adjacentIndexes = new int[]{sourceIndex - 1, sourceIndex + 1};
        for (int idx : adjacentIndexes) {
            if (idx < 0 || idx >= owner.species.size()) {
                continue;
            }
            SpeciesState ally = owner.species.get(idx);
            if (ally.getDietType() == DietType.CARNIVORE) {
                continue;
            }
            ally.food += 1;
            shared += 1;
            if (shared >= 2) {
                break;
            }
        }

        if (shared > 0) {
            String message = getSpeciesLabel(sourceRef)
                    + " comparte alimento por Mandíbula Lofodonta (+" + shared + " comida a especies aliadas adyacentes).";
            appendLog(message);
            showMessage(message);
        }
    }

    private void maybeApplyFailedAttackDefenderBonus(SpeciesState defender, SpeciesRef defenderRef) {
        if (!defender.hasCard("A113") || defender.failedAttackFoodBonusAppliedThisRound) {
            return;
        }
        defender.food += 1;
        defender.failedAttackFoodBonusAppliedThisRound = true;
        String message = getSpeciesLabel(defenderRef) + " activa Mandíbula de Corte de Corteza y gana +1 comida por ataque fallido recibido.";
        appendLog(message);
        showMessage(message);
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
            if (onMessagesDrained != null) {
                Runnable callback = onMessagesDrained;
                onMessagesDrained = null;
                callback.run();
            }
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

    private String getBiomeEffectsText() {
        if (activeBiome == null || activeBiome.description == null || activeBiome.description.trim().isEmpty()) {
            return "Sin efectos de bioma activos.";
        }
        return "Efectos: " + activeBiome.description.replace("|", " · ");
    }

    private String getRevealedBiomesText() {
        if (revealedBiomes.isEmpty()) {
            return "Biomas revelados: -";
        }
        List<String> labels = new ArrayList<>();
        for (int i = 0; i < revealedBiomes.size(); i++) {
            GameCard biome = revealedBiomes.get(i);
            String activeTag = activeBiome == biome ? " (activo)" : "";
            labels.add((i + 1) + ") " + biome.name + activeTag);
        }
        return "Biomas revelados: " + String.join(" · ", labels);
    }

    private void appendLog(String message) {
        CharSequence current = logLabel.getText();
        String next = (current == null || current.length() == 0) ? message : current + "\n" + message;
        logLabel.setText(next);
    }

    private void refreshUi() {
        biomeLabel.setText(getString(R.string.play_forage_zone, activeBiome == null ? "N/D" : activeBiome.name));
        biomeEffectsLabel.setText(getBiomeEffectsText());
        biomeRevealedLabel.setText(getRevealedBiomesText());

        PlayerState human = players.get(0);
        PlayerState bot1 = players.size() > 1 ? players.get(1) : null;
        PlayerState bot2 = players.size() > 2 ? players.get(2) : null;

        humanDeckLabel.setText(formatHumanDeck(human));
        renderHumanHandSlots(human);

        renderSpeciesSlot(humanSpecies1Label, human, 0);
        renderSpeciesSlot(humanSpecies2Label, human, 1);
        renderSpeciesSlot(humanSpecies3Label, human, 2);

        if (bot1 != null) {
            bot1DeckLabel.setText(formatBotDeck(bot1));
            renderSpeciesSlot(bot1Species1Label, bot1, 0);
            renderSpeciesSlot(bot1Species2Label, bot1, 1);
            renderSpeciesSlot(bot1Species3Label, bot1, 2);
        }

        if (bot2 != null) {
            bot2DeckLabel.setText(formatBotDeck(bot2));
            renderSpeciesSlot(bot2Species1Label, bot2, 0);
            renderSpeciesSlot(bot2Species2Label, bot2, 1);
            renderSpeciesSlot(bot2Species3Label, bot2, 2);
        }
    }

    private void renderSpeciesSlot(TextView slotView, PlayerState player, int speciesIndex) {
        slotView.setText(formatSpeciesAt(player, speciesIndex));
        slotView.setBackgroundResource(getSpeciesBackground(player, speciesIndex));
    }

    private int getSpeciesBackground(PlayerState player, int speciesIndex) {
        if (speciesIndex >= player.species.size()) {
            return R.drawable.bg_species_card;
        }

        DietType dietType = player.species.get(speciesIndex).getDietType();
        if (dietType == DietType.CARNIVORE) {
            return R.drawable.bg_species_card_carnivore;
        }
        if (dietType == DietType.HERBIVORE) {
            return R.drawable.bg_species_card_herbivore;
        }
        return R.drawable.bg_species_card;
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
            if (playerIndex >= players.size()) {
                return;
            }
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
        TextView attackTarget = panel.findViewById(R.id.detailCardAttackTarget);
        TextView stats = panel.findViewById(R.id.detailCardStats);
        TextView ability = panel.findViewById(R.id.detailCardAbility);

        CardDesignDetails.DesignCardInfo designInfo = CardDesignDetails.findByGameCard(card);

        title.setText(card.id + " · " + card.name);
        type.setText("Tipo: " + card.type);
        rarity.setText("Rareza: " + (card.metadata == null || card.metadata.trim().isEmpty() ? "Sin dato" : card.metadata));
        description.setText("Descripción: " + (card.description == null || card.description.trim().isEmpty() ? "Sin descripción" : card.description));
        String targetText = buildAttackTargetText(card, designInfo);
        if (targetText.isEmpty()) {
            attackTarget.setVisibility(View.GONE);
        } else {
            attackTarget.setVisibility(View.VISIBLE);
            attackTarget.setText(targetText);
        }
        String statsText = buildStatsText(designInfo);
        stats.setText("Estadísticas que modifica: " + (statsText.isEmpty() ? "No modifica estadísticas." : statsText));
        String abilityText = buildAbilityText(designInfo);
        ability.setText("Habilidad: " + abilityText);

        new AlertDialog.Builder(this)
                .setTitle("Detalle de carta")
                .setView(panel)
                .setPositiveButton(R.string.play_confirm, null)
                .show();
    }

    private String buildAttackTargetText(GameCard card, CardDesignDetails.DesignCardInfo designInfo) {
        if (designInfo == null) {
            return "";
        }
        if (!"mandíbula".equalsIgnoreCase(card.type)) {
            return "";
        }
        if (!isPredatorFeedingType(designInfo.feedingType)) {
            return "";
        }
        if (isBlankOrDash(designInfo.attackTarget)) {
            return "";
        }
        return "Objetivo de ataque: " + designInfo.attackTarget;
    }

    private String buildStatsText(CardDesignDetails.DesignCardInfo designInfo) {
        if (designInfo == null) {
            return "";
        }
        List<String> parts = new ArrayList<>();
        appendStat(parts, "Ataque", designInfo.attack);
        appendStat(parts, "Armadura", designInfo.armor);
        appendStat(parts, "Salud", designInfo.health);
        appendStat(parts, "Velocidad", designInfo.speed);
        appendStat(parts, "Percepción", designInfo.perception);
        appendStat(parts, "Fertilidad", designInfo.fertility);
        appendStat(parts, "Metabolismo", designInfo.metabolism);
        appendStat(parts, "Temperatura", designInfo.temperature);
        return String.join(" | ", parts);
    }

    private String buildAbilityText(CardDesignDetails.DesignCardInfo designInfo) {
        if (designInfo == null || isBlankOrDash(designInfo.ability)) {
            return "Sin habilidad";
        }
        return designInfo.ability;
    }

    private void appendStat(List<String> parts, String label, String rawValue) {
        if (isBlankOrDash(rawValue)) {
            return;
        }
        try {
            double value = Double.parseDouble(rawValue);
            if (Math.abs(value) < 0.00001d) {
                return;
            }
            int intValue = (int) value;
            if (Math.abs(value - intValue) < 0.00001d) {
                parts.add(label + " " + (intValue > 0 ? "+" : "") + intValue);
                return;
            }
        } catch (NumberFormatException ignored) {
            // Si no es número, se muestra tal cual.
        }
        parts.add(label + " " + rawValue);
    }


    private boolean isPredatorFeedingType(String feedingType) {
        String normalized = normalizeSimple(feedingType);
        return "carnivoro".equals(normalized) || "omnivoro".equals(normalized);
    }

    private String normalizeSimple(String text) {
        return text == null ? "" : text.trim().toLowerCase(Locale.US).replace("í", "i");
    }

    private boolean isBlankOrDash(String value) {
        if (value == null) {
            return true;
        }
        String normalized = value.trim();
        return normalized.isEmpty() || "-".equals(normalized) || "—".equals(normalized)
                || "----".equals(normalized);
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

    private CharSequence buildSpeciesBarsText(SpeciesState species) {
        SpannableStringBuilder builder = new SpannableStringBuilder();
        builder.append(createStatLine("Salud", species.health, 0xFF8BC34A)).append("\n");
        builder.append(createStatLine("Ataque", species.getAttack(), 0xFFE53935)).append("\n");
        builder.append(createStatLine("Armadura", species.getArmor(), 0xFF4FC3F7)).append("\n");
        builder.append(createStatLine("Velocidad", species.getSpeed(), 0xFFFDD835)).append("\n");
        builder.append(createStatLine("Percepción", species.getPerception(), 0xFFBDBDBD)).append("\n");
        builder.append(createStatLine("Metabolismo", species.getMetabolism(activeBiome), 0xFFE6A57E)).append("\n");
        builder.append(createStatLine("Fertilidad", species.getFertility(), 0xFFBA68C8)).append("\n");
        builder.append(createStatLine("Temperatura", species.getTemperature(activeBiome), 0xFFF5F5F5));
        return builder;
    }

    private String buildSpeciesCardsText(SpeciesState species) {
        if (species.cards.isEmpty()) {
            return "Sin cartas de adaptación.";
        }
        StringBuilder builder = new StringBuilder();
        for (GameCard card : species.cards) {
            CardDesignDetails.DesignCardInfo designInfo = CardDesignDetails.findByGameCard(card);
            String statsText = buildStatsText(designInfo);
            String abilityText = buildAbilityText(designInfo);
            builder.append("• ")
                    .append(card.id)
                    .append(" · ")
                    .append(card.name)
                    .append(" (")
                    .append(card.type)
                    .append(")\n")
                    .append("   ")
                    .append(card.description)
                    .append("\n")
                    .append("   Estadísticas: ")
                    .append(statsText.isEmpty() ? "No modifica estadísticas." : statsText)
                    .append("\n")
                    .append("   Habilidad: ")
                    .append(abilityText)
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
        builder.append(styledHeader("Fichas de individuo")).append("\n");
        builder.append("Individuos: ");
        builder.append(createTokenDots(species.individuals, 0xFF000000));
        builder.append("\nAlimentación: ");
        builder.append(createTokenDots(species.food, 0xFF2E7D32));
        builder.append("\n\n");

        appendAttributeLine(builder, "Salud", species.health, 0xFF8BC34A);
        appendAttributeLine(builder, "Ataque", species.getAttack(), 0xFFE53935);
        appendAttributeLine(builder, "Armadura", species.getArmor(), 0xFF4FC3F7);
        appendAttributeLine(builder, "Velocidad", species.getSpeed(), 0xFFFDD835);
        appendAttributeLine(builder, "Percepción", species.getPerception(), 0xFFBDBDBD);
        appendAttributeLine(builder, "Metabolismo", species.getMetabolism(activeBiome), 0xFFE6A57E);

        int fertility = species.getFertility();
        appendAttributeLine(builder, "Fertilidad", fertility, 0xFFBA68C8);

        if (!species.statuses.isEmpty()) {
            builder.append("\nEstados: ");
            for (int i = 0; i < species.statuses.size(); i++) {
                if (i > 0) {
                    builder.append(", ");
                }
                builder.append(species.statuses.get(i).label);
            }
        }

        appendAttributeLine(builder, "Temperatura", species.getTemperature(activeBiome), 0xFFF5F5F5);
        return builder;
    }

    private void appendAttributeLine(SpannableStringBuilder builder, String label, int rawValue, int color) {
        builder.append(createStatLine(label, rawValue, color));
        if (!"Temperatura".contentEquals(label)) {
            builder.append("\n");
        }
    }

    private CharSequence createStatLine(String label, int rawValue, int color) {
        if ("Temperatura".contentEquals(label)) {
            return createTemperatureStatLine(rawValue);
        }
        int value = clamp(rawValue, ATTRIBUTE_MIN, ATTRIBUTE_MAX);
        SpannableStringBuilder line = new SpannableStringBuilder();

        String prefix = String.format(Locale.US, "%-" + STAT_LABEL_WIDTH + "s=%2d ", label, value);
        line.append(prefix);
        line.setSpan(new StyleSpan(android.graphics.Typeface.BOLD), 0, prefix.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);

        for (int i = 0; i < ATTRIBUTE_MAX; i++) {
            int start = line.length();
            line.append(i < value ? "█" : "░");
            int textColor = i < value ? color : Color.parseColor("#607D8B");
            line.setSpan(new ForegroundColorSpan(textColor), start, line.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        }
        return line;
    }

    private CharSequence createTemperatureStatLine(int value) {
        SpannableStringBuilder line = new SpannableStringBuilder();
        line.append("Temperatura=");
        int valueStart = line.length();
        line.append(String.valueOf(value));
        line.setSpan(new StyleSpan(android.graphics.Typeface.BOLD), 0, line.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        if (value > 0) {
            line.setSpan(new ForegroundColorSpan(0xFFE53935), valueStart, line.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        } else if (value < 0) {
            line.setSpan(new ForegroundColorSpan(0xFF1E88E5), valueStart, line.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        }
        return line;
    }

    private CharSequence styledHeader(String text) {
        SpannableString header = new SpannableString(text);
        header.setSpan(new StyleSpan(android.graphics.Typeface.BOLD), 0, header.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        return header;
    }

    private int clamp(int value, int min, int max) {
        return Math.max(min, Math.min(max, value));
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

    private class SpeciesState {
        private  final Set<String> HERBIVORE_JAWS = new HashSet<>(Arrays.asList(
                "A11", "A102", "A103", "A104", "A105", "A106", "A107", "A108",
                "A110", "A111", "A112", "A113", "A114", "A115", "A116"
        ));
        private  final Set<String> OMNIVORE_JAWS = new HashSet<>(Arrays.asList("A15", "A109"));

        final List<GameCard> cards = new ArrayList<>();
        final List<Status> statuses = new ArrayList<>();
        final Map<String, List<Integer>> selectedTemperatureEffects = new java.util.HashMap<>();
        int individuals;
        int food;
        int health;
        boolean tookPredationDamageThisRound;
        boolean forageSuccessThisRound;
        boolean failedAttackFoodBonusAppliedThisRound;
        boolean wasFirstAttackDefenseUsedThisRound;
        boolean ignoredFoodConsumptionThisRound;
        boolean paidStatusProtectionThisRound;
        int lastStatusAppliedRound = -1;
        PlayerState lastAttacker;

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
            GameCard primaryJaw = getPrimaryJawCard();
            if (primaryJaw == null) {
                return TargetRule.RANDOM;
            }
            return JawTargetRules.resolveForJaw(primaryJaw);
        }

        private String getPrimaryJawId() {
            GameCard primaryJaw = getPrimaryJawCard();
            return primaryJaw == null || primaryJaw.id == null
                    ? null
                    : primaryJaw.id.toUpperCase(Locale.US);
        }

        private GameCard getPrimaryJawCard() {
            for (GameCard card : cards) {
                if ("Mandíbula".equalsIgnoreCase(card.type)) {
                    return card;
                }
            }
            return null;
        }

        boolean hasCard(String cardId) {
            if (cardId == null) {
                return false;
            }
            for (GameCard card : cards) {
                if (cardId.equalsIgnoreCase(card.id)) {
                    return true;
                }
            }
            return false;
        }

        int getAttack() {
            return Math.max(0, getBaseStat("attack") + getBiomeModifier("Ataque") + getAbilityBasedStatModifier("attack"));
        }

        int getSpeed() {
            int speed = getBaseStat("speed") + getBiomeModifier("Velocidad");
            speed += getAbilityBasedStatModifier("speed");
            if (hasStatus(Status.PARALIZADO)) {
                return 0;
            }
            if (hasStatus(Status.TERROR)) {
                speed += 2;
            }
            return speed;
        }

        int getArmor() {
            return getBaseStat("armor") + getBiomeModifier("Armadura") + getAbilityBasedStatModifier("armor");
        }

        int getPerception() {
            int perception = getBaseStat("perception") + getBiomeModifier("Percepción");
            perception += getAbilityBasedStatModifier("perception");
            if (hasStatus(Status.CONFUNDIDO)) {
                perception -= 3;
            }
            if (hasStatus(Status.TERROR)) {
                return 0;
            }
            return perception;
        }

        int getTotalAttributes() {
            return getAttack() + getSpeed() + getArmor() + getPerception();
        }

        int getMetabolism(GameCard biome) {
            int metabolism = Math.max(1, individuals + getBaseStat("metabolism") + getBiomeModifier("Metabolismo"));
            metabolism += getAbilityBasedStatModifier("metabolism");
            int currentTemperature = getTemperature(biome);
            if (currentTemperature >= 5 || currentTemperature <= -5) {
                metabolism += 2;
            } else if (currentTemperature >= 3 || currentTemperature <= -3) {
                metabolism += 1;
            }
            if (hasStatus(Status.ENFERMEDAD)) {
                metabolism += 1;
            }
            return metabolism;
        }

        int getAbilityBasedStatModifier(String stat) {
            int total = 0;
            if ("speed".equals(stat)) {
                if (isInAnyBiome("manglar", "manglares", "playa", "arrecife")
                        && (hasAbilityText("manglares y arrecife +1 de velocidad")
                        || hasAbilityText("obtiene +1 de velocidad en manglares, playa y arrecife"))) {
                    total += 1;
                }
                if (isInAnyBiome("playa", "arrecife") && hasAbilityText("obtiene +1 de velocidad en playa y arrecife")) {
                    total += 1;
                }
                if (isInAnyBiome("jungla", "bosque") && (hasAbilityText("obtiene +2 de velocidad en jungla y bosque")
                        || hasAbilityText("+2 de velocidad en jungla y bosque"))) {
                    total += 2;
                }
                if (isInAnyBiome("jungla", "montaña") && hasAbilityText("+2 de velocidad en jungla y montaña")) {
                    total += 2;
                }
                if (isInAnyBiome("taiga", "estepa", "tundra", "montaña fría", "glaciares")
                        && hasAbilityText("+2 de velocidad en taiga, estepa, tundras, montaña fría y glaciares")) {
                    total += 2;
                }
                if (isInAnyBiome("desierto", "montaña", "sabana")
                        && hasAbilityText("esta especie tiene +1 de velocidad en desierto, montaña y sabana")) {
                    total += 1;
                }
                if (isInAnyBiome("taiga", "estepa", "tundra")
                        && hasAbilityText("esta especie tiene +1 de velocidad en taiga, estepa y tundra")) {
                    total += 1;
                }
            }
            if ("metabolism".equals(stat)
                    && isInAnyBiome("desierto", "montaña", "sabana")
                    && hasAbilityText("esta especie tiene -1 de metabolismo en desierto, montaña y sabana")) {
                total -= 1;
            }
            if ("perception".equals(stat)
                    && isInAnyBiome("manglar", "arrecife", "playa", "glaciares")
                    && hasAbilityText("obtiene mas 2 de persepcion en manglar, arecife, playa y glaciares")) {
                total += 2;
            }
            total += getGenericBiomeListModifier(stat);
            return total;
        }

        private int getGenericBiomeListModifier(String stat) {
            if (activeBiome == null || activeBiome.name == null) {
                return 0;
            }
            int total = 0;
            String normalizedActiveBiome = normalizeSimple(activeBiome.name);
            String[] statTokens;
            switch (stat) {
                case "attack":
                    statTokens = new String[]{"ataque"};
                    break;
                case "armor":
                    statTokens = new String[]{"armadura"};
                    break;
                case "speed":
                    statTokens = new String[]{"velocidad"};
                    break;
                case "perception":
                    statTokens = new String[]{"percepcion"};
                    break;
                case "fertility":
                    statTokens = new String[]{"fertilidad"};
                    break;
                case "metabolism":
                    statTokens = new String[]{"metabolismo"};
                    break;
                default:
                    statTokens = new String[0];
                    break;
            }
            if (statTokens.length == 0) {
                return 0;
            }

            Pattern valueInBiomes = Pattern.compile("([+-]?\\d+)\\s+de\\s+([a-záéíóúñ]+)\\s+en\\s+([^\\.|]+)");
            for (GameCard card : cards) {
                CardDesignDetails.DesignCardInfo info = CardDesignDetails.findByGameCard(card);
                if (info == null || info.ability == null) {
                    continue;
                }
                String ability = normalizeSimple(info.ability);
                Matcher matcher = valueInBiomes.matcher(ability);
                while (matcher.find()) {
                    String statName = matcher.group(2);
                    if (!matchesAnyToken(statName, statTokens)) {
                        continue;
                    }
                    int amount = parseIntSafe(matcher.group(1));
                    String biomeList = matcher.group(3);
                    if (isBiomeIncludedInText(normalizedActiveBiome, biomeList)) {
                        total += amount;
                    } else if (biomeList.contains("otros biomas")) {
                        total += amount;
                    }
                }
            }
            return total;
        }

        private boolean isBiomeIncludedInText(String activeBiomeNormalized, String biomeListText) {
            String list = normalizeSimple(biomeListText)
                    .replace("/", ",")
                    .replace(" y ", ",")
                    .replace(" e ", ",");
            String[] parts = list.split(",");
            for (String part : parts) {
                String biomeToken = part.trim();
                if (biomeToken.isEmpty() || "otros biomas".equals(biomeToken)) {
                    continue;
                }
                if (activeBiomeNormalized.contains(biomeToken) || biomeToken.contains(activeBiomeNormalized)) {
                    return true;
                }
            }
            return false;
        }

        private boolean matchesAnyToken(String candidate, String[] tokens) {
            String normalizedCandidate = normalizeSimple(candidate);
            for (String token : tokens) {
                if (normalizedCandidate.contains(normalizeSimple(token))) {
                    return true;
                }
            }
            return false;
        }

        boolean hasAbilityText(String fragment) {
            if (fragment == null || fragment.trim().isEmpty()) {
                return false;
            }
            String needle = normalizeSimple(fragment);
            for (GameCard card : cards) {
                CardDesignDetails.DesignCardInfo info = CardDesignDetails.findByGameCard(card);
                if (info == null || info.ability == null) {
                    continue;
                }
                if (normalizeSimple(info.ability).contains(needle)) {
                    return true;
                }
            }
            return false;
        }

        boolean isInAnyBiome(String... biomeNames) {
            if (activeBiome == null || activeBiome.name == null || biomeNames == null) {
                return false;
            }
            String active = normalizeSimple(activeBiome.name);
            for (String biomeName : biomeNames) {
                if (active.contains(normalizeSimple(biomeName))) {
                    return true;
                }
            }
            return false;
        }

        int getFertility() {
            return Math.max(1, individuals + getBaseStat("fertility") + getBiomeModifier("Fertilidad")
                    + getAbilityBasedStatModifier("fertility"));
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

        int getAdaptationHealth() {
            return getNonJawCardCount();
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
            GameCard removed = cards.remove(idx);
            unregisterTemperatureChoice(removed);
            addCard(replacement, random);
            return true;
        }

        boolean replaceNonJawAt(int cardIndex, GameCard replacement, Random random) {
            if (cardIndex < 0 || cardIndex >= cards.size()) {
                return false;
            }
            GameCard existing = cards.get(cardIndex);
            if ("Mandíbula".equalsIgnoreCase(existing.type)) {
                return false;
            }
            GameCard removed = cards.remove(cardIndex);
            unregisterTemperatureChoice(removed);
            addCard(replacement, random);
            return true;
        }

        void trimNonJawToIndividuals(Random random) {
            while (getNonJawCardCount() > individuals) {
                List<Integer> replaceable = new ArrayList<>();
                for (int i = 0; i < cards.size(); i++) {
                    if (!"Mandíbula".equalsIgnoreCase(cards.get(i).type)) {
                        replaceable.add(i);
                    }
                }
                if (replaceable.isEmpty()) {
                    return;
                }
                int idx = replaceable.get(random.nextInt(replaceable.size()));
                GameCard removed = cards.remove(idx);
                unregisterTemperatureChoice(removed);
            }
        }


        int getTemperature(GameCard biome) {
            return getBiomeTemperatureValue(biome) + getBaseStat("temperature") + getSelectedTemperatureEffects();
        }

        void addCard(GameCard card, Random random) {
            cards.add(card);
            registerTemperatureChoice(card, random);
        }

        private int getSelectedTemperatureEffects() {
            int total = 0;
            for (List<Integer> effects : selectedTemperatureEffects.values()) {
                for (int effect : effects) {
                    total += effect;
                }
            }
            return total;
        }

        private void registerTemperatureChoice(GameCard card, Random random) {
            Integer magnitude = getSelectableTemperatureMagnitude(card);
            if (magnitude == null || magnitude <= 0) {
                return;
            }
            int selectedEffect = random.nextBoolean() ? magnitude : -magnitude;
            String key = normalizeSimple(card.id);
            List<Integer> effects = selectedTemperatureEffects.get(key);
            if (effects == null) {
                effects = new ArrayList<>();
                selectedTemperatureEffects.put(key, effects);
            }
            effects.add(selectedEffect);
        }

        private void unregisterTemperatureChoice(GameCard card) {
            Integer magnitude = getSelectableTemperatureMagnitude(card);
            if (magnitude == null || magnitude <= 0) {
                return;
            }
            String key = normalizeSimple(card.id);
            List<Integer> effects = selectedTemperatureEffects.get(key);
            if (effects == null || effects.isEmpty()) {
                return;
            }
            effects.remove(effects.size() - 1);
            if (effects.isEmpty()) {
                selectedTemperatureEffects.remove(key);
            }
        }

        private Integer getSelectableTemperatureMagnitude(GameCard card) {
            if (card == null || card.id == null) {
                return null;
            }
            String normalizedId = normalizeSimple(card.id);
            if ("a30".equals(normalizedId) || "a60".equals(normalizedId)) {
                return 1;
            }
            if ("a34".equals(normalizedId)) {
                return 2;
            }
            return null;
        }

        void applyStatus(Status status) {
            if (status == null || statuses.contains(status)) {
                return;
            }
            if (hasAbilityText("inmunidad a todos los estados")) {
                return;
            }
            if (hasAbilityText("puede pagar 1 comida para ignorar un estado negativo")
                    && !paidStatusProtectionThisRound
                    && food > 0) {
                food -= 1;
                paidStatusProtectionThisRound = true;
                return;
            }
            if (lastStatusAppliedRound == round) {
                return;
            }
            if (statuses.size() >= 2) {
                statuses.remove(0);
            }
            statuses.add(status);
            lastStatusAppliedRound = round;
        }

        void clearStatuses() {
            statuses.clear();
            lastStatusAppliedRound = -1;
        }

        boolean hasStatus(Status status) {
            return statuses.contains(status);
        }

        private int getBaseStat(String stat) {
            int value = 0;
            for (GameCard card : cards) {
                CardDesignDetails.DesignCardInfo info = CardDesignDetails.findByGameCard(card);
                if (info == null) {
                    continue;
                }
                value += extractStatValue(info, stat);
                value += extractBiomeAbilityModifier(info, stat);
            }
            if ("attack".equals(stat) && value == 0) {
                for (GameCard card : cards) {
                    if ("Mandíbula".equalsIgnoreCase(card.type)) {
                        value += 1;
                    }
                }
            }
            return value;
        }

        private int extractStatValue(CardDesignDetails.DesignCardInfo info, String stat) {
            String raw;
            switch (stat) {
                case "attack":
                    raw = info.attack;
                    break;
                case "armor":
                    raw = info.armor;
                    break;
                case "health":
                    raw = info.health;
                    break;
                case "speed":
                    raw = info.speed;
                    break;
                case "perception":
                    raw = info.perception;
                    break;
                case "fertility":
                    raw = info.fertility;
                    break;
                case "metabolism":
                    raw = info.metabolism;
                    break;
                case "temperature":
                    raw = info.temperature;
                    break;
                default:
                    raw = "";
                    break;
            }
            return parseIntSafe(raw);
        }

        private int extractBiomeAbilityModifier(CardDesignDetails.DesignCardInfo info, String stat) {
            if (info == null || info.ability == null || activeBiome == null || activeBiome.name == null) {
                return 0;
            }
            String ability = info.ability;
            String biome = activeBiome.name;
            String statLabel = mapStatToAbilityLabel(stat);
            if (statLabel == null) {
                return 0;
            }
            Pattern pattern = Pattern.compile("En\\s+" + Pattern.quote(biome) + "[^:]*:\\s*([+-]?\\d+)\\s+" + Pattern.quote(statLabel), Pattern.CASE_INSENSITIVE);
            Matcher matcher = pattern.matcher(ability);
            if (!matcher.find()) {
                return 0;
            }
            return parseIntSafe(matcher.group(1));
        }

        private String mapStatToAbilityLabel(String stat) {
            switch (stat) {
                case "attack":
                    return "Ataque";
                case "armor":
                    return "Armadura";
                case "health":
                    return "Salud";
                case "speed":
                    return "Velocidad";
                case "perception":
                    return "Percepción";
                case "fertility":
                    return "Fertilidad";
                case "metabolism":
                    return "Metabolismo";
                case "temperature":
                    return "Temperatura";
                default:
                    return null;
            }
        }

        private int getBiomeModifier(String statLabel) {
            if (activeBiome == null || activeBiome.description == null) {
                return 0;
            }
            String[] parts = activeBiome.description.split("\\|");
            for (String part : parts) {
                String normalized = part.trim();
                if (!normalized.startsWith(statLabel + ":")) {
                    continue;
                }
                String rawValue = normalized.substring((statLabel + ":").length()).trim();
                return parseIntSafe(rawValue);
            }
            return 0;
        }

        private int parseIntSafe(String rawValue) {
            if (rawValue == null) {
                return 0;
            }
            String cleaned = rawValue.trim().replace("+", "");
            if (cleaned.isEmpty() || "-".equals(cleaned) || "----".equals(cleaned)) {
                return 0;
            }
            try {
                return Integer.parseInt(cleaned);
            } catch (NumberFormatException ignored) {
                return 0;
            }
        }
    }

    private void applyOnPlayCardEffects(PlayerState owner, SpeciesState targetSpecies, GameCard card) {
        if (owner == null || targetSpecies == null || card == null || card.id == null) {
            return;
        }
        String cardId = card.id.toUpperCase(Locale.US);
        CardDesignDetails.DesignCardInfo info = CardDesignDetails.findByGameCard(card);
        String ability = info == null || info.ability == null ? "" : normalizeSimple(info.ability);

        if (ability.contains("roba 1 carta") || ability.contains("roba una carta")) {
            owner.drawTo(Math.min(HAND_TARGET + 1, owner.hand.size() + 1));
        }
        if (ability.contains("roba 1 carta y luego descarta 1 carta") || ability.contains("roba 1 carta y luego descarta 1")) {
            if (!owner.hand.isEmpty()) {
                owner.hand.remove(random.nextInt(owner.hand.size()));
            }
        }
        if (ability.contains("coloca 2 de comida") || ability.contains("mueve 2 planta")) {
            targetSpecies.food += 2;
        }
        if (ability.contains("queda confundida") || ability.contains("queda confundido")) {
            SpeciesRef rival = chooseRandomEnemySpecies(owner);
            if (rival != null) {
                rival.species.applyStatus(Status.CONFUNDIDO);
            }
        }
        if (ability.contains("queda en terror")) {
            SpeciesRef rival = chooseRandomEnemySpecies(owner);
            if (rival != null) {
                rival.species.applyStatus(Status.TERROR);
            }
        }
        if (ability.contains("cura 1 punto de salud") && targetSpecies.health < targetSpecies.getAdaptationHealth()) {
            targetSpecies.health += 1;
        }
        if (ability.contains("cura 2 punto de salud") && targetSpecies.health < targetSpecies.getAdaptationHealth()) {
            targetSpecies.health += 2;
        }
        if (ability.contains("mira la mano de un rival") && ability.contains("ese rival la descarta")) {
            PlayerState rival = chooseRandomEnemyPlayerWithCards(owner);
            if (rival != null) {
                GameCard discarded = rival.hand.remove(random.nextInt(rival.hand.size()));
                appendLog(owner.name + " activa descarte de mano rival: " + rival.name
                        + " descarta " + discarded.id + " · " + discarded.name + ".");
            }
        }
        if (ability.contains("cambiar la carta de bioma activa por una al azar") && !biomeDeck.isEmpty()) {
            GameCard previousBiome = activeBiome;
            GameCard nextBiome = biomeDeck.get(random.nextInt(biomeDeck.size()));
            activeBiome = nextBiome;
            appendLog(owner.name + " cambia el bioma activo al jugar " + card.name + ": "
                    + (previousBiome == null ? "(sin bioma)" : previousBiome.name) + " → " + nextBiome.name + ".");
        }

        if ("A4".equals(cardId)) {
            owner.drawTo(Math.min(HAND_TARGET + 1, owner.hand.size() + 1));
            if (!owner.hand.isEmpty()) {
                owner.hand.remove(random.nextInt(owner.hand.size()));
            }
            appendLog(owner.name + " activa Mandíbula Canina: roba 1 carta y descarta 1 aleatoria.");
        } else if ("A112".equals(cardId)) {
            targetSpecies.food += 2;
            appendLog(owner.name + " activa Pico Granívoro: la especie obtiene +2 comida al jugarla.");
        } else if ("A126".equals(cardId) && activeBiome != null
                && ("Llanura".equalsIgnoreCase(activeBiome.name) || "Estepa".equalsIgnoreCase(activeBiome.name))) {
            owner.drawTo(Math.min(HAND_TARGET + 1, owner.hand.size() + 1));
            appendLog(owner.name + " activa Microhábitat Favorable: roba 1 carta por bioma activo.");
        }

        targetSpecies.health = Math.min(targetSpecies.health, targetSpecies.getAdaptationHealth());
    }

    private PlayerState chooseRandomEnemyPlayerWithCards(PlayerState owner) {
        List<PlayerState> candidates = new ArrayList<>();
        for (PlayerState player : players) {
            if (player == owner || player.hand.isEmpty()) {
                continue;
            }
            candidates.add(player);
        }
        if (candidates.isEmpty()) {
            return null;
        }
        return candidates.get(random.nextInt(candidates.size()));
    }

    private SpeciesRef chooseRandomEnemySpecies(PlayerState owner) {
        List<SpeciesRef> enemies = new ArrayList<>();
        for (PlayerState player : players) {
            if (player == owner) {
                continue;
            }
            for (SpeciesState species : player.species) {
                enemies.add(new SpeciesRef(player, species));
            }
        }
        if (enemies.isEmpty()) {
            return null;
        }
        return enemies.get(random.nextInt(enemies.size()));
    }

    private static class JawTargetRules {
        static TargetRule resolveForJaw(GameCard jawCard) {
            if (jawCard == null) {
                return TargetRule.RANDOM;
            }

            CardDesignDetails.DesignCardInfo designInfo = CardDesignDetails.findByGameCard(jawCard);
            if (designInfo == null || !isPredatorFeedingType(designInfo.feedingType)) {
                return TargetRule.RANDOM;
            }

            return fromAttackTargetText(designInfo.attackTarget);
        }

        private static boolean isPredatorFeedingType(String feedingType) {
            String normalized = normalize(feedingType);
            return "carnivoro".equals(normalized) || "omnivoro".equals(normalized);
        }

        private static TargetRule fromAttackTargetText(String rawText) {
            String normalized = normalize(rawText);
            if (normalized.isEmpty() || "----".equals(normalized)) {
                return TargetRule.RANDOM;
            }
            if (normalized.contains("menor velocidad")) {
                return TargetRule.LOWEST_SPEED;
            }
            if (normalized.contains("mayor armadura")) {
                return TargetRule.HIGHEST_ARMOR;
            }
            if (normalized.contains("menor persepcion") || normalized.contains("menor percepcion")) {
                return TargetRule.LOWEST_PERCEPTION;
            }
            if (normalized.contains("menor defensa")) {
                return TargetRule.LOWEST_DEFENSE;
            }
            return TargetRule.RANDOM;
        }

        private static String normalize(String value) {
            if (value == null) {
                return "";
            }
            String withoutAccents = Normalizer.normalize(value, Normalizer.Form.NFD)
                    .replaceAll("\\p{M}+", "");
            return withoutAccents.trim().toLowerCase(Locale.ROOT);
        }
    }

    private static class PhaseStep {
        final Phase phase;
        final String instruction;
        final Runnable resolver;

        PhaseStep(Phase phase, String instruction, Runnable resolver) {
            this.phase = phase;
            this.instruction = instruction;
            this.resolver = resolver;
        }
    }

    private static class AttackResolution {
        final String modeLabel;
        final boolean success;
        final int damage;
        final int attackPower;

        AttackResolution(String modeLabel, boolean success, int damage, int attackPower) {
            this.modeLabel = modeLabel;
            this.success = success;
            this.damage = damage;
            this.attackPower = attackPower;
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

    private enum Status {
        ENVENENADO("Envenenado"),
        PARALIZADO("Paralizado"),
        CONFUNDIDO("Confundido"),
        ENFERMEDAD("Enfermedad"),
        TERROR("Terror");

        final String label;

        Status(String label) {
            this.label = label;
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
