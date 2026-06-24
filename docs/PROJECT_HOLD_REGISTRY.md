# TITAN CRYPTO ORACLE NEXUS — PROJECT HOLD REGISTRY

## Current Refactor Phase

Phase:
Package-by-feature refactor foundation.

Branch:
refactor/package-by-feature-phase1

Backup branch:
golden-signal-radar-file-restore-backup

Main synced:
YES

## Global Holds

The following must not be changed without explicit user approval:

1. App identity:
   Crypto Oracle Nexus

2. Application ID:
   com.titancryptoraclenexus.app

3. Protected backup branch:
   golden-signal-radar-file-restore-backup

4. APK build rule:
   .build-outputs/app-debug.apk

5. Gemini API rule:
   Do not use GEMINI_API_KEY unless explicitly asked.

## Signal Pro Holds

Protected UI contract:

LEFT BUTTON:
HIGH CONFIDENCE
| VERIFY ENTRY |

or:

REVIEW CAREFULLY
| VERIFY ENTRY |

RIGHT BUTTON:
ACCEPT SIGNAL.

No extra outer SETUP SIGNAL button.

Internal flow must keep:
- SIGNAL SETUP
- ACCEPT SIGNAL
- CLOSE

**Expanded Institutional Matrix Hold:**
Keep the Gemini Validation Cockpit / Institutional Matrix details added between the "Tap to unfold deep institutional matrix ➔" (or ↑) and the final Accept Signal / Verify Entry buttons. Do NOT remove this long expanded portion unless explicitly and clearly requested at a later time. Even if it looks awkward or dense, it is required for research.
*Future Architecture Plan (Logged for Context):* The user plans to eventually condense this dense matrix and move it behind a new "See Details" or "Summary" button placed between Setup Signal and Accept Signal. When clicked, it will pop up a concise version of this validation cockpit. *Status: PLANNING PHASE ONLY. Do not implement this popup or remove the inline matrix until the user explicitly commands it.*

## Mission Center Holds

Preserve:
- INVALID / VALID / INACTIVE
- Auto-trading condition validation
- Invalid reason in details
- Override setup behavior
- Target / TP / SL formatting
- Spot leverage shown as 1X
- Mission log history

## Live Radar Holds

Preserve:
- Trigger section headers
- Header outline styling
- Timeframe-aware mission setup
- AI Guard Impact tiles
- AI Oracle Analytical Metadata cards

## Refactor Hold

No package-by-feature movement may change visible UI behavior.

Each phase must:
1. Move only intended files.
2. Build inside Google AI Studio.
3. Generate:
   .build-outputs/app-debug.apk
4. Commit only after build succeeds.
