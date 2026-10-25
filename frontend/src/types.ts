export type ProviderMode = 'DEMO' | 'PROVIDER';

export type PolicyAction = 'ALLOW' | 'MASK' | 'BLOCK';

export interface FindingView {
  category: string;
  ruleId: string;
  action: PolicyAction;
  explanation: string;
  maskedValue: string;
}

export interface InspectResponse {
  decision: string;
  finalAction: PolicyAction;
  blocked: boolean;
  originalPrompt: string;
  maskedPrompt: string;
  forwardedPrompt: string | null;
  mode: ProviderMode;
  providerResponse: string;
  findings: FindingView[];
  processingMs: number;
}

export interface PolicyRule {
  id: string;
  category: string;
  action: PolicyAction;
  description: string;
}

export interface AuditEntry {
  id: number;
  timestamp: string;
  finalAction: PolicyAction;
  blocked: boolean;
  mode: ProviderMode;
  processingMs: number;
  findingCount: number;
  matchedRuleIds: string[];
  matchedCategories: string[];
}

export interface AuditSummary {
  total: number;
  allowCount: number;
  maskCount: number;
  blockCount: number;
}
