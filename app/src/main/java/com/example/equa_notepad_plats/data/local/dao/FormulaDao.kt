package com.example.equa_notepad_plats.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.equa_notepad_plats.data.local.entities.FormulaEntity

@Dao
interface FormulaDao {
    @Query("SELECT * FROM formulas WHERE bookId = :bookId ORDER BY createdAt DESC")
    fun getFormulasByBookId(bookId: Int): kotlinx.coroutines.flow.Flow<List<FormulaEntity>>

    @Query("SELECT * FROM formulas WHERE id = :formulaId")
    suspend fun getFormulaById(formulaId: Int): FormulaEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFormula(formula: FormulaEntity): Long

    @Update
    suspend fun updateFormula(formula: FormulaEntity)

    @Delete
    suspend fun deleteFormula(formula: FormulaEntity)

    @Query("DELETE FROM formulas WHERE bookId = :bookId")
    suspend fun deleteFormulasByBookId(bookId: Int)
}