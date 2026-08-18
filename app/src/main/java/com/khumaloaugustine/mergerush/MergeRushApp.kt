package com.khumaloaugustine.mergerush

import androidx.compose.foundation.*
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.*
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.draw.*
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.*
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import kotlin.math.abs
import java.time.LocalDate

private val Ink: Color @Composable get() = MaterialTheme.colorScheme.background
private val Panel: Color @Composable get() = MaterialTheme.colorScheme.surfaceVariant
private val Violet: Color @Composable get() = MaterialTheme.colorScheme.primary
private val Mint: Color @Composable get() = MaterialTheme.colorScheme.secondary
private val Readable: Color @Composable get() = MaterialTheme.colorScheme.onSurface

private val evolutionNames = listOf("Spark", "Seed", "Sprout", "Bloom", "Grove", "Village", "Town", "City", "Kingdom", "Empire", "Legend", "Mythic", "Cosmic", "Infinite")
private fun evolutionName(value: Int): String {
    if (value < 2) return "Empty"
    val index = Integer.numberOfTrailingZeros(value).coerceIn(1, evolutionNames.size) - 1
    return evolutionNames[index]
}

@Composable fun MergeRushApp(vm: GameViewModel = viewModel()) {
    val game by vm.game.collectAsStateWithLifecycle()
    val player by vm.player.collectAsStateWithLifecycle()
    val undos by vm.undos.collectAsStateWithLifecycle()
    val hint by vm.hint.collectAsStateWithLifecycle()
    val shuffles by vm.shuffles.collectAsStateWithLifecycle()
    var screen by rememberSaveable { mutableStateOf("home") }
    var onboardingDismissed by rememberSaveable { mutableStateOf(false) }
    val dark = when (player.themeMode) { "light" -> false; "dark" -> true; else -> isSystemInDarkTheme() }
    val scheme = if (dark) darkColorScheme(primary = Color(0xFFA78BFA), secondary = Color(0xFF5EE7B7), background = Color(0xFF101426), surface = Color(0xFF151A31), surfaceVariant = Color(0xFF202743), onBackground = Color(0xFFF7F5FF), onSurface = Color(0xFFF7F5FF))
    else lightColorScheme(primary = Color(0xFF6842C2), secondary = Color(0xFF087F5B), background = Color(0xFFF7F6FC), surface = Color.White, surfaceVariant = Color(0xFFECE9F6), onBackground = Color(0xFF171525), onSurface = Color(0xFF171525))
    MaterialTheme(colorScheme = scheme) {
        if (screen == "game") {
            Surface(Modifier.fillMaxSize()) { GameScreen(game, player, undos, shuffles, hint, vm::move, vm::undo, vm::shuffle, vm::activateFusionPulse, vm::showHint, vm::replay, vm::nextLevel, vm::continueAfterReward, { screen = "levels" }) { screen = "home" } }
        } else {
            Scaffold(bottomBar = { AppNavigation(screen) { screen = it } }) { padding ->
                Surface(Modifier.fillMaxSize().padding(padding)) {
                    when (screen) {
                        "levels" -> LevelsScreen(player) { level -> vm.startLevel(level); screen = "game" }
                        "stats" -> StatsScreen(player)
                        "settings" -> SettingsScreen(player.themeMode, vm::setTheme)
                        else -> HomeScreen(player, game, { vm.restart(); screen = "game" }, { screen = "game" }, { screen = "levels" }, { vm.startDailyChallenge(); screen = "game" })
                    }
                }
            }
        }
        if (!player.onboardingSeen && !onboardingDismissed) Onboarding {
            onboardingDismissed = true
            vm.completeOnboarding()
        }
    }
}

@Composable private fun AppNavigation(selected: String, select: (String) -> Unit) {
    NavigationBar(containerColor = Panel) {
        listOf(Triple("home", Icons.Rounded.Home, "Home"), Triple("levels", Icons.Rounded.EmojiEvents, "Play"), Triple("stats", Icons.Rounded.BarChart, "Stats"), Triple("settings", Icons.Rounded.Settings, "Settings")).forEach { (route, icon, label) ->
            NavigationBarItem(selected = selected == route, onClick = { select(route) }, icon = { Icon(icon, label) }, label = { Text(label) }, colors = NavigationBarItemDefaults.colors(indicatorColor = Violet.copy(.3f), selectedIconColor = Mint))
        }
    }
}

