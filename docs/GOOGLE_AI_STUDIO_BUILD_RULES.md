# GOOGLE AI STUDIO BUILD RULES

## Mandatory APK Build Location

Always build APK inside Google AI Studio.

Required output file:

.build-outputs/app-debug.apk

## Gemini API Rule

Do not use GEMINI_API_KEY unless explicitly asked.

Do not add Gemini key to:
- Kotlin source
- Gradle files
- local.properties
- CI config
- documentation as real secret

## Build Acceptance Rule

A refactor phase is accepted only if:

1. Build succeeds.
2. APK exists at:
   .build-outputs/app-debug.apk
3. No unrelated UI behavior changes.
4. No working feature removed.
5. No build output APK committed unless explicitly asked.

## Refactor Phase Rule

After each phase:
1. Build.
2. Verify APK output path.
3. Commit.
4. Push.
5. Continue next phase only after successful build.
