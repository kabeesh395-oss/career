// Client-side offline engine & Mock API service for Career Hub

interface MockUser {
  id: string;
  email: string;
  fullName: string;
  role: string;
  avatarUrl?: string;
  createdAt: string;
}

interface MockProfile {
  id: string;
  user_id: string;
  headline: string;
  bio: string;
  location: string;
  education: string;
  experience_years: number;
  target_role: string;
  target_industry: string;
  target_salary: string;
  current_readiness_score: number;
  onboarding_completed: number;
  updated_at: string;
}

interface MockSkill {
  id: string;
  skill_id: string;
  name: string;
  category: string;
  proficiency_level: number;
  verified: number;
  source: string;
  demand_weight: number;
}

interface MockRoadmapItem {
  id: string;
  phase_number: number;
  phase_title: string;
  title: string;
  description: string;
  estimated_hours: number;
  status: string;
  priority: string;
}

interface MockProject {
  id: string;
  title: string;
  description: string;
  technologies: string[];
  github_url: string;
  live_url: string;
  status: string;
  created_at: string;
}

interface MockInterview {
  id: string;
  title: string;
  role: string;
  difficulty: string;
  score: number;
  status: string;
  date: string;
  questions: Array<{
    id: string;
    question: string;
    type: string;
    idealAnswer: string;
    userAnswer?: string;
    feedback?: string;
    score?: number;
  }>;
}

const STORAGE_KEYS = {
  USERS: 'ch_mock_users',
  CURRENT_USER: 'ch_mock_curr_user',
  PROFILE: 'ch_mock_profile',
  SKILLS: 'ch_mock_skills',
  ROADMAP: 'ch_mock_roadmap',
  PROJECTS: 'ch_mock_projects',
  INTERVIEWS: 'ch_mock_interviews',
  RESUMES: 'ch_mock_resumes',
  EVENTS: 'ch_mock_events',
  INTEGRATIONS: 'ch_mock_integrations',
};

function getStorage<T>(key: string, defaultVal: T): T {
  try {
    const raw = localStorage.getItem(key);
    return raw ? JSON.parse(raw) : defaultVal;
  } catch {
    return defaultVal;
  }
}

function setStorage<T>(key: string, val: T): void {
  try {
    localStorage.setItem(key, JSON.stringify(val));
  } catch (e) {
    console.warn('Storage set failed', e);
  }
}

// Initial defaults
const DEFAULT_SKILLS: MockSkill[] = [
  { id: 'usk_1', skill_id: 'sk_ts', name: 'TypeScript', category: 'Languages', proficiency_level: 4, verified: 1, source: 'assessment', demand_weight: 0.95 },
  { id: 'usk_2', skill_id: 'sk_react', name: 'React / Next.js', category: 'Frontend', proficiency_level: 4, verified: 1, source: 'assessment', demand_weight: 0.92 },
  { id: 'usk_3', skill_id: 'sk_node', name: 'Node.js / Express', category: 'Backend', proficiency_level: 4, verified: 1, source: 'github', demand_weight: 0.88 },
  { id: 'usk_4', skill_id: 'sk_sysdes', name: 'System Design & Arch', category: 'Architecture', proficiency_level: 3, verified: 1, source: 'interview', demand_weight: 0.96 },
  { id: 'usk_5', skill_id: 'sk_k8s', name: 'Docker & Kubernetes', category: 'DevOps', proficiency_level: 3, verified: 0, source: 'self_reported', demand_weight: 0.85 },
  { id: 'usk_6', skill_id: 'sk_distrib', name: 'Distributed Systems & Caching', category: 'Backend', proficiency_level: 4, verified: 1, source: 'assessment', demand_weight: 0.94 },
];

