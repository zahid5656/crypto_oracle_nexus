# TITAN CRYPTO ORACLE NEXUS — AGENT GUARDRAILS

## Project Identity

TITAN CRYPTO ORACLE NEXUS is an institutional-grade AI-powered crypto trading intelligence platform.

It is not a simple signal app.

AI assists, explains, validates, monitors, and recommends.
Final trading decisions always belong to the user.
No assistant or agent may guarantee profit or market outcome.

## Refactor Mode

Current task type:
Package-by-layer to package-by-feature refactor.

Current branch:
refactor/package-by-feature-phase1

Protected backup branch:
golden-signal-radar-file-restore-backup

Do not modify or delete the backup branch.

## Mandatory Build Rule

Always build APK inside Google AI Studio.

Required APK output path:

.build-outputs/app-debug.apk

A refactor phase is not accepted unless this APK is generated successfully.

## Gemini API Rule

Do not use GEMINI_API_KEY unless explicitly asked.

Do not add GEMINI_API_KEY to source code.
Do not add GEMINI_API_KEY to Gradle files.
Do not add GEMINI_API_KEY to documentation as a real key.
Do not force AI Oracle mode during refactor.

Use local simulator / existing mode unless the user explicitly asks to enable Gemini API.

## Protected Signal Pro UI Contract

Signal Pro action area must show exactly two side-by-side actions.

LEFT BUTTON:
Animated gradient VERIFY ENTRY button.

Text must be vertically split:

HIGH CONFIDENCE
| VERIFY ENTRY |

or:

REVIEW CAREFULLY
| VERIFY ENTRY |

RIGHT BUTTON:
ACCEPT SIGNAL.

Rules:
- No extra outer SETUP SIGNAL / SIGNAL SETUP button.
- VERIFY ENTRY opens existing verify/setup decision flow.
- Internal setup flow keeps:
  - SIGNAL SETUP
  - ACCEPT SIGNAL
  - CLOSE
- Do not change button position.
- Do not change click behavior.
- Do not change ACCEPT SIGNAL behavior.
- Do not change animated gradient style.

## Protected Mission Center Contract

Preserve:
- INVALID / VALID / INACTIVE auto-trading condition state.
- Auto-trading override behavior.
- Details invalid reason.
- Target / TP / SL formatting.
- Leverage 1X display for spot.
- Mission logs.

Do not add real trading execution.

## Protected Live Radar Contract

Preserve:
- HOT SPOT TRIGGERS (TOP 3)
- FUTURES LONG TRIGGERS (TOP 3)
- FUTURES SHORT TRIGGERS (TOP 3)
- Header outline styling.
- Timeframe behavior.
- AI Guard Impact tile logic.
- AI Oracle Analytical Metadata behavior.

## Refactor Rules

1. Do not redesign UI during refactor.
2. Do not remove working features.
3. Do not modify unrelated files.
4. Do not rewrite business logic unless needed for compile safety.
5. Do not move more than one feature per phase.
6. Build after every phase.
7. Commit only after build succeeds.
8. Never commit APK/build outputs unless explicitly asked.
9. Never use git add -A.
10. Stage only intended files.

## Stop Rules

If build fails:
- Stop refactor.
- Repair compile only.
- Do not move more files.

If UI behavior changes:
- Stop refactor.
- Restore behavior.
- Do not redesign.

If Gemini API is introduced accidentally:
- Stop.
- Remove key usage.
- Continue with local simulator only.

If APK is not generated at .build-outputs/app-debug.apk:
- Build is not accepted.
