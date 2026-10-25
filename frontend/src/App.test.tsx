import { render, screen } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import { beforeEach, describe, expect, it, vi } from 'vitest';
import App from './App';

vi.mock('./api', () => ({
  listPolicies: vi.fn(async () => [
    { id: 'rule-email', category: 'EMAIL', action: 'MASK', description: 'Mask email' }
  ]),
  listAudits: vi.fn(async () => []),
  getAuditSummary: vi.fn(async () => ({ total: 0, allowCount: 0, maskCount: 0, blockCount: 0 })),
  inspectPrompt: vi.fn(async () => ({
    decision: 'Request allowed with masking. Sensitive matches were redacted before forwarding.',
    finalAction: 'MASK',
    blocked: false,
    originalPrompt: 'Contact me at user@example.com',
    maskedPrompt: 'Contact me at [EMAIL_REDACTED]',
    forwardedPrompt: 'Contact me at [EMAIL_REDACTED]',
    mode: 'DEMO',
    providerResponse: 'DEMO_PROVIDER_RESPONSE: synthetic summary',
    findings: [
      {
        category: 'EMAIL',
        ruleId: 'rule-email',
        action: 'MASK',
        explanation: 'Rule requires masking before provider forwarding.',
        maskedValue: '[EMAIL_REDACTED]'
      }
    ],
    processingMs: 5
  })),
  updatePolicy: vi.fn(async (ruleId: string, action: string) => ({ id: ruleId, category: 'EMAIL', action, description: '' })),
  deleteOldAudits: vi.fn(async () => 0)
}));

describe('App', () => {
  beforeEach(() => {
    vi.clearAllMocks();
  });

  it('renders demo badge', async () => {
    render(<App />);
    expect(await screen.findByText(/Demo mode default/i)).toBeInTheDocument();
  });

  it('shows inspection result after submit', async () => {
    const user = userEvent.setup();
    render(<App />);
    const button = await screen.findByRole('button', { name: /Inspect Prompt/i });
    await user.click(button);

    expect(await screen.findByTestId('inspection-result')).toBeInTheDocument();
    expect(screen.getByText(/MASK/)).toBeInTheDocument();
    expect(screen.getByText(/DEMO_PROVIDER_RESPONSE/)).toBeInTheDocument();
  });
});
