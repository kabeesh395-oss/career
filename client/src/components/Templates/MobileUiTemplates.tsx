import React, { useState } from 'react';
import { 
  Target, 
  ChevronRight, 
  CheckCircle2, 
  Clock, 
  ArrowUpRight,
  Search,
  Sparkles
} from 'lucide-react';
import { PremiumMobileUiTemplate } from './PremiumMobileUiTemplate';

export const MobileUiTemplates: React.FC = () => {
  const [activeTab, setActiveTab] = useState<'ultra' | 'linear' | 'stripe' | 'vercel'>('ultra');

  return (
    <div className="w-full max-w-5xl mx-auto p-4 md:p-8 bg-slate-950 min-h-screen text-slate-100 font-sans antialiased">
      {/* Template Selector Bar */}
      <div className="flex flex-col md:flex-row md:items-center justify-between pb-6 mb-8 border-b border-slate-800/80 gap-4">
        <div>
          <h1 className="text-xl font-semibold tracking-tight text-slate-100">Tier-1 Mobile UI Design Templates</h1>
          <p className="text-xs text-slate-400 mt-1">Human-Centric Ultra-Premium Mobile OS Experience</p>
        </div>
        <div className="flex items-center gap-1 p-1 bg-slate-900 border border-slate-800 rounded-xl text-xs font-semibold">
          <button
            onClick={() => setActiveTab('ultra')}
            className={`px-3.5 py-1.5 rounded-lg transition-all duration-150 ${
              activeTab === 'ultra'
                ? 'bg-gradient-to-r from-amber-500 to-indigo-600 text-white shadow-md'
                : 'text-slate-400 hover:text-slate-200'
            }`}
          >
            ★ Ultra Premium OS
          </button>
          <button
            onClick={() => setActiveTab('linear')}
            className={`px-3 py-1.5 rounded-lg transition-all duration-150 ${
              activeTab === 'linear'
                ? 'bg-indigo-600 text-white shadow-xs'
                : 'text-slate-400 hover:text-slate-200'
            }`}
          >
            Linear
          </button>
          <button
            onClick={() => setActiveTab('stripe')}
            className={`px-3 py-1.5 rounded-lg transition-all duration-150 ${
              activeTab === 'stripe'
                ? 'bg-indigo-600 text-white shadow-xs'
                : 'text-slate-400 hover:text-slate-200'
            }`}
          >
            Stripe
          </button>
          <button
            onClick={() => setActiveTab('vercel')}
            className={`px-3 py-1.5 rounded-lg transition-all duration-150 ${
              activeTab === 'vercel'
                ? 'bg-indigo-600 text-white shadow-xs'
                : 'text-slate-400 hover:text-slate-200'
            }`}
          >
            Vercel
          </button>
        </div>
      </div>

      {activeTab === 'ultra' ? (
        <PremiumMobileUiTemplate />
      ) : (
        /* Mobile Frame Container (Simulated Smartphone Frame) */
        <div className="flex justify-center">
          <div className="w-full max-w-md bg-slate-900 border border-slate-800/80 rounded-2xl p-4 shadow-xl">
            {activeTab === 'linear' && <LinearMobileTemplate />}
            {activeTab === 'stripe' && <StripeMobileTemplate />}
            {activeTab === 'vercel' && <VercelMobileTemplate />}
          </div>
        </div>
      )}
    </div>
  );
};

/* ==========================================================================
   TEMPLATE 1: LINEAR-STYLE MOBILE DASHBOARD
   Clean, dark neutral, high contrast, 8pt grid, sharp borders, zero neon glow
   ========================================================================== */
