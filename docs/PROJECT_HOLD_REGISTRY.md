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
