import type { AuditEntry, AuditSummary, InspectResponse, PolicyAction, PolicyRule, ProviderMode } from './types';

const FALLBACK_API_BASE_URL = 'http://localhost:8080';
const LEGACY_RENDER_BACKEND_URL = 'https://promptshield-backend.onrender.com';
const LIVE_RENDER_BACKEND_URL = 'https://promptshield-backend-bxjc.onrender.com';

function resolveApiBaseUrl(): string {
  const configured = import.meta.env.VITE_API_BASE_URL;
  if (!configured) {
    return FALLBACK_API_BASE_URL;
  }

  const trimmed = configured.trim();
  if (trimmed === LEGACY_RENDER_BACKEND_URL) {
    return LIVE_RENDER_BACKEND_URL;
  }

  return trimmed;
}

const API_BASE_URL = resolveApiBaseUrl();

async function parseJson<T>(response: Response): Promise<T> {
  if (!response.ok) {
    const payload = await response.json().catch(() => ({}));
    const message = (payload as { message?: string }).message || `Request failed with status ${response.status}`;
    throw new Error(message);
  }
  return response.json() as Promise<T>;
}

export async function inspectPrompt(prompt: string, mode: ProviderMode): Promise<InspectResponse> {
  const response = await fetch(`${API_BASE_URL}/api/inspect`, {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify({ prompt, mode })
  });
  return parseJson<InspectResponse>(response);
}

export async function listPolicies(): Promise<PolicyRule[]> {
  return parseJson<PolicyRule[]>(await fetch(`${API_BASE_URL}/api/policies`));
}

export async function updatePolicy(ruleId: string, action: PolicyAction): Promise<PolicyRule> {
  const response = await fetch(`${API_BASE_URL}/api/policies/${ruleId}`, {
    method: 'PUT',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify({ action })
  });
  return parseJson<PolicyRule>(response);
}

export async function listAudits(): Promise<AuditEntry[]> {
  return parseJson<AuditEntry[]>(await fetch(`${API_BASE_URL}/api/audits`));
}

export async function getAuditSummary(): Promise<AuditSummary> {
  return parseJson<AuditSummary>(await fetch(`${API_BASE_URL}/api/audits/summary`));
}

export async function deleteOldAudits(days: number): Promise<number> {
  const response = await fetch(`${API_BASE_URL}/api/audits?olderThanDays=${days}`, { method: 'DELETE' });
  const payload = await parseJson<{ deleted: number }>(response);
  return payload.deleted;
}
