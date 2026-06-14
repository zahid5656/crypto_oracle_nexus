package com.example.`data`

import androidx.room.EntityDeleteOrUpdateAdapter
import androidx.room.EntityInsertAdapter
import androidx.room.RoomDatabase
import androidx.room.coroutines.createFlow
import androidx.room.util.getColumnIndexOrThrow
import androidx.room.util.performSuspending
import androidx.sqlite.SQLiteStatement
import javax.`annotation`.processing.Generated
import kotlin.Boolean
import kotlin.Double
import kotlin.Int
import kotlin.Long
import kotlin.String
import kotlin.Suppress
import kotlin.Unit
import kotlin.collections.List
import kotlin.collections.MutableList
import kotlin.collections.mutableListOf
import kotlin.reflect.KClass
import kotlinx.coroutines.flow.Flow

@Generated(value = ["androidx.room.RoomProcessor"])
@Suppress(names = ["UNCHECKED_CAST", "DEPRECATION", "REDUNDANT_PROJECTION", "REMOVAL"])
public class SignalDao_Impl(
  __db: RoomDatabase,
) : SignalDao {
  private val __db: RoomDatabase

  private val __insertAdapterOfSignalEntity: EntityInsertAdapter<SignalEntity>

  private val __updateAdapterOfSignalEntity: EntityDeleteOrUpdateAdapter<SignalEntity>
  init {
    this.__db = __db
    this.__insertAdapterOfSignalEntity = object : EntityInsertAdapter<SignalEntity>() {
      protected override fun createQuery(): String = "INSERT OR REPLACE INTO `crypto_signals` (`id`,`coinName`,`coinSymbol`,`signalType`,`entryPrice`,`currentPrice`,`targetPrice`,`priceChangePct`,`probabilityPct`,`timeframe`,`result`,`isLong`,`marketRegime`,`timestamp`) VALUES (nullif(?, 0),?,?,?,?,?,?,?,?,?,?,?,?,?)"

      protected override fun bind(statement: SQLiteStatement, entity: SignalEntity) {
        statement.bindLong(1, entity.id.toLong())
        statement.bindText(2, entity.coinName)
        statement.bindText(3, entity.coinSymbol)
        statement.bindText(4, entity.signalType)
        statement.bindDouble(5, entity.entryPrice)
        statement.bindDouble(6, entity.currentPrice)
        statement.bindDouble(7, entity.targetPrice)
        statement.bindDouble(8, entity.priceChangePct)
        statement.bindLong(9, entity.probabilityPct.toLong())
        statement.bindText(10, entity.timeframe)
        statement.bindText(11, entity.result)
        val _tmp: Int = if (entity.isLong) 1 else 0
        statement.bindLong(12, _tmp.toLong())
        statement.bindText(13, entity.marketRegime)
        statement.bindLong(14, entity.timestamp)
      }
    }
    this.__updateAdapterOfSignalEntity = object : EntityDeleteOrUpdateAdapter<SignalEntity>() {
      protected override fun createQuery(): String = "UPDATE OR ABORT `crypto_signals` SET `id` = ?,`coinName` = ?,`coinSymbol` = ?,`signalType` = ?,`entryPrice` = ?,`currentPrice` = ?,`targetPrice` = ?,`priceChangePct` = ?,`probabilityPct` = ?,`timeframe` = ?,`result` = ?,`isLong` = ?,`marketRegime` = ?,`timestamp` = ? WHERE `id` = ?"

      protected override fun bind(statement: SQLiteStatement, entity: SignalEntity) {
        statement.bindLong(1, entity.id.toLong())
        statement.bindText(2, entity.coinName)
        statement.bindText(3, entity.coinSymbol)
        statement.bindText(4, entity.signalType)
        statement.bindDouble(5, entity.entryPrice)
        statement.bindDouble(6, entity.currentPrice)
        statement.bindDouble(7, entity.targetPrice)
        statement.bindDouble(8, entity.priceChangePct)
        statement.bindLong(9, entity.probabilityPct.toLong())
        statement.bindText(10, entity.timeframe)
        statement.bindText(11, entity.result)
        val _tmp: Int = if (entity.isLong) 1 else 0
        statement.bindLong(12, _tmp.toLong())
        statement.bindText(13, entity.marketRegime)
        statement.bindLong(14, entity.timestamp)
        statement.bindLong(15, entity.id.toLong())
      }
    }
  }

  public override suspend fun insertSignal(signal: SignalEntity): Unit = performSuspending(__db, false, true) { _connection ->
    __insertAdapterOfSignalEntity.insert(_connection, signal)
  }

  public override suspend fun insertSignals(signals: List<SignalEntity>): Unit = performSuspending(__db, false, true) { _connection ->
    __insertAdapterOfSignalEntity.insert(_connection, signals)
  }

  public override suspend fun updateSignal(signal: SignalEntity): Unit = performSuspending(__db, false, true) { _connection ->
    __updateAdapterOfSignalEntity.handle(_connection, signal)
  }

  public override fun getAllSignals(): Flow<List<SignalEntity>> {
    val _sql: String = "SELECT * FROM crypto_signals ORDER BY timestamp DESC"
    return createFlow(__db, false, arrayOf("crypto_signals")) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        val _columnIndexOfId: Int = getColumnIndexOrThrow(_stmt, "id")
        val _columnIndexOfCoinName: Int = getColumnIndexOrThrow(_stmt, "coinName")
        val _columnIndexOfCoinSymbol: Int = getColumnIndexOrThrow(_stmt, "coinSymbol")
        val _columnIndexOfSignalType: Int = getColumnIndexOrThrow(_stmt, "signalType")
        val _columnIndexOfEntryPrice: Int = getColumnIndexOrThrow(_stmt, "entryPrice")
        val _columnIndexOfCurrentPrice: Int = getColumnIndexOrThrow(_stmt, "currentPrice")
        val _columnIndexOfTargetPrice: Int = getColumnIndexOrThrow(_stmt, "targetPrice")
        val _columnIndexOfPriceChangePct: Int = getColumnIndexOrThrow(_stmt, "priceChangePct")
        val _columnIndexOfProbabilityPct: Int = getColumnIndexOrThrow(_stmt, "probabilityPct")
        val _columnIndexOfTimeframe: Int = getColumnIndexOrThrow(_stmt, "timeframe")
        val _columnIndexOfResult: Int = getColumnIndexOrThrow(_stmt, "result")
        val _columnIndexOfIsLong: Int = getColumnIndexOrThrow(_stmt, "isLong")
        val _columnIndexOfMarketRegime: Int = getColumnIndexOrThrow(_stmt, "marketRegime")
        val _columnIndexOfTimestamp: Int = getColumnIndexOrThrow(_stmt, "timestamp")
        val _result: MutableList<SignalEntity> = mutableListOf()
        while (_stmt.step()) {
          val _item: SignalEntity
          val _tmpId: Int
          _tmpId = _stmt.getLong(_columnIndexOfId).toInt()
          val _tmpCoinName: String
          _tmpCoinName = _stmt.getText(_columnIndexOfCoinName)
          val _tmpCoinSymbol: String
          _tmpCoinSymbol = _stmt.getText(_columnIndexOfCoinSymbol)
          val _tmpSignalType: String
          _tmpSignalType = _stmt.getText(_columnIndexOfSignalType)
          val _tmpEntryPrice: Double
          _tmpEntryPrice = _stmt.getDouble(_columnIndexOfEntryPrice)
          val _tmpCurrentPrice: Double
          _tmpCurrentPrice = _stmt.getDouble(_columnIndexOfCurrentPrice)
          val _tmpTargetPrice: Double
          _tmpTargetPrice = _stmt.getDouble(_columnIndexOfTargetPrice)
          val _tmpPriceChangePct: Double
          _tmpPriceChangePct = _stmt.getDouble(_columnIndexOfPriceChangePct)
          val _tmpProbabilityPct: Int
          _tmpProbabilityPct = _stmt.getLong(_columnIndexOfProbabilityPct).toInt()
          val _tmpTimeframe: String
          _tmpTimeframe = _stmt.getText(_columnIndexOfTimeframe)
          val _tmpResult: String
          _tmpResult = _stmt.getText(_columnIndexOfResult)
          val _tmpIsLong: Boolean
          val _tmp: Int
          _tmp = _stmt.getLong(_columnIndexOfIsLong).toInt()
          _tmpIsLong = _tmp != 0
          val _tmpMarketRegime: String
          _tmpMarketRegime = _stmt.getText(_columnIndexOfMarketRegime)
          val _tmpTimestamp: Long
          _tmpTimestamp = _stmt.getLong(_columnIndexOfTimestamp)
          _item = SignalEntity(_tmpId,_tmpCoinName,_tmpCoinSymbol,_tmpSignalType,_tmpEntryPrice,_tmpCurrentPrice,_tmpTargetPrice,_tmpPriceChangePct,_tmpProbabilityPct,_tmpTimeframe,_tmpResult,_tmpIsLong,_tmpMarketRegime,_tmpTimestamp)
          _result.add(_item)
        }
        _result
      } finally {
        _stmt.close()
      }
    }
  }

  public override suspend fun getCount(): Int {
    val _sql: String = "SELECT COUNT(*) FROM crypto_signals"
    return performSuspending(__db, true, false) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        val _result: Int
        if (_stmt.step()) {
          val _tmp: Int
          _tmp = _stmt.getLong(0).toInt()
          _result = _tmp
        } else {
          _result = 0
        }
        _result
      } finally {
        _stmt.close()
      }
    }
  }

  public override suspend fun clearHistory() {
    val _sql: String = "DELETE FROM crypto_signals"
    return performSuspending(__db, false, true) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        _stmt.step()
      } finally {
        _stmt.close()
      }
    }
  }

  public companion object {
    public fun getRequiredConverters(): List<KClass<*>> = emptyList()
  }
}
