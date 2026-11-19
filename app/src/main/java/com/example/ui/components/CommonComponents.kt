package com.example.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CloudDone
import androidx.compose.material.icons.filled.CloudOff
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Translate
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.model.AppLanguage
import com.example.model.ContentLabel
import com.example.model.Difficulty
import com.example.ui.theme.Amber100
import com.example.ui.theme.Amber600
import com.example.ui.theme.Crimson100
import com.example.ui.theme.Crimson600
import com.example.ui.theme.Emerald100
import com.example.ui.theme.Emerald600
import com.example.ui.theme.Indigo100
import com.example.ui.theme.Indigo600
import com.example.ui.theme.Navy100
import com.example.ui.theme.Navy800
import com.example.ui.theme.Saffron100
import com.example.ui.theme.Saffron500
import com.example.ui.theme.Saffron600

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BoardSaathiTopBar(
    title: String,
    language: AppLanguage,
    onToggleLanguage: () -> Unit,
    isSignedIn: Boolean = false,
    onBack: (() -> Unit)? = null
) {
    TopAppBar(
        title = {
            Text(
                text = title,
                style = MaterialTheme.typography.titleLarge,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        },
        navigationIcon = {
            if (onBack != null) {
                IconButton(
                    onClick = onBack,
                    modifier = Modifier.testTag("top_bar_back_button")
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back"
                    )
                }
            }
        },
        actions = {
            Surface(
                shape = CircleShape,
                color = if (isSignedIn) Emerald100 else Navy100,
                modifier = Modifier.padding(end = 6.dp)
            ) {
                Icon(
                    imageVector = if (isSignedIn) Icons.Default.CloudDone else Icons.Default.CloudOff,
                    contentDescription = if (isSignedIn) "Cloud synced" else "Offline guest mode",
                    tint = if (isSignedIn) Emerald600 else Navy800,
                    modifier = Modifier
                        .padding(6.dp)
                        .size(16.dp)
                )
            }
            Surface(
                shape = RoundedCornerShape(50),
                color = Saffron100,
                border = BorderStroke(1.dp, Saffron500.copy(alpha = 0.4f)),
                modifier = Modifier
                    .padding(end = 12.dp)
                    .clip(RoundedCornerShape(50))
                    .clickable(onClick = onToggleLanguage)
                    .testTag("language_toggle_pill")
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Translate,
                        contentDescription = "Switch language",
                        tint = Saffron600,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(Modifier.width(4.dp))
                    Text(
                        text = if (language == AppLanguage.EN) "ଓଡ଼ିଆ" else "EN",
                        style = MaterialTheme.typography.labelMedium,
                        color = Saffron600,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = MaterialTheme.colorScheme.surface,
            titleContentColor = MaterialTheme.colorScheme.onSurface
        )
    )
}

@Composable
fun StatusBadge(
    text: String,
    containerColor: Color,
    contentColor: Color,
    modifier: Modifier = Modifier
) {
    Surface(
        shape = RoundedCornerShape(50),
        color = containerColor,
        modifier = modifier
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.labelSmall,
            color = contentColor,
            fontWeight = FontWeight.SemiBold,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
        )
    }
}

@Composable
fun ContentLabelBadge(labelRaw: String, language: AppLanguage) {
    val label = ContentLabel.fromRaw(labelRaw)
    val (bg, fg) = when (label) {
        ContentLabel.VERIFIED_PYQ -> Emerald100 to Emerald600
        ContentLabel.ADMIN_APPROVED -> Navy100 to Navy800
        ContentLabel.PRACTICE_QUESTION -> Saffron100 to Saffron600
        ContentLabel.AI_PRACTICE -> Indigo100 to Indigo600
    }
    StatusBadge(
        text = if (language == AppLanguage.OR) label.labelOr else label.labelEn,
        containerColor = bg,
        contentColor = fg
    )
}

@Composable
fun DifficultyBadge(diffRaw: String, language: AppLanguage) {
    val diff = Difficulty.fromRaw(diffRaw)
    val (bg, fg) = when (diff) {
        Difficulty.EASY -> Emerald100 to Emerald600
        Difficulty.MEDIUM -> Amber100 to Amber600
        Difficulty.HARD -> Crimson100 to Crimson600
    }
    StatusBadge(
        text = if (language == AppLanguage.OR) diff.labelOr else diff.labelEn,
        containerColor = bg,
        contentColor = fg
    )
}

@Composable
fun AcademicDisclaimerBanner(text: String, modifier: Modifier = Modifier) {
    Surface(
        shape = RoundedCornerShape(14.dp),
        color = Saffron100.copy(alpha = 0.65f),
        border = BorderStroke(1.dp, Saffron500.copy(alpha = 0.35f)),
        modifier = modifier.fillMaxWidth()
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(12.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Info,
                contentDescription = null,
                tint = Saffron600,
                modifier = Modifier.size(18.dp)
            )
            Spacer(Modifier.width(10.dp))
            Text(
                text = text,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}

@Composable
fun EmptyStateCard(
    icon: ImageVector,
    message: String,
    modifier: Modifier = Modifier
) {
    Surface(
        shape = RoundedCornerShape(18.dp),
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
        modifier = modifier.fillMaxWidth()
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(28.dp)
        ) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .size(56.dp)
                    .clip(CircleShape)
                    .background(Navy100)
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = Navy800,
                    modifier = Modifier.size(28.dp)
                )
            }
            Spacer(Modifier.height(12.dp))
            Text(
                text = message,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
fun GoogleSignInActionButton(
    text: String,
    isLoading: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Button(
        onClick = onClick,
        enabled = !isLoading,
        colors = ButtonDefaults.buttonColors(
            containerColor = Navy800,
            contentColor = Color.White
        ),
        shape = RoundedCornerShape(14.dp),
        modifier = modifier
            .fillMaxWidth()
            .height(52.dp)
            .testTag("google_sign_in_button")
    ) {
        if (isLoading) {
            CircularProgressIndicator(
                modifier = Modifier.size(20.dp),
                color = Color.White,
                strokeWidth = 2.dp
            )
        } else {
            Text(
                text = text,
                style = MaterialTheme.typography.labelLarge
            )
        }
    }
}