@Composable private fun HomeScreen(player: PlayerData, game: GameState, play: () -> Unit, resume: () -> Unit, levels: () -> Unit, daily: () -> Unit) {
    val canResume = game.moves > 0 && !game.gameOver && !game.won
    val completedToday = player.lastDailyDay == LocalDate.now().toEpochDay().toInt()
    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(horizontal = 22.dp, vertical = 14.dp), horizontalAlignment = Alignment.CenterHorizontally) {
        Spacer(Modifier.height(8.dp))
        Box(Modifier.size(72.dp).background(Violet, RoundedCornerShape(22.dp)), contentAlignment = Alignment.Center) {
            Text("2⁺", fontSize = 42.sp, fontWeight = FontWeight.Black)
        }
        Spacer(Modifier.height(10.dp))
        Text("MERGE RUSH", fontSize = 30.sp, fontWeight = FontWeight.Black, letterSpacing = 2.sp)
        Text("Slide. Merge. Go bigger.", color = Readable.copy(.72f))
        Surface(color = Panel, shape = RoundedCornerShape(18.dp), modifier = Modifier.fillMaxWidth().padding(top = 12.dp)) {
            Row(Modifier.padding(10.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.Center) {
                Text("2", color = Mint, fontSize = 22.sp, fontWeight = FontWeight.Black)
                Text("  +  ", color = Readable.copy(.6f))
                Text("2", color = Mint, fontSize = 22.sp, fontWeight = FontWeight.Black)
                Icon(Icons.Rounded.ArrowForward, null, Modifier.padding(horizontal = 10.dp), tint = Violet)
                Column { Text("4  •  ${evolutionName(4)}", fontWeight = FontWeight.Black); Text("Keep merging to evolve your world", color = Readable.copy(.68f), fontSize = 11.sp) }
            }
        }
        Spacer(Modifier.height(12.dp))
        Button(if (canResume) resume else play, Modifier.fillMaxWidth().height(52.dp), shape = RoundedCornerShape(18.dp)) {
            Icon(if (canResume) Icons.Rounded.Restore else Icons.Rounded.PlayArrow, null); Spacer(Modifier.width(8.dp)); Text(if (canResume) "CONTINUE GAME" else "PLAY", fontWeight = FontWeight.Bold)
        }
        if (canResume) TextButton(play, Modifier.padding(top = 4.dp)) { Text("PLAY NEW GAME") }
        Button(levels, Modifier.fillMaxWidth().padding(top = 8.dp).height(52.dp), colors = ButtonDefaults.buttonColors(containerColor = Mint, contentColor = Ink), shape = RoundedCornerShape(18.dp)) {
            Icon(Icons.Rounded.EmojiEvents, null); Spacer(Modifier.width(8.dp)); Text("LEVELS", fontWeight = FontWeight.Bold)
        }
        FilledTonalButton(daily, Modifier.fillMaxWidth().padding(top = 8.dp).height(48.dp), shape = RoundedCornerShape(16.dp), enabled = !completedToday) {
            Icon(if (completedToday) Icons.Rounded.CheckCircle else Icons.Rounded.Today, null)
            Spacer(Modifier.width(8.dp))
            Text(if (completedToday) "DAILY CHALLENGE COMPLETE" else "TODAY'S FUSION CHALLENGE", fontWeight = FontWeight.Bold)
            if (player.dailyStreak > 0) Text("  •  ${player.dailyStreak} day streak", fontSize = 11.sp)
        }
        Spacer(Modifier.height(16.dp))
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
            MiniStat("BEST", player.highScore.toString()); MiniStat("COINS", player.coins.toString())
        }
    }
}

@Composable private fun MiniStat(label: String, value: String) = Column(horizontalAlignment = Alignment.CenterHorizontally) {
    Text(value, fontSize = 22.sp, fontWeight = FontWeight.Bold, color = Mint); Text(label, fontSize = 11.sp, color = Readable.copy(.68f))
}

