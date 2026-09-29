package com.example.careerpilot.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.careerpilot.data.model.*

val MIGRATION_5_6 = object : Migration(5, 6) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL("ALTER TABLE user_skills ADD COLUMN userId TEXT NOT NULL DEFAULT 'legacy_user'")
        db.execSQL("ALTER TABLE skill_gaps ADD COLUMN userId TEXT NOT NULL DEFAULT 'legacy_user'")
        db.execSQL("ALTER TABLE roadmaps ADD COLUMN userId TEXT NOT NULL DEFAULT 'legacy_user'")
        db.execSQL("ALTER TABLE roadmap_items ADD COLUMN userId TEXT NOT NULL DEFAULT 'legacy_user'")
        db.execSQL("ALTER TABLE portfolio_projects ADD COLUMN userId TEXT NOT NULL DEFAULT 'legacy_user'")
        db.execSQL("ALTER TABLE resume_audits ADD COLUMN userId TEXT NOT NULL DEFAULT 'legacy_user'")
        db.execSQL("ALTER TABLE interview_sessions ADD COLUMN userId TEXT NOT NULL DEFAULT 'legacy_user'")
        db.execSQL("ALTER TABLE interview_answers ADD COLUMN userId TEXT NOT NULL DEFAULT 'legacy_user'")
        db.execSQL("ALTER TABLE analytics_events ADD COLUMN userId TEXT NOT NULL DEFAULT 'legacy_user'")
        db.execSQL("ALTER TABLE audit_issues ADD COLUMN userId TEXT NOT NULL DEFAULT 'legacy_user'")
        db.execSQL("ALTER TABLE job_matches ADD COLUMN userId TEXT NOT NULL DEFAULT 'legacy_user'")
        db.execSQL("ALTER TABLE job_applications ADD COLUMN userId TEXT NOT NULL DEFAULT 'legacy_user'")

        // Recreate integrations table with composite primary key (provider, userId)
        db.execSQL("""
            CREATE TABLE IF NOT EXISTS integrations_new (
                provider TEXT NOT NULL,
                userId TEXT NOT NULL DEFAULT 'legacy_user',
                username TEXT NOT NULL DEFAULT '',
                connectionStatus TEXT NOT NULL DEFAULT 'NOT_CONNECTED',
                isConnected INTEGER NOT NULL DEFAULT 0,
                lastSyncedAt INTEGER NOT NULL DEFAULT 0,
                avatarUrl TEXT NOT NULL DEFAULT '',
                displayName TEXT NOT NULL DEFAULT '',
                publicReposCount INTEGER NOT NULL DEFAULT 0,
                followersCount INTEGER NOT NULL DEFAULT 0,
                followingCount INTEGER NOT NULL DEFAULT 0,
                publicGistsCount INTEGER NOT NULL DEFAULT 0,
                bio TEXT NOT NULL DEFAULT '',
                company TEXT NOT NULL DEFAULT '',
                location TEXT NOT NULL DEFAULT '',
                topRepositoriesJson TEXT NOT NULL DEFAULT '',
                details TEXT NOT NULL DEFAULT '',
                errorMessage TEXT NOT NULL DEFAULT '',
                PRIMARY KEY(provider, userId)
            )
        """.trimIndent())
        db.execSQL("""
            INSERT OR IGNORE INTO integrations_new (provider, userId, username, connectionStatus, isConnected, lastSyncedAt, avatarUrl, displayName, publicReposCount, followersCount, followingCount, publicGistsCount, bio, company, location, topRepositoriesJson, details, errorMessage)
            SELECT provider, 'legacy_user', username, connectionStatus, isConnected, lastSyncedAt, avatarUrl, displayName, publicReposCount, followersCount, followingCount, publicGistsCount, bio, company, location, topRepositoriesJson, details, errorMessage FROM integrations
        """.trimIndent())
        db.execSQL("DROP TABLE IF EXISTS integrations")
        db.execSQL("ALTER TABLE integrations_new RENAME TO integrations")
    }
}

@Database(
    entities = [
        UserProfile::class,
        UserSkill::class,
        SkillGap::class,
        Roadmap::class,
        RoadmapItem::class,
        PortfolioProject::class,
        ResumeAudit::class,
        InterviewSession::class,
        InterviewAnswer::class,
        LearningResource::class,
        IntegrationAccount::class,
        AnalyticsEvent::class,
        AuditIssue::class,
        TargetJobPosting::class,
        JobMatchResult::class,
        JobApplication::class,
        CodingChallenge::class,
        PeerMatch::class,
        SkillSprint::class,
        CareerOpportunity::class
    ],
    version = 6,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun careerDao(): CareerDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "careerpilot_database"
                )
                    .addMigrations(MIGRATION_5_6)
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
