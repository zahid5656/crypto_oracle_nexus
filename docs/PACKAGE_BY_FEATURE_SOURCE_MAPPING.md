# Crypto Oracle Nexus — Package-by-Feature Source Mapping

Phase: 1B — Source tree mapping only

Generated purpose: create a safe package-by-feature movement plan before any Kotlin source movement.

## Hard Rules

1. No file movement in Phase 1B.
2. No Kotlin source modification in Phase 1B.
3. No package declaration changes.
4. No import changes.
5. No UI change.
6. No business logic change.
7. No API behavior change.
8. No GEMINI_API_KEY usage.
9. No build required unless a source file is modified.

## Current-to-Target Mapping

| Current file | Current path | Responsibility | Future target | Move risk | Wrapper needed |
|---|---|---|---|---|---|
|  |  | Oracle Feed UI / home feed screen |  | Medium | YES |
|  |  | Signal Pro screen and signal action flow |  | High | YES |
|  |  | Live Radar screen and trigger sections |  | High | YES |
|  |  | Mission Center / active trade guardian UI |  | High | YES |
|  |  | Accuracy / Stats Hub screen |  | Medium | YES |
|  |  | App navigation shell / entry activity |  | High | NO |
|  |  | Shared app state and orchestration |  | Extreme | NO |
|  |  | Shared models / market data structures |  | High | NO |
|  |  | AI/Gemini service integration |  | High | NO |
|  |  | Room database root |  | High | NO |
|  |  | Signal persistence entity |  | High | NO |
|  |  | Android app manifest |  | High | NO |
|  |  | String resources |  | Medium | NO |

## Phase Order After Mapping

1. Oracle Feed structural move.
2. Signal Pro structural move.
3. Live Radar structural move.
4. Mission Center structural move.
5. Accuracy / Stats Hub structural move.
6. Model/data boundary planning.
7. Core UI extraction only after feature package movement is stable.
8. AI cost guard extraction only after structural movement is stable.

## Protected Contracts

### Signal Pro

VERIFY ENTRY must remain beside ACCEPT SIGNAL.

Correct VERIFY ENTRY text:

HIGH CONFIDENCE
| VERIFY ENTRY |

or:

REVIEW CAREFULLY
| VERIFY ENTRY |

Internal setup flow must keep:
- SIGNAL SETUP
- ACCEPT SIGNAL
- CLOSE

### Live Radar

Preserve:
- HOT SPOT TRIGGERS
- FUTURES LONG TRIGGERS
- FUTURES SHORT TRIGGERS
- timeframe behavior
- AI Guard Impact
- AI Oracle Analytical Metadata

### Mission Center

Preserve:
- INVALID / VALID / INACTIVE
- auto-trading validation
- invalid reason
- override behavior
- mission logs
- TP / SL / target formatting
- spot leverage 1X

## Bengali Translation Source

Use:



Do not auto-expand glossary terms.
If a term is omitted, keep the English/local trading word unchanged unless owner approves Bengali wording.

## Build Rule

No build is required for this mapping-only phase.

When actual source movement starts, every movement phase must build inside Google AI Studio and produce:


