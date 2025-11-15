package com.example.equa_notepad_plats

import kotlinx.serialization.Serializable

@Serializable
object LoginRoute

@Serializable
object HomeRoute

@Serializable
data class BookRoute(
    val bookId: Int
)

@Serializable
data class PracticeRoute(
    val bookId: Int
)

@Serializable
data class FormulaDetailRoute(
    val formulaId: Int,
    val bookId: Int
)

@Serializable
object NewFormulaRoute

@Serializable
object ProfileRoute

@Serializable
object ExerciseGeneratorRoute

@Serializable
object SyncRoute