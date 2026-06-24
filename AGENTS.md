AGENTS.md — GEMINI ONLY ROOT GUARDRAILS FOR TITAN ORACLE

Document Authority

This "AGENTS.md" is for Google AI Studio / Gemini only.

Gemini must treat this file as the root behavior contract when working inside this repository.

This file does not replace the project roadmap. It enforces how Gemini must operate while following the roadmap.

---

1. Project Identity

Project name:

TITAN CRYPTO ORACLE NEXUS

App name:

TITAN ORACLE

Category:

AI-powered crypto trading intelligence platform

TITAN ORACLE is an institutional-grade crypto intelligence, signal validation, live radar, mission-control, active trade monitoring, risk-validation, and guarded automation ecosystem.

It is not a casual crypto signal app.

AI assists, validates, explains, monitors, recommends, and may later operate only under explicitly approved guardrails.

Final trading decisions and execution authority belong to the user unless a future controlled mode explicitly grants limited authority.

No agent may claim guaranteed profit, guaranteed accuracy, or guaranteed market outcome.

---

2. Mandatory Files To Read Before Work

Before modifying code, Gemini must read these files when they exist:

1. "PROJECT_MASTER_DOCUMENTATION_&_ROADMAP.md"
2. "AGENTS.md"
3. "docs/PROJECT_HOLD_REGISTRY.md"
4. "docs/GOOGLE_AI_STUDIO_NEW_BUILD_RULES.md"
5. "docs/GOOGLE_AI_STUDIO_BUILD_RULES.md"
6. "docs/bengali_translation_glossary.md"
7. "docs/PACKAGE_BY_FEATURE_SOURCE_MAPPING.md"

If any required file is missing, Gemini must report it.

Do not contradict "PROJECT_MASTER_DOCUMENTATION_&_ROADMAP.md".

Do not delete, rename, rewrite, or replace project roadmap documents unless explicitly requested by the owner.

---

3. Owner Context

The project owner has background in:

- Executive IT
- full-stack web development
- networking
- server security
- firewall/security operations
- Android custom kernel/device maintenance
- technical community administration
- practical engineering =

Do not treat the owner as a beginner.

The owner may use AI to reduce memory burden, continuity errors, repetitive manual work, and execution mistakes.

Treat automation scripts, copy-paste prompts, and detailed guardrails as reliability tools, not as lack of knowledge.

Maintain project state clearly.

Never force the owner to re-explain previously locked requirements.

---

4. Continuity Rule

Never restart from zero.

Assume previous approved work remains active unless the user explicitly changes direction.

Active continuity includes:

- Oracle Feed
- Signal Pro
- Live Radar
- Mission Center
- Active Trades
- AI Trade Guardian
- Stats Hub / Accuracy Center
- Consensus Engine
- Binance Sync
- UX reference design
- Bengali / English system
- AI Copilot
- Mission Intelligence Packet
- Signal validation workflow
- Setup / Accept Signal workflow
- package-by-feature structure
- API cost reduction strategy
- real-time latency optimization

Do not reapply old prompts blindly.

Do not assume an old prompt is more correct than the current recovered branch.

Current branch source code + latest screenshots/PDFs are the source of truth for visual/UI recovery tasks.

---

5. Priority Order

Follow this order:

1. Accuracy
2. Stability
3. Scalability
4. Performance
5. User Experience
6. Maintainability
7. API Cost Efficiency

Never sacrifice trading accuracy for visual polish.

Never sacrifice stability for feature speed.

Never sacrifice protected working UI for new feature insertion.

---

6. Global Execution Rules

Gemini must follow these rules for every task:

- Do not simplify features unless explicitly instructed.
- Do not remove working functionality.
- Do not replace working UI unless explicitly instructed.
- New features must be additive.
- Preserve backward compatibility.
- Modify only requested files.
- Do not modify unrelated screens.
- Do not invent requirements.
- Do not casually patch.
- Do not guess.
- Do not perform broad refactors unless explicitly requested.
- Do not move code across modules unless the task explicitly asks.
- If a requirement is unclear, stop and report.
- If exact insertion point is unclear, stop and report.
- If implementation would damage an existing module, stop and report.
- If source and screenshot disagree, stop and report.

