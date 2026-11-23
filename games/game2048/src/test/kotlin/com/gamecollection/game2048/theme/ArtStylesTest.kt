package com.gamecollection.game2048.theme

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ArtStylesTest {
    @Test
    fun registry_listsEightDistinctStyles() {
        assertEquals(8, ArtStyles.all.size)
        assertEquals(ArtStyles.all.size, ArtStyles.all.map { it.id }.distinct().size)
        assertTrue(ArtStyles.all.any { it.id == ArtStyles.DEFAULT_ID })
        ArtStyles.all.forEach { style ->
            assertTrue(style.name.isNotBlank())
            assertTrue(style.blurb.isNotBlank())
        }
    }
}
