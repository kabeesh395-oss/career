package com.example.careerpilot.data.remote.github

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONArray
import org.json.JSONObject
import java.util.concurrent.TimeUnit

data class GitHubProfileResult(
    val username: String,
    val displayName: String,
    val avatarUrl: String,
    val publicRepos: Int,
    val followers: Int,
    val following: Int,
    val publicGists: Int,
    val bio: String,
    val company: String,
    val location: String,
    val profileUrl: String,
    val topRepos: List<GitHubRepoItem>
)

data class GitHubRepoItem(
    val name: String,
    val description: String,
    val url: String,
    val stars: Int,
    val forks: Int,
    val language: String,
    val updatedAt: String
)

sealed class GitHubValidationResult {
    data class Success(val profile: GitHubProfileResult) : GitHubValidationResult()
    data class UserNotFound(val username: String, val message: String) : GitHubValidationResult()
    data class RateLimited(val message: String) : GitHubValidationResult()
    data class Error(val code: Int, val message: String) : GitHubValidationResult()
}

object GitHubApiClient {
    private val client = OkHttpClient.Builder()
        .connectTimeout(10, TimeUnit.SECONDS)
        .readTimeout(10, TimeUnit.SECONDS)
        .build()

    suspend fun fetchGitHubProfileAndRepos(username: String): GitHubValidationResult = withContext(Dispatchers.IO) {
        val trimmed = username.trim()
        if (trimmed.isBlank()) {
            return@withContext GitHubValidationResult.Error(400, "GitHub username cannot be empty.")
        }

        try {
            // 1. Fetch user profile
            val userUrl = "https://api.github.com/users/$trimmed"
            val userRequest = Request.Builder()
                .url(userUrl)
                .addHeader("User-Agent", "CareerPilot-AI-Android")
                .addHeader("Accept", "application/vnd.github.v3+json")
                .build()

            client.newCall(userRequest).execute().use { userResponse ->
                val code = userResponse.code
                val bodyString = userResponse.body?.string() ?: ""

                when (code) {
                    200 -> {
                        val userObj = JSONObject(bodyString)
                        val login = userObj.optString("login", trimmed)
                        val name = userObj.optString("name", login)
                        val avatarUrl = userObj.optString("avatar_url", "")
                        val publicRepos = userObj.optInt("public_repos", 0)
                        val followers = userObj.optInt("followers", 0)
                        val following = userObj.optInt("following", 0)
                        val publicGists = userObj.optInt("public_gists", 0)
                        val bio = userObj.optString("bio", "")
                        val company = userObj.optString("company", "")
                        val location = userObj.optString("location", "")
                        val profileUrl = userObj.optString("html_url", "https://github.com/$trimmed")

                        // 2. Fetch public repositories
                        val repos = mutableListOf<GitHubRepoItem>()
                        try {
                            val reposUrl = "https://api.github.com/users/$trimmed/repos?sort=updated&per_page=6"
                            val reposRequest = Request.Builder()
                                .url(reposUrl)
                                .addHeader("User-Agent", "CareerPilot-AI-Android")
                                .addHeader("Accept", "application/vnd.github.v3+json")
                                .build()

                            client.newCall(reposRequest).execute().use { reposResponse ->
                                if (reposResponse.isSuccessful) {
                                    val reposBody = reposResponse.body?.string() ?: "[]"
                                    val array = JSONArray(reposBody)
                                    for (i in 0 until array.length()) {
                                        val item = array.getJSONObject(i)
                                        repos.add(
                                            GitHubRepoItem(
                                                name = item.optString("name", "repo"),
                                                description = item.optString("description", "No description provided."),
                                                url = item.optString("html_url", ""),
                                                stars = item.optInt("stargazers_count", 0),
                                                forks = item.optInt("forks_count", 0),
                                                language = item.optString("language", "Code"),
                                                updatedAt = item.optString("updated_at", "")
                                            )
                                        )
                                    }
                                }
                            }
                        } catch (e: Exception) {
                            // Non-fatal if repos call fails, we still return the user profile
                        }

                        val result = GitHubProfileResult(
                            username = login,
                            displayName = name,
                            avatarUrl = avatarUrl,
                            publicRepos = publicRepos,
                            followers = followers,
                            following = following,
                            publicGists = publicGists,
                            bio = bio,
                            company = company,
                            location = location,
                            profileUrl = profileUrl,
                            topRepos = repos
                        )
                        return@withContext GitHubValidationResult.Success(result)
                    }
                    404 -> {
                        return@withContext GitHubValidationResult.UserNotFound(
                            username = trimmed,
                            message = "GitHub user '$trimmed' was not found (HTTP 404). Please verify username spelling."
                        )
                    }
                    403 -> {
                        return@withContext GitHubValidationResult.RateLimited(
                            message = "GitHub API rate limit exceeded for unauthenticated requests. Please wait a minute or retry shortly."
                        )
                    }
                    else -> {
                        return@withContext GitHubValidationResult.Error(
                            code = code,
                            message = "GitHub API returned HTTP $code: ${userResponse.message}"
                        )
                    }
                }
            }
        } catch (e: Exception) {
            return@withContext GitHubValidationResult.Error(
                code = 0,
                message = "Network error communicating with GitHub API: ${e.localizedMessage ?: e.message}"
            )
        }
    }
}