function LinearMobileTemplate() {
  return (
    <div className="space-y-6">
      {/* Header */}
      <div className="flex items-center justify-between pb-4 border-b border-slate-800/80">
        <div className="flex items-center gap-3">
          <div className="w-9 h-9 rounded-lg bg-indigo-600 flex items-center justify-center text-white text-sm font-semibold shadow-xs">
            CH
          </div>
          <div>
            <h2 className="text-sm font-semibold text-slate-100 tracking-tight">CareerHub OS</h2>
            <p className="text-xs text-slate-400">Alex Rivera • Staff Engineer</p>
          </div>
        </div>
        <span className="inline-flex items-center gap-1.5 px-2.5 py-1 rounded-full bg-slate-800 border border-slate-700/60 text-xs font-medium text-slate-300">
          <span className="w-1.5 h-1.5 rounded-full bg-emerald-500"></span>
          Ready
        </span>
      </div>

      {/* Primary Hero Metric Card */}
      <div className="p-4 bg-slate-900/90 border border-slate-800 rounded-xl space-y-3">
        <div className="flex items-center justify-between text-xs text-slate-400 font-medium uppercase tracking-wider">
          <span>Readiness Score</span>
          <span className="text-indigo-400 font-semibold">78 / 100</span>
        </div>
        <div className="w-full h-2 bg-slate-800 rounded-full overflow-hidden">
          <div className="h-full bg-indigo-600 rounded-full w-[78%]" />
        </div>
        <div className="flex items-center justify-between pt-1 text-xs">
          <span className="text-slate-400">Target Role: Senior AI Architect</span>
          <span className="text-slate-200 font-medium">92% Match</span>
        </div>
      </div>

      {/* Priority Task Action Card */}
      <div className="p-4 bg-slate-800/40 border border-slate-800 rounded-xl space-y-3">
        <div className="flex items-start justify-between">
          <div>
            <span className="text-[10px] font-semibold uppercase tracking-wider text-indigo-400 bg-indigo-950/60 border border-indigo-800/40 px-2 py-0.5 rounded-md">
              Recommended Action
            </span>
            <h3 className="text-sm font-medium text-slate-100 mt-2">Update System Design Section</h3>
            <p className="text-xs text-slate-400 mt-0.5">3 resume bullet points can be quantified with metrics.</p>
          </div>
          <Target className="w-4 h-4 text-slate-400 shrink-0 mt-1" />
        </div>
        <button className="w-full py-2 px-3 bg-indigo-600 hover:bg-indigo-500 active:scale-[0.98] text-white text-xs font-medium rounded-lg transition-all duration-150 flex items-center justify-center gap-1.5">
          <span>Review Recommendations</span>
          <ChevronRight className="w-3.5 h-3.5" />
        </button>
      </div>

      {/* Stream Metrics Grid */}
      <div className="grid grid-cols-2 gap-3">
        <div className="p-3.5 bg-slate-900 border border-slate-800 rounded-xl space-y-1">
          <span className="text-xs font-medium text-slate-400">Applications</span>
          <div className="text-lg font-semibold text-slate-100">14 Active</div>
          <span className="text-[11px] text-emerald-400 font-medium">↑ 2 interviews booked</span>
        </div>
        <div className="p-3.5 bg-slate-900 border border-slate-800 rounded-xl space-y-1">
          <span className="text-xs font-medium text-slate-400">Skill Sprints</span>
          <div className="text-lg font-semibold text-slate-100">4 Complete</div>
          <span className="text-[11px] text-slate-400 font-medium">Next: Distributed Locks</span>
        </div>
      </div>

      {/* Activity Timeline List */}
      <div className="space-y-3">
        <h4 className="text-xs font-semibold uppercase tracking-wider text-slate-400">Recent Stream</h4>
        <div className="divide-y divide-slate-800/60 border border-slate-800 rounded-xl bg-slate-900/60 overflow-hidden">
          <div className="p-3 flex items-center justify-between text-xs">
            <div className="flex items-center gap-2.5">
              <CheckCircle2 className="w-4 h-4 text-emerald-400 shrink-0" />
              <div>
                <p className="font-medium text-slate-200">Applied to Stripe</p>
                <p className="text-slate-500 text-[11px]">Staff Infrastructure Engineer</p>
              </div>
            </div>
            <span className="text-slate-500 text-[11px]">2h ago</span>
          </div>
          <div className="p-3 flex items-center justify-between text-xs">
            <div className="flex items-center gap-2.5">
              <Clock className="w-4 h-4 text-amber-400 shrink-0" />
              <div>
                <p className="font-medium text-slate-200">System Design Interview</p>
                <p className="text-slate-500 text-[11px]">Scheduled with Linear</p>
              </div>
            </div>
            <span className="text-slate-500 text-[11px]">Tomorrow</span>
          </div>
        </div>
      </div>
    </div>
  );
}

