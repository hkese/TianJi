# 0.6.1 login recovery

The owner's phone showed the official browser authorization success page, then TianJi returned to signed out with a generic connection error. The phone's underlying exception and exact failed HTTP stage are not available; the screenshot alone cannot establish DNS, TLS, timeout or exchange failure.

Confirmed code defects in 0.6.0:

- A single polling or exchange IOException ended the job and cleared the displayed device code.
- An unfinished authorization was not persisted, so process recreation could not resume it.
- All IO errors used a generic AI-message path that incorrectly mentioned retained local guidance during account login.

Implementation plan and changes:

1. Retain the same device authorization through declared temporary network/server failures, with bounded backoff and the original expiry. Terminal denial, expiry and non-retryable errors terminate explicitly. Implemented.
2. Persist pending device and exchange-stage values in a separate Android Keystore-encrypted record. Resume automatically after model/process reconstruction. Cancel/logout/delete invalidate pending writes and clear recovery. Saved grants remain separate from model-catalog success. Implemented.
3. Open the browser only for a new login request, not every restored device-code state. Show safe Chinese stage-specific progress/errors without tokens or raw service bodies. Implemented.
4. Initial CI passed build/88 Kotlin/21 Lab checks, but the new native reconstruction test exposed NetworkOnMainThreadException while TLS close-notify ran in ChatGptClient.close. Socket cancellation and pool cleanup now run on a background executor; logical cancellation is immediate. Final CI 38779648069 passed 88 Kotlin, 21 Lab, 7 data and 3 app device tests, lint/build and Ruff. The same native regression passes with no StrictMode relaxation. The retained-signer update APK is saved for delivery.

Authorization-code exchange may be ambiguous after a lost response: if the service has already consumed the code and returns invalid grant, the app reports that stage and requires a fresh login. It cannot recreate lost service credentials.

No real user credentials, phone screenshots or keyboard source are published. Tests use synthetic data and local HTTPS test servers. Recovery data is absent from app backups/research exports. The original device-code compatibility protocol and fixed OpenAI origins remain as documented in INTENTION_0_6.md.

The current keyboard request fields and callback URI were rechecked on 2026-10-15: TianJi uses the same device-code protocol. The native cleanup failure and resilience changes are TianJi implementation issues; no user credentials or private keyboard implementation are imported.
