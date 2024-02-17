package com.mine.helpfle.data

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import android.provider.BaseColumns
import android.util.Log
import java.io.BufferedReader
import java.io.InputStream
import java.io.InputStreamReader
import java.util.Collections.max

private const val TAG_DATABASEHELPER = "DATABASEHELPER"

private object Schema {
    object Entry : BaseColumns {
        const val TABLE_NAME = "words"
        const val COLUMN_NAME_WORD = "word"
        const val COLUMN_NAME_SOLUTION = "solution"
        const val COLUMN_NAME_SOLUTIONORDER = "solutionOrder"
        const val COLUMN_NAME_TRIED = "tried"
        const val COLUMN_NAME_WON = "won"
        const val COLUMN_NAME_NUMGUESSES = "numGuesses"
    }
}

private const val SQL_CREATE_ENTRIES =
    "CREATE TABLE ${Schema.Entry.TABLE_NAME} ( " +
            "${BaseColumns._ID} INTEGER PRIMARY KEY, " +
            "${Schema.Entry.COLUMN_NAME_WORD} TEXT NOT NULL, " +
            "${Schema.Entry.COLUMN_NAME_SOLUTION} INTEGER NOT NULL, " +
            "${Schema.Entry.COLUMN_NAME_SOLUTIONORDER} INTEGER, " +
            "${Schema.Entry.COLUMN_NAME_TRIED} INTEGER," +
            "${Schema.Entry.COLUMN_NAME_WON} INTEGER," +
            "${Schema.Entry.COLUMN_NAME_NUMGUESSES} INTEGER)"

private const val SQL_DELETE_ENTRIES = "DROP TABLE IF EXISTS ${Schema.Entry.TABLE_NAME}"

