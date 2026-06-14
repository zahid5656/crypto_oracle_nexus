package com.example.core.database

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Entity(tableName = "crypto_signals")
data class SignalEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val coinName: String,
    val coinSymbol: String,
    val signalType: String, // "SPOT", "FUTURES_LONG", "FUTURES_SHORT"
    val entryPrice: Double,
    val currentPrice: Double,
    val targetPrice: Double,
    val priceChangePct: Double,
    val probabilityPct: Int,
    val timeframe: String, // "6h", "12h", "1m", "5m", "15m", "30m"
    val result: String, // "WIN", "LOSS", "PENDING"
    val isLong: Boolean, // true for long, false for short (or spot)
    val marketRegime: String = "BULLISH",
    val timestamp: Long = System.currentTimeMillis()
)

@Dao
interface SignalDao {
    @Query("SELECT * FROM crypto_signals ORDER BY timestamp DESC")
    fun getAllSignals(): Flow<List<SignalEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSignal(signal: SignalEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSignals(signals: List<SignalEntity>)

    @Query("DELETE FROM crypto_signals")
    suspend fun clearHistory()

    @Update
    suspend fun updateSignal(signal: SignalEntity)

    @Query("SELECT COUNT(*) FROM crypto_signals")
    suspend fun getCount(): Int
}
