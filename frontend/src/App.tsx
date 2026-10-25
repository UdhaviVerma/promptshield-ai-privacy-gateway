import { FormEvent, useEffect, useMemo, useState } from 'react';
import {
  deleteOldAudits,
  getAuditSummary,
  inspectPrompt,
  listAudits,
  listPolicies,
  updatePolicy
} from './api';
import type { AuditEntry, AuditSummary, InspectResponse, PolicyAction, PolicyRule, ProviderMode } from './types';

const EXAMPLES = [
  'Summarize this incident report for teammate contact alex99@example.com and call +1 (202) 555-0199.',
  'Draft release notes. My key is sk-123456789012345678901234 and ticket owner is demo@sample.org.',
  'Please optimize billing flow for test card 4111 1111 1111 1111 and share with team.'
];

function App() {
  const [prompt, setPrompt] = useState(EXAMPLES[0]);
  const [mode, setMode] = useState<ProviderMode>('DEMO');
  const [result, setResult] = useState<InspectResponse | null>(null);
  const [policies, setPolicies] = useState<PolicyRule[]>([]);
  const [audits, setAudits] = useState<AuditEntry[]>([]);
  const [summary, setSummary] = useState<AuditSummary | null>(null);
  const [isInspecting, setIsInspecting] = useState(false);
  const [loadingData, setLoadingData] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const [retentionDays, setRetentionDays] = useState(30);

  async function loadData() {
    const [policyRows, auditRows, summaryRows] = await Promise.all([
      listPolicies(),
      listAudits(),
      getAuditSummary()
    ]);
    setPolicies(policyRows);
    setAudits(auditRows);
    setSummary(summaryRows);
  }

  useEffect(() => {
    setLoadingData(true);
    loadData()
      .catch((e: Error) => setError(e.message))
      .finally(() => setLoadingData(false));
  }, []);

  async function onInspect(event: FormEvent) {
    event.preventDefault();
    setError(null);
    setIsInspecting(true);
    try {
      const response = await inspectPrompt(prompt, mode);
      setResult(response);
      await loadData();
    } catch (e) {
      const message = e instanceof Error ? e.message : 'Inspection failed.';
      setError(message);
    } finally {
      setIsInspecting(false);
    }
  }

  async function onPolicyChange(ruleId: string, action: PolicyAction) {
    setError(null);
    try {
      await updatePolicy(ruleId, action);
      setPolicies((prev) => prev.map((row) => (row.id === ruleId ? { ...row, action } : row)));
    } catch (e) {
      const message = e instanceof Error ? e.message : 'Policy update failed.';
      setError(message);
    }
  }

  async function onDeleteAudits() {
    setError(null);
    try {
      await deleteOldAudits(retentionDays);
      await loadData();
    } catch (e) {
      const message = e instanceof Error ? e.message : 'Delete failed.';
      setError(message);
    }
  }

  const decisionClass = useMemo(() => {
    if (!result) {
      return 'neutral';
    }
    if (result.finalAction === 'BLOCK') {
      return 'danger';
    }
    if (result.finalAction === 'MASK') {
      return 'warn';
    }
    return 'safe';
  }, [result]);

  return (
    <div className="page">
      <header className="hero">
        <p className="kicker">PromptShield</p>
        <h1>AI Privacy Gateway for Prompt Inspection</h1>
        <p>
          PromptShield sits between your app and an AI provider. It inspects prompt text, applies policy rules,
          and decides whether to allow, mask, or block forwarding.
        </p>
        <div className="badges">
          <span className="badge">Demo mode default</span>
          <span className="badge muted">Synthetic examples only</span>
        </div>
      </header>

      <main className="grid">
        <section className="card inspector">
          <h2>Inspect Prompt</h2>
          <form onSubmit={onInspect}>
            <label htmlFor="prompt-input">Prompt</label>
            <textarea
              id="prompt-input"
              value={prompt}
              onChange={(e) => setPrompt(e.target.value)}
              maxLength={6000}
              rows={7}
              required
            />

            <div className="row">
              <label htmlFor="mode-select">Mode</label>
              <select id="mode-select" value={mode} onChange={(e) => setMode(e.target.value as ProviderMode)}>
                <option value="DEMO">Demo provider (deterministic mock)</option>
                <option value="PROVIDER">Optional provider mode</option>
              </select>
            </div>

            <div className="example-row" aria-label="Synthetic prompt examples">
              {EXAMPLES.map((sample) => (
                <button key={sample} type="button" className="ghost" onClick={() => setPrompt(sample)}>
                  Use sample
                </button>
              ))}
            </div>

            <button className="primary" type="submit" disabled={isInspecting}>
              {isInspecting ? 'Inspecting...' : 'Inspect Prompt'}
            </button>
          </form>

          {error && <p className="error" role="alert">{error}</p>}

          {result && (
            <div className="result" data-testid="inspection-result">
              <h3>Decision</h3>
              <p className={`status ${decisionClass}`}>
                {result.finalAction} {result.blocked ? '(forwarding stopped)' : '(forwarding permitted)'}
              </p>
              <p>{result.decision}</p>
              <p className="meta">Processing time: {result.processingMs} ms</p>

              <h4>Before / After</h4>
              <div className="compare">
                <article>
                  <h5>Original</h5>
                  <pre>{result.originalPrompt}</pre>
                </article>
                <article>
                  <h5>Masked</h5>
                  <pre>{result.maskedPrompt}</pre>
                </article>
              </div>

              <h4>Forwarded Payload</h4>
              <pre>{result.forwardedPrompt ?? 'No payload forwarded due to block rule.'}</pre>

              <h4>Provider Response</h4>
              <pre>{result.providerResponse}</pre>

              <h4>Matched Rules</h4>
              {result.findings.length === 0 ? (
                <p>No configured patterns matched this prompt.</p>
              ) : (
                <ul>
                  {result.findings.map((finding, index) => (
                    <li key={`${finding.ruleId}-${index}`}>
                      <strong>{finding.category}</strong> via {finding.ruleId}: {finding.action} ({finding.maskedValue})
                    </li>
                  ))}
                </ul>
              )}
            </div>
          )}
        </section>

        <section className="card">
          <h2>Demo Policy Rules</h2>
          <p className="note">
            Policy editing is intentionally open in demo mode to show rule behavior. Use authentication in a
            production setup.
          </p>
          {loadingData ? (
            <p>Loading policies...</p>
          ) : (
            <table>
              <thead>
                <tr>
                  <th>Rule ID</th>
                  <th>Category</th>
                  <th>Action</th>
                </tr>
              </thead>
              <tbody>
                {policies.map((rule) => (
                  <tr key={rule.id}>
                    <td>{rule.id}</td>
                    <td>{rule.category}</td>
                    <td>
                      <select
                        aria-label={`action-${rule.id}`}
                        value={rule.action}
                        onChange={(e) => onPolicyChange(rule.id, e.target.value as PolicyAction)}
                      >
                        <option value="ALLOW">ALLOW</option>
                        <option value="MASK">MASK</option>
                        <option value="BLOCK">BLOCK</option>
                      </select>
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          )}
        </section>

        <section className="card">
          <h2>Audit Trail (Safe Metadata)</h2>
          <p className="note">Stored fields: timestamps, action, categories, rule IDs, processing time, mode.</p>
          {summary && (
            <div className="summary">
              <div><span>Total</span><strong>{summary.total}</strong></div>
              <div><span>Allow</span><strong>{summary.allowCount}</strong></div>
              <div><span>Mask</span><strong>{summary.maskCount}</strong></div>
              <div><span>Block</span><strong>{summary.blockCount}</strong></div>
            </div>
          )}

          <div className="row retention">
            <label htmlFor="retention">Delete entries older than (days)</label>
            <input
              id="retention"
              type="number"
              min={1}
              max={365}
              value={retentionDays}
              onChange={(e) => setRetentionDays(Number(e.target.value))}
            />
            <button type="button" onClick={onDeleteAudits}>Delete</button>
          </div>

          {audits.length === 0 ? (
            <p>No audit entries yet.</p>
          ) : (
            <div className="audit-list">
              {audits.map((entry) => (
                <article key={entry.id} className="audit-item">
                  <p>
                    <strong>{entry.finalAction}</strong> in {entry.mode} at {new Date(entry.timestamp).toLocaleString()}
                  </p>
                  <p>Rules: {entry.matchedRuleIds.join(', ') || 'none'} | Categories: {entry.matchedCategories.join(', ') || 'none'}</p>
                  <p>Processing: {entry.processingMs} ms | Findings: {entry.findingCount}</p>
                </article>
              ))}
            </div>
          )}
        </section>
      </main>

      <footer className="card footnote">
        <h2>Limitations and Privacy Notes</h2>
        <ul>
          <li>Pattern matching can have false positives and false negatives.</li>
          <li>This MVP does not claim legal compliance or guaranteed leak prevention.</li>
          <li>Audit records intentionally exclude raw prompts, detected values, and full provider responses.</li>
          <li>Demo mode uses a deterministic mock provider response, not live model output.</li>
        </ul>
        <p>
          Repository link placeholder: update after push to GitHub.
        </p>
      </footer>
    </div>
  );
}

export default App;