class DatabaseHelper(context : Context, solnPath : String, dictPath : String) :
    SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION), IDatabase {

    private var context: Context
    private var solnPath: String
    private var dictPath: String

    companion object {
        const val DATABASE_VERSION = 1
        const val DATABASE_NAME = "Helpfle.db"
    }

    init {
        this.context = context
        this.solnPath = solnPath
        this.dictPath = dictPath
    }

    override fun onCreate(db: SQLiteDatabase?) {
        Log.d(TAG_DATABASEHELPER, "Creating database")
        db?.execSQL(SQL_CREATE_ENTRIES)

        if (db != null) {
            Log.d(TAG_DATABASEHELPER, "begining loadDataFromTextFile")
            loadFile(db, this.context.assets.open(solnPath), isSolution = 1)
            generateSolutionOrder(db)
            loadFile(db, this.context.assets.open(dictPath), isSolution = 0)
        }

    }

    private fun loadFile(db: SQLiteDatabase, fSream: InputStream, isSolution: Int = 0) {
        fSream.use {
            BufferedReader(InputStreamReader(it)).readLines().forEach { word ->
                val values = ContentValues().apply {
                    put(Schema.Entry.COLUMN_NAME_WORD, word)
                    put(Schema.Entry.COLUMN_NAME_SOLUTION, isSolution)
                    if (isSolution == 1) {
                        put(Schema.Entry.COLUMN_NAME_TRIED, 0)
                        put(Schema.Entry.COLUMN_NAME_WON, 0)
                    }
                }

                db.insert(Schema.Entry.TABLE_NAME, null, values)
            }
        }
    }

    private fun generateSolutionOrder(db: SQLiteDatabase) {
        val entryIds = arrayListOf<Int>()
        val shuffledIds = arrayListOf<Int>()

        db.rawQuery(
            "SELECT ${BaseColumns._ID} FROM ${Schema.Entry.TABLE_NAME}",
            null
        ).use { cursor ->
            cursor.moveToFirst()
            val colIdx = cursor.getColumnIndexOrThrow(BaseColumns._ID)
            var nextId = cursor.getLong(colIdx).toInt()
            entryIds += nextId
            shuffledIds += nextId
            while (cursor.moveToNext()) {
                nextId = cursor.getLong(colIdx).toInt()
                entryIds += nextId
                shuffledIds += nextId
            }
        }

        shuffledIds.shuffle()

        for (i in 0..<entryIds.size) {
            db.update(
                Schema.Entry.TABLE_NAME,
                ContentValues().apply {
                    put(Schema.Entry.COLUMN_NAME_SOLUTIONORDER, shuffledIds[i])
                },
                "${BaseColumns._ID} = ?",
                arrayOf(entryIds[i].toString())
            )
        }
    }

    override fun onUpgrade(db: SQLiteDatabase?, oldVersion: Int, newVersion: Int) {
        // This database is only a cache for online data, so its upgrade policy is
        // to simply to discard the data and start over
        db?.execSQL(SQL_DELETE_ENTRIES)
        onCreate(db)
    }

    override fun onDowngrade(db: SQLiteDatabase?, oldVersion: Int, newVersion: Int) {
        onUpgrade(db, oldVersion, newVersion)
    }

    override fun getCurrentSolution(): String {
        return writableDatabase.use { db -> pullCurrentWord(db).values.first() }
    }

    override fun onFinishGame(gameOutcome: IDatabase.OUTCOME, numGuesses: Int?) {
        writableDatabase.use { db ->
            val finishedIdx = pullCurrentWord(db).keys.first()

            db.update(
                Schema.Entry.TABLE_NAME,
                ContentValues().apply {
                    put(Schema.Entry.COLUMN_NAME_TRIED, 1)
                    put(
                        Schema.Entry.COLUMN_NAME_WON,
                        when (gameOutcome) {
                            IDatabase.OUTCOME.WON -> 1
                            IDatabase.OUTCOME.LOST -> 0
                        }
                    )
                    numGuesses?.let {
                        put(Schema.Entry.COLUMN_NAME_NUMGUESSES, numGuesses)
                    }
                },
                "${BaseColumns._ID} = ?",
                arrayOf(finishedIdx.toString())
            )
        }
    }

    private fun getAgg(sql : String) : Long {
        return writableDatabase.rawQuery(sql, null).use { cursor ->
            cursor.moveToFirst()
            cursor.getLong(0)
        }
    }

    override fun getUserStats(): UserStats {

        val allGameOutcomes = arrayListOf<Int>()
        writableDatabase.rawQuery(
            "SELECT ${Schema.Entry.COLUMN_NAME_WON} FROM ${Schema.Entry.TABLE_NAME} " +
            "WHERE ${Schema.Entry.COLUMN_NAME_TRIED} = 1 ORDER BY ${Schema.Entry.COLUMN_NAME_SOLUTIONORDER}",
            null
        ).use {cursor ->
            cursor.moveToFirst()
            cursor.getInt(0).also { allGameOutcomes.add(it) }
            while (cursor.moveToNext()) {
                cursor.getInt(0).also { allGameOutcomes.add(it) }
            }
        }

        val numGames = allGameOutcomes.size

        val gamesWon = allGameOutcomes.sum()

        val gamesLost = numGames.minus(gamesWon)

        val avgGuesses = writableDatabase.rawQuery(
            "SELECT AVG(${Schema.Entry.COLUMN_NAME_NUMGUESSES}) FROM ${Schema.Entry.TABLE_NAME} " +
                    "WHERE ${Schema.Entry.COLUMN_NAME_WON} = 1",
            null
        ).use { cursor ->
            cursor.moveToFirst()
            cursor.getDouble(0)
        }

        val islandLengths = arrayListOf<Int>()
        allGameOutcomes.let {
            var i = 0
            while (i < it.size) {
                if (it[i] == 0) {
                    i++
                } else {
                    var j = i + 1
                    while (j < it.size && it[j] != 0) {
                        j++
                    }
                    islandLengths.add(j.minus(i))
                    i = j
                }
            }
        }

        val currentStreak = if (islandLengths.isEmpty() || allGameOutcomes.last() == 0) 0 else islandLengths.last()
        val longestStreak = if (islandLengths.isEmpty()) 0 else max(islandLengths)

        return UserStats(
            numGames,
            gamesWon,
            gamesLost,
            avgGuesses,
            currentStreak,
            longestStreak
        )
    }

    // Get the word by searching for the smallest order value that is a solution word and
    // hasn't been solved yet
    private fun pullCurrentWord(db : SQLiteDatabase) : HashMap<Int, String> {
        return db.rawQuery("SELECT ${BaseColumns._ID}, ${Schema.Entry.COLUMN_NAME_WORD} " +
                "FROM ${Schema.Entry.TABLE_NAME} " +
                "WHERE ${Schema.Entry.COLUMN_NAME_SOLUTION} = 1 " +
                "AND ${Schema.Entry.COLUMN_NAME_WON} = 0 " +
                "ORDER BY ${Schema.Entry.COLUMN_NAME_SOLUTIONORDER} LIMIT 1",
            null
        ).use {cursor ->
            cursor.moveToFirst()
            val wordId = cursor.getInt(cursor.getColumnIndexOrThrow(BaseColumns._ID))
            val word = cursor.getString(cursor.getColumnIndexOrThrow(Schema.Entry.COLUMN_NAME_WORD))
            HashMap<Int, String>().apply {
                put(wordId, word)
            }
        }
    }

    override fun lookupGuess(guess: String): Boolean {
        return getAgg(
                "SELECT COUNT(*) FROM ${Schema.Entry.TABLE_NAME} " +
                        "WHERE ${Schema.Entry.COLUMN_NAME_WORD} LIKE '$guess'"
        ) > 0
    }

}