# 0.6 — one intention, in-app guidance

Implementation plan, 2026-10-13:

1. Inspect the owner's keyboard integration and current OpenAI documentation. Complete.
2. Add a single intention field and submit action, automatic local chart/rule routing, first matching itinerary and saved history. Implemented; verification pending.
3. Add native ChatGPT/Codex device authorization, encrypted grant renewal, model discovery and direct completed AI interpretation. Implemented; mock and device verification pending.
4. Run Kotlin/Lab/lint/build and Android 16 offline journey; inspect screenshots, sign with the retained daily-app key and deliver an update. Pending.

## User flow

Home → write an intention → 畀我奇門指引. The app persists the question chart and bounded candidate charts and selects the first chronological match. With ChatGPT enabled, this same button sends only this intention and these charts for interpretation; the answer is saved and displayed in the app. The account flow opens OpenAI's browser page for authorization, then resumes in the app. There is no clipboard prompt handoff in this primary path.

Automatic routing is a documented product heuristic: relationship/harmony words choose the existing harmony profile; other text chooses the existing general-outing profile. It does not add a specialized classical method for every intention. Users can override routing in Advanced. Today/tonight/tomorrow keywords use the selected IANA zone; evening begins at 18:00 as a visible scheduling convention, not an auspicious boundary. Unspecified dates search the next 24 hours. Empty matches remain empty; a question chart can still be interpreted. Default 20-minute trip allowance and no extra stay are product arrangements.

AI cannot supply executable chart fields or arbitrary destinations. Its structured reply may select an existing unexpired action ID and add short interpretive text; the time, compass direction and steps shown as instructions remain calculated app fields. Free text is labelled AI interpretation, not independently verified. A completed stream is required, including sparse-final-snapshot handling; partial deltas are rejected. Invented, cross-plan or expired action IDs are rejected. Coordinates, journal entries, other intentions and credentials are absent from the AI context.

## Authentication

The owner's existing keyboard uses the native Codex device-code compatibility flow. TianJi implements the same protocol independently; no private keyboard source or credentials are copied into this public repository. Public client IDs are identifiers, not secrets. Requests identify TianJi as `TianJi/0.6.0`, originator `tianji-navigator`. The OpenAI authorization page may identify the upstream Codex client. This is not a claim of partner registration or unlimited subscription eligibility.

Fixed authentication origin: `https://auth.openai.com`; compatibility catalog/inference origin: `https://chatgpt.com/backend-api/codex`. These OAuth grants never go to the editable API URL. The separate API-key path continues using the user's explicit HTTPS provider configuration. Redirects are rejected, errors never echo response bodies, one 401 refresh/retry is allowed, 403/429 do not trigger retries, and logout/cancellation invalidate pending login results. Android Keystore encrypts one atomic grant record; it is excluded from backups and research exports. Token claims supply routing metadata only, not local entitlement verification. Models come from the authenticated catalog; inference success remains the access check.

Current official OpenAI documentation also describes a newer dynamic-agent Sign in with ChatGPT flow using issued per-agent client IDs and the public Responses API. That is a different token/endpoint contract and is not silently mixed with the owner's existing device-code integration.

Primary documentation inspected 2026-10-13:

- https://learn.chatgpt.com/docs/auth — device-code login and account settings.
- https://learn.chatgpt.com/docs/app-server — local/open-source account authentication and newer SIWC migration.
- https://developers.openai.com/siwc/token-sharing-open-source/sign-in
- https://developers.openai.com/siwc/token-sharing-open-source/models-and-inference
- https://developers.openai.com/siwc/token-sharing-open-source/preview-limitations

Real account authorization and inference in TianJi require the owner to sign in on the phone. Mock tests and a login-start probe, if run, are not a completed live account test. No app may read another Android package's private token storage.
