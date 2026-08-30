import React, { useState } from 'react';
import { 
  Sparkles, 
  ChevronRight, 
  ShieldCheck, 
  TrendingUp, 
  Award, 
  Cpu, 
  CheckCircle2, 
  ArrowUpRight,
  Zap
} from 'lucide-react';

export const PremiumMobileUiTemplate: React.FC = () => {
  const [activeSegment, setActiveSegment] = useState<'executive' | 'intelligence' | 'portfolio'>('executive');

  return (
    <div className="w-full max-w-5xl mx-auto p-4 md:p-8 bg-[#090D16] min-h-screen text-slate-100 font-sans antialiased">
      {/* Top Header */}
      <div className="flex flex-col md:flex-row md:items-center justify-between pb-6 mb-8 border-b border-slate-800/80 gap-4">
        <div>
          <div className="flex items-center gap-2">
            <span className="px-2.5 py-0.5 rounded-full text-[10px] font-bold uppercase tracking-widest bg-amber-500/10 text-amber-400 border border-amber-500/20">
              ULTRA PREMIUM SYSTEM
            </span>
            <span className="text-xs text-slate-400">• Mobile Executive OS</span>
          </div>
          <h1 className="text-2xl font-bold tracking-tight text-white mt-1">CareerHub Executive Experience</h1>
        </div>

        {/* Segmented Switcher */}
        <div className="flex p-1 bg-slate-900/90 border border-slate-800 rounded-xl text-xs font-semibold">
          <button
            onClick={() => setActiveSegment('executive')}
            className={`px-4 py-2 rounded-lg transition-all duration-200 ${
              activeSegment === 'executive'
                ? 'bg-gradient-to-r from-indigo-600 to-blue-600 text-white shadow-lg shadow-indigo-500/20'
                : 'text-slate-400 hover:text-slate-200'
            }`}
          >
            Executive Dashboard
          </button>
          <button
            onClick={() => setActiveSegment('intelligence')}
            className={`px-4 py-2 rounded-lg transition-all duration-200 ${
              activeSegment === 'intelligence'
                ? 'bg-gradient-to-r from-indigo-600 to-blue-600 text-white shadow-lg shadow-indigo-500/20'
                : 'text-slate-400 hover:text-slate-200'
            }`}
          >
            Market Intelligence
          </button>
          <button
            onClick={() => setActiveSegment('portfolio')}
            className={`px-4 py-2 rounded-lg transition-all duration-200 ${
              activeSegment === 'portfolio'
                ? 'bg-gradient-to-r from-indigo-600 to-blue-600 text-white shadow-lg shadow-indigo-500/20'
                : 'text-slate-400 hover:text-slate-200'
            }`}
          >
            Portfolio Studio
          </button>
        </div>
      </div>

      {/* Smartphone Device Frame Mockup */}
      <div className="flex justify-center">
        <div className="w-full max-w-[390px] bg-slate-950/95 border border-slate-800/90 rounded-[44px] p-4 shadow-2xl shadow-black/80 relative overflow-hidden ring-1 ring-white/10">
          {/* Dynamic Island / Notch */}
          <div className="w-28 h-5 bg-black rounded-full mx-auto mb-4 flex items-center justify-between px-3">
            <div className="w-2 h-2 rounded-full bg-slate-800" />
            <div className="w-2 h-2 rounded-full bg-blue-500/60" />
          </div>

          {activeSegment === 'executive' && <ExecutiveScreen />}
          {activeSegment === 'intelligence' && <IntelligenceScreen />}
          {activeSegment === 'portfolio' && <PortfolioScreen />}
        </div>
      </div>
    </div>
  );
};

/* ==========================================================================
   1. EXECUTIVE DASHBOARD SCREEN (Apple / Revolut Ultra Style)
   ========================================================================== */
