package com.example.`data`

import androidx.room.InvalidationTracker
import androidx.room.RoomOpenDelegate
import androidx.room.migration.AutoMigrationSpec
import androidx.room.migration.Migration
import androidx.room.util.TableInfo
import androidx.room.util.TableInfo.Companion.read
import androidx.room.util.dropFtsSyncTriggers
import androidx.sqlite.SQLiteConnection
import androidx.sqlite.execSQL
import javax.`annotation`.processing.Generated
import kotlin.Lazy
import kotlin.String
import kotlin.Suppress
import kotlin.collections.List
import kotlin.collections.Map
import kotlin.collections.MutableList
import kotlin.collections.MutableMap
import kotlin.collections.MutableSet
import kotlin.collections.Set
import kotlin.collections.mutableListOf
import kotlin.collections.mutableMapOf
import kotlin.collections.mutableSetOf
import kotlin.reflect.KClass

@Generated(value = ["androidx.room.RoomProcessor"])
@Suppress(names = ["UNCHECKED_CAST", "DEPRECATION", "REDUNDANT_PROJECTION", "REMOVAL"])
public class AppDatabase_Impl : AppDatabase() {
  private val _signalDao: Lazy<SignalDao> = lazy {
    SignalDao_Impl(this)
  }

  protected override fun createOpenDelegate(): RoomOpenDelegate {
    val _openDelegate: RoomOpenDelegate = object : RoomOpenDelegate(2, "c27893e1e15d86d2fab91535fbfcc9a8", "40c11b5a0230e20058bebf01209785c7") {
      public override fun createAllTables(connection: SQLiteConnection) {
        connection.execSQL("CREATE TABLE IF NOT EXISTS `crypto_signals` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `coinName` TEXT NOT NULL, `coinSymbol` TEXT NOT NULL, `signalType` TEXT NOT NULL, `entryPrice` REAL NOT NULL, `currentPrice` REAL NOT NULL, `targetPrice` REAL NOT NULL, `priceChangePct` REAL NOT NULL, `probabilityPct` INTEGER NOT NULL, `timeframe` TEXT NOT NULL, `result` TEXT NOT NULL, `isLong` INTEGER NOT NULL, `marketRegime` TEXT NOT NULL, `timestamp` INTEGER NOT NULL)")
        connection.execSQL("CREATE TABLE IF NOT EXISTS room_master_table (id INTEGER PRIMARY KEY,identity_hash TEXT)")
        connection.execSQL("INSERT OR REPLACE INTO room_master_table (id,identity_hash) VALUES(42, 'c27893e1e15d86d2fab91535fbfcc9a8')")
      }

      public override fun dropAllTables(connection: SQLiteConnection) {
        connection.execSQL("DROP TABLE IF EXISTS `crypto_signals`")
      }

      public override fun onCreate(connection: SQLiteConnection) {
      }

      public override fun onOpen(connection: SQLiteConnection) {
        internalInitInvalidationTracker(connection)
      }

      public override fun onPreMigrate(connection: SQLiteConnection) {
        dropFtsSyncTriggers(connection)
      }

      public override fun onPostMigrate(connection: SQLiteConnection) {
      }

      public override fun onValidateSchema(connection: SQLiteConnection): RoomOpenDelegate.ValidationResult {
        val _columnsCryptoSignals: MutableMap<String, TableInfo.Column> = mutableMapOf()
        _columnsCryptoSignals.put("id", TableInfo.Column("id", "INTEGER", true, 1, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsCryptoSignals.put("coinName", TableInfo.Column("coinName", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsCryptoSignals.put("coinSymbol", TableInfo.Column("coinSymbol", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsCryptoSignals.put("signalType", TableInfo.Column("signalType", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsCryptoSignals.put("entryPrice", TableInfo.Column("entryPrice", "REAL", true, 0, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsCryptoSignals.put("currentPrice", TableInfo.Column("currentPrice", "REAL", true, 0, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsCryptoSignals.put("targetPrice", TableInfo.Column("targetPrice", "REAL", true, 0, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsCryptoSignals.put("priceChangePct", TableInfo.Column("priceChangePct", "REAL", true, 0, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsCryptoSignals.put("probabilityPct", TableInfo.Column("probabilityPct", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsCryptoSignals.put("timeframe", TableInfo.Column("timeframe", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsCryptoSignals.put("result", TableInfo.Column("result", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsCryptoSignals.put("isLong", TableInfo.Column("isLong", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsCryptoSignals.put("marketRegime", TableInfo.Column("marketRegime", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsCryptoSignals.put("timestamp", TableInfo.Column("timestamp", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY))
        val _foreignKeysCryptoSignals: MutableSet<TableInfo.ForeignKey> = mutableSetOf()
        val _indicesCryptoSignals: MutableSet<TableInfo.Index> = mutableSetOf()
        val _infoCryptoSignals: TableInfo = TableInfo("crypto_signals", _columnsCryptoSignals, _foreignKeysCryptoSignals, _indicesCryptoSignals)
        val _existingCryptoSignals: TableInfo = read(connection, "crypto_signals")
        if (!_infoCryptoSignals.equals(_existingCryptoSignals)) {
          return RoomOpenDelegate.ValidationResult(false, """
              |crypto_signals(com.example.data.SignalEntity).
              | Expected:
              |""".trimMargin() + _infoCryptoSignals + """
              |
              | Found:
              |""".trimMargin() + _existingCryptoSignals)
        }
        return RoomOpenDelegate.ValidationResult(true, null)
      }
    }
    return _openDelegate
  }

  protected override fun createInvalidationTracker(): InvalidationTracker {
    val _shadowTablesMap: MutableMap<String, String> = mutableMapOf()
    val _viewTables: MutableMap<String, Set<String>> = mutableMapOf()
    return InvalidationTracker(this, _shadowTablesMap, _viewTables, "crypto_signals")
  }

  public override fun clearAllTables() {
    super.performClear(false, "crypto_signals")
  }

  protected override fun getRequiredTypeConverterClasses(): Map<KClass<*>, List<KClass<*>>> {
    val _typeConvertersMap: MutableMap<KClass<*>, List<KClass<*>>> = mutableMapOf()
    _typeConvertersMap.put(SignalDao::class, SignalDao_Impl.getRequiredConverters())
    return _typeConvertersMap
  }

  public override fun getRequiredAutoMigrationSpecClasses(): Set<KClass<out AutoMigrationSpec>> {
    val _autoMigrationSpecsSet: MutableSet<KClass<out AutoMigrationSpec>> = mutableSetOf()
    return _autoMigrationSpecsSet
  }

  public override fun createAutoMigrations(autoMigrationSpecs: Map<KClass<out AutoMigrationSpec>, AutoMigrationSpec>): List<Migration> {
    val _autoMigrations: MutableList<Migration> = mutableListOf()
    return _autoMigrations
  }

  public override fun signalDao(): SignalDao = _signalDao.value
}
