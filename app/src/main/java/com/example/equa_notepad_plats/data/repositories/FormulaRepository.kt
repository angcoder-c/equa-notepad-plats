package com.example.equa_notepad_plats.data.repositories

import com.example.equa_notepad_plats.data.local.AppDatabase
import com.example.equa_notepad_plats.data.local.entities.FormulaEntity
import kotlinx.coroutines.flow.Flow

class FormulaRepository(
    private val database: AppDatabase
) {
    fun getFormulasByBookId(bookId: Int): Flow<List<FormulaEntity>> = database.formulaDao().getFormulasByBookId(bookId)

    suspend fun getFormulaById(formulaId: Int): FormulaEntity? = database.formulaDao().getFormulaById(formulaId)

    suspend fun insertFormula(formula: FormulaEntity): Long = database.formulaDao().insertFormula(formula)

    suspend fun updateFormula(formula: FormulaEntity) = database.formulaDao().updateFormula(formula)

    suspend fun deleteFormula(formula: FormulaEntity) = database.formulaDao().deleteFormula(formula)
}