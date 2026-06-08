from pathlib import Path

target = Path("app/src/main/java/com/example/ui/AnalysisScreen.kt")
if not target.exists():
    raise SystemExit("ERROR: AnalysisScreen.kt not found.")

text = target.read_text(encoding="utf-8")

required_imports = [
    "import androidx.compose.foundation.rememberScrollState",
    "import androidx.compose.foundation.verticalScroll",
    "import androidx.compose.foundation.layout.navigationBarsPadding",
]

lines = text.splitlines()
existing = set(line.strip() for line in lines)

missing = [imp for imp in required_imports if imp not in existing]

if not missing:
    print("OK: Required imports already exist.")
    raise SystemExit(0)

# Insert after the last existing import line.
last_import_index = -1
for i, line in enumerate(lines):
    if line.startswith("import "):
        last_import_index = i

if last_import_index == -1:
    raise SystemExit("ERROR: No import section found in AnalysisScreen.kt")

for imp in missing:
    lines.insert(last_import_index + 1, imp)
    last_import_index += 1

target.write_text("\n".join(lines) + "\n", encoding="utf-8")

print("OK: Added missing imports:")
for imp in missing:
    print(" -", imp)
