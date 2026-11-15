export type PrimarySection = 'home' | 'resume' | 'practice' | 'skills' | 'profile';

export interface ResolvedRoute {
  section: PrimarySection;
  subTab?: string;
}

export function resolveRoute(rawRoute: string): ResolvedRoute {
  const cleaned = (rawRoute || '')
    .trim()
    .replace(/^#\/?/, '')
    .replace(/^\/+/, '');

  if (!cleaned) {
    return { section: 'home', subTab: 'overview' };
  }

  const [rawBase, rawSub] = cleaned.split('/');
  const base = rawBase.toLowerCase();
  const sub = rawSub ? rawSub.toLowerCase() : undefined;

  switch (base) {
    case 'home':
    case 'dashboard':
      return { section: 'home', subTab: sub === 'analytics' ? 'analytics' : 'overview' };
    case 'analytics':
      return { section: 'home', subTab: 'analytics' };
    case 'resume':
      return { section: 'resume', subTab: sub === 'job_description' || sub === 'jd' ? 'job_description' : 'resume' };
    case 'practice':
      return { section: 'practice', subTab: sub === 'learning' ? 'learning' : 'interview' };
    case 'interview':
      return { section: 'practice', subTab: 'interview' };
    case 'learning':
      return { section: 'practice', subTab: 'learning' };
    case 'skills': {
      const validSkillsTabs = new Set(['my-skills', 'gaps', 'roadmap', 'projects']);
      return { section: 'skills', subTab: sub && validSkillsTabs.has(sub) ? sub : 'my-skills' };
    }
    case 'career':
      return { section: 'skills', subTab: 'gaps' };
    case 'roadmap':
      return { section: 'skills', subTab: 'roadmap' };
    case 'projects':
      return { section: 'skills', subTab: 'projects' };
    case 'profile': {
      const validProfileTabs = new Set(['general', 'integrations', 'system']);
      return { section: 'profile', subTab: sub && validProfileTabs.has(sub) ? sub : 'general' };
    }
    case 'integrations':
      return { section: 'profile', subTab: 'integrations' };
    case 'edge-ai':
      return { section: 'profile', subTab: 'system' };
    default:
      return { section: 'home', subTab: 'overview' };
  }
}
