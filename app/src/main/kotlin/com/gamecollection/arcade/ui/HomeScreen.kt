package com.gamecollection.arcade.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.gamecollection.arcade.R
import com.gamecollection.game2048.theme.ArtStyle
import com.gamecollection.game2048.ui.ThemePreview

@Composable
fun HomeScreen(
    style: ArtStyle,
    onPlay: () -> Unit,
) {
    val openLabel = stringResource(R.string.cd_open_2048)
    Column(
        Modifier
            .fillMaxSize()
            .windowInsetsPadding(WindowInsets.safeDrawing)
            .padding(horizontal = 20.dp, vertical = 18.dp),
    ) {
        Text(
            text = stringResource(R.string.home_title),
            style = MaterialTheme.typography.headlineLarge,
            color = MaterialTheme.colorScheme.onBackground,
        )
        Spacer(Modifier.size(8.dp))
        Text(
            text = stringResource(R.string.home_subtitle),
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Spacer(Modifier.size(28.dp))
        Text(
            text = stringResource(R.string.home_section),
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onBackground,
        )
        Spacer(Modifier.size(12.dp))
        Card(
            onClick = onPlay,
            modifier = Modifier
                .fillMaxWidth()
                .semantics { contentDescription = openLabel },
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        ) {
            Row(
                Modifier.padding(14.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                ThemePreview(style, Modifier.size(92.dp))
                Spacer(Modifier.width(14.dp))
                Column(Modifier.weight(1f)) {
                    Text(
                        text = stringResource(R.string.game_2048),
                        style = MaterialTheme.typography.titleLarge,
                    )
                    Spacer(Modifier.size(4.dp))
                    Text(
                        text = stringResource(R.string.game_2048_tagline),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    Spacer(Modifier.size(6.dp))
                    Text(
                        text = stringResource(R.string.current_theme, style.name),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.primary,
                    )
                }
            }
        }
        Spacer(Modifier.weight(1f))
        Text(
            text = stringResource(R.string.more_games),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}