/* ==========================================================================
   TEMPLATE 2: STRIPE-STYLE CAREER ANALYTICS & RESUME AUDIT
   Precision typography, subtle borders, high contrast data tables
   ========================================================================== */
function StripeMobileTemplate() {
  return (
    <div className="space-y-6">
      {/* Header */}
      <div className="flex items-center justify-between pb-4 border-b border-slate-800/80">
        <div>
          <h2 className="text-base font-semibold text-slate-100 tracking-tight">Resume & Skill Audit</h2>
          <p className="text-xs text-slate-400">ATS Keyword & Impact Benchmark</p>
        </div>
        <span className="px-2.5 py-1 bg-indigo-950/80 border border-indigo-800/60 text-indigo-300 font-semibold text-xs rounded-md">
          v2.4 Score
        </span>
      </div>

      {/* Segmented Control */}
      <div className="flex p-1 bg-slate-950 border border-slate-800 rounded-lg text-xs font-medium text-slate-400">
        <button className="flex-1 py-1.5 rounded-md bg-slate-800 text-slate-100 text-center font-medium shadow-xs">
          ATS Score
        </button>
        <button className="flex-1 py-1.5 rounded-md text-center hover:text-slate-200">
          Skills Gap
        </button>
        <button className="flex-1 py-1.5 rounded-md text-center hover:text-slate-200">
          Market Salary
        </button>
      </div>

      {/* Score Summary Box */}
      <div className="p-4 bg-slate-900 border border-slate-800 rounded-xl space-y-4">
        <div className="flex items-center justify-between">
          <div>
            <span className="text-2xl font-bold tracking-tight text-slate-100">84 / 100</span>
            <p className="text-xs text-emerald-400 font-medium mt-0.5">Top 8% candidate profile</p>
          </div>
          <div className="w-12 h-12 rounded-xl bg-indigo-600/10 border border-indigo-500/20 flex items-center justify-center text-indigo-400 font-bold text-base">
            A+
          </div>
        </div>

        {/* Dimension Breakdown Bars */}
        <div className="space-y-2.5 pt-2 border-t border-slate-800/80 text-xs">
          <div className="space-y-1">
            <div className="flex justify-between text-slate-300">
              <span>Technical Keywords</span>
              <span className="font-medium text-slate-100">90%</span>
            </div>
            <div className="h-1.5 bg-slate-800 rounded-full overflow-hidden">
              <div className="h-full bg-indigo-600 rounded-full w-[90%]" />
            </div>
          </div>
          <div className="space-y-1">
            <div className="flex justify-between text-slate-300">
              <span>Quantified Metric Bullet Points</span>
              <span className="font-medium text-slate-100">75%</span>
            </div>
            <div className="h-1.5 bg-slate-800 rounded-full overflow-hidden">
              <div className="h-full bg-indigo-600 rounded-full w-[75%]" />
            </div>
          </div>
          <div className="space-y-1">
            <div className="flex justify-between text-slate-300">
              <span>Formatting & ATS Parsability</span>
              <span className="font-medium text-slate-100">95%</span>
            </div>
            <div className="h-1.5 bg-slate-800 rounded-full overflow-hidden">
              <div className="h-full bg-emerald-500 rounded-full w-[95%]" />
            </div>
          </div>
        </div>
      </div>

      {/* Key Detected Skills Badges */}
      <div className="space-y-2.5">
        <span className="text-xs font-semibold uppercase tracking-wider text-slate-400">Verified Technical Skills</span>
        <div className="flex flex-wrap gap-1.5">
          {['Distributed Systems', 'TypeScript', 'Node.js', 'Go', 'Kubernetes', 'PostgreSQL', 'System Design'].map((skill) => (
            <span key={skill} className="px-2.5 py-1 bg-slate-800/80 border border-slate-700/60 rounded-md text-xs font-medium text-slate-300">
              {skill}
            </span>
          ))}
        </div>
      </div>

      {/* Action Button */}
      <button className="w-full py-2.5 bg-indigo-600 hover:bg-indigo-500 active:scale-[0.98] text-white text-xs font-medium rounded-lg transition-all duration-150 shadow-xs flex items-center justify-center gap-2">
        <Sparkles className="w-4 h-4" />
        <span>Export Optimized Resume PDF</span>
      </button>
    </div>
  );
}

