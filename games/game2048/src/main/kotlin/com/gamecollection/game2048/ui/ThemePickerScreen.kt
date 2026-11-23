package com.gamecollection.game2048.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.gamecollection.game2048.R
import com.gamecollection.game2048.theme.ArtStyle
import com.gamecollection.game2048.theme.ArtStyles

@Composable
fun ThemePickerScreen(
    selectedId: String,
    onSelect: (String) -> Unit,
    onBack: () -> Unit,
) {
    Column(
        Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .windowInsetsPadding(WindowInsets.safeDrawing),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(horizontal = 8.dp)) {
            IconButton(onClick = onBack) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = stringResource(R.string.cd_back),
                )
            }
            Column(Modifier.padding(end = 16.dp)) {
                Text(
                    text = stringResource(R.string.theme_picker_title),
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                )
                Text(
                    text = stringResource(R.string.theme_picker_subtitle),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
        LazyVerticalGrid(
            columns = GridCells.Adaptive(156.dp),
            contentPadding = PaddingValues(16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.fillMaxSize(),
        ) {
            items(ArtStyles.all, key = { it.id }) { style ->
                ThemeCard(
                    style = style,
                    selected = style.id == selectedId,
                    onClick = { onSelect(style.id) },
                )
            }
        }
    }
}

@Composable
private fun ThemeCard(
    style: ArtStyle,
    selected: Boolean,
    onClick: () -> Unit,
) {
    val shape = RoundedCornerShape(16.dp)
    val borderColor = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline.copy(alpha = 0.4f)
    Column(
        Modifier
            .fillMaxWidth()
            .clip(shape)
            .border(if (selected) 2.dp else 1.dp, borderColor, shape)
            .background(MaterialTheme.colorScheme.surface)
            .clickable(onClick = onClick)
            .padding(10.dp),
    ) {
        Box {
            ThemePreview(style, Modifier.fillMaxWidth().height(132.dp))
            if (selected) {
                Text(
                    text = stringResource(R.string.theme_in_use),
                    color = MaterialTheme.colorScheme.onPrimary,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(6.dp)
                        .clip(RoundedCornerShape(999.dp))
                        .background(MaterialTheme.colorScheme.primary)
                        .padding(horizontal = 8.dp, vertical = 3.dp),
                )
            }
        }
        Spacer(Modifier.size(8.dp))
        Text(style.name, fontWeight = FontWeight.Bold, fontSize = 16.sp)
        Text(
            text = style.blurb,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            fontSize = 12.sp,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
        )
    }
}
