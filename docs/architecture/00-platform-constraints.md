# Platform constraints (Fase 0)

- Form factors: compact (phone, primary), medium/expanded (tablet/foldable, responsive support).
- `minSdk` 26+ (28+ if StrongBox hardware-backed keystore is required).
- `targetSdk` 37 (Android 17) — mandatory for new apps on Google Play since 2026-08-31.
- Edge-to-edge cannot be disabled when targeting API 37 — design for it from Fase 2, don't patch it later.
- Predictive Back Gestures supported.
- Background restrictions: Doze Mode, exact alarm restrictions, WorkManager quotas.
- 16 KB native memory page alignment: verify any bundled native library (including third-party
  AI SDKs like LiteRT) before each release. Deadline has moved before; latest published date is
  2027-02-01 — reverify, don't assume it's still current.
