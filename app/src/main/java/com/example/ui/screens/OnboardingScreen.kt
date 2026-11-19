package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.School
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.credentials.CredentialManager
import com.example.R
import com.example.data.remote.onGoogleSignInClicked
import com.example.model.AppLanguage
import com.example.model.AppStrings
import com.example.model.BoardType
import com.example.ui.components.GoogleSignInActionButton
import com.example.ui.theme.Navy100
import com.example.ui.theme.Navy800
import com.example.ui.theme.Navy900
import com.example.ui.theme.Saffron100
import com.example.ui.theme.Saffron500
import com.example.ui.theme.Saffron600
import kotlin.math.roundToInt

@Composable
fun OnboardingScreen(
    initialBoard: BoardType,
    initialLanguage: AppLanguage,
    isSignedIn: Boolean,
    onSignedIn: () -> Unit,
    onComplete: (BoardType, AppLanguage, String, Long, Int) -> Unit
) {
    var selectedBoard by remember { mutableStateOf(initialBoard) }
    var selectedLang by remember { mutableStateOf(initialLanguage) }
    var studentName by remember { mutableStateOf("") }
    var examDays by remember { mutableFloatStateOf(90f) }
    var dailyTarget by remember { mutableFloatStateOf(20f) }
    var authLoading by remember { mutableStateOf(false) }
    var authError by remember { mutableStateOf<String?>(null) }

    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val credentialManager = remember(context) { CredentialManager.create(context) }
    val s = AppStrings.get(selectedLang)

    Box(
        contentAlignment = Alignment.TopCenter,
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .statusBarsPadding()
            .navigationBarsPadding()
    ) {
        Column(
            modifier = Modifier
                .widthIn(max = 600.dp)
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 16.dp)
        ) {
            // Hero Banner Card
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(190.dp)
                    .clip(RoundedCornerShape(24.dp))
            ) {
                Image(
                    painter = painterResource(id = R.drawable.img_onboarding_hero),
                    contentDescription = "BoardSaathi Odisha illustration",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.verticalGradient(
                                colors = listOf(Color.Transparent, Navy900.copy(alpha = 0.88f))
                            )
                        )
                )
                Column(
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        .padding(18.dp)
                ) {
                    Surface(
                        shape = RoundedCornerShape(50),
                        color = Saffron500
                    ) {
                        Text(
                            text = "ODISHA BOARD PREP",
                            style = MaterialTheme.typography.labelSmall,
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                        )
                    }
                    Spacer(Modifier.height(6.dp))
                    Text(
                        text = s.welcomeTitle,
                        style = MaterialTheme.typography.headlineSmall,
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = s.welcomeSubtitle,
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.White.copy(alpha = 0.88f)
                    )
                }
            }

            Spacer(Modifier.height(20.dp))

            // Language Picker
            Text(
                text = s.selectLanguage,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onBackground
            )
            Spacer(Modifier.height(8.dp))
            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                AppLanguage.entries.forEach { lang ->
                    val selected = selectedLang == lang
                    Surface(
                        shape = RoundedCornerShape(14.dp),
                        color = if (selected) Saffron100 else MaterialTheme.colorScheme.surface,
                        border = BorderStroke(
                            width = if (selected) 2.dp else 1.dp,
                            color = if (selected) Saffron500 else MaterialTheme.colorScheme.outline
                        ),
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(14.dp))
                            .clickable { selectedLang = lang }
                            .testTag("onboarding_lang_${lang.code}")
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center,
                            modifier = Modifier.padding(vertical = 14.dp)
                        ) {
                            Text(
                                text = lang.nativeLabel,
                                style = MaterialTheme.typography.titleSmall,
                                color = if (selected) Saffron600 else MaterialTheme.colorScheme.onSurface,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }

            Spacer(Modifier.height(20.dp))

            // Board & Class Selector (Strictly Isolated Categories)
            Text(
                text = s.selectBoard,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onBackground
            )
            Spacer(Modifier.height(8.dp))
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                BoardType.entries.forEach { board ->
                    val selected = selectedBoard == board
                    Surface(
                        shape = RoundedCornerShape(16.dp),
                        color = if (selected) Navy100.copy(alpha = 0.6f) else MaterialTheme.colorScheme.surface,
                        border = BorderStroke(
                            width = if (selected) 2.dp else 1.dp,
                            color = if (selected) Navy800 else MaterialTheme.colorScheme.outline
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(16.dp))
                            .clickable { selectedBoard = board }
                            .testTag("onboarding_board_${board.id}")
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(16.dp)
                        ) {
                            Box(
                                contentAlignment = Alignment.Center,
                                modifier = Modifier
                                    .size(44.dp)
                                    .clip(CircleShape)
                                    .background(if (selected) Navy800 else Navy100)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.School,
                                    contentDescription = null,
                                    tint = if (selected) Color.White else Navy800
                                )
                            }
                            Spacer(Modifier.width(14.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = if (selectedLang == AppLanguage.OR) board.titleOr else board.titleEn,
                                    style = MaterialTheme.typography.titleMedium,
                                    color = MaterialTheme.colorScheme.onSurface,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = if (selectedLang == AppLanguage.OR) board.subtitleOr else board.subtitleEn,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = Saffron600,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                            if (selected) {
                                Icon(
                                    imageVector = Icons.Default.CheckCircle,
                                    contentDescription = "Selected",
                                    tint = Navy800
                                )
                            }
                        }
                    }
                }
            }

            Spacer(Modifier.height(20.dp))

            OutlinedTextField(
                value = studentName,
                onValueChange = { if (it.length <= 60) studentName = it },
                label = { Text(s.studentNameOptional) },
                singleLine = true,
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("onboarding_name_input")
            )

            Spacer(Modifier.height(16.dp))

            // Exam Days & Daily Target Sliders
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = MaterialTheme.colorScheme.surface,
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(s.examTargetDaysLabel, style = MaterialTheme.typography.titleSmall)
                        Text(
                            "${examDays.roundToInt()} days",
                            style = MaterialTheme.typography.titleSmall,
                            color = Saffron600
                        )
                    }
                    Slider(
                        value = examDays,
                        onValueChange = { examDays = it },
                        valueRange = 15f..365f,
                        colors = SliderDefaults.colors(thumbColor = Saffron500, activeTrackColor = Saffron500)
                    )
                    Spacer(Modifier.height(8.dp))
                    Row(
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(s.dailyTargetLabel, style = MaterialTheme.typography.titleSmall)
                        Text(
                            "${dailyTarget.roundToInt()} Qs/day",
                            style = MaterialTheme.typography.titleSmall,
                            color = Navy800
                        )
                    }
                    Slider(
                        value = dailyTarget,
                        onValueChange = { dailyTarget = it },
                        valueRange = 10f..100f,
                        colors = SliderDefaults.colors(thumbColor = Navy800, activeTrackColor = Navy800)
                    )
                }
            }

            Spacer(Modifier.height(20.dp))

            if (!isSignedIn) {
                GoogleSignInActionButton(
                    text = s.signInGoogleSync,
                    isLoading = authLoading,
                    onClick = {
                        authLoading = true
                        authError = null
                        onGoogleSignInClicked(
                            context = context,
                            credentialManager = credentialManager,
                            onAuthSuccess = {
                                authLoading = false
                                onSignedIn()
                            },
                            onAuthError = { msg ->
                                authLoading = false
                                authError = msg
                            },
                            scope = scope,
                            onAuthCancelled = { authLoading = false }
                        )
                    }
                )
                if (authError != null) {
                    Spacer(Modifier.height(8.dp))
                    Text(
                        text = authError!!,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.error
                    )
                }
                Spacer(Modifier.height(10.dp))
            }

            Button(
                onClick = {
                    onComplete(
                        selectedBoard,
                        selectedLang,
                        studentName,
                        examDays.roundToInt().toLong(),
                        dailyTarget.roundToInt()
                    )
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = Saffron500,
                    contentColor = Color.White
                ),
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(54.dp)
                    .testTag("onboarding_start_button")
            ) {
                Text(
                    text = if (isSignedIn) s.startPreparing else s.continueGuest,
                    style = MaterialTheme.typography.labelLarge
                )
            }

            Spacer(Modifier.height(24.dp))
        }
    }
}
