package com.gamecollection.engine2048

fun interface IntSource {
    /** Returns an integer in `0 until until`. */
    fun nextInt(until: Int): Int
}

class RandomIntSource(
    private val random: kotlin.random.Random = kotlin.random.Random.Default,
) : IntSource {
    override fun nextInt(until: Int): Int = random.nextInt(until)
}
