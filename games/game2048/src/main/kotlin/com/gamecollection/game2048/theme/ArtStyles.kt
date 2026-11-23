package com.gamecollection.game2048.theme

import com.gamecollection.game2048.theme.styles.CyberpunkStyle
import com.gamecollection.game2048.theme.styles.DunhuangStyle
import com.gamecollection.game2048.theme.styles.InkWashStyle
import com.gamecollection.game2048.theme.styles.MonetStyle
import com.gamecollection.game2048.theme.styles.PixelStyle
import com.gamecollection.game2048.theme.styles.StarryNightStyle
import com.gamecollection.game2048.theme.styles.UkiyoEStyle
import com.gamecollection.game2048.theme.styles.WatercolorStyle

object ArtStyles {
    const val DEFAULT_ID: String = "ink"

    val previewValues: List<Int> = listOf(2, 32, 128, 2048)

    val all: List<ArtStyle> = listOf(
        InkWashStyle,
        WatercolorStyle,
        StarryNightStyle,
        MonetStyle,
        CyberpunkStyle,
        UkiyoEStyle,
        PixelStyle,
        DunhuangStyle,
    )

    fun byId(id: String?): ArtStyle = all.firstOrNull { it.id == id } ?: all.first()
}
