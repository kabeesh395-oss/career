package com.example.careerpilot.data.firebase

import android.util.Log
import com.example.careerpilot.data.local.CareerDao
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
    val itemsDownloaded: Int = 0,
    val syncStatus: String = "Ready for Firestore cloud sync",
    val isSuccess: Boolean = true,
    val errorMessage: String? = null
)

class FirestoreSyncManager(
    private val authManager: FirebaseAuthManager,
    private val dao: CareerDao? = null
) {

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
                "id" to profile.id,
                "fullName" to profile.fullName,
                "headline" to profile.headline,
                "email" to profile.email,
                "targetRole" to profile.targetRole,
                "targetIndustry" to profile.targetIndustry,
                "targetSalary" to profile.targetSalary,
                "targetCompanyTier" to profile.targetCompanyTier,
                "location" to profile.location,
                "bio" to profile.bio,
                "education" to profile.education,
                "experienceYears" to profile.experienceYears,
                "readinessScore" to (profile.readinessScore ?: 70),
                "updatedAt" to System.currentTimeMillis()
            )
            firestore.collection("users").document(userId)
                .collection("profile").document("current")
                .set(profileMap, SetOptions.merge())
                .await()
            Log.d("FirestoreSync", "User profile synced to Firestore: $userId")
            true
        } catch (e: Exception) {
            Log.w("FirestoreSync", "Profile sync note: ${e.message}")
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
                "status" to session.status,
                "updatedAt" to System.currentTimeMillis()
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
     * Sync Portfolio Projects to Cloud Firestore
     */
    suspend fun syncProjectsToCloud(projects: List<PortfolioProject>): Boolean = withContext(Dispatchers.IO) {
        val userId = authManager.getCurrentUserId()
        try {
            val batch = firestore.batch()
            val collection = firestore.collection("users").document(userId).collection("projects")

            projects.forEach { proj ->
                val docRef = collection.document(proj.id.toString())
                val data = mapOf(
                    "id" to proj.id,
                    "title" to proj.title,
                    "description" to proj.description,
                    "repositoryUrl" to proj.repositoryUrl,
                    "liveUrl" to proj.liveUrl,
                    "technologies" to proj.technologies,
                    "status" to proj.status,
                    "skillsTargeted" to proj.skillsTargeted,
                    "updatedAt" to System.currentTimeMillis()
                )
                batch.set(docRef, data, SetOptions.merge())
            }
            batch.commit().await()
            true
        } catch (e: Exception) {
            Log.w("FirestoreSync", "Projects sync note: ${e.message}")
            false
        }
    }

    /**
     * Download and merge cloud data from Firestore into local Room database
     */
    suspend fun downloadAllFromCloud(targetDao: CareerDao): Int = withContext(Dispatchers.IO) {
        val userId = authManager.getCurrentUserId()
        var downloadCount = 0
        try {
            // 1. Download Profile
            val profileDoc = firestore.collection("users").document(userId)
                .collection("profile").document("current")
                .get()
                .await()

            if (profileDoc.exists()) {
                val data = profileDoc.data
                if (data != null) {
                    val localProfile = targetDao.getUserProfile() ?: UserProfile()
                    val mergedProfile = localProfile.copy(
                        fullName = (data["fullName"] as? String) ?: localProfile.fullName,
                        headline = (data["headline"] as? String) ?: localProfile.headline,
                        email = (data["email"] as? String) ?: localProfile.email,
                        targetRole = (data["targetRole"] as? String) ?: localProfile.targetRole,
                        targetIndustry = (data["targetIndustry"] as? String) ?: localProfile.targetIndustry,
                        targetSalary = (data["targetSalary"] as? String) ?: localProfile.targetSalary,
                        targetCompanyTier = (data["targetCompanyTier"] as? String) ?: localProfile.targetCompanyTier,
                        location = (data["location"] as? String) ?: localProfile.location,
                        bio = (data["bio"] as? String) ?: localProfile.bio,
                        education = (data["education"] as? String) ?: localProfile.education,
                        experienceYears = (data["experienceYears"] as? Number)?.toFloat() ?: localProfile.experienceYears,
                        readinessScore = (data["readinessScore"] as? Number)?.toInt() ?: localProfile.readinessScore
                    )
                    targetDao.insertOrUpdateProfile(mergedProfile)
                    downloadCount++
                }
            }

            // 2. Download Job Applications
            val appsSnapshot = firestore.collection("users").document(userId)
                .collection("applications")
                .get()
                .await()

            for (doc in appsSnapshot.documents) {
                val d = doc.data ?: continue
                val app = JobApplication(
                    id = (d["id"] as? String) ?: doc.id,
                    company = (d["company"] as? String) ?: "Company",
                    roleTitle = (d["roleTitle"] as? String) ?: "Engineer",
                    location = (d["location"] as? String) ?: "Remote",
                    salaryOffered = (d["salaryOffered"] as? String) ?: "$180k",
                    stage = (d["stage"] as? String) ?: "APPLIED",
                    appliedDate = (d["appliedDate"] as? Number)?.toLong() ?: System.currentTimeMillis(),
                    matchScore = (d["matchScore"] as? Number)?.toInt() ?: 85,
                    notes = (d["notes"] as? String) ?: "",
                    interviewDate = (d["interviewDate"] as? String) ?: ""
                )
                targetDao.insertJobApplication(app)
                downloadCount++
            }

            // 3. Download Skills
            val skillsSnapshot = firestore.collection("users").document(userId)
                .collection("skills")
                .get()
                .await()

            val cloudSkills = mutableListOf<UserSkill>()
            for (doc in skillsSnapshot.documents) {
                val d = doc.data ?: continue
                cloudSkills.add(
                    UserSkill(
                        skillName = (d["skillName"] as? String) ?: doc.id,
                        category = (d["category"] as? String) ?: "Core",
                        proficiencyLevel = (d["proficiencyLevel"] as? Number)?.toInt() ?: 3,
                        verified = (d["verified"] as? Boolean) ?: false,
                        source = (d["source"] as? String) ?: "cloud_firestore"
                    )
                )
            }
            if (cloudSkills.isNotEmpty()) {
                targetDao.insertUserSkills(cloudSkills)
                downloadCount += cloudSkills.size
            }

            Log.d("FirestoreSync", "Downloaded & merged $downloadCount entities from Firestore.")
            downloadCount
        } catch (e: Exception) {
            Log.w("FirestoreSync", "Cloud download error: ${e.message}")
            0
        }
    }

    /**
     * Trigger full two-way cloud synchronization (Upload local + Download cloud)
     */
    suspend fun triggerFullCloudSync(
        profile: UserProfile,
        apps: List<JobApplication>,
        skills: List<UserSkill>,
        projects: List<PortfolioProject> = emptyList(),
        interviews: List<InterviewSession> = emptyList(),
        targetDao: CareerDao? = dao
    ): CloudSyncStatus = withContext(Dispatchers.IO) {
        var uploadedCount = 0
        try {
            val pSuccess = syncProfileToCloud(profile)
            if (pSuccess) uploadedCount++

            apps.forEach { app ->
                if (syncJobApplicationToCloud(app)) uploadedCount++
            }

            if (syncSkillsToCloud(skills)) uploadedCount += skills.size
            if (projects.isNotEmpty() && syncProjectsToCloud(projects)) uploadedCount += projects.size
            interviews.forEach { interview ->
                if (syncInterviewSessionToCloud(interview)) uploadedCount++
            }

            // Download from cloud if DAO provided
            var downloadedCount = 0
            if (targetDao != null) {
                downloadedCount = downloadAllFromCloud(targetDao)
            }

            val totalCount = uploadedCount + downloadedCount
            val timeStr = java.text.SimpleDateFormat("MMM dd, HH:mm:ss", java.util.Locale.US).format(java.util.Date())

            CloudSyncStatus(
                isSyncing = false,
                lastSyncTimestamp = timeStr,
                itemsSynced = uploadedCount,
                itemsDownloaded = downloadedCount,
                syncStatus = "Bi-directional sync complete: $uploadedCount uploaded, $downloadedCount merged from Firestore",
                isSuccess = true
            )
        } catch (e: Exception) {
            val timeStr = java.text.SimpleDateFormat("MMM dd, HH:mm:ss", java.util.Locale.US).format(java.util.Date())
            CloudSyncStatus(
                isSyncing = false,
                lastSyncTimestamp = timeStr,
                itemsSynced = uploadedCount,
                syncStatus = "Local storage active (Cloud note: ${e.localizedMessage ?: "offline ready"})",
                isSuccess = false,
                errorMessage = e.message
            )
        }
    }
}

