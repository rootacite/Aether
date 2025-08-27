package com.acitelight.aether.helper

import com.acitelight.aether.model.Video
import java.text.Collator
import java.util.Locale

fun MutableList<Video>.insertInNaturalOrder(n: Video) {
    // Windows sorting is locale-sensitive. Use the default locale.
    val collator = Collator.getInstance(Locale.getDefault())

    val naturalComparator = Comparator<String> { s1, s2 ->
        val naturalOrderComparator = fun(str1: String, str2: String): Int {
            // Function to compare segments (numeric vs. non-numeric)
            val compareSegments = fun(segment1: String, segment2: String, isNumeric: Boolean): Int {
                return if (isNumeric) {
                    val num1 = segment1.toLongOrNull() ?: 0
                    val num2 = segment2.toLongOrNull() ?: 0
                    num1.compareTo(num2)
                } else {
                    collator.compare(segment1, segment2)
                }
            }

            // Regex to split string into numeric and non-numeric parts
            val regex = "(\\d+)|(\\D+)".toRegex()
            val matches1 = regex.findAll(str1).toList()
            val matches2 = regex.findAll(str2).toList()

            var i = 0
            while (i < matches1.size && i < matches2.size) {
                val match1 = matches1[i]
                val match2 = matches2[i]

                val isNumeric1 = match1.groupValues[1].isNotEmpty()
                val isNumeric2 = match2.groupValues[1].isNotEmpty()

                when {
                    isNumeric1 && isNumeric2 -> {
                        val result = compareSegments(match1.value, match2.value, true)
                        if (result != 0) return result
                    }
                    !isNumeric1 && !isNumeric2 -> {
                        val result = compareSegments(match1.value, match2.value, false)
                        if (result != 0) return result
                    }
                    isNumeric1 -> return -1 // Numeric part comes before non-numeric
                    isNumeric2 -> return 1
                }
                i++
            }

            // If one string is a prefix of the other, the shorter one comes first
            return str1.length.compareTo(str2.length)
        }

        naturalOrderComparator(s1, s2)
    }

    var inserted = false

    // Find the correct insertion point
    for (i in this.indices) {
        if (naturalComparator.compare(n.video.name, this[i].video.name) <= 0) {
            this.add(i, n)
            inserted = true
            break
        }
    }

    // If it's the largest, add to the end
    if (!inserted) {
        this.add(n)
    }
}