export interface GitHubRepoSummary {
  name: string;
  description: string | null;
  html_url: string;
  language: string | null;
  stargazers_count: number;
  forks_count: number;
  updated_at: string;
}

export interface GitHubProfileAnalysis {
  username: string;
  publicRepoCount: number;
  totalStars: number;
  totalForks: number;
  topLanguages: Array<{ language: string; count: number; percentage: number }>;
  recentRepositories: GitHubRepoSummary[];
  syncedAt: string;
}

export class GitHubService {
  public static async fetchAndAnalyzeGitHubProfile(username: string): Promise<GitHubProfileAnalysis> {
    const cleanUsername = username.trim().replace(/^@/, '');
    if (!cleanUsername) {
      throw new Error('GitHub username is required.');
    }

    const url = `https://api.github.com/users/${encodeURIComponent(cleanUsername)}/repos?per_page=100&sort=updated`;
    const headers: Record<string, string> = {
      'User-Agent': 'CareerPilot-AI-Engine',
      'Accept': 'application/vnd.github.v3+json'
    };

    if (process.env.GITHUB_TOKEN) {
      headers['Authorization'] = `token ${process.env.GITHUB_TOKEN}`;
    }

    const response = await fetch(url, { headers });

    if (response.status === 404) {
      throw new Error(`GitHub user "${cleanUsername}" was not found.`);
    }

    if (response.status === 403) {
      throw new Error('GitHub API rate limit exceeded. Please configure a GITHUB_TOKEN or try again later.');
    }

    if (!response.ok) {
      throw new Error(`GitHub API request failed with status ${response.status}: ${response.statusText}`);
    }

    const repos = await response.json() as any[];

    let totalStars = 0;
    let totalForks = 0;
    const languageCounts: Record<string, number> = {};

    const recentRepositories: GitHubRepoSummary[] = repos.slice(0, 10).map(r => {
      totalStars += r.stargazers_count || 0;
      totalForks += r.forks_count || 0;
      if (r.language) {
        languageCounts[r.language] = (languageCounts[r.language] || 0) + 1;
      }
      return {
        name: r.name,
        description: r.description,
        html_url: r.html_url,
        language: r.language,
        stargazers_count: r.stargazers_count,
        forks_count: r.forks_count,
        updated_at: r.updated_at
      };
    });

    const totalLangInstances = Object.values(languageCounts).reduce((a, b) => a + b, 0);
    const topLanguages = Object.entries(languageCounts)
      .map(([language, count]) => ({
        language,
        count,
        percentage: totalLangInstances > 0 ? Math.round((count / totalLangInstances) * 100) : 0
      }))
      .sort((a, b) => b.count - a.count);

    return {
      username: cleanUsername,
      publicRepoCount: repos.length,
      totalStars,
      totalForks,
      topLanguages,
      recentRepositories,
      syncedAt: new Date().toISOString()
    };
  }
}
