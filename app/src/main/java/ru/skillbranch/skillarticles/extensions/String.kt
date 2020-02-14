package ru.skillbranch.skillarticles.extensions

fun String?.indexesOf(substring: String, ignoreCase: Boolean = true): List<Int> {
    val result = mutableListOf<Int>()
    if (!this.isNullOrEmpty() && substring.isNotEmpty()) {
        var index = 0
        while (index > -1) {
            index = indexOf(substring, index, ignoreCase)
            if (index > -1) {
                result.add(index)
                index += substring.length
            }
        }
    }
    return result
}