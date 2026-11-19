package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Assignment
import androidx.compose.material.icons.filled.Quiz
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.model.AppLanguage
import com.example.model.AppStrings
import com.example.ui.components.AcademicDisclaimerBanner
import com.example.ui.theme.Emerald100
import com.example.ui.theme.Emerald600
import com.example.ui.theme.Indigo100
import com.example.ui.theme.Indigo600
import com.example.ui.theme.Navy100
import com.example.ui.theme.Navy800
import com.example.ui.theme.Saffron100
import com.example.ui.theme.Saffron600
import com.example.viewmodel.BoardCatalogState

@Composable
fun PracticeHubScreen(
    state: BoardCatalogState,
    onOpenImportantQuestions: () -> Unit,
    onOpenMcqPractice: () -> Unit,
    onOpenQuizSetup: () -> Unit,
    onOpenMockTests: () -> Unit
) {
    val lang = state.preferences.language
    val s = AppStrings.get(lang)
    val wrongIdsCount = state.attempts.flatMap { it.wrongMcqIds }.distinct().size

    Box(
        contentAlignment = Alignment.TopCenter,
        modifier = Modifier.fillMaxSize()
    ) {
        LazyColumn(
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier
                .widthIn(max = 720.dp)
                .fillMaxSize()
        ) {
            item {
                Text(
                    text = s.practiceHubTitle,
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    text = if (lang == AppLanguage.OR) {
                        "${state.preferences.board.titleOr} · ${state.preferences.board.subtitleOr}"
                    } else {
                        "${state.preferences.board.titleEn} · ${state.preferences.board.subtitleEn}"
                    },
                    style = MaterialTheme.typography.bodyMedium,
                    color = Saffron600,
                    fontWeight = FontWeight.SemiBold
                )
            }

            item {
                AcademicDisclaimerBanner(text = s.disclaimerBanner)
            }

            item {
                PracticeModuleCard(
                    title = s.importantBankTitle,
                    subtitle = "${state.importantQuestions.size} Qs · 1M, 2M, 3M, 5M & Long Answers",
                    icon = Icons.Default.Star,
                    iconBg = Navy100,
                    iconTint = Navy800,
                    tag = "hub_important_questions",
                    onClick = onOpenImportantQuestions
                )
            }

            item {
                PracticeModuleCard(
                    title = s.mcqPracticeTitle,
                    subtitle = "${state.mcqs.size} Qs · Chapter, Subject, Random & Wrong Retry ($wrongIdsCount)",
                    icon = Icons.Default.Quiz,
                    iconBg = Saffron100,
                    iconTint = Saffron600,
                    tag = "hub_mcq_practice",
                    onClick = onOpenMcqPractice
                )
            }

            item {
                PracticeModuleCard(
                    title = s.quizEngineTitle,
                    subtitle = "10 / 20 / 30 / 50 Qs · Timer, Palette & Mark for Review",
                    icon = Icons.Default.Timer,
                    iconBg = Indigo100,
                    iconTint = Indigo600,
                    tag = "hub_quiz_engine",
                    onClick = onOpenQuizSetup
                )
            }

            item {
                PracticeModuleCard(
                    title = s.mockExamTitle,
                    subtitle = "${state.mockExams.size} Mock Papers · Admin-configured pattern & review",
                    icon = Icons.Default.Assignment,
                    iconBg = Emerald100,
                    iconTint = Emerald600,
                    tag = "hub_mock_exams",
                    onClick = onOpenMockTests
                )
            }
        }
    }
}

@Composable
private fun PracticeModuleCard(
    title: String,
    subtitle: String,
    icon: ImageVector,
    iconBg: Color,
    iconTint: Color,
    tag: String,
    onClick: () -> Unit
) {
    Surface(
        shape = RoundedCornerShape(18.dp),
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .clickable(onClick = onClick)
            .testTag(tag)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(16.dp)
        ) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .size(50.dp)
                    .clip(CircleShape)
                    .background(iconBg)
            ) {
                Icon(icon, contentDescription = null, tint = iconTint, modifier = Modifier.size(26.dp))
            }
            Spacer(Modifier.width(14.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                Spacer(Modifier.height(2.dp))
                Text(subtitle, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                contentDescription = "Open",
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