const DEFAULT_ROADMAP_ITEMS: MockRoadmapItem[] = [
  { id: 'rd_1', phase_number: 1, phase_title: 'Phase 1: Architecture & Distributed Systems', title: 'Implement Idempotent Payment API & Redlock Distributed Locks', description: 'Design fault-tolerant transactional APIs with distributed redis locking.', estimated_hours: 8, status: 'completed', priority: 'high' },
  { id: 'rd_2', phase_number: 1, phase_title: 'Phase 1: Architecture & Distributed Systems', title: 'High-Throughput Kafka Streaming Partitioning & DLQ routing', description: 'Ensure strict FIFO ordering per customer and route unparseable messages.', estimated_hours: 12, status: 'completed', priority: 'high' },
  { id: 'rd_3', phase_number: 2, phase_title: 'Phase 2: Database Scalability & Query Tuning', title: 'PostgreSQL Database Sharding & B-Tree Composite Indexing', description: 'Optimize heavy aggregation queries and benchmark partition pruning.', estimated_hours: 10, status: 'in_progress', priority: 'medium' },
  { id: 'rd_4', phase_number: 2, phase_title: 'Phase 2: Database Scalability & Query Tuning', title: 'Elasticsearch CQRS Denormalization Engine', description: 'Build asynchronous event sync pipeline from primary SQL store.', estimated_hours: 14, status: 'pending', priority: 'medium' },
  { id: 'rd_5', phase_number: 3, phase_title: 'Phase 3: Production Reliability & Security', title: 'Zero-Downtime Blue-Green Deployment & Canary Gates', description: 'Configure automated rollback based on error rate thresholds in ArgoCD.', estimated_hours: 6, status: 'pending', priority: 'medium' },
  { id: 'rd_6', phase_number: 3, phase_title: 'Phase 3: Production Reliability & Security', title: 'API Gateway Rate Limiting with Sliding Window Counters', description: 'Protect against credential-stuffing and DDoS spikes using distributed Redis token buckets.', estimated_hours: 8, status: 'pending', priority: 'high' }
];

const DEFAULT_PROJECTS: MockProject[] = [
  {
    id: 'proj_1',
    title: 'Cloud-Native Event Stream Processing Hub',
    description: 'High-throughput real-time telemetry processing pipeline capable of 50,000 events/sec with sliding-window anomaly detection.',
    technologies: ['TypeScript', 'Node.js', 'Kafka', 'Redis', 'Docker'],
    github_url: 'https://github.com/engineer/event-stream-hub',
    live_url: 'https://stream.hub.dev',
    status: 'completed',
    created_at: new Date(Date.now() - 86400000 * 14).toISOString()
  },
  {
    id: 'proj_2',
    title: 'Distributed Transaction Coordinator with Sagas',
    description: 'Microservice orchestrator implementing the Saga pattern with compensating transactions and distributed tracing via OpenTelemetry.',
    technologies: ['Kotlin', 'PostgreSQL', 'gRPC', 'Kubernetes'],
    github_url: 'https://github.com/engineer/saga-orchestrator',
    live_url: '',
    status: 'in_progress',
    created_at: new Date(Date.now() - 86400000 * 5).toISOString()
  }
];

const DEFAULT_INTERVIEWS: MockInterview[] = [
  {
    id: 'int_1',
    title: 'Distributed Systems & Cache Strategy',
    role: 'Senior Staff Software Engineer',
    difficulty: 'hard',
    score: 92,
    status: 'completed',
    date: new Date(Date.now() - 86400000 * 2).toISOString(),
    questions: [
      {
        id: 'q_1',
        question: 'How do you prevent cache stampedes and dogpiling under massive concurrent traffic?',
        type: 'technical',
        idealAnswer: 'Implement distributed locking (e.g., Redlock) so only one worker thread regenerates cache keys while others receive stale-while-revalidate data.',
        userAnswer: 'I use distributed locks in Redis with probabilistic early expiration (XFetch algorithm) and stale-while-revalidate caching headers.',
        feedback: 'Superb explanation of cache invalidation mitigation and XFetch probabilistic expiration.',
        score: 95
      }
    ]
  }
];

