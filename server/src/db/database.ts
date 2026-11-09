import { DatabaseSync } from 'node:sqlite';
import fs from 'fs';
import path from 'path';
import { fileURLToPath } from 'url';
import { config } from '../config/environment.js';

const __filename = fileURLToPath(import.meta.url);
const __dirname = path.dirname(__filename);

const DEFAULT_DB_PATH = path.resolve(__dirname, '../../careerpilot.db');
const DB_PATH = path.resolve(config.databasePath || DEFAULT_DB_PATH);

export interface CareerStatement {
  run(...params: unknown[]): { changes: number; lastInsertRowid: number };
  get(...params: unknown[]): unknown;
  all(...params: unknown[]): unknown[];
}

export interface CareerDatabase {
  prepare(sql: string): CareerStatement;
  exec(sql: string): void;
  close(): void;
  transaction<T, A extends any[]>(fn: (...args: A) => T): (...args: A) => T;
}

export interface DatabaseHealth {
  path: string;
  journalMode: string;
  foreignKeysEnabled: boolean;
  integrity: 'ok' | 'failed';
}

export let db: CareerDatabase;

export function initDatabase(): CareerDatabase {
  const dbDir = path.dirname(DB_PATH);
  if (!fs.existsSync(dbDir)) {
    fs.mkdirSync(dbDir, { recursive: true });
  }

  db = new DatabaseSync(DB_PATH) as CareerDatabase;

  // node:sqlite does not provide db.transaction; add a BEGIN/COMMIT/ROLLBACK shim
  db.transaction = <T, A extends any[]>(fn: (...args: A) => T): ((...args: A) => T) => {
    return (...args: A): T => {
      db.exec('BEGIN TRANSACTION');
      try {
        const result = fn(...args);
        db.exec('COMMIT');
        return result;
      } catch (err) {
        try {
          db.exec('ROLLBACK');
        } catch {
          /* ignore rollback errors */
        }
        throw err;
      }
    };
  };

  // Enable WAL mode for high concurrent performance and Foreign Keys for relational integrity.
  db.exec('PRAGMA busy_timeout = 5000');
  db.exec('PRAGMA journal_mode = WAL');
  db.exec('PRAGMA foreign_keys = ON');
  db.exec('PRAGMA synchronous = NORMAL');

  // Read schema.sql
  const schemaPath = path.resolve(__dirname, 'schema.sql');
  const schemaSql = fs.readFileSync(schemaPath, 'utf8');
  db.exec(schemaSql);

  runMigrations();

  // Seed canonical master skills and verified learning resources
  seedCanonicalSkills();
  seedVerifiedLearningResources();

  console.log(`[Database] Initialized SQLite at ${DB_PATH} with WAL mode & Foreign Keys enabled.`);
  return db;
}

export function getDatabase(): CareerDatabase {
  if (!db) {
    return initDatabase();
  }
  return db;
}

function runMigrations(): void {
  const migrations = [
    ['001_roadmap_item_detail_fields', [
      'ALTER TABLE roadmap_items ADD COLUMN skill TEXT',
      'ALTER TABLE roadmap_items ADD COLUMN why_it_matters TEXT',
      'ALTER TABLE roadmap_items ADD COLUMN learning_objective TEXT',
      'ALTER TABLE roadmap_items ADD COLUMN practice_suggestion TEXT',
      'ALTER TABLE roadmap_items ADD COLUMN interview_relevance TEXT'
    ]]
  ] as const;

  const applied = new Set(
    (db.prepare('SELECT migration_id FROM schema_migrations').all() as Array<{ migration_id: string }>)
      .map((row) => row.migration_id)
  );

  for (const [migrationId, statements] of migrations) {
    if (applied.has(migrationId)) continue;

    const applyMigration = db.transaction(() => {
      for (const statement of statements) {
        try {
          db.exec(statement);
        } catch (error) {
          if (!String(error).toLowerCase().includes('duplicate column')) throw error;
        }
      }
      db.prepare('INSERT INTO schema_migrations (migration_id) VALUES (?)').run(migrationId);
    });
    applyMigration();
  }
}

export function getDatabasePath(): string {
  return DB_PATH;
}

