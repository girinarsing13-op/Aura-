package com.example.aura.data.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

@Database(
    entities = [
        UserProfileEntity::class,
        BlueprintCacheEntity::class,
        WorkoutSessionEntity::class,
        DailyHabitEntity::class,
        ExerciseLogEntity::class,
        ExerciseSubstitutionEntity::class,
        BodyCheckInEntity::class,
        UnlockedAchievementEntity::class,
        FoodEntity::class,
        MealLogEntity::class
    ],
    version = 10,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun auraDao(): AuraDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        val MIGRATION_7_8 = object : Migration(7, 8) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE user_profile ADD COLUMN gymEquipmentCsv TEXT NOT NULL DEFAULT ''")
                db.execSQL("ALTER TABLE user_profile ADD COLUMN calisthenicsEquipmentCsv TEXT NOT NULL DEFAULT 'Pull-up Bar,Dip Bars'")
                db.execSQL("ALTER TABLE user_profile ADD COLUMN wakeTimeMinutes INTEGER NOT NULL DEFAULT 420")
                db.execSQL("ALTER TABLE user_profile ADD COLUMN hasWorkOrSchoolHours INTEGER NOT NULL DEFAULT 1")
                db.execSQL("ALTER TABLE user_profile ADD COLUMN workStartMinutes INTEGER NOT NULL DEFAULT 540")
                db.execSQL("ALTER TABLE user_profile ADD COLUMN workEndMinutes INTEGER NOT NULL DEFAULT 1020")
                db.execSQL("ALTER TABLE user_profile ADD COLUMN sleepTimeMinutes INTEGER NOT NULL DEFAULT 1380")
                db.execSQL("ALTER TABLE user_profile ADD COLUMN preferredWorkoutMinutes INTEGER NOT NULL DEFAULT 1080")
                db.execSQL("ALTER TABLE user_profile ADD COLUMN preferredWorkoutDaysCsv TEXT NOT NULL DEFAULT ''")
                db.execSQL("ALTER TABLE user_profile ADD COLUMN workoutReminderEnabled INTEGER NOT NULL DEFAULT 1")

                db.execSQL("ALTER TABLE daily_habits ADD COLUMN waterMl INTEGER NOT NULL DEFAULT 0")
                db.execSQL("ALTER TABLE daily_habits ADD COLUMN loggedCalories INTEGER NOT NULL DEFAULT 0")
                db.execSQL("ALTER TABLE daily_habits ADD COLUMN loggedProteinG INTEGER NOT NULL DEFAULT 0")
                db.execSQL("ALTER TABLE daily_habits ADD COLUMN eatenMealsCsv TEXT NOT NULL DEFAULT ''")
            }
        }

        val MIGRATION_8_9 = object : Migration(8, 9) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE user_profile ADD COLUMN weighInReminderEnabled INTEGER NOT NULL DEFAULT 1")
                db.execSQL("ALTER TABLE user_profile ADD COLUMN deloadActive INTEGER NOT NULL DEFAULT 0")
                db.execSQL("ALTER TABLE user_profile ADD COLUMN deloadStartDateKey TEXT NOT NULL DEFAULT ''")
                db.execSQL("ALTER TABLE user_profile ADD COLUMN deloadDismissedAtWeekKey TEXT NOT NULL DEFAULT ''")

                db.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS `body_check_ins` (
                        `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        `dateKey` TEXT NOT NULL,
                        `timestampMillis` INTEGER NOT NULL,
                        `weightKg` REAL NOT NULL,
                        `waistCm` REAL,
                        `chestCm` REAL,
                        `armCm` REAL,
                        `photoPath` TEXT,
                        `note` TEXT NOT NULL
                    )
                    """.trimIndent()
                )

                db.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS `achievements` (
                        `id` TEXT NOT NULL,
                        `title` TEXT NOT NULL,
                        `description` TEXT NOT NULL,
                        `unlockedDateKey` TEXT NOT NULL,
                        `unlockedAtMillis` INTEGER NOT NULL,
                        PRIMARY KEY(`id`)
                    )
                    """.trimIndent()
                )
            }
        }

        val MIGRATION_9_10 = object : Migration(9, 10) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS `foods` (
                        `id` TEXT NOT NULL,
                        `name` TEXT NOT NULL,
                        `servingDesc` TEXT NOT NULL,
                        `gramsPerServing` REAL NOT NULL,
                        `calories` INTEGER NOT NULL,
                        `protein` REAL NOT NULL,
                        `carbs` REAL NOT NULL,
                        `fat` REAL NOT NULL,
                        `dietType` TEXT NOT NULL,
                        `isVegan` INTEGER NOT NULL,
                        `allergensCsv` TEXT NOT NULL,
                        `isCustom` INTEGER NOT NULL,
                        `isFavorite` INTEGER NOT NULL,
                        `lastUsedAt` INTEGER NOT NULL,
                        PRIMARY KEY(`id`)
                    )
                    """.trimIndent()
                )
                db.execSQL("CREATE INDEX IF NOT EXISTS `index_foods_name` ON `foods` (`name`)")

                db.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS `meal_logs` (
                        `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        `dateKey` TEXT NOT NULL,
                        `timestampMillis` INTEGER NOT NULL,
                        `mealSlot` TEXT NOT NULL,
                        `foodId` TEXT NOT NULL,
                        `foodName` TEXT NOT NULL,
                        `servingDesc` TEXT NOT NULL,
                        `gramsPerServing` REAL NOT NULL,
                        `quantity` REAL NOT NULL,
                        `unitMode` TEXT NOT NULL,
                        `gramsLogged` REAL NOT NULL,
                        `calories` INTEGER NOT NULL,
                        `protein` REAL NOT NULL,
                        `carbs` REAL NOT NULL,
                        `fat` REAL NOT NULL,
                        `isQuickAdd` INTEGER NOT NULL
                    )
                    """.trimIndent()
                )
                db.execSQL("CREATE INDEX IF NOT EXISTS `index_meal_logs_dateKey` ON `meal_logs` (`dateKey`)")
            }
        }

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "aura_database"
                )
                    .addMigrations(MIGRATION_7_8, MIGRATION_8_9, MIGRATION_9_10)
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
