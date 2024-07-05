package org.intelehealth.helpline.ui2.validations

import android.text.InputFilter
import android.text.Spanned


class UpperCaseAlphabetsInputFilter(private val maxLength: Int = Int.MAX_VALUE) : InputFilter {

    private val regex = Regex("^[a-zA-Z ]*$")
    private val lengthFilter = InputFilter.LengthFilter(maxLength)

    override fun filter(
            source: CharSequence,
            start: Int,
            end: Int,
            dest: Spanned,
            dstart: Int,
            dend: Int
    ): CharSequence? {
        // Apply length filter
        val maxLengthFiltered = lengthFilter.filter(source, start, end, dest, dstart, dend)

        // Check if filtered input exceeds max length
        if (maxLengthFiltered != null && maxLengthFiltered.length > maxLength) {
            return ""
        }

        // Filter to keep only valid characters
        val input = maxLengthFiltered ?: source.subSequence(start, end).toString()
        val filtered = input.filter { regex.matches(it.toString()) }

        if (dstart == 0 && filtered.isNotEmpty()) {
            // Capitalize the first character if it's at the beginning of the text
            val firstChar = filtered[0].uppercase()
            val restOfInput = if (filtered.length > 1) filtered.substring(1) else ""
            return firstChar + restOfInput
        }

        // If the filtered text differs from the original, return it
        return if (filtered != input) filtered else null
    }
}
/*
class UpperCaseAlphabetsInputFilter : InputFilter {
    private val regex = Regex("^[a-zA-Z ]*$")

    override fun filter(
            source: CharSequence,
            start: Int,
            end: Int,
            dest: Spanned,
            dstart: Int,
            dend: Int
    ): CharSequence? {
        val input = source.subSequence(start, end).toString()

        // Filter to keep only valid characters
        val filtered = input.filter { regex.matches(it.toString()) }

        if (dstart == 0 && filtered.isNotEmpty()) {
            // Capitalize the first character if it's at the beginning of the text
            val firstChar = filtered[0].uppercase()
            val restOfInput = if (filtered.length > 1) filtered.substring(1) else ""
            return firstChar + restOfInput
        }

        // If the filtered text differs from the original, return it
        return if (filtered != input) filtered else null
    }
}*/