export function checkDatabaseHealth(): DatabaseHealth {
  const database = getDatabase();
  const journalMode = String((database.prepare('PRAGMA journal_mode').get() as { journal_mode: string }).journal_mode);
  const foreignKeys = Number((database.prepare('PRAGMA foreign_keys').get() as { foreign_keys: number }).foreign_keys);
  const integrityResult = String((database.prepare('PRAGMA integrity_check').get() as { integrity_check: string }).integrity_check);

  return {
    path: DB_PATH,
    journalMode,
    foreignKeysEnabled: foreignKeys === 1,
    integrity: integrityResult === 'ok' ? 'ok' : 'failed'
  };
}

export function backupDatabase(destinationPath: string): string {
  const database = getDatabase();
  const destination = path.resolve(destinationPath);
  fs.mkdirSync(path.dirname(destination), { recursive: true });
  database.exec('PRAGMA wal_checkpoint(TRUNCATE)');
  fs.copyFileSync(DB_PATH, destination);
  return destination;
}

function seedCanonicalSkills() {
  const count = db.prepare('SELECT COUNT(*) as count FROM skills').get() as { count: number };
  if (count.count > 0) return;

  const canonicalSkills = [
    // Languages
    { id: 'skill_ts', name: 'TypeScript', category: 'Programming Languages', demand_weight: 1.5 },
    { id: 'skill_js', name: 'JavaScript', category: 'Programming Languages', demand_weight: 1.3 },
    { id: 'skill_py', name: 'Python', category: 'Programming Languages', demand_weight: 1.6 },
    { id: 'skill_go', name: 'Go', category: 'Programming Languages', demand_weight: 1.4 },
    { id: 'skill_java', name: 'Java', category: 'Programming Languages', demand_weight: 1.2 },
    { id: 'skill_rust', name: 'Rust', category: 'Programming Languages', demand_weight: 1.4 },
    { id: 'skill_sql', name: 'SQL', category: 'Programming Languages', demand_weight: 1.5 },
    
    // Frontend
    { id: 'skill_react', name: 'React', category: 'Frontend', demand_weight: 1.6 },
    { id: 'skill_nextjs', name: 'Next.js', category: 'Frontend', demand_weight: 1.5 },
    { id: 'skill_vue', name: 'Vue.js', category: 'Frontend', demand_weight: 1.1 },
    { id: 'skill_css', name: 'Tailwind CSS / Modern CSS', category: 'Frontend', demand_weight: 1.2 },
    { id: 'skill_web_perf', name: 'Web Performance Optimization', category: 'Frontend', demand_weight: 1.3 },

    // Backend & Distributed Systems
    { id: 'skill_node', name: 'Node.js', category: 'Backend', demand_weight: 1.5 },
    { id: 'skill_express', name: 'Express / Fastify', category: 'Backend', demand_weight: 1.2 },
    { id: 'skill_rest', name: 'RESTful API Architecture', category: 'Backend', demand_weight: 1.4 },
    { id: 'skill_graphql', name: 'GraphQL', category: 'Backend', demand_weight: 1.2 },
    { id: 'skill_microservices', name: 'Microservices & Distributed Systems', category: 'Backend', demand_weight: 1.5 },
    { id: 'skill_grpc', name: 'gRPC & Protocol Buffers', category: 'Backend', demand_weight: 1.3 },

    // Databases & Cache
    { id: 'skill_postgres', name: 'PostgreSQL', category: 'Databases', demand_weight: 1.6 },
    { id: 'skill_mongo', name: 'MongoDB', category: 'Databases', demand_weight: 1.2 },
    { id: 'skill_redis', name: 'Redis Caching & Pub/Sub', category: 'Databases', demand_weight: 1.4 },
    { id: 'skill_query_opt', name: 'Database Indexing & Query Optimization', category: 'Databases', demand_weight: 1.5 },

    // Cloud, DevOps & SRE
    { id: 'skill_docker', name: 'Docker & Containerization', category: 'DevOps & Cloud', demand_weight: 1.6 },
    { id: 'skill_k8s', name: 'Kubernetes', category: 'DevOps & Cloud', demand_weight: 1.5 },
    { id: 'skill_aws', name: 'AWS Cloud Architecture', category: 'DevOps & Cloud', demand_weight: 1.6 },
    { id: 'skill_cicd', name: 'CI/CD Pipelines (GitHub Actions)', category: 'DevOps & Cloud', demand_weight: 1.4 },
    { id: 'skill_tf', name: 'Terraform (IaC)', category: 'DevOps & Cloud', demand_weight: 1.3 },

    // AI & Machine Learning
    { id: 'skill_llm', name: 'LLM Prompting & Function Calling', category: 'AI & Machine Learning', demand_weight: 1.8 },
    { id: 'skill_rag', name: 'RAG (Retrieval Augmented Generation)', category: 'AI & Machine Learning', demand_weight: 1.7 },
    { id: 'skill_vector_db', name: 'Vector Databases (Pinecone/pgvector)', category: 'AI & Machine Learning', demand_weight: 1.5 },
    { id: 'skill_pytorch', name: 'PyTorch / ML Fundamentals', category: 'AI & Machine Learning', demand_weight: 1.4 },

    // Security & Architecture
    { id: 'skill_auth', name: 'OAuth2 / JWT & Identity Security', category: 'Security & Architecture', demand_weight: 1.5 },
    { id: 'skill_sys_design', name: 'System Design & Scalability', category: 'Security & Architecture', demand_weight: 1.7 },
    { id: 'skill_testing', name: 'Automated Testing (TDD/E2E)', category: 'Security & Architecture', demand_weight: 1.4 }
  ];

  const insertStmt = db.prepare('INSERT OR IGNORE INTO skills (id, name, category, demand_weight) VALUES (@id, @name, @category, @demand_weight)');
  const insertMany = db.transaction((skillsList) => {
    for (const skill of skillsList) insertStmt.run(skill);
  });
  insertMany(canonicalSkills);
}