---

7. Google AI Studio Workflow Rules

AI Studio is not assumed to be a full Git terminal.

Do not assume AI Studio can:

- push to GitHub
- create pull requests
- merge branches
- sync remote branches
- run arbitrary Git operations
- access a complete terminal environment

In AI Studio, focus on:

- code modification
- diff review
- compile repair
- build verification
- APK generation when supported
- visual/source validation
- final implementation report

Do not provide GitHub push/merge/PR instructions unless the user explicitly asks.

---

8. Build Rule

For any Kotlin/source change, build must be run.

If build fails:

1. Stop feature work.
2. Repair compile only.
3. Do not add extra features.
4. Report exact file, line, and error reason.
5. Do not continue broad modifications.

A patch is not accepted unless build succeeds or the user explicitly accepts a partial compile-fix report.

---

9. Gemini API / Secret Rule

Do not use "GEMINI_API_KEY" unless explicitly asked.

Do not add "GEMINI_API_KEY" to:

- Kotlin files
- Gradle files
- local.properties
- CI files
- documentation as a real key
- generated files
- logs
- hardcoded constants

Never expose secrets.

Never force AI Oracle mode during UI, refactor, or mockup phases.

Use local simulator, mock data, static state, or existing mode unless the user explicitly asks to enable live Gemini API.

---

10. Protected Files And Areas

Do not modify unless explicitly requested:

- "GeminiService.kt"
- "CryptoViewModel.kt"
- "AppDatabase.kt"
- "SignalEntity.kt"
- "CryptoData.kt"
- DAO files
- Repository files
- Room/database schema
- Binance/API/network logic
- real trading execution logic
- Gradle files
- "AndroidManifest.xml"
- signing files
- launcher icons
- drawable logo resources
- API key files
- CI secret handling files

If a protected file appears in the diff accidentally:

1. Stop.
2. Report it.
3. Do not continue.
4. Do not claim success.

---

11. Protected UI Contracts

11.1 Signal Pro Contract

Signal Pro is a protected core module.

Do not rebuild Signal Pro.

Do not replace the current recovered Signal Pro dashboard.

Do not reorder the main screen.

Do not remove existing cards, toggles, rows, action buttons, expanded details, or bottom navigation.

Preserve:

- "PREDICTIONS"
- "Oracle Dashboard"
- Spot / Futures selector
- Buy Long / Sell Short selector
- timeframe row
- "Oracle Pick of the Moment"
- "All Scanned Exchange Assets"
- asset cards
- probability chips
- predicted/current/expected/growth rows
- expanded deep institutional matrix
- Verify Entry / Accept Signal area
- current gradient/card visual identity
- bottom navigation

Signal Pro action area contract:

LEFT BUTTON:

- animated gradient "VERIFY ENTRY"
- text may include:
  - "HIGH CONFIDENCE"
  - "REVIEW CAREFULLY"
  - "| VERIFY ENTRY |"

RIGHT BUTTON:

- "ACCEPT SIGNAL"

Rules:

- Do not add an extra outer "SETUP SIGNAL" button beside these two buttons unless explicitly requested.
- "VERIFY ENTRY" opens existing verify/setup decision flow.
- Internal setup flow may contain:
  - "SIGNAL SETUP"
  - "ACCEPT SIGNAL"
  - "CLOSE"
- Do not change button position.
- Do not change click behavior.
- Do not change animated gradient style.
- New detailed information must be inserted inside existing expanded matrix only.

If adding information to Signal Pro:

- add compact rows/chips/cards inside the existing expanded flow
- preserve current order unless task explicitly asks rearrangement
- use exact insertion point
- if exact insertion point is unclear, stop and report

11.2 Oracle Feed Contract

Oracle Feed is a protected market intelligence module.

Preserve:

- terminal identity if present
- internal tabs if present
- Oracle Reference Index
- Market Dashboard
- Oracle Analytical Insights
- Market Feed & Intelligence
- FX Open-Market Index
- Market Regime / Global Snapshot
- Top Movers / feed cards
- Alerts & Watchlist
- System States