/* ==========================================================================
   TEMPLATE 3: VERCEL-STYLE JOB PIPELINE TRACKER
   Monochrome cards, sharp status pills, clean list hierarchy
   ========================================================================== */
function VercelMobileTemplate() {
  const applications = [
    { company: 'Linear', role: 'Staff Frontend Engineer', status: 'Interview', salary: '$220k - $260k', date: '2d ago', statusColor: 'bg-indigo-950 text-indigo-300 border-indigo-800/50' },
    { company: 'Stripe', role: 'Infrastructure Architect', status: 'Offer Received', salary: '$240k - $290k', date: '5d ago', statusColor: 'bg-emerald-950 text-emerald-300 border-emerald-800/50' },
    { company: 'Vercel', role: 'Senior Platform Engineer', status: 'Applied', salary: '$210k - $250k', date: '1w ago', statusColor: 'bg-slate-800 text-slate-300 border-slate-700/50' },
  ];

  return (
    <div className="space-y-5">
      {/* Header & Search */}
      <div className="space-y-3 pb-3 border-b border-slate-800/80">
        <div className="flex items-center justify-between">
          <h2 className="text-base font-semibold text-slate-100 tracking-tight">Job Applications</h2>
          <span className="text-xs text-slate-400 font-medium">3 Active</span>
        </div>

        {/* Search Bar */}
        <div className="relative">
          <Search className="w-3.5 h-3.5 text-slate-400 absolute left-3 top-2.5" />
          <input
            type="text"
            placeholder="Search company or role..."
            className="w-full pl-9 pr-3 py-1.5 bg-slate-950 border border-slate-800 rounded-lg text-xs text-slate-200 placeholder-slate-500 focus:outline-none focus:border-indigo-500 transition-all"
            readOnly
          />
        </div>
      </div>

      {/* Filter Chips */}
      <div className="flex items-center gap-1.5 overflow-x-auto text-xs font-medium pb-1 scrollbar-none">
        <span className="px-2.5 py-1 rounded-md bg-indigo-600 text-white shrink-0">All (3)</span>
        <span className="px-2.5 py-1 rounded-md bg-slate-800 text-slate-300 shrink-0">Interviewing</span>
        <span className="px-2.5 py-1 rounded-md bg-slate-800 text-slate-300 shrink-0">Offers</span>
        <span className="px-2.5 py-1 rounded-md bg-slate-800 text-slate-300 shrink-0">Archived</span>
      </div>

      {/* Application Cards List */}
      <div className="space-y-2.5">
        {applications.map((app) => (
          <div
            key={app.company}
            className="p-3.5 bg-slate-900 border border-slate-800 hover:border-slate-700 rounded-xl space-y-2.5 transition-all cursor-pointer"
          >
            <div className="flex items-start justify-between">
              <div>
                <h3 className="text-xs font-semibold text-slate-100 flex items-center gap-1.5">
                  {app.company}
                  <ArrowUpRight className="w-3 h-3 text-slate-500" />
                </h3>
                <p className="text-xs text-slate-400 font-medium">{app.role}</p>
              </div>
              <span className={`text-[10px] font-semibold px-2 py-0.5 rounded-full border ${app.statusColor}`}>
                {app.status}
              </span>
            </div>

            <div className="flex items-center justify-between pt-2 border-t border-slate-800/60 text-[11px] text-slate-400">
              <span>{app.salary}</span>
              <span>{app.date}</span>
            </div>
          </div>
        ))}
      </div>

      {/* Add New Application Button */}
      <button className="w-full py-2.5 border border-dashed border-slate-800 hover:border-slate-700 hover:bg-slate-800/30 text-slate-400 hover:text-slate-200 text-xs font-medium rounded-xl transition-all duration-150 flex items-center justify-center gap-2">
        <span>+ Add Job Application</span>
      </button>
    </div>
  );
}

export default MobileUiTemplates;
