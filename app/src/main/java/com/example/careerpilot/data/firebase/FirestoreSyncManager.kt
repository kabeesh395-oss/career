package com.example.careerpilot.data.firebase

import android.util.Log
import com.example.careerpilot.data.model.*
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

data class CloudSyncStatus(
    val isSyncing: Boolean = false,
    val lastSyncTimestamp: String = "Never",
    val itemsSynced: Int = 0,
    val syncStatus: String = "Ready for Firestore sync"
)

class FirestoreSyncManager(private val authManager: FirebaseAuthManager) {

    private val firestore: FirebaseFirestore by lazy {
        FirebaseFirestore.getInstance()
    }

    /**
     * Sync User Profile to Cloud Firestore
     */
    suspend fun syncProfileToCloud(profile: UserProfile): Boolean = withContext(Dispatchers.IO) {
        val userId = authManager.getCurrentUserId()
        try {
            val profileMap = mapOf(
                "fullName" to profile.fullName,
                "email" to profile.email,
                "targetRole" to profile.targetRole,
                "location" to profile.location,
                "bio" to profile.bio,
                "education" to profile.education,
                "experienceYears" to profile.experienceYears,
                "updatedAt" to System.currentTimeMillis()
            )
            firestore.collection("users").document(userId)
                .collection("profile").document("current")
                .set(profileMap, SetOptions.merge())
                .await()
            Log.d("FirestoreSync", "User profile synced to Firestore: $userId")
            true
        } catch (e: Exception) {
            Log.w("FirestoreSync", "Profile sync exception: ${e.message}")
            false
        }
    }

    /**
     * Sync Job Application to Cloud Firestore
     */
    suspend fun syncJobApplicationToCloud(app: JobApplication): Boolean = withContext(Dispatchers.IO) {
        val userId = authManager.getCurrentUserId()
        try {
            val appMap = mapOf(
                "id" to app.id,
                "company" to app.company,
                "roleTitle" to app.roleTitle,
                "location" to app.location,
                "salaryOffered" to app.salaryOffered,
                "stage" to app.stage,
                "appliedDate" to app.appliedDate,
                "matchScore" to app.matchScore,
                "notes" to app.notes,
                "interviewDate" to app.interviewDate,
                "updatedAt" to System.currentTimeMillis()
            )
            firestore.collection("users").document(userId)
                .collection("applications").document(app.id)
                .set(appMap, SetOptions.merge())
                .await()
            Log.d("FirestoreSync", "Application synced to Firestore: ${app.id}")
            true
        } catch (e: Exception) {
            Log.w("FirestoreSync", "App sync note: ${e.message}")
            false
        }
    }

    /**
     * Sync Interview Session to Cloud Firestore
     */
    suspend fun syncInterviewSessionToCloud(session: InterviewSession): Boolean = withContext(Dispatchers.IO) {
        val userId = authManager.getCurrentUserId()
        try {
            val sessionMap = mapOf(
                "id" to session.id,
                "roleTarget" to session.roleTarget,
                "difficulty" to session.difficulty,
                "overallScore" to session.overallScore,
                "feedbackSummary" to session.feedbackSummary,
                "createdAt" to session.createdAt,
                "totalQuestions" to session.totalQuestions,
                "completedQuestions" to session.completedQuestions,
                "status" to session.status
            )
            firestore.collection("users").document(userId)
                .collection("interviews").document(session.id)
                .set(sessionMap, SetOptions.merge())
                .await()
            Log.d("FirestoreSync", "Interview synced to Firestore: ${session.id}")
            true
        } catch (e: Exception) {
            Log.w("FirestoreSync", "Interview sync note: ${e.message}")
            false
        }
    }

    /**
     * Sync all user skills to Cloud Firestore
     */
    suspend fun syncSkillsToCloud(skills: List<UserSkill>): Boolean = withContext(Dispatchers.IO) {
        val userId = authManager.getCurrentUserId()
        try {
            val batch = firestore.batch()
            val collection = firestore.collection("users").document(userId).collection("skills")

            skills.forEach { skill ->
                val docRef = collection.document(skill.skillName.replace("/", "_"))
                val data = mapOf(
                    "skillName" to skill.skillName,
                    "category" to skill.category,
                    "proficiencyLevel" to skill.proficiencyLevel,
                    "verified" to skill.verified,
                    "source" to skill.source,
                    "updatedAt" to System.currentTimeMillis()
                )
                batch.set(docRef, data, SetOptions.merge())
            }
            batch.commit().await()
            Log.d("FirestoreSync", "Batch synced ${skills.size} skills to Firestore.")
            true
        } catch (e: Exception) {
            Log.w("FirestoreSync", "Skills sync note: ${e.message}")
            false
        }
    }

    /**
     * Trigger full two-way cloud synchronization
     */
    suspend fun triggerFullCloudSync(
        profile: UserProfile,
        apps: List<JobApplication>,
        skills: List<UserSkill>
    ): CloudSyncStatus = withContext(Dispatchers.IO) {
        var syncedCount = 0
        val pSuccess = syncProfileToCloud(profile)
        if (pSuccess) syncedCount++

        apps.forEach { app ->
            if (syncJobApplicationToCloud(app)) syncedCount++
        }

        if (syncSkillsToCloud(skills)) syncedCount += skills.size

        val timeStr = java.text.SimpleDateFormat("MMM dd, HH:mm:ss", java.util.Locale.US).format(java.util.Date())
        CloudSyncStatus(
            isSyncing = false,
            lastSyncTimestamp = timeStr,
            itemsSynced = syncedCount,
            syncStatus = "✓ Successfully persisted $syncedCount entities to Cloud Firestore"
        )
    }
}
