import { useEffect, useState } from 'react';
import { api } from '../../api/client';

export default function EdgeAiPage() {
  const [data, setData] = useState<any>(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');
  const [measuring, setMeasuring] = useState(false);
  const [realMeasurement, setRealMeasurement] = useState<{ clientParseMs: number; serverRoundtripMs: number } | null>(null);

  const fetchStatus = async () => {
    setError('');
    try {
      const res = await api('/edge-ai/status');
      setData(res);
    } catch (err: any) {
      setError(err?.message || 'Could not load Edge AI status.');
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchStatus();
  }, []);

  const runVerifiedMeasurement = async () => {
    setMeasuring(true);
    setError('');
    const startClient = performance.now();

    const sampleText = 'Senior Full Stack Engineer with expertise in TypeScript, React, Node.js, and PostgreSQL distributed systems.';
    sampleText.toLowerCase().split(/\s+/);
    const clientParseMs = Math.round((performance.now() - startClient) * 100) / 100;

    const startServer = performance.now();
    try {
      await api('/edge-ai/status');
      const serverRoundtripMs = Math.round(performance.now() - startServer);
      setRealMeasurement({ clientParseMs, serverRoundtripMs });
    } catch (err: any) {
      setError(err?.message || 'Latency measurement failed because server did not respond.');
    } finally {
      setMeasuring(false);
    }
  };

  if (loading) {
    return <div style={{ color: '#94a3b8', padding: 32 }}>Loading AI & System status…</div>;
  }

  return (
    <div className="animate-fade-in" style={{ display: 'flex', flexDirection: 'column', gap: 18 }}>
      <div>
        <h2 style={{ fontSize: 18, fontWeight: 700, color: '#f8fafc', margin: '0 0 4px' }}>
          System AI & Runtime Diagnostics
        </h2>
        <p style={{ color: '#94a3b8', fontSize: 13, maxWidth: 780, margin: 0 }}>
          Live AI provider configuration and empirical network latency diagnostics. Simulated hardware benchmarks remain disabled.
        </p>
      </div>

      {error && (
        <div style={{ background: 'rgba(239,68,68,0.1)', border: '1px solid rgba(239,68,68,0.3)', borderRadius: 10, padding: '12px 16px', color: '#f87171', fontSize: 13 }}>
          {error}
        </div>
      )}

      <div
        style={{
          background: 'linear-gradient(135deg, rgba(30, 41, 59, 0.9) 0%, rgba(15, 23, 42, 0.95) 100%)',
          border: '1px solid rgba(245, 158, 11, 0.4)',
          borderRadius: 12,
          padding: 18,
          display: 'flex',
          alignItems: 'flex-start',
          gap: 14,
          flexWrap: 'wrap',
        }}
      >
        <div
          style={{
            width: 34,
            height: 34,
            borderRadius: 8,
            background: 'rgba(245, 158, 11, 0.15)',
            color: '#fbbf24',
            display: 'flex',
            alignItems: 'center',
            justifyContent: 'center',
            fontSize: 17,
            flexShrink: 0,
          }}
        >
          ⚡
        </div>
        <div style={{ flex: '1 1 200px', minWidth: 0 }}>
          <div style={{ display: 'flex', alignItems: 'center', gap: 8, flexWrap: 'wrap', marginBottom: 6 }}>
            <span style={{ fontSize: 14.5, fontWeight: 700, color: '#f8fafc' }}>Hardware Validation Status</span>
            <span
              style={{
                background: 'rgba(245, 158, 11, 0.15)',
                border: '1px solid rgba(245, 158, 11, 0.4)',
                color: '#fbbf24',
                fontSize: 10.5,
                fontWeight: 700,
                padding: '2px 8px',
                borderRadius: 6,
                textTransform: 'uppercase',
              }}
            >
              Not running on NPU hardware
            </span>
          </div>
          <p style={{ color: '#cbd5e1', fontSize: 12.5, lineHeight: 1.5, margin: 0 }}>
            Hardware NPU performance metrics are reported only when running directly on Snapdragon reference hardware. Zero simulated NPU percentages or mock memory figures are shown here.
          </p>
        </div>
      </div>

      {/* Overview Grid */}
      <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fit, minmax(min(100%, 220px), 1fr))', gap: 14 }}>
        <div className="glass-card" style={{ padding: 18 }}>
          <div style={{ fontSize: 11.5, color: '#94a3b8', fontWeight: 600, textTransform: 'uppercase', marginBottom: 6 }}>
            Active AI Provider
          </div>
          <div style={{ fontSize: 17, fontWeight: 700, color: '#60a5fa', marginBottom: 4 }}>
            {data?.activeProvider?.providerName || 'Unavailable'}
          </div>
          <div style={{ fontSize: 12, color: '#94a3b8' }}>
            Mode: {data?.activeProvider?.type === 'cloud' ? 'Cloud API' : 'Deterministic / Local'}
          </div>
          {data?.activeProvider?.notes && (
            <div style={{ marginTop: 10, fontSize: 11.5, color: '#94a3b8' }}>{data.activeProvider.notes}</div>
          )}
        </div>

        <div className="glass-card" style={{ padding: 18 }}>
          <div style={{ fontSize: 11.5, color: '#94a3b8', fontWeight: 600, textTransform: 'uppercase', marginBottom: 6 }}>
            Edge Reference Model
          </div>
          <div style={{ fontSize: 16, fontWeight: 700, color: '#38bdf8', marginBottom: 4 }}>
            Llama-3.2-3B-Instruct
          </div>
          <div style={{ fontSize: 12, color: '#94a3b8' }}>Target Quantization: INT8 / W8A8</div>
          <div style={{ marginTop: 10, fontSize: 11.5, color: '#94a3b8' }}>
            Native QNN execution applies on Android/Qualcomm reference builds.
          </div>
        </div>
      </div>

      {/* Supported Workloads */}
      {data?.competitionMetadata?.supportedWorkloads?.length > 0 && (
        <div className="glass-card" style={{ padding: 20 }}>
          <h3 style={{ fontSize: 15.5, fontWeight: 600, color: '#f8fafc', margin: '0 0 12px' }}>
            Declared Runtime Workloads
          </h3>
          <div style={{ display: 'flex', flexDirection: 'column', gap: 10 }}>
            {data.competitionMetadata.supportedWorkloads.map((w: any) => (
              <div
                key={w.id}
                style={{
                  background: 'rgba(15, 23, 42, 0.6)',
                  border: '1px solid rgba(51, 65, 85, 0.5)',
                  borderRadius: 10,
                  padding: '12px 14px',
                  display: 'flex',
                  justifyContent: 'space-between',
                  alignItems: 'center',
                  flexWrap: 'wrap',
                  gap: 10,
                }}
              >
                <div style={{ flex: '1 1 200px', minWidth: 0 }}>
                  <div style={{ fontSize: 14, fontWeight: 600, color: '#e2e8f0', marginBottom: 3 }}>{w.name}</div>
                  <div style={{ fontSize: 12, color: '#94a3b8' }}>
                    Target: <span style={{ color: '#38bdf8' }}>{w.targetNpu}</span> · Format: <code>{w.format}</code>
                  </div>
                </div>
                <span
                  style={{
                    background: w.status.includes('Active') ? 'rgba(16, 185, 129, 0.15)' : 'rgba(245, 158, 11, 0.15)',
                    border: `1px solid ${w.status.includes('Active') ? 'rgba(16, 185, 129, 0.35)' : 'rgba(245, 158, 11, 0.35)'}`,
                    color: w.status.includes('Active') ? '#34d399' : '#fbbf24',
                    fontSize: 11,
                    fontWeight: 600,
                    padding: '4px 10px',
                    borderRadius: 6,
                  }}
                >
                  {w.status}
                </span>
              </div>
            ))}
          </div>
        </div>
      )}

      {/* Empirical Measurement */}
      <div className="glass-card" style={{ padding: 20 }}>
        <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: 12, flexWrap: 'wrap', gap: 10 }}>
          <div>
            <h3 style={{ fontSize: 15.5, fontWeight: 600, color: '#f8fafc', margin: '0 0 2px' }}>
              Empirical Roundtrip Latency
            </h3>
            <p style={{ color: '#94a3b8', fontSize: 12.5, margin: 0 }}>
              Measures real browser JS tokenization and live HTTP roundtrip to `/api/edge-ai/status`.
            </p>
          </div>
          <button type="button" className="btn-primary" onClick={runVerifiedMeasurement} disabled={measuring} style={{ fontSize: 12.5, padding: '8px 14px' }}>
            {measuring ? 'Measuring…' : 'Measure Latency'}
          </button>
        </div>

        {realMeasurement ? (
          <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fit, minmax(min(100%, 180px), 1fr))', gap: 12 }}>
            <div style={{ background: 'rgba(15, 23, 42, 0.7)', border: '1px solid rgba(51, 65, 85, 0.5)', borderRadius: 10, padding: '14px 16px' }}>
              <div style={{ fontSize: 12, color: '#94a3b8', marginBottom: 4 }}>Client Tokenize Time</div>
              <div style={{ fontSize: 22, fontWeight: 700, color: '#10b981' }}>{realMeasurement.clientParseMs} ms</div>
            </div>
            <div style={{ background: 'rgba(15, 23, 42, 0.7)', border: '1px solid rgba(51, 65, 85, 0.5)', borderRadius: 10, padding: '14px 16px' }}>
              <div style={{ fontSize: 12, color: '#94a3b8', marginBottom: 4 }}>API Roundtrip</div>
              <div style={{ fontSize: 22, fontWeight: 700, color: '#3b82f6' }}>{realMeasurement.serverRoundtripMs} ms</div>
            </div>
          </div>
        ) : (
          <div style={{ background: 'rgba(15, 23, 42, 0.5)', border: '1px dashed rgba(51, 65, 85, 0.6)', borderRadius: 10, padding: 16, textAlign: 'center', color: '#94a3b8', fontSize: 13 }}>
            No latency measurement run in this session yet.
          </div>
        )}
      </div>
    </div>
  );
}