function seedVerifiedLearningResources() {
  const count = db.prepare('SELECT COUNT(*) as count FROM learning_resources').get() as { count: number };
  if (count.count > 0) return;

  const verifiedResources = [
    {
      id: 'res_sys_design',
      title: 'System Design Primer & Scalability Patterns',
      provider: 'GitHub Open Resource',
      url: 'https://github.com/donnemartin/system-design-primer',
      category: 'System Architecture',
      skill_tags: 'System Design & Scalability,Microservices & Distributed Systems',
      estimated_minutes: 240,
      difficulty: 'advanced',
      is_verified: 1
    },
    {
      id: 'res_ts_deep',
      title: 'TypeScript Deep Dive & Advanced Generics',
      provider: 'TypeScript Guide',
      url: 'https://basarat.gitbook.io/typescript/',
      category: 'Programming Languages',
      skill_tags: 'TypeScript,JavaScript',
      estimated_minutes: 180,
      difficulty: 'intermediate',
      is_verified: 1
    },
    {
      id: 'res_pg_perf',
      title: 'Use The Index, Luke! - Relational Database Indexing Guide',
      provider: 'Markus Winand',
      url: 'https://use-the-index-luke.com/',
      category: 'Databases',
      skill_tags: 'SQL,PostgreSQL,Database Indexing & Query Optimization',
      estimated_minutes: 150,
      difficulty: 'advanced',
      is_verified: 1
    },
    {
      id: 'res_docker_k8s',
      title: 'Docker & Kubernetes Mastery for Production Microservices',
      provider: 'Cloud Native Computing Foundation',
      url: 'https://kubernetes.io/docs/tutorials/',
      category: 'DevOps & Cloud',
      skill_tags: 'Docker & Containerization,Kubernetes,CI/CD Pipelines (GitHub Actions)',
      estimated_minutes: 300,
      difficulty: 'intermediate',
      is_verified: 1
    },
    {
      id: 'res_rag_ai',
      title: 'Building Production RAG and Vector Search Systems',
      provider: 'Google Cloud & HuggingFace',
      url: 'https://cloud.google.com/learn/what-is-retrieval-augmented-generation',
      category: 'AI & Machine Learning',
      skill_tags: 'LLM Prompting & Function Calling,RAG (Retrieval Augmented Generation),Vector Databases (Pinecone/pgvector)',
      estimated_minutes: 200,
      difficulty: 'advanced',
      is_verified: 1
    },
    {
      id: 'res_react_patterns',
      title: 'Modern React Architecture, Hooks & Concurrency',
      provider: 'React Documentation',
      url: 'https://react.dev/learn',
      category: 'Frontend',
      skill_tags: 'React,Next.js,Web Performance Optimization',
      estimated_minutes: 120,
      difficulty: 'intermediate',
      is_verified: 1
    }
  ];

  const insertStmt = db.prepare('INSERT OR IGNORE INTO learning_resources (id, title, provider, url, category, skill_tags, estimated_minutes, difficulty, is_verified) VALUES (@id, @title, @provider, @url, @category, @skill_tags, @estimated_minutes, @difficulty, @is_verified)');
  const insertMany = db.transaction((resources) => {
    for (const res of resources) insertStmt.run(res);
  });
  insertMany(verifiedResources);
}