@Composable private fun GameScreen(game: GameState, player: PlayerData, undos: Int, shuffles: Int, hint: Direction?, move: (Direction) -> Unit, undo: () -> Unit, shuffle: () -> Unit, fusionPulse: () -> Unit, showHint: () -> Unit, restart: () -> Unit, nextLevel: () -> Unit, continueGame: () -> Unit, levels: () -> Unit, back: () -> Unit) {
    var showHelp by rememberSaveable { mutableStateOf(false) }
    var soundOn by rememberSaveable { mutableStateOf(true) }
    val haptics = LocalHapticFeedback.current
    val sounds = remember { SoundEffects() }
    DisposableEffect(Unit) { onDispose { sounds.release() } }
    LaunchedEffect(game.moves, game.won, game.gameOver, game.fusionValue) {
        if (soundOn) when {
            game.won -> sounds.win()
            game.gameOver -> sounds.lose()
            game.fusionValue > 0 -> sounds.powerUp()
            game.milestone != null -> sounds.milestone()
            game.lastGain > 0 -> sounds.merge(game.combo)
            game.moves > 0 -> sounds.move()
        }
    }
    BoxWithConstraints(Modifier.fillMaxSize()) {
      val compact = maxHeight < 620.dp || maxWidth < 300.dp
      Column(Modifier.widthIn(max = 600.dp).fillMaxSize().verticalScroll(rememberScrollState()).padding(horizontal = if (compact) 10.dp else 14.dp, vertical = if (compact) 4.dp else 10.dp), horizontalAlignment = Alignment.CenterHorizontally) {
        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            IconButton(back, Modifier.size(if (compact) 40.dp else 48.dp)) { Icon(Icons.Rounded.ArrowBack, "Back") }
            Text("Merge Rush", fontWeight = FontWeight.Black, fontSize = if (compact) 15.sp else 18.sp, modifier = Modifier.weight(1f))
            IconButton({ soundOn = !soundOn }, Modifier.size(if (compact) 40.dp else 48.dp)) { Icon(if (soundOn) Icons.Rounded.VolumeUp else Icons.Rounded.VolumeOff, if (soundOn) "Mute sounds" else "Turn sounds on") }
            TextButton({ showHelp = true }, Modifier.height(if (compact) 40.dp else 48.dp), contentPadding = PaddingValues(horizontal = 5.dp)) { Icon(Icons.Rounded.HelpOutline, null, Modifier.size(17.dp)); Spacer(Modifier.width(3.dp)); Text("Help", fontSize = if (compact) 11.sp else 14.sp) }
        }
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            FilledTonalButton(showHint, Modifier.weight(1f).height(if (compact) 34.dp else 40.dp), enabled = !game.gameOver && !game.won, contentPadding = PaddingValues(horizontal = 4.dp)) { Icon(Icons.Rounded.Lightbulb, null, Modifier.size(15.dp), tint = if (hint != null) Color(0xFFFFD166) else LocalContentColor.current); Spacer(Modifier.width(3.dp)); Text("Hint", fontSize = 10.sp) }
            FilledTonalButton(undo, Modifier.weight(1f).height(if (compact) 34.dp else 40.dp), enabled = undos > 0 && game.moves > 0 && !game.gameOver, contentPadding = PaddingValues(horizontal = 4.dp)) { Icon(Icons.Rounded.Undo, null, Modifier.size(15.dp)); Spacer(Modifier.width(3.dp)); Text("Undo ($undos)", fontSize = 10.sp) }
            FilledTonalButton(restart, Modifier.weight(1f).height(if (compact) 34.dp else 40.dp), contentPadding = PaddingValues(horizontal = 4.dp)) { Icon(Icons.Rounded.Refresh, null, Modifier.size(15.dp)); Spacer(Modifier.width(3.dp)); Text("Restart", fontSize = 10.sp) }
        }
        Row(Modifier.fillMaxWidth().padding(vertical = if (compact) 4.dp else 10.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            ScoreCard("SCORE", game.score, Modifier.weight(1f), compact); ScoreCard("BEST", maxOf(game.score, player.highScore), Modifier.weight(1f), compact)
        }
        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f)) { Text(when { game.levelNumber == DAILY_LEVEL_NUMBER -> "DAILY CHALLENGE  •  GOAL ${game.target}"; game.levelNumber > 0 -> "LEVEL ${game.levelNumber}  •  GOAL ${game.target}"; else -> "GOAL  ${game.target}" }, color = Mint, fontSize = 12.sp, fontWeight = FontWeight.Bold); LinearProgressIndicator(progress = { ((game.board.maxOrNull() ?: 2).toFloat() / game.target).coerceIn(0f, 1f) }, Modifier.fillMaxWidth().padding(top = 5.dp)) }
            AnimatedVisibility(game.combo >= 2) { Text("  x${game.combo.coerceAtMost(5)} COMBO", color = Color(0xFFFFD166), fontWeight = FontWeight.Black) }
        }
        AnimatedVisibility(game.moves < 3) {
            Surface(color = Violet.copy(.14f), shape = RoundedCornerShape(14.dp), modifier = Modifier.fillMaxWidth().padding(top = 8.dp)) {
                Row(Modifier.padding(horizontal = 10.dp, vertical = if (compact) 6.dp else 9.dp), verticalAlignment = Alignment.CenterVertically) {
                    Icon(if (game.moves == 0) Icons.Rounded.Swipe else Icons.Rounded.AutoAwesome, null, Modifier.size(if (compact) 16.dp else 24.dp), tint = Violet)
                    Text(if (game.moves == 0) "Swipe the board. Bring two matching numbers together." else "Great! Equal numbers become the next evolution. Keep going toward ${game.target}.", Modifier.padding(start = 8.dp), fontSize = if (compact) 10.sp else 12.sp, lineHeight = if (compact) 13.sp else 16.sp, fontWeight = FontWeight.SemiBold)
                }
            }
        }
        if (game.moveLimit > 0) Text("${game.moveLimit - game.moves} moves remaining", Modifier.padding(top = 8.dp), color = if (game.moveLimit - game.moves <= 8) Color(0xFFD84315) else Readable.copy(.78f), fontWeight = FontWeight.Bold)
        if (game.scoreGoal > 0 || game.comboGoal > 0) Column(Modifier.fillMaxWidth().padding(top = 6.dp), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(4.dp)) {
            if (game.scoreGoal > 0) ObjectiveChip(if (game.score >= game.scoreGoal) "✓ SCORE ${game.scoreGoal}" else "SCORE ${game.score}/${game.scoreGoal}", game.score >= game.scoreGoal)
            if (game.comboGoal > 0) ObjectiveChip(if (game.bestCombo >= game.comboGoal) "✓ COMBO ×${game.comboGoal}" else "BEST ×${game.bestCombo}/${game.comboGoal}", game.bestCombo >= game.comboGoal)
        }
        val occupied = game.board.count { it != 0 }
        val danger = occupied / game.board.size.toFloat()
        Row(Modifier.fillMaxWidth().padding(top = 7.dp), verticalAlignment = Alignment.CenterVertically) {
            Text(when { danger >= .82f -> "DANGER"; danger >= .62f -> "BOARD BUSY"; else -> "BOARD SAFE" }, color = when { danger >= .82f -> Color(0xFFFF6B6B); danger >= .62f -> Color(0xFFFFD166); else -> Mint }, fontSize = 10.sp, fontWeight = FontWeight.Black)
            LinearProgressIndicator(progress = { danger }, Modifier.weight(1f).padding(horizontal = 8.dp), color = when { danger >= .82f -> Color(0xFFFF6B6B); danger >= .62f -> Color(0xFFFFD166); else -> Mint })
            FilledTonalIconButton(onClick = { if (soundOn) sounds.powerUp(); shuffle() }, enabled = shuffles > 0 && !game.won, modifier = Modifier.size(34.dp)) { Icon(Icons.Rounded.Shuffle, "Shuffle board", Modifier.size(18.dp)) }
            Text("×$shuffles", fontSize = 11.sp, color = Readable.copy(.72f))
        }
        if (game.fusionEnergy >= 3) Button({ if (soundOn) sounds.powerUp(); fusionPulse() }, Modifier.fillMaxWidth().padding(top = 5.dp).height(if (compact) 34.dp else 42.dp), shape = RoundedCornerShape(14.dp), colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFFB829), contentColor = Color(0xFF171525)), contentPadding = PaddingValues(horizontal = 8.dp)) {
            Icon(Icons.Rounded.Bolt, null, Modifier.size(18.dp)); Spacer(Modifier.width(5.dp)); Text("FUSION READY — COMBINE A PAIR", fontSize = if (compact) 10.sp else 12.sp, fontWeight = FontWeight.Black)
        } else Row(Modifier.fillMaxWidth().padding(top = 4.dp), verticalAlignment = Alignment.CenterVertically) {
            Text("FUSION", fontSize = 9.sp, fontWeight = FontWeight.Black, color = Violet)
            repeat(3) { index -> Box(Modifier.weight(1f).height(5.dp).padding(horizontal = 2.dp).background(if (index < game.fusionEnergy) Color(0xFFFFB829) else Panel, RoundedCornerShape(5.dp))) }
            Text(" ${game.fusionEnergy}/3", fontSize = 9.sp, color = Readable.copy(.68f))
        }
        AnimatedVisibility(hint != null) { Text("Hint: swipe ${hint?.name?.lowercase()}  •  You still choose the move", Modifier.padding(top = 6.dp), color = Color(0xFFFFD166), fontWeight = FontWeight.Bold, fontSize = 13.sp) }
        AnimatedVisibility(game.fusionValue > 0) { Text("FUSION PULSE created ${game.fusionValue}!", Modifier.padding(top = 4.dp), color = Color(0xFFFFB829), fontWeight = FontWeight.Black, fontSize = 12.sp) }
        Spacer(Modifier.height(if (compact) 5.dp else 14.dp))
        var dx by remember { mutableFloatStateOf(0f) }; var dy by remember { mutableFloatStateOf(0f) }
        Column(Modifier.widthIn(max = if (compact) 190.dp else 520.dp).fillMaxWidth().aspectRatio(1f).background(Panel, RoundedCornerShape(24.dp)).padding(if (compact) 5.dp else 8.dp)
            .pointerInput(game.board) { detectDragGestures(onDragStart = { dx = 0f; dy = 0f }, onDrag = { change, amount -> change.consume(); dx += amount.x; dy += amount.y }, onDragEnd = {
                if (maxOf(abs(dx), abs(dy)) > 36f) { haptics.performHapticFeedback(HapticFeedbackType.TextHandleMove); move(if (abs(dx) > abs(dy)) if (dx > 0) Direction.RIGHT else Direction.LEFT else if (dy > 0) Direction.DOWN else Direction.UP) }
            }) }) {
            repeat(BOARD_SIZE) { r -> Row(Modifier.weight(1f)) { repeat(BOARD_SIZE) { c -> Tile(game.board[r * BOARD_SIZE + c], Modifier.weight(1f).fillMaxHeight()) } } }
        }
        Spacer(Modifier.height(if (compact) 5.dp else 12.dp))
        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.Center) {
            AnimatedContent(game.lastGain, Modifier.then(if (compact) Modifier.weight(1f) else Modifier), label = "score gain") { gain ->
                val largest = game.board.maxOrNull() ?: 2
                Text(if (gain > 0) "+$gain  •  ${evolutionName(largest)} discovered" else "Next mission: merge equal tiles to reach ${game.target}", color = if (gain > 0) Mint else Readable.copy(.72f), fontSize = if (compact) 10.sp else 13.sp, lineHeight = if (compact) 13.sp else 16.sp, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center)
            }
            if (compact) Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.padding(start = 6.dp)) { Text("NEXT", fontSize = 8.sp, color = Readable.copy(.68f)); Tile(game.nextTile, Modifier.size(38.dp)) }
        }
        if (!compact) { Spacer(Modifier.height(8.dp)); Text("NEXT TILE", fontSize = 11.sp, color = Readable.copy(.68f)); Tile(game.nextTile, Modifier.size(54.dp)) }
      }
    }
    if (game.gameOver) AlertDialog(onDismissRequest = {}, icon = { OutcomeIcon(false) }, title = { Text(if (game.levelNumber > 0) "Try this level again" else "Game complete") }, text = {
        Column(horizontalAlignment = Alignment.CenterHorizontally) { Text("Score  ${game.score}", fontSize = 26.sp, fontWeight = FontWeight.Bold); Text(if (game.levelNumber > 0) "Almost there. Review the objectives, adjust your strategy, and try again." else "You earned ${maxOf(1, game.score / 100)} coins.", color = Readable.copy(.78f), textAlign = TextAlign.Center) }
    }, confirmButton = { Button(restart) { Text("PLAY AGAIN") } }, dismissButton = {
        if (!game.continued && game.levelNumber == 0) OutlinedButton(continueGame) { Text("ONE MORE CHANCE") }
    })
    if (game.won) AlertDialog(onDismissRequest = {}, icon = { OutcomeIcon(true) }, title = { Text(if (game.levelNumber == DAILY_LEVEL_NUMBER) "Daily challenge complete!" else "Level ${game.levelNumber} complete!") }, text = {
        val level = campaignLevels.firstOrNull { it.number == game.levelNumber }
        val remaining = game.moveLimit - game.moves
        val stars = when { remaining >= game.moveLimit / 3 -> 3; remaining > 0 -> 2; else -> 1 }
        Column(horizontalAlignment = Alignment.CenterHorizontally) { Text("★".repeat(stars) + "☆".repeat(3 - stars), color = Color(0xFFFFD166), fontSize = 34.sp, letterSpacing = 5.sp); Text(if (game.levelNumber == DAILY_LEVEL_NUMBER) "You completed today's shared Fusion mission and extended your streak. Come back tomorrow for a new challenge." else "Excellent strategy! You reached ${game.target} with $remaining moves left and earned ${level?.reward ?: 0} bonus coins.", textAlign = TextAlign.Center) }
    }, confirmButton = {
        if (game.levelNumber == DAILY_LEVEL_NUMBER) Button(levels) { Text("DONE") } else if (game.levelNumber < campaignLevels.size) Button(nextLevel) { Text("NEXT LEVEL  ›") } else Button(levels) { Text("ALL LEVELS") }
    }, dismissButton = { TextButton(restart) { Text("REPLAY") } })
    if (showHelp) AlertDialog(onDismissRequest = { showHelp = false }, icon = { Icon(Icons.Rounded.Swipe, null, tint = Mint) }, title = { Text("How to play") }, text = {
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Text("1. Swipe in any direction. Every tile moves together.")
            Text("2. Two equal numbers merge: 2 + 2 = 4, then 4 + 4 = 8.")
            Text("3. Keep making bigger numbers until you reach the goal above the board.")
            Text("4. Merge on consecutive moves for bonus points. Tap Hint whenever you get stuck.")
            Text("5. Make three successful merging moves to charge Fusion. Tap it to combine a matching pair anywhere on the board.")
        }
    }, confirmButton = { Button({ showHelp = false }) { Text("GOT IT") } })
    if (game.milestone != null) Box(Modifier.fillMaxSize().padding(top = 115.dp), contentAlignment = Alignment.TopCenter) {
        Surface(color = Color(0xFFFFD166), shape = RoundedCornerShape(50), shadowElevation = 12.dp) { Text("${evolutionName(game.milestone)} discovered  •  ${game.milestone}", Modifier.padding(horizontal = 22.dp, vertical = 12.dp), color = Ink, fontWeight = FontWeight.Black) }
    }
}

