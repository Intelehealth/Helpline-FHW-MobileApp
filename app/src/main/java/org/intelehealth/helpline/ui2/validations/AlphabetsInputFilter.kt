package org.intelehealth.helpline.ui2.validations

import android.text.InputFilter
import android.text.Spanned

class AlphabetsInputFilter(private val maxLength: Int? = null, private val isFirstLetterCapital: Boolean = false) : InputFilter {
    private val regex = Regex("^[a-zA-Z0-9\\p{Punct} ]*$")

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
        var filtered = input.filter { regex.matches(it.toString()) }

        // Convert to uppercase if needed
        if (isFirstLetterCapital && filtered.isNotEmpty()) {
            filtered = filtered.substring(0, 1).toUpperCase() + filtered.substring(1)
        }

        // Apply max length filter
        maxLength?.let {
            if (dest.length + filtered.length - (dend - dstart) > it) {
                val trimmed = filtered.substring(0, it - dest.length + (dend - dstart))
                if (trimmed != input)
                    return trimmed
            }
        }

        // If the filtered text differs from the original, return it
        return if (filtered != input) filtered else null
    }
}

/*
class AlphabetsInputFilter : InputFilter {
    private val regex = Regex("^[a-zA-Z0-9\\p{Punct} ]*$")

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

        // If the filtered text differs from the original, return it
        return if (filtered != input) filtered else null
    }
}*/
