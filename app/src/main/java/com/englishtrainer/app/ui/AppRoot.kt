package com.englishtrainer.app.ui

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.englishtrainer.app.Accent
import com.englishtrainer.app.Speaker
import com.englishtrainer.app.data.Word
import com.englishtrainer.app.data.WordRepository
import kotlin.random.Random

private enum class Screen { HOME, STUDY, TRANSLATE, QUIZ }

@Composable
fun AppRoot(speaker: Speaker) {
    var screen by rememberSaveable { mutableStateOf(Screen.HOME) }
    BackHandler(enabled = screen != Screen.HOME) { screen = Screen.HOME }

    when (screen) {
        Screen.HOME -> HomeScreen(onOpen = { screen = it })
        Screen.STUDY -> StudyScreen(speaker, onBack = { screen = Screen.HOME })
        Screen.TRANSLATE -> TranslateScreen(speaker, onBack = { screen = Screen.HOME })
        Screen.QUIZ -> QuizScreen(speaker, onBack = { screen = Screen.HOME })
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ScreenFrame(
    title: String,
    onBack: (() -> Unit)?,
    content: @Composable (PaddingValues) -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(title) },
                navigationIcon = {
                    if (onBack != null) {
                        TextButton(onClick = onBack) { Text("← Назад") }
                    }
                }
            )
        },
        content = content
    )
}

@Composable
private fun HomeScreen(onOpen: (Screen) -> Unit) {
    ScreenFrame("EnglishTrainer", onBack = null) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                "Учите английские слова и выражения",
                style = MaterialTheme.typography.titleMedium
            )
            Button(onClick = { onOpen(Screen.STUDY) }, modifier = Modifier.fillMaxWidth()) {
                Text("Изучение слов")
            }
            Button(onClick = { onOpen(Screen.TRANSLATE) }, modifier = Modifier.fillMaxWidth()) {
                Text("Тренировка перевода")
            }
            Button(onClick = { onOpen(Screen.QUIZ) }, modifier = Modifier.fillMaxWidth()) {
                Text("Проверка знаний (тест)")
            }
            Text(
                "Приложение работает без подключения к интернету. Для озвучки нужны голоса " +
                    "English (US) и English (UK) в настройках синтеза речи Android.",
                style = MaterialTheme.typography.bodySmall
            )
        }
    }
}

@Composable
private fun SpeakButtons(speaker: Speaker, text: String) {
    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        Accent.entries.forEach { accent ->
            OutlinedButton(onClick = { speaker.speak(text, accent) }) {
                Text("🔊 ${accent.label}")
            }
        }
    }
}

@Composable
private fun StudyScreen(speaker: Speaker, onBack: () -> Unit) {
    val words = WordRepository.words
    var index by rememberSaveable { mutableIntStateOf(0) }
    var showTranslation by rememberSaveable { mutableStateOf(false) }
    val word = words[index]

    ScreenFrame("Изучение слов", onBack) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            Text("${index + 1} / ${words.size}", style = MaterialTheme.typography.labelLarge)

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { showTranslation = !showTranslation },
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.secondaryContainer
                )
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(32.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        word.en,
                        style = MaterialTheme.typography.headlineLarge,
                        textAlign = TextAlign.Center
                    )
                    Text(
                        if (showTranslation) word.ru else "Нажмите, чтобы увидеть перевод",
                        style = if (showTranslation) MaterialTheme.typography.titleLarge
                        else MaterialTheme.typography.bodyMedium,
                        textAlign = TextAlign.Center
                    )
                }
            }

            SpeakButtons(speaker, word.en)

            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedButton(
                    enabled = index > 0,
                    onClick = { index--; showTranslation = false }
                ) { Text("← Предыдущее") }
                Button(
                    enabled = index < words.lastIndex,
                    onClick = { index++; showTranslation = false }
                ) { Text("Следующее →") }
            }
        }
    }
}

@Composable
private fun TranslateScreen(speaker: Speaker, onBack: () -> Unit) {
    val seed = rememberSaveable { Random.nextLong() }
    val words = remember(seed) { WordRepository.words.shuffled(Random(seed)) }
    var index by rememberSaveable { mutableIntStateOf(0) }
    var input by rememberSaveable { mutableStateOf("") }
    var result by rememberSaveable { mutableStateOf<Boolean?>(null) }
    val word = words[index % words.size]

    ScreenFrame("Тренировка перевода", onBack) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(20.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text("Переведите на английский:", style = MaterialTheme.typography.titleMedium)
            Text(word.ru, style = MaterialTheme.typography.headlineLarge)

            OutlinedTextField(
                value = input,
                onValueChange = { input = it; result = null },
                label = { Text("Английский перевод") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

            when (result) {
                true -> Text("Верно! ✅", color = MaterialTheme.colorScheme.primary)
                false -> Text(
                    "Неверно. Правильный ответ: ${word.en}",
                    color = MaterialTheme.colorScheme.error
                )
                null -> {}
            }

            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Button(
                    enabled = input.isNotBlank() && result == null,
                    onClick = {
                        result = input.trim().equals(word.en, ignoreCase = true)
                        speaker.speak(word.en, Accent.US)
                    }
                ) { Text("Проверить") }
                OutlinedButton(onClick = {
                    index++
                    input = ""
                    result = null
                }) { Text("Дальше →") }
            }

            if (result != null) SpeakButtons(speaker, word.en)
        }
    }
}

@Composable
private fun QuizScreen(speaker: Speaker, onBack: () -> Unit) {
    val total = 10
    val seed = rememberSaveable { Random.nextLong() }
    var restart by rememberSaveable { mutableIntStateOf(0) }
    val questions: List<Word> = remember(seed, restart) {
        WordRepository.words.shuffled(Random(seed + restart)).take(total)
    }
    var q by rememberSaveable { mutableIntStateOf(0) }
    var score by rememberSaveable { mutableIntStateOf(0) }
    var selected by rememberSaveable { mutableStateOf<String?>(null) }

    ScreenFrame("Проверка знаний", onBack) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(20.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            if (q >= total) {
                Text("Тест завершён", style = MaterialTheme.typography.headlineMedium)
                Text("Результат: $score из $total", style = MaterialTheme.typography.titleLarge)
                Button(onClick = {
                    q = 0; score = 0; selected = null; restart++
                }) { Text("Пройти ещё раз") }
                OutlinedButton(onClick = onBack) { Text("В меню") }
            } else {
                val word = questions[q]
                val options = remember(seed, q, restart) {
                    val wrong = WordRepository.words
                        .filter { it.ru != word.ru }
                        .shuffled(Random(seed + q + restart))
                        .take(3)
                        .map { it.ru }
                    (wrong + word.ru).shuffled(Random(seed * 31 + q))
                }

                Text("Вопрос ${q + 1} из $total", style = MaterialTheme.typography.labelLarge)
                Text("Выберите перевод:", style = MaterialTheme.typography.titleMedium)
                Text(word.en, style = MaterialTheme.typography.headlineLarge)
                SpeakButtons(speaker, word.en)

                options.forEach { option ->
                    val answered = selected != null
                    val label = when {
                        !answered -> option
                        option == word.ru -> "✅ $option"
                        option == selected -> "❌ $option"
                        else -> option
                    }
                    OutlinedButton(
                        enabled = !answered,
                        modifier = Modifier.fillMaxWidth(),
                        onClick = {
                            selected = option
                            if (option == word.ru) score++
                        }
                    ) { Text(label) }
                }

                if (selected != null) {
                    Button(onClick = { q++; selected = null }) {
                        Text(if (q == total - 1) "Результат" else "Далее →")
                    }
                }
            }
        }
    }
}