@Composable private fun LevelsScreen(player: PlayerData, play: (Level) -> Unit) {
    Column(Modifier.fillMaxSize()) {
        Column(Modifier.padding(horizontal = 24.dp, vertical = 18.dp)) { Text("LEVELS", fontSize = 24.sp, fontWeight = FontWeight.Black); Text("Choose a level. Your progress saves automatically.", color = Readable.copy(.7f), fontSize = 14.sp) }
        Column(Modifier.verticalScroll(rememberScrollState()).padding(horizontal = 20.dp, vertical = 4.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            campaignLevels.forEach { level ->
                if (level.number in listOf(1, 11, 21, 31)) Text(when (level.number) { 1 -> "FOUNDATIONS"; 11 -> "STRATEGIST"; 21 -> "EXPERT LEAGUE"; else -> "LEGEND LEAGUE" }, color = if (level.number >= 21) Color(0xFFD99A00) else Mint, fontSize = 12.sp, fontWeight = FontWeight.Black, letterSpacing = 2.sp, modifier = Modifier.padding(top = 10.dp, bottom = 2.dp))
                val unlocked = level.number <= player.unlockedLevel
                Surface(onClick = { if (unlocked) play(level) }, enabled = unlocked, color = if (unlocked) Panel else Panel.copy(.45f), shape = RoundedCornerShape(18.dp), modifier = Modifier.fillMaxWidth()) {
                    Row(Modifier.padding(18.dp), verticalAlignment = Alignment.CenterVertically) {
                        Box(Modifier.size(46.dp).background(if (unlocked) Violet else Color.Gray, RoundedCornerShape(14.dp)), contentAlignment = Alignment.Center) { Text(if (unlocked) level.number.toString() else "🔒", fontWeight = FontWeight.Black) }
                        Column(Modifier.weight(1f).padding(horizontal = 14.dp)) { Text(level.name, fontWeight = FontWeight.Bold, fontSize = 17.sp); Text(buildString { append("Tile ${level.target} · ${level.moveLimit} moves"); if (level.scoreGoal > 0) append(" · ${level.scoreGoal} pts"); if (level.comboGoal > 0) append(" · ×${level.comboGoal} combo") }, color = Readable.copy(.72f), fontSize = 13.sp, lineHeight = 18.sp) }
                        Column(horizontalAlignment = Alignment.End) { Text("+${level.reward}", color = Mint, fontWeight = FontWeight.Bold); Text("coins", fontSize = 10.sp, color = Readable.copy(.62f)) }
                    }
                }
            }
            Spacer(Modifier.height(20.dp))
        }
    }
}

@Composable private fun OutcomeIcon(won: Boolean) {
    val animation = rememberInfiniteTransition(label = "outcome")
    val pulse by animation.animateFloat(1f, if (won) 1.22f else .9f, infiniteRepeatable(tween(if (won) 650 else 110), RepeatMode.Reverse), label = "pulse")
    val motion by animation.animateFloat(if (won) -8f else -10f, if (won) 8f else 10f, infiniteRepeatable(tween(if (won) 900 else 90), RepeatMode.Reverse), label = "motion")
    Box(Modifier.size(94.dp).scale(pulse).background((if (won) Color(0xFFFFD166) else Color(0xFFFF6B6B)).copy(.16f), RoundedCornerShape(50)), contentAlignment = Alignment.Center) {
        Icon(if (won) Icons.Rounded.EmojiEvents else Icons.Rounded.HeartBroken, null, Modifier.size(62.dp).then(if (won) Modifier.rotate(motion) else Modifier.offset(x = motion.dp)), tint = if (won) Color(0xFFFFD166) else Color(0xFFFF6B6B))
    }
}

@Composable private fun ObjectiveChip(text: String, complete: Boolean) {
    Surface(color = if (complete) Mint.copy(.2f) else Panel, shape = RoundedCornerShape(30.dp), modifier = Modifier.padding(horizontal = 4.dp)) {
        Text(text, Modifier.padding(horizontal = 10.dp, vertical = 5.dp), color = if (complete) Mint else Readable.copy(.82f), fontSize = 11.sp, fontWeight = FontWeight.Bold)
    }
}

@Composable private fun Onboarding(done: () -> Unit) {
    var page by rememberSaveable { mutableIntStateOf(0) }
    val pages = listOf(
        Triple(Icons.Rounded.Swipe, "Welcome to Merge Rush", "Swipe the whole board. Equal tiles collide and grow into a bigger number."),
        Triple(Icons.Rounded.TrendingUp, "What happens next?", "Two 2s become 4. Two 4s become 8. Keep repeating this simple rule until you reach the goal shown above the board."),
        Triple(Icons.Rounded.AutoAwesome, "Build clever combos", "Merge on consecutive moves to multiply points. Later levels ask you to master these chains."),
        Triple(Icons.Rounded.Bolt, "Charge your Fusion Pulse", "Make three successful merging moves. Then Fusion can combine a matching pair anywhere and open valuable board space."),
        Triple(Icons.Rounded.EmojiEvents, "A campaign that grows with you", "Complete tile, score, combo, and move-limit objectives. Hints and three undos help you learn without removing the challenge.")
    )
    val item = pages[page]
    Dialog(onDismissRequest = {}) {
        Surface(shape = RoundedCornerShape(28.dp), color = Panel) {
            Column(Modifier.heightIn(max = 560.dp).verticalScroll(rememberScrollState()).padding(28.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(item.first, null, Modifier.size(64.dp), tint = if (page == 1) Color(0xFFFFD166) else Mint)
                Spacer(Modifier.height(22.dp)); Text(item.second, fontSize = 24.sp, fontWeight = FontWeight.Black, textAlign = TextAlign.Center)
                Spacer(Modifier.height(12.dp)); Text(item.third, color = Readable.copy(.8f), textAlign = TextAlign.Center, lineHeight = 22.sp)
                Spacer(Modifier.height(26.dp)); Row(horizontalArrangement = Arrangement.spacedBy(7.dp)) { repeat(pages.size) { Box(Modifier.size(if (it == page) 22.dp else 8.dp, 8.dp).background(if (it == page) Violet else Readable.copy(.25f), RoundedCornerShape(10.dp))) } }
                Spacer(Modifier.height(24.dp)); Button({ if (page < pages.lastIndex) page++ else done() }, Modifier.fillMaxWidth().height(52.dp), shape = RoundedCornerShape(16.dp)) { Text(if (page < pages.lastIndex) "NEXT" else "START PLAYING", fontWeight = FontWeight.Bold) }
                if (page > 0) TextButton({ page-- }) { Text("BACK") }
            }
        }
    }
}

@Composable private fun ScoreCard(label: String, value: Int, modifier: Modifier, compact: Boolean = false) = Column(modifier.background(Panel, RoundedCornerShape(16.dp)).padding(if (compact) 7.dp else 14.dp), horizontalAlignment = Alignment.CenterHorizontally) {
    Text(label, fontSize = if (compact) 8.sp else 10.sp, color = Readable.copy(.68f)); Text(value.toString(), fontSize = if (compact) 18.sp else 24.sp, fontWeight = FontWeight.Black)
}

@Composable private fun Tile(value: Int, modifier: Modifier) {
    val colors = listOf(Color(0xFF303754), Color(0xFF47E6B1), Color(0xFF44B4F5), Color(0xFF8B5CF6), Color(0xFFEC6BC7), Color(0xFFFF8A5B), Color(0xFFFFD166))
    val index = if (value == 0) 0 else (Integer.numberOfTrailingZeros(value).coerceAtMost(colors.lastIndex))
    val color by animateColorAsState(colors[index], tween(220), label = "tile color")
    Box(modifier.padding(4.dp).background(color, RoundedCornerShape(14.dp)), contentAlignment = Alignment.Center) {
        AnimatedContent(value, transitionSpec = { (scaleIn(tween(180), initialScale = .7f) + fadeIn()) togetherWith fadeOut(tween(90)) }, label = "tile value") { tile ->
            if (tile != 0) Text(tile.toString(), fontSize = if (tile < 1000) 25.sp else 19.sp, fontWeight = FontWeight.Black, color = if (index in listOf(1, 5, 6)) Color(0xFF171525) else Color.White)
        }
    }
}

@Composable private fun SettingsScreen(themeMode: String, setTheme: (String) -> Unit) {
    val uriHandler = LocalUriHandler.current
    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(24.dp)) {
        Text("SETTINGS", fontSize = 25.sp, fontWeight = FontWeight.Black, modifier = Modifier.padding(vertical = 14.dp))
        Text("APPEARANCE", color = Mint, fontSize = 12.sp, fontWeight = FontWeight.Black, letterSpacing = 1.5.sp)
        Text("Choose the display that is easiest for you to read.", color = Readable.copy(.72f), modifier = Modifier.padding(top = 6.dp, bottom = 14.dp))
        listOf(Triple("system", Icons.Rounded.BrightnessAuto, "Use device setting"), Triple("light", Icons.Rounded.LightMode, "Light mode"), Triple("dark", Icons.Rounded.DarkMode, "Dark mode")).forEach { (mode, icon, label) ->
            Surface(onClick = { setTheme(mode) }, color = if (themeMode == mode) Violet.copy(.16f) else Panel, shape = RoundedCornerShape(16.dp), border = if (themeMode == mode) BorderStroke(2.dp, Violet) else null, modifier = Modifier.fillMaxWidth().padding(vertical = 5.dp)) {
                Row(Modifier.padding(18.dp), verticalAlignment = Alignment.CenterVertically) {
                    Icon(icon, null, tint = if (themeMode == mode) Violet else Readable.copy(.72f)); Text(label, Modifier.weight(1f).padding(horizontal = 14.dp), fontWeight = FontWeight.SemiBold)
                    if (themeMode == mode) Icon(Icons.Rounded.CheckCircle, "Selected", tint = Mint)
                }
            }
        }
        Spacer(Modifier.height(24.dp))
        Text("ACCESSIBILITY", color = Mint, fontSize = 12.sp, fontWeight = FontWeight.Black, letterSpacing = 1.5.sp)
        Surface(color = Panel, shape = RoundedCornerShape(16.dp), modifier = Modifier.fillMaxWidth().padding(top = 12.dp)) {
            Row(Modifier.padding(18.dp), verticalAlignment = Alignment.CenterVertically) { Icon(Icons.Rounded.Visibility, null, tint = Violet); Column(Modifier.padding(start = 14.dp)) { Text("High-contrast game tiles", fontWeight = FontWeight.SemiBold); Text("Numbers use bold type and distinct colours in both themes.", color = Readable.copy(.7f), fontSize = 13.sp) } }
        }
        Spacer(Modifier.height(24.dp))
        Text("ABOUT", color = Mint, fontSize = 12.sp, fontWeight = FontWeight.Black, letterSpacing = 1.5.sp)
        Surface(onClick = { uriHandler.openUri("https://khumaloaugustine.github.io/merge-rush/privacy-policy.html") }, color = Panel, shape = RoundedCornerShape(16.dp), modifier = Modifier.fillMaxWidth().padding(top = 12.dp)) {
            Row(Modifier.padding(18.dp), verticalAlignment = Alignment.CenterVertically) { Icon(Icons.Rounded.PrivacyTip, null, tint = Violet); Text("Privacy Policy", Modifier.weight(1f).padding(horizontal = 14.dp), fontWeight = FontWeight.SemiBold); Icon(Icons.Rounded.OpenInNew, "Open privacy policy") }
        }
        Text("Support: augustinekhumalo96@gmail.com", color = Readable.copy(.7f), fontSize = 13.sp, modifier = Modifier.padding(16.dp))
    }
}

@Composable private fun StatsScreen(player: PlayerData) {
    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(22.dp)) {
        Text("STATISTICS", fontSize = 25.sp, fontWeight = FontWeight.Black, modifier = Modifier.padding(vertical = 14.dp))
        Spacer(Modifier.height(28.dp))
        listOf("Best score" to player.highScore, "Games played" to player.games, "Highest tile" to player.highestTile, "Total coins" to player.coins, "Daily streak" to player.dailyStreak, "Best daily streak" to player.bestDailyStreak).forEach { (label, value) ->
            Row(Modifier.fillMaxWidth().padding(vertical = 6.dp).background(Panel, RoundedCornerShape(16.dp)).padding(20.dp)) { Text(label, Modifier.weight(1f)); Text(value.toString(), color = Mint, fontWeight = FontWeight.Bold) }
        }
        val discovered = if (player.highestTile < 2) 0 else Integer.numberOfTrailingZeros(player.highestTile).coerceAtMost(evolutionNames.size)
        Surface(color = Violet.copy(.14f), shape = RoundedCornerShape(18.dp), modifier = Modifier.fillMaxWidth().padding(top = 18.dp)) {
            Column(Modifier.padding(18.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) { Icon(Icons.Rounded.AutoAwesome, null, tint = Violet); Text("EVOLUTION JOURNEY", Modifier.padding(start = 10.dp), fontWeight = FontWeight.Black) }
                Text(if (discovered == 0) "Merge your first pair to discover Spark." else "$discovered discoveries  •  Latest: ${evolutionName(player.highestTile)}", Modifier.padding(top = 10.dp), color = Readable.copy(.78f))
                LinearProgressIndicator(progress = { discovered / evolutionNames.size.toFloat() }, Modifier.fillMaxWidth().padding(top = 12.dp))
            }
        }
    }
}
