package ru.skillbranch.skillarticles.extensions

fun List<Pair<Int, Int>>.groupByBounds(bounds: List<Pair<Int, Int>>): List<List<Pair<Int, Int>>> =
    bounds.map { boundary ->
        this.filter { it.second > boundary.first && it.first < boundary.second }
            .map {
                when {
                    it.first < boundary.first -> Pair(boundary.first, it.second)
                    it.second > boundary.second -> Pair(it.first, boundary.second)
                    else -> it
                }
            }
    }