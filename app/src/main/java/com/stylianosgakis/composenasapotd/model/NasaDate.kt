package com.stylianosgakis.composenasapotd.model

import java.time.LocalDate

class NasaDate private constructor(
    private val dateString: String
) {
    /**
     * @return Formatted string understood by the NASA API
     */
    fun formattedString(): String = dateString

    companion object {
        fun fromLocalDate(date: LocalDate): NasaDate {
            return NasaDate(date.toString())
        }
    }
}