package com.studyplanner.data.local.database

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

/**
 * Declare all Room schema migrations here.
 *
 * Usage in DatabaseModule:
 *   .addMigrations(MIGRATION_1_2, MIGRATION_2_3)
 *
 * Each migration must update the Room schema version in StudyPlannerDatabase
 * and export a new schema JSON to app/schemas/.
 */
object Migrations {

    /**
     * Example — version 1 → 2:
     * Adds a `priority` column (INTEGER, default 0) to the tasks table.
     *
     * Uncomment and increment StudyPlannerDatabase.version when needed.
     */
    /*
    val MIGRATION_1_2 = object : Migration(1, 2) {
        override fun migrate(database: SupportSQLiteDatabase) {
            database.execSQL(
                "ALTER TABLE tasks ADD COLUMN priority INTEGER NOT NULL DEFAULT 0"
            )
        }
    }
    */

    /**
     * Example — version 2 → 3:
     * Adds a `notes` TEXT column to subjects.
     */
    /*
    val MIGRATION_2_3 = object : Migration(2, 3) {
        override fun migrate(database: SupportSQLiteDatabase) {
            database.execSQL(
                "ALTER TABLE subjects ADD COLUMN notes TEXT NOT NULL DEFAULT ''"
            )
        }
    }
    */
}