export async function handleMockApi(endpoint: string, options: RequestInit = {}): Promise<any> {
  const method = (options.method || 'GET').toUpperCase();
  let body: any = {};
  if (options.body && typeof options.body === 'string') {
    try { body = JSON.parse(options.body); } catch { /* ignore */ }
  }

  // Small latency for realistic snappy UI feel
  await new Promise(r => setTimeout(r, 60));

  // --- AUTH SIGNUP ---
  if (endpoint === '/auth/signup' && method === 'POST') {
    const email = (body.email || 'user@careerhub.io').toLowerCase().trim();
    const fullName = (body.fullName || email.substringBefore('@') || 'Engineer').trim();
    const userId = `usr_${Date.now()}_${Math.random().toString(36).substring(2, 7)}`;
    
    const newUser: MockUser = {
      id: userId,
      email,
      fullName: fullName || 'Engineer',
      role: 'user',
      createdAt: new Date().toISOString()
    };

    const newProfile: MockProfile = {
      id: `prf_${Date.now()}`,
      user_id: userId,
      headline: 'Software Engineer',
      bio: 'Passionate software engineer building resilient, scalable systems.',
      location: 'San Francisco, CA',
      education: 'B.S. in Computer Science',
      experience_years: 4,
      target_role: 'Senior Software Engineer',
      target_industry: 'Tech / Cloud Infrastructure',
      target_salary: '$180,000 - $220,000',
      current_readiness_score: 84,
      onboarding_completed: 1,
      updated_at: new Date().toISOString()
    };

    const users = getStorage<MockUser[]>(STORAGE_KEYS.USERS, []);
    users.push(newUser);
    setStorage(STORAGE_KEYS.USERS, users);
    setStorage(STORAGE_KEYS.CURRENT_USER, newUser);
    setStorage(STORAGE_KEYS.PROFILE, newProfile);
    if (!localStorage.getItem(STORAGE_KEYS.SKILLS)) {
      setStorage(STORAGE_KEYS.SKILLS, DEFAULT_SKILLS);
    }
    if (!localStorage.getItem(STORAGE_KEYS.ROADMAP)) {
      setStorage(STORAGE_KEYS.ROADMAP, DEFAULT_ROADMAP_ITEMS);
    }

    const token = `ch_tok_${Date.now()}_${Math.random().toString(36).substring(2, 9)}`;
    return {
      user: newUser,
      token,
      profile: newProfile
    };
  }

  // --- AUTH LOGIN ---
  if (endpoint === '/auth/login' && method === 'POST') {
    const email = (body.email || '').toLowerCase().trim();
    const users = getStorage<MockUser[]>(STORAGE_KEYS.USERS, []);
    let user = users.find(u => u.email.toLowerCase() === email);

    if (!user) {
      const name = email ? email.split('@')[0] : 'Engineer';
      const cleanName = name.charAt(0).toUpperCase() + name.slice(1);
      user = {
        id: `usr_${Date.now()}_${Math.random().toString(36).substring(2, 7)}`,
        email: email || 'engineer@careerhub.io',
        fullName: cleanName,
        role: 'user',
        createdAt: new Date().toISOString()
      };
      users.push(user);
      setStorage(STORAGE_KEYS.USERS, users);
    }

    setStorage(STORAGE_KEYS.CURRENT_USER, user);

    let profile = getStorage<MockProfile | null>(STORAGE_KEYS.PROFILE, null);
    if (!profile || profile.user_id !== user.id) {
      profile = {
        id: `prf_${Date.now()}`,
        user_id: user.id,
        headline: 'Staff Software Engineer',
        bio: 'Building distributed cloud platforms and high-throughput systems.',
        location: 'San Francisco, CA',
        education: 'B.S. in Computer Science',
        experience_years: 5,
        target_role: 'Staff Software Engineer',
        target_industry: 'Cloud Infrastructure / AI',
        target_salary: '$210,000 - $260,000',
        current_readiness_score: 88,
        onboarding_completed: 1,
        updated_at: new Date().toISOString()
      };
      setStorage(STORAGE_KEYS.PROFILE, profile);
    }

    if (!localStorage.getItem(STORAGE_KEYS.SKILLS)) {
      setStorage(STORAGE_KEYS.SKILLS, DEFAULT_SKILLS);
    }
    if (!localStorage.getItem(STORAGE_KEYS.ROADMAP)) {
      setStorage(STORAGE_KEYS.ROADMAP, DEFAULT_ROADMAP_ITEMS);
    }

    const token = `ch_tok_${Date.now()}_${Math.random().toString(36).substring(2, 9)}`;
    return {
      user,
      token,
      profile
    };
  }

  // --- AUTH ME ---
  if (endpoint === '/auth/me') {
    let user = getStorage<MockUser | null>(STORAGE_KEYS.CURRENT_USER, null);
    if (!user) {
      user = {
        id: 'usr_default',
        email: 'alex.chen@careerhub.io',
        fullName: 'Alex Chen',
        role: 'user',
        createdAt: new Date().toISOString()
      };
      setStorage(STORAGE_KEYS.CURRENT_USER, user);
    }

    let profile = getStorage<MockProfile | null>(STORAGE_KEYS.PROFILE, null);
    if (!profile) {
      profile = {
        id: 'prf_default',
        user_id: user.id,
        headline: 'Staff Software Engineer',
        bio: 'Building distributed systems and real-time platforms.',
        location: 'San Francisco, CA',
        education: 'B.S. in Computer Science',
        experience_years: 5,
        target_role: 'Staff Software Engineer',
        target_industry: 'Cloud Infrastructure / AI',
        target_salary: '$220,000 - $260,000',
        current_readiness_score: 88,
        onboarding_completed: 1,
        updated_at: new Date().toISOString()
      };
      setStorage(STORAGE_KEYS.PROFILE, profile);
    }

    return { user, profile };
  }

  // --- PROFILE ---
  if (endpoint === '/profile') {
    let profile = getStorage<MockProfile | null>(STORAGE_KEYS.PROFILE, null);
    if (method === 'PUT') {
      profile = {
        ...(profile || {
          id: `prf_${Date.now()}`,
          user_id: 'usr_default',
          headline: 'Software Engineer',
          bio: '',
          location: 'San Francisco, CA',
          education: '',
          experience_years: 3,
          target_role: 'Senior Software Engineer',
          target_industry: 'Tech',
          target_salary: '',
          current_readiness_score: 82,
          onboarding_completed: 1,
          updated_at: new Date().toISOString()
        }),
        ...body,
        updated_at: new Date().toISOString()
      };
      setStorage(STORAGE_KEYS.PROFILE, profile);
      return { profile };
    }

    const skills = getStorage<MockSkill[]>(STORAGE_KEYS.SKILLS, DEFAULT_SKILLS);
    return {
      profile,
      skills,
      goals: []
    };
  }

  // --- PROFILE SKILLS ---
  if (endpoint === '/profile/skills' && method === 'POST') {
    const skills = getStorage<MockSkill[]>(STORAGE_KEYS.SKILLS, DEFAULT_SKILLS);
    const newSkill: MockSkill = {
      id: `usk_${Date.now()}`,
      skill_id: body.skillId || `sk_${Date.now()}`,
      name: body.name || body.skillId || 'New Skill',
      category: body.category || 'General',
      proficiency_level: body.proficiencyLevel || 3,
      verified: 1,
      source: 'self_reported',
      demand_weight: 0.9
    };
    skills.push(newSkill);
    setStorage(STORAGE_KEYS.SKILLS, skills);
    return { skills };
  }

  if (endpoint.startsWith('/profile/skills/') && method === 'DELETE') {
    const id = endpoint.split('/').pop();
    let skills = getStorage<MockSkill[]>(STORAGE_KEYS.SKILLS, DEFAULT_SKILLS);
    skills = skills.filter(s => s.id !== id && s.skill_id !== id);
    setStorage(STORAGE_KEYS.SKILLS, skills);
    return { skills };
  }

  // --- DASHBOARD / ANALYTICS ---
  if (endpoint === '/analytics/dashboard') {
    const roadmapItems = getStorage<MockRoadmapItem[]>(STORAGE_KEYS.ROADMAP, DEFAULT_ROADMAP_ITEMS);
    const total = roadmapItems.length;
    const completed = roadmapItems.filter(i => i.status === 'completed').length;
    const percent = total > 0 ? Math.round((completed / total) * 100) : 0;
    const skills = getStorage<MockSkill[]>(STORAGE_KEYS.SKILLS, DEFAULT_SKILLS);
    const projects = getStorage<MockProject[]>(STORAGE_KEYS.PROJECTS, DEFAULT_PROJECTS);
    const interviews = getStorage<MockInterview[]>(STORAGE_KEYS.INTERVIEWS, DEFAULT_INTERVIEWS);
    const profile = getStorage<MockProfile | null>(STORAGE_KEYS.PROFILE, null);

    return {
      analytics: {
        readinessScore: profile?.current_readiness_score || 86,
        targetRole: profile?.target_role || 'Staff Software Engineer',
        onboardingCompleted: true,
        tasks: { total, completed, percent },
        interviews: { completed: interviews.length, averageScore: 92 },
        skills: { acquired: skills.length, gapsIdentified: 2 },
        projects: { total: projects.length, completed: projects.filter(p => p.status === 'completed').length },
        resume: { overallScore: 89, impactScore: 91, brevityScore: 87, styleScore: 90 },
        recentActivity: [
          { eventName: 'profile_sync_completed', data: {}, timestamp: new Date().toISOString() },
          { eventName: 'skill_matrix_updated', data: {}, timestamp: new Date(Date.now() - 3600000).toISOString() },
          { eventName: 'roadmap_milestone_completed', data: {}, timestamp: new Date(Date.now() - 86400000).toISOString() }
        ]
      }
    };
  }

  // --- CAREER NEXT BEST ACTION ---
  if (endpoint === '/career/next-best-action') {
    return {
      action: {
        actionId: 'nba_system_design',
        title: 'Complete Distributed Lock & Caching System Design Mock',
        category: 'Interview Preparation',
        whyItMatters: 'Targeting L6 / Senior Staff roles requires verified mastery of multi-region caching topologies and distributed mutex algorithms.',
        evidence: 'System Design benchmark score is at 88% — boost to 95% with 1 technical simulation.',
        estimatedMinutes: 15,
        priority: 'high',
        targetRoute: 'interview',
        ctaText: 'Start Mock Interview'
      }
    };
  }

  // --- CAREER SKILL GAPS & ANALYSIS ---
  if (endpoint === '/career/skill-gaps') {
    return {
      skillGaps: [
        {
          skill: 'Distributed Consensus (Raft/Paxos)',
          importance: 'High',
          currentLevel: 'Intermediate',
          targetLevel: 'Advanced',
          recommendation: 'Study etcd/Raft election state machines and implement a mini leader-election simulator.'
        },
        {
          skill: 'eBPF Kernel Tracing & Performance Tuning',
          importance: 'Medium',
          currentLevel: 'Beginner',
          targetLevel: 'Intermediate',
          recommendation: 'Instrument production microservices using BCC tools and analyze socket I/O bottlenecks.'
        }
      ]
    };
  }

  if (endpoint === '/career/analyze' && method === 'POST') {
    return {
      success: true,
      analysis: {
        marketAlignment: 92,
        demandTier: 'Top 5% Highest Compensation Band ($220k - $280k)',
        strengths: ['High-Concurrency Backend Architecture', 'Kotlin / TypeScript Microservices', 'Kafka Event Streaming'],
        growthAreas: ['Multi-Region Cross-Cluster Replication', 'FinOps Cloud Cost Optimization'],
        estimatedTimeToReadyWeeks: 3
      }
    };
  }

  // --- ROADMAP ---
  if (endpoint === '/roadmap') {
    const items = getStorage<MockRoadmapItem[]>(STORAGE_KEYS.ROADMAP, DEFAULT_ROADMAP_ITEMS);
    const total = items.length;
    const completed = items.filter(i => i.status === 'completed').length;
    const progress_percent = total > 0 ? Math.round((completed / total) * 100) : 0;
    const profile = getStorage<MockProfile | null>(STORAGE_KEYS.PROFILE, null);

    return {
      roadmap: {
        id: 'rdm_active',
        title: `${profile?.target_role || 'Staff Software Engineer'} Mastery Track`,
        target_role: profile?.target_role || 'Staff Software Engineer',
        total_tasks: total,
        completed_tasks: completed,
        progress_percent,
        estimated_weeks: 6
      },
      items
    };
  }

  if (endpoint === '/roadmap/generate' && method === 'POST') {
    const items = [...DEFAULT_ROADMAP_ITEMS];
    setStorage(STORAGE_KEYS.ROADMAP, items);
    return {
      roadmap: {
        id: `rdm_${Date.now()}`,
        title: 'Accelerated Engineering Trajectory',
        target_role: 'Senior Staff Engineer',
        total_tasks: items.length,
        completed_tasks: 2,
        progress_percent: 33,
        estimated_weeks: 6
      },
      items
    };
  }

  if (endpoint.startsWith('/roadmap/items/') && method === 'PATCH') {
    const itemId = endpoint.split('/').pop();
    const items = getStorage<MockRoadmapItem[]>(STORAGE_KEYS.ROADMAP, DEFAULT_ROADMAP_ITEMS);
    const item = items.find(i => i.id === itemId);
    if (item) {
      item.status = item.status === 'completed' ? 'pending' : 'completed';
      setStorage(STORAGE_KEYS.ROADMAP, items);
    }
    const total = items.length;
    const completed = items.filter(i => i.status === 'completed').length;
    const progress_percent = total > 0 ? Math.round((completed / total) * 100) : 0;

    return {
      item,
      roadmap: {
        id: 'rdm_active',
        title: 'Engineering Mastery Track',
        total_tasks: total,
        completed_tasks: completed,
        progress_percent
      }
    };
  }

  // --- INTERVIEW ---
  if (endpoint === '/interview/history') {
    const interviews = getStorage<MockInterview[]>(STORAGE_KEYS.INTERVIEWS, DEFAULT_INTERVIEWS);
    return { interviews };
  }

  if (endpoint === '/interview/start' && method === 'POST') {
    const newInterview: MockInterview = {
      id: `int_${Date.now()}`,
      title: 'Senior Staff Distributed Architecture Simulation',
      role: 'Staff Software Engineer',
      difficulty: body.difficulty || 'hard',
      score: 0,
      status: 'in_progress',
      date: new Date().toISOString(),
      questions: [
        {
          id: 'q_1',
          question: 'How do you structure rate limiting at the API Gateway layer to prevent DDoS without inducing Redis latency spikes?',
          type: 'architecture',
          idealAnswer: 'Implement a Sliding Window Counter algorithm backed by Redis with local in-memory L1 cache (token bucket burst tolerance) to reduce roundtrips.',
        },
        {
          id: 'q_2',
          question: 'When designing a distributed transactional payment system across 5 microservices, how do you handle partial failures?',
          type: 'distributed_systems',
          idealAnswer: 'Use the Saga pattern with choreography or an orchestrator, pairing idempotent keys with compensating transactions and dead letter queues.',
        }
      ]
    };
    const interviews = getStorage<MockInterview[]>(STORAGE_KEYS.INTERVIEWS, DEFAULT_INTERVIEWS);
    interviews.unshift(newInterview);
    setStorage(STORAGE_KEYS.INTERVIEWS, interviews);
    return newInterview;
  }

  if (endpoint.startsWith('/interview/') && !endpoint.includes('/answer') && method === 'GET') {
    const id = endpoint.split('/').pop();
    const interviews = getStorage<MockInterview[]>(STORAGE_KEYS.INTERVIEWS, DEFAULT_INTERVIEWS);
    const match = interviews.find(i => i.id === id) || DEFAULT_INTERVIEWS[0];
    return match;
  }

  if (endpoint.includes('/answer') && method === 'POST') {
    return {
      feedback: 'Excellent answer. You articulated distributed locking, idempotency guarantees, and failover topologies with precision.',
      score: 94,
      isCorrect: true
    };
  }

  // --- PROJECTS ---
  if (endpoint === '/projects') {
    const projects = getStorage<MockProject[]>(STORAGE_KEYS.PROJECTS, DEFAULT_PROJECTS);
    if (method === 'POST') {
      const newProj: MockProject = {
        id: `proj_${Date.now()}`,
        title: body.title || 'New Engineering Project',
        description: body.description || '',
        technologies: Array.isArray(body.technologies) ? body.technologies : ['TypeScript', 'Kotlin'],
        github_url: body.github_url || '',
        live_url: body.live_url || '',
        status: body.status || 'in_progress',
        created_at: new Date().toISOString()
      };
      projects.unshift(newProj);
      setStorage(STORAGE_KEYS.PROJECTS, projects);
      return { project: newProj, projects };
    }
    return { projects };
  }

  if (endpoint.startsWith('/projects/') && method === 'DELETE') {
    const id = endpoint.split('/').pop();
    let projects = getStorage<MockProject[]>(STORAGE_KEYS.PROJECTS, DEFAULT_PROJECTS);
    projects = projects.filter(p => p.id !== id);
    setStorage(STORAGE_KEYS.PROJECTS, projects);
    return { success: true, projects };
  }

  // --- RESUME ---
  if (endpoint === '/resume') {
    return {
      resumes: [
        {
          id: 'res_1',
          filename: 'Alex_Chen_Staff_Engineer_Resume.pdf',
          overallScore: 89,
          impactScore: 92,
          brevityScore: 86,
          styleScore: 90,
          created_at: new Date().toISOString(),
          feedback: [
            { category: 'Impact', text: 'Quantified metrics present in 85% of bullet points.' },
            { category: 'ATS Keywords', text: 'Strong keyword match for Distributed Systems, Kafka, and Kubernetes.' }
          ]
        }
      ]
    };
  }

  if (endpoint === '/resume/upload' && method === 'POST') {
    return {
      resume: {
        id: `res_${Date.now()}`,
        filename: 'Uploaded_Resume.pdf',
        overallScore: 91,
        impactScore: 94,
        brevityScore: 88,
        styleScore: 92,
        created_at: new Date().toISOString()
      }
    };
  }

  // --- INTEGRATIONS ---
  if (endpoint === '/integrations') {
    return {
      integrations: [
        { provider: 'github', connected: true, username: 'alex-chen-dev', reposCount: 24, lastSync: new Date().toISOString() },
        { provider: 'gitlab', connected: false },
        { provider: 'linkedin', connected: false }
      ]
    };
  }

  if (endpoint === '/integrations/github/connect' && method === 'POST') {
    return {
      success: true,
      integration: { provider: 'github', connected: true, username: body.username || 'engineer-dev', reposCount: 18, lastSync: new Date().toISOString() }
    };
  }

  // --- LEARNING RESOURCES ---
  if (endpoint === '/learning/resources') {
    return {
      resources: [
        {
          id: 'lr_1',
          title: 'Designing Data-Intensive Applications (Martin Kleppmann)',
          category: 'Distributed Systems',
          type: 'book',
          progress: 80,
          url: 'https://dataintensive.net'
        },
        {
          id: 'lr_2',
          title: 'Advanced Kotlin Coroutines & Asynchronous Flow Architecture',
          category: 'Modern Android & Backend',
          type: 'course',
          progress: 100,
          url: 'https://kotlinlang.org'
        }
      ]
    };
  }

  // Generic fallback
  return { success: true };
}
