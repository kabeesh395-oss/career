package com.example.careerpilot.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.example.careerpilot.data.model.*

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
        SkillSprint::class
    ],
    version = 4,
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
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
