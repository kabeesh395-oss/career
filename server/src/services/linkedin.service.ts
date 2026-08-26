import crypto from 'crypto';

export interface LinkedInProfileData {
  id: string;
  username: string;
  fullName: string;
  headline: string;
  email?: string;
  profilePicture?: string;
  syncedAt: string;
  isSimulated: boolean;
}

export class LinkedInService {
  private static getClientId(): string | undefined {
    return process.env.LINKEDIN_CLIENT_ID;
  }

  private static getClientSecret(): string | undefined {
    return process.env.LINKEDIN_CLIENT_SECRET;
  }

  private static getRedirectUri(): string {
    return process.env.LINKEDIN_REDIRECT_URI || 'http://localhost:5173/#/integrations/linkedin/callback';
  }

  public static isSimulationMode(): boolean {
    return !this.getClientId() || !this.getClientSecret();
  }

  public static getAuthorizationUrl(state: string): string {
    if (this.isSimulationMode()) {
      // Direct link back to local callback with simulation parameters
      return `${this.getRedirectUri()}?code=sim_code_${crypto.randomBytes(6).toString('hex')}&state=${state}`;
    }

    const params = new URLSearchParams({
      response_type: 'code',
      client_id: this.getClientId()!,
      redirect_uri: this.getRedirectUri(),
      state,
      scope: 'r_liteprofile r_emailaddress'
    });

    return `https://www.linkedin.com/oauth/v2/authorization?${params.toString()}`;
  }

  public static async exchangeCodeForToken(code: string): Promise<string> {
    if (this.isSimulationMode() || code.startsWith('sim_code_')) {
      return `sim_token_${crypto.randomBytes(16).toString('hex')}`;
    }

    const params = new URLSearchParams({
      grant_type: 'authorization_code',
      code,
      redirect_uri: this.getRedirectUri(),
      client_id: this.getClientId()!,
      client_secret: this.getClientSecret()!
    });

    const response = await fetch('https://www.linkedin.com/oauth/v2/accessToken', {
      method: 'POST',
      headers: {
        'Content-Type': 'application/x-www-form-urlencoded'
      },
      body: params.toString()
    });

    if (!response.ok) {
      const errorText = await response.text();
      throw new Error(`LinkedIn OAuth token exchange failed: ${response.status} - ${errorText}`);
    }

    const data = await response.json() as { access_token: string };
    return data.access_token;
  }

  public static async fetchLinkedInProfile(accessToken: string): Promise<LinkedInProfileData> {
    if (this.isSimulationMode() || accessToken.startsWith('sim_token_')) {
      // Return highly realistic mock LinkedIn Profile Data
      return {
        id: `li_sim_${crypto.randomBytes(4).toString('hex')}`,
        username: 'alex-rivera-staff',
        fullName: 'Alex Rivera',
        headline: 'Senior Staff Engineer | Distributed Systems & AI Architect',
        email: 'alex.rivera@linkedin-sim.com',
        profilePicture: 'https://images.unsplash.com/photo-1534528741775-53994a69daeb?w=150&auto=format&fit=crop&q=80',
        syncedAt: new Date().toISOString(),
        isSimulated: true
      };
    }

    // 1. Fetch Profile Data (lite profile)
    const profileRes = await fetch('https://api.linkedin.com/v2/me', {
      headers: {
        'Authorization': `Bearer ${accessToken}`,
        'Connection': 'Keep-Alive'
      }
    });

    if (!profileRes.ok) {
      throw new Error(`Failed to fetch LinkedIn profile: ${profileRes.status} - ${await profileRes.text()}`);
    }

    const profileData = await profileRes.json() as any;
    
    // Extract localized full name
    const firstName = profileData.localizedFirstName || '';
    const lastName = profileData.localizedLastName || '';
    const fullName = `${firstName} ${lastName}`.trim() || 'LinkedIn Member';
    
    // Headline extraction (if provided in lite profile or fallback)
    const headline = profileData.headline?.localized?.[profileData.headline?.preferredLocale?.language + '_' + profileData.headline?.preferredLocale?.country] 
      || 'Software Professional';

    // Profile photo extraction
    let profilePicture: string | undefined;
    const displayImage = profileData['displayImage~']?.elements?.[0];
    if (displayImage) {
      const identifiers = displayImage.identifiers?.[0];
      if (identifiers?.identifier) {
        profilePicture = identifiers.identifier;
      }
    }

    // 2. Fetch Email Address
    let email: string | undefined;
    try {
      const emailRes = await fetch('https://api.linkedin.com/v2/emailAddress?q=members&projection=(elements*(handle~))', {
        headers: { 'Authorization': `Bearer ${accessToken}` }
      });
      if (emailRes.ok) {
        const emailData = await emailRes.json() as any;
        email = emailData.elements?.[0]?.['handle~']?.emailAddress;
      }
    } catch (err) {
      console.warn('[LinkedIn] Failed to fetch email address:', err);
    }

    return {
      id: profileData.id,
      username: profileData.vanityName || profileData.id,
      fullName,
      headline,
      email,
      profilePicture,
      syncedAt: new Date().toISOString(),
      isSimulated: false
    };
  }
}