Do not turn Oracle Feed into a random long uncontrolled page if current implementation uses tabs.

Do not replace legacy terminal sections with synthetic-only index names.

Do not remove "45M" timeframe if present.

11.3 Live Radar Contract

Live Radar is a protected tactical radar module.

Preserve:

- Hot Spot triggers
- Futures Long triggers
- Futures Short triggers
- Spot/Futures separation
- timeframe behavior
- header outline style
- AI Guard Impact tile logic
- AI Oracle Analytical Metadata
- Radar-to-Mission handoff behavior if present

Do not add AI Auto Pilot here until explicitly requested.

11.4 Mission Center Contract

Mission Center is a protected mission supervision module.

Preserve:

- Running / History tabs if present
- INVALID / VALID / INACTIVE auto-trading condition states
- auto-trading override behavior
- details invalid reason
- Target / TP / SL formatting
- leverage display
- mission logs
- active mission state cards

Do not add real trading execution.

Do not expand Mission Center before core intelligence stability unless explicitly requested.

---

12. Semantic Rules

Do not mix these terms:

Risk Score

Meaning: danger level.

Values:

- LOW
- MEDIUM
- HIGH
- EXTREME

Bangla:

- "ঝুঁকির পরিমান"

Execution Readiness

Meaning: whether the trade can be executed cleanly now.

Inputs:

- spread
- liquidity
- slippage
- latency
- data freshness
- orderbook/depth
- API availability

Values:

- OPTIMAL
- ACCEPTABLE
- DEGRADED
- POOR

Consensus Bias

Meaning: AI/engine conviction posture, not danger.

Values:

- CONSERVATIVE
- MODERATE
- AGGRESSIVE

Do not show "Risk Profile = Aggressive" when meaning is consensus posture.

Use:

- "Consensus Bias = AGGRESSIVE"

Position Allocation

Meaning: capital sizing recommendation.

Typical values:

- Conservative = 2.0% cap
- Moderate = 5.0% cap
- Aggressive = 10.0% max

This is not a trading command.

---

13. Math Validation Rules

Signal Pro and all trade validation surfaces must respect:

Long / Spot

- Stop Loss < Entry
- Target > Entry
- Target > Entry > Stop Loss

Short

- Stop Loss > Entry
- Target < Entry
- Stop Loss > Entry > Target

Risk/reward formula:

- Risk = abs(Entry - StopLoss)
- Reward = abs(Target - Entry)
- RR = Reward / Risk

If invalid, mark as invalid/review state.

Do not render invalid trade math as normal valid signal.

---

14. Color And UI Rule

Approved semantic colors:

- TitanGreen / success / favorable
- TitanRed / danger / loss / invalid
- TitanGold / warning / aggressive posture
- TitanOrange / high warning
- TitanCyan / conservative / tech neutral

Do not globally change colors without owner approval.

Do not introduce neon/faded unreadable UI.

Do not use red for "Consensus Bias = Aggressive".

Use tabular numerals where possible for:

- price
- percent
- timer
- score
- ROI
- TP/SL
- spread
- slippage
- latency
- volume
- rank

Avoid layout-jumping animation in live market rows.

---

15. Bengali / English Rule

Do not create broad Bengali translation patches unless explicitly requested.

Keep crypto/trading primitives in English:

- SPOT
- FUTURES
- LONG
- SHORT
- BUY LONG
- SELL SHORT
- ENTRY
- CURRENT
- EXPECTED
- PREDICTED
- TARGET
- STOP LOSS
- TAKE PROFIT
- TP
- SL
- ROI
- PnL
- SCORE
- RANK
- SPREAD
- SLIPPAGE
- LEVERAGE
- BULLISH
- BEARISH
- AI
- ORACLE
- RADAR
- SIGNAL
- SCAN

Approved Bangla terms:

- Risk Score = ঝুঁকির পরিমান
- Consensus Confidence = সম্মিলিত আস্থা
- Direction = দিকনির্দেশনা
- Probability = সম্ভাবনা
- Probability Score = সম্ভাব্যতা স্কোর
- Current Market Regime = অন-চেইন বাজার পরিস্থিতি
- Multi-AI Consensus Engines = মাল্টি-এআই মডেলের ঐক্যমত ইঞ্জিন

No broad localization rewrite now.

---

16. Patch Scope Rules

Every patch must define:

1. exact target module
2. exact target file(s)
3. exact insertion point
4. files not to touch
5. protected UI areas
6. stop condition
7. build requirement
8. final report requirement

If the user provides screenshots or PDF:

- use screenshots/PDF as source-of-truth for UI placement
- do not infer a different design
- if source does not match screenshots, stop and report

---

17. Stop Rules

Stop and report without modifying if:

- exact insertion point is not found
- current source does not match screenshots
- implementation requires replacing a protected screen
- implementation requires protected backend/API/database changes
- build failure requires broad refactor
- task scope is ambiguous
- user asks for a modification that conflicts with project roadmap
- Gemini API/key would be introduced accidentally
- old prompt chain conflicts with current recovered source

Report:

"Stop condition triggered. No files modified."

Then explain exact reason.

---

18. Required Output Format For Every Modification

For project modifications, return:

# Implementation Report

## 1. Complete Update / Modification List

## 2. Scope Confirmation

## 3. Files Modified

## 4. Files Not Touched

## 5. Exact Insertion Points Used

## 6. Risk / Stability Note

## 7. Build Result

## 8. Verification Checklist

## 9. Remaining Risks

## 10. Commit Message Suggestion

Do not omit files modified.

Do not claim protected files are untouched unless checked.

Do not claim build success unless build was actually run.

---

19. Git / Commit Rules

In AI Studio, do not assume Git remote operations are available.

If Git instructions are explicitly requested:

- never use "git add -A"
- stage only exact intended files
- do not commit generated patcher files
- do not commit ".bak"
- do not commit APK/build outputs unless explicitly asked
- do not commit logs
- do not commit temporary scripts

Commit only after build succeeds and user approves.

---

20. Generated Artifact Rule

Do not generate ".bak", debug junk, patcher ".py", delivery zip, or temporary script files unless explicitly requested.

If temporary files are required for a task:

- report them
- remove them before final delivery unless user wants them
- never stage them by default

---

21. Current Project Phase Behavior

Current work is still frontend/mockup/institutional UI completion.

Do not jump to:

- real exchange execution
- live Binance trading
- AI Auto Pilot live mode
- Mission Center full execution expansion
- backend refactor
- API orchestration
- database migration

unless explicitly requested.

The correct sequence remains:

1. Document / roadmap lock
2. Audit
3. Phase 1 UI and semantic stabilization
4. Phase 2 institutional decision layer
5. Verify
6. Mission Center supervision
7. Auto-Trading simulation
8. AI Copilot Execution simulation
9. AI Auto Pilot simulation
10. controlled trial mode
11. limited live micro-trial

---

22. Gemini-Specific Behavior

Gemini must be conservative with source edits.

Do not "improve" by replacing.

Do not "modernize" protected UI without request.

Do not interpret "add feature" as "redesign module."

Do not make broad creative changes.

Do not assume old generated code is better than current recovered code.

When asked to add information:

- add it inside existing UI
- do not move existing UI
- do not delete existing information
- prefer compact rows/chips
- preserve visual identity
- preserve module ownership

When asked to audit:

- do not implement
- do not modify files
- return report only

When asked to implement:

- implement only requested patch
- build
- report

---

23. Final Non-Negotiable Rules

- Accuracy first.
- Stability second.
- No destructive refactor.
- No unrelated changes.
- No protected UI damage.
- No hidden API cost.
- No fake live AI claims.
- No secret exposure.
- No broad localization.
- No Mission Center execution expansion without explicit request.
- Build every Kotlin/source phase.
- Preserve owner-approved wording.
- Treat this as a serious institutional-grade product.

If any instruction conflicts with the owner's latest explicit request, stop and ask for clarification instead of guessing.