function ExecutiveScreen() {
  return (
    <div className="space-y-5">
      {/* Top Profile Header */}
      <div className="flex items-center justify-between">
        <div className="flex items-center gap-3">
          <div className="relative">
            <div className="w-10 h-10 rounded-full bg-gradient-to-tr from-slate-800 to-slate-700 p-0.5 shadow-md">
              <div className="w-full h-full rounded-full bg-slate-900 flex items-center justify-center font-bold text-xs text-white">
                AR
              </div>
            </div>
            <span className="absolute bottom-0 right-0 w-3 h-3 bg-emerald-500 rounded-full border-2 border-slate-950" />
          </div>
          <div>
            <div className="flex items-center gap-1.5">
              <h3 className="text-sm font-bold text-white tracking-tight">Alex Rivera</h3>
              <ShieldCheck className="w-3.5 h-3.5 text-blue-400" />
            </div>
            <p className="text-[11px] text-slate-400 font-medium">Principal AI Architect</p>
          </div>
        </div>
        <button className="p-2 bg-slate-900/80 border border-slate-800/80 hover:border-slate-700 rounded-full text-slate-300 transition-all">
          <Zap className="w-4 h-4 text-amber-400" />
        </button>
      </div>

      {/* Hero Circular Score Radial Card */}
      <div className="relative p-5 rounded-3xl bg-gradient-to-b from-slate-900/90 via-slate-900/40 to-slate-950 border border-slate-800/80 shadow-xl overflow-hidden">
        {/* Background Radial Glow */}
        <div className="absolute -top-12 -right-12 w-40 h-40 bg-blue-600/15 rounded-full blur-3xl pointer-events-none" />
        
        <div className="flex items-center justify-between">
          <div className="space-y-1">
            <span className="text-[10px] font-bold uppercase tracking-wider text-slate-400">Career Mastery Index</span>
            <div className="text-2xl font-black text-white tracking-tight flex items-baseline gap-1">
              <span>94.8</span>
              <span className="text-xs text-emerald-400 font-semibold">↑ 3.2%</span>
            </div>
            <p className="text-[11px] text-slate-400">Top 2% among Staff Engineers</p>
          </div>

          {/* SVG Radial Score Ring */}
          <div className="relative w-20 h-20 flex items-center justify-center">
            <svg className="w-full h-full transform -rotate-90" viewBox="0 0 36 36">
              <path
                className="text-slate-800"
                strokeWidth="3.5"
                stroke="currentColor"
                fill="none"
                d="M18 2.0845 a 15.9155 15.9155 0 0 1 0 31.831 a 15.9155 15.9155 0 0 1 0 -31.831"
              />
              <path
                className="text-indigo-500"
                strokeDasharray="94, 100"
                strokeWidth="3.5"
                strokeLinecap="round"
                stroke="currentColor"
                fill="none"
                d="M18 2.0845 a 15.9155 15.9155 0 0 1 0 31.831 a 15.9155 15.9155 0 0 1 0 -31.831"
              />
            </svg>
            <Award className="w-6 h-6 text-amber-400 absolute" />
          </div>
        </div>

        {/* Key Metrics Pills */}
        <div className="grid grid-cols-3 gap-2 pt-4 mt-4 border-t border-slate-800/80">
          <div className="text-center p-2 rounded-xl bg-slate-950/60 border border-slate-800/50">
            <span className="text-[10px] text-slate-400 block font-medium">ATS Match</span>
            <span className="text-xs font-bold text-white">96%</span>
          </div>
          <div className="text-center p-2 rounded-xl bg-slate-950/60 border border-slate-800/50">
            <span className="text-[10px] text-slate-400 block font-medium">Comp Goal</span>
            <span className="text-xs font-bold text-emerald-400">$320k</span>
          </div>
          <div className="text-center p-2 rounded-xl bg-slate-950/60 border border-slate-800/50">
            <span className="text-[10px] text-slate-400 block font-medium">Pipeline</span>
            <span className="text-xs font-bold text-blue-400">4 Active</span>
          </div>
        </div>
      </div>

      {/* AI Recommended Priority Card */}
      <div className="p-4 rounded-2xl bg-slate-900/60 border border-slate-800/80 space-y-3">
        <div className="flex items-center justify-between">
          <div className="flex items-center gap-2">
            <Sparkles className="w-4 h-4 text-indigo-400" />
            <span className="text-xs font-bold text-white tracking-wide">Next Strategic Action</span>
          </div>
          <span className="text-[10px] font-bold text-indigo-400 bg-indigo-950/80 px-2 py-0.5 rounded-full border border-indigo-800/50">
            +4.5 Points
          </span>
        </div>
        <p className="text-xs text-slate-300 leading-relaxed font-medium">
          Calibrate System Architecture bullets with quantitative latency benchmarks for Senior Staff roles.
        </p>
        <button className="w-full py-2.5 px-4 bg-indigo-600 hover:bg-indigo-500 active:scale-[0.98] text-white text-xs font-semibold rounded-xl transition-all shadow-md shadow-indigo-600/20 flex items-center justify-center gap-2">
          <span>Apply High-Impact Rewrite</span>
          <ChevronRight className="w-4 h-4" />
        </button>
      </div>

      {/* High Value Target Applications */}
      <div className="space-y-2.5">
        <div className="flex items-center justify-between">
          <span className="text-xs font-bold uppercase tracking-wider text-slate-400">Top Opportunity Pipeline</span>
          <span className="text-[11px] font-semibold text-indigo-400">View All</span>
        </div>

        <div className="space-y-2">
          <div className="p-3 rounded-2xl bg-slate-900/80 border border-slate-800/80 flex items-center justify-between">
            <div className="flex items-center gap-3">
              <div className="w-8 h-8 rounded-xl bg-blue-600/10 border border-blue-500/20 flex items-center justify-center font-black text-xs text-blue-400">
                L
              </div>
              <div>
                <h4 className="text-xs font-bold text-white">Linear Systems</h4>
                <p className="text-[10px] text-slate-400">Staff Infrastructure Architect</p>
              </div>
            </div>
            <span className="px-2.5 py-1 rounded-full text-[10px] font-bold bg-emerald-950/80 text-emerald-400 border border-emerald-800/60">
              $290k Offer
            </span>
          </div>

          <div className="p-3 rounded-2xl bg-slate-900/80 border border-slate-800/80 flex items-center justify-between">
            <div className="flex items-center gap-3">
              <div className="w-8 h-8 rounded-xl bg-purple-600/10 border border-purple-500/20 flex items-center justify-center font-black text-xs text-purple-400">
                S
              </div>
              <div>
                <h4 className="text-xs font-bold text-white">Stripe Core</h4>
                <p className="text-[10px] text-slate-400">Principal Systems Engineer</p>
              </div>
            </div>
            <span className="px-2.5 py-1 rounded-full text-[10px] font-bold bg-indigo-950/80 text-indigo-300 border border-indigo-800/60">
              Final Round
            </span>
          </div>
        </div>
      </div>
    </div>
  );
}

/* ==========================================================================
   2. MARKET INTELLIGENCE SCREEN
   ========================================================================== */
function IntelligenceScreen() {
  return (
    <div className="space-y-5">
      <div className="flex items-center justify-between">
        <div>
          <h3 className="text-sm font-bold text-white">Market Intelligence</h3>
          <p className="text-[11px] text-slate-400">Real-time Tech Benchmark Index</p>
        </div>
        <span className="p-2 rounded-xl bg-slate-900 border border-slate-800 text-xs font-bold text-emerald-400">
          Q3 2026 Live
        </span>
      </div>

      <div className="p-4 rounded-2xl bg-slate-900/90 border border-slate-800 space-y-3">
        <div className="flex items-center justify-between text-xs">
          <span className="text-slate-400 font-medium">95th Percentile Compensation</span>
          <span className="text-emerald-400 font-bold">$340,000 / yr</span>
        </div>
        <div className="w-full h-2 bg-slate-800 rounded-full overflow-hidden">
          <div className="h-full bg-gradient-to-r from-blue-500 to-emerald-400 rounded-full w-[88%]" />
        </div>
        <p className="text-[11px] text-slate-400">Based on 1,420 verified AI Engineer compensation packages.</p>
      </div>

      <div className="space-y-2.5">
        <span className="text-xs font-bold uppercase tracking-wider text-slate-400">In-Demand Tech Stack</span>
        <div className="grid grid-cols-2 gap-2">
          <div className="p-3 rounded-xl bg-slate-900/70 border border-slate-800 space-y-1">
            <div className="flex items-center justify-between">
              <Cpu className="w-4 h-4 text-blue-400" />
              <span className="text-[10px] font-bold text-emerald-400">+42% demand</span>
            </div>
            <h5 className="text-xs font-bold text-white">LLM Fine-Tuning</h5>
            <p className="text-[10px] text-slate-400">PyTorch, vLLM, FlashAttention</p>
          </div>
          <div className="p-3 rounded-xl bg-slate-900/70 border border-slate-800 space-y-1">
            <div className="flex items-center justify-between">
              <TrendingUp className="w-4 h-4 text-purple-400" />
              <span className="text-[10px] font-bold text-emerald-400">+38% demand</span>
            </div>
            <h5 className="text-xs font-bold text-white">Distributed Systems</h5>
            <p className="text-[10px] text-slate-400">Raft, Kafka, Kubernetes</p>
          </div>
        </div>
      </div>
    </div>
  );
}

/* ==========================================================================
   3. PORTFOLIO STUDIO SCREEN
   ========================================================================== */
function PortfolioScreen() {
  return (
    <div className="space-y-5">
      <div className="flex items-center justify-between">
        <div>
          <h3 className="text-sm font-bold text-white">Engineering Portfolio</h3>
          <p className="text-[11px] text-slate-400">Live GitHub & Project Audit</p>
        </div>
        <button className="px-3 py-1.5 bg-blue-600 text-white rounded-xl text-xs font-semibold shadow-sm">
          + Add Project
        </button>
      </div>

      <div className="p-4 rounded-2xl bg-slate-900/90 border border-slate-800 space-y-3">
        <div className="flex items-center justify-between">
          <div className="flex items-center gap-2">
            <CheckCircle2 className="w-4 h-4 text-emerald-400" />
            <h4 className="text-xs font-bold text-white">CareerPilot AI Backend</h4>
          </div>
          <ArrowUpRight className="w-4 h-4 text-slate-400" />
        </div>
        <p className="text-[11px] text-slate-300 leading-relaxed">
          Distributed career optimization engine written in TypeScript & SQLite WAL mode with 100% test coverage.
        </p>
        <div className="flex items-center gap-2 text-[10px] text-slate-400">
          <span className="px-2 py-0.5 rounded-md bg-slate-800 text-slate-300">TypeScript</span>
          <span className="px-2 py-0.5 rounded-md bg-slate-800 text-slate-300">SQLite</span>
          <span className="px-2 py-0.5 rounded-md bg-slate-800 text-slate-300">Express</span>
        </div>
      </div>
    </div>
  );
}

export default PremiumMobileUiTemplate;
