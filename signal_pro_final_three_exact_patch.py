from pathlib import Path
import re

TARGET_FILE = Path("app/src/main/java/com/example/ui/AnalysisScreen.kt")
if not TARGET_FILE.exists():
    raise SystemExit("ERROR: AnalysisScreen.kt not found.")

text = TARGET_FILE.read_text(encoding="utf-8")


def replace_composable_function(source: str, function_name: str, replacement: str) -> str:
    pattern = re.compile(
        r"(?:@OptIn\([^\n]+\)\s*)?@Composable\s+fun\s+"
        + re.escape(function_name)
        + r"\s*\("
    )
    match = pattern.search(source)
    if not match:
        raise SystemExit(f"ERROR: Composable function not found: {function_name}")

    start = match.start()
    brace_start = source.find("{", match.end())
    if brace_start == -1:
        raise SystemExit(f"ERROR: Opening brace not found for: {function_name}")

    depth = 0
    end = None

    for i in range(brace_start, len(source)):
        if source[i] == "{":
            depth += 1
        elif source[i] == "}":
            depth -= 1
            if depth == 0:
                end = i + 1
                break

    if end is None:
        raise SystemExit(f"ERROR: Function end not found for: {function_name}")

    return source[:start] + replacement + source[end:]


def get_function_block(source: str, function_name: str):
    pattern = re.compile(
        r"(?:@OptIn\([^\n]+\)\s*)?@Composable\s+fun\s+"
        + re.escape(function_name)
        + r"\s*\("
    )
    match = pattern.search(source)
    if not match:
        raise SystemExit(f"ERROR: Function not found: {function_name}")

    start = match.start()
    brace_start = source.find("{", match.end())
    if brace_start == -1:
        raise SystemExit(f"ERROR: Opening brace not found for: {function_name}")

    depth = 0
    end = None

    for i in range(brace_start, len(source)):
        if source[i] == "{":
            depth += 1
        elif source[i] == "}":
            depth -= 1
            if depth == 0:
                end = i + 1
                break

    if end is None:
        raise SystemExit(f"ERROR: Function end not found: {function_name}")

    return start, end, source[start:end]


# ============================================================
# 1) Reload circle + Global Language button visible middle gap
# ============================================================
def patch_toolbar_gap(source: str) -> str:
    anchor = "SignalProLanguageSwitchButton("
    anchor_pos = source.find(anchor)
    if anchor_pos == -1:
        raise SystemExit("ERROR: SignalProLanguageSwitchButton call not found.")

    row_start = source.rfind("Row(", 0, anchor_pos)
    if row_start == -1:
        raise SystemExit("ERROR: Toolbar Row not found.")

    line_start = source.rfind("\n", 0, row_start)
    line_start = 0 if line_start == -1 else line_start + 1

    brace_start = source.find("{", row_start)
    if brace_start == -1:
        raise SystemExit("ERROR: Toolbar Row opening brace not found.")

    depth = 0
    row_end = None

    for i in range(brace_start, len(source)):
        if source[i] == "{":
            depth += 1
        elif source[i] == "}":
            depth -= 1
            if depth == 0:
                row_end = i + 1
                break

    if row_end is None:
        raise SystemExit("ERROR: Toolbar Row end not found.")

    row_block = source[line_start:row_end]

    # Force manual spacing because automatic spacedBy did not create visible separation.
    row_block = re.sub(
        r"horizontalArrangement\s*=\s*Arrangement\.spacedBy\(\s*\d+\.dp\s*\)",
        "horizontalArrangement = Arrangement.spacedBy(0.dp)",
        row_block,
        count=1
    )

    # Remove any previous spacer directly before the language button to avoid duplicates.
    row_block = re.sub(
        r"\n\s*Spacer\(modifier\s*=\s*Modifier\.width\(\s*(?:6|8|10|12|14|16|18|20)\.dp\s*\)\)\s*\n\s*(?=SignalProLanguageSwitchButton\()",
        "\n",
        row_block
    )

    lang_pos = row_block.find("SignalProLanguageSwitchButton(")
    if lang_pos == -1:
        raise SystemExit("ERROR: Language button call not found inside toolbar row.")

    insert_at = row_block.rfind("\n", 0, lang_pos) + 1

    # Visible gap between reload circle and global language switch.
    spacer = "                Spacer(modifier = Modifier.width(16.dp))\n\n"

    row_block = row_block[:insert_at] + spacer + row_block[insert_at:]

    return source[:line_start] + row_block + source[row_end:]


text = patch_toolbar_gap(text)


# ============================================================
# 2) AI Decision Brief popup text slightly bigger/readable
#    No bottom sheet full-open behavior change here.
# ============================================================
decision_brief_code = '''@Composable
fun DecisionBriefBlock(
    title: String,
    value: String,
    accentColor: Color
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .background(
                Brush.linearGradient(
                    listOf(
                        Color(0xFF02050D),
                        Color(0xFF08111C),
                        Color(0xFF02050D)
                    )
                )
            )
            .border(0.75.dp, accentColor.copy(alpha = 0.46f), RoundedCornerShape(10.dp))
            .padding(horizontal = 11.dp, vertical = 9.dp)
    ) {
        Text(
            text = title,
            fontSize = 11.5.sp,
            fontWeight = FontWeight.Black,
            color = accentColor,
            maxLines = 1,
            overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
        )

        Spacer(modifier = Modifier.height(4.dp))

        Text(
            text = value,
            fontSize = 13.5.sp,
            color = TextPrimary,
            lineHeight = 18.sp
        )
    }
}'''

text = replace_composable_function(text, "DecisionBriefBlock", decision_brief_code)

start, end, block = get_function_block(text, "StartTradeFlow")

block = re.sub(
    r'(text\s*=\s*if\s*\(isBengali\)\s*"AI সিদ্ধান্ত সংক্ষেপ"\s*else\s*"AI Decision Brief",\s*\n\s*)fontSize\s*=\s*\d+(?:\.\d+)?\.sp',
    r'\1fontSize = 20.sp',
    block,
    count=1
)

block = re.sub(
    r'(text\s*=\s*if\s*\(isBengali\)\s*"দ্রুত সিদ্ধান্ত নেওয়ার জন্য সংক্ষিপ্ত সারাংশ"\s*else\s*"Compact signal summary for faster decision-making",\s*\n\s*)fontSize\s*=\s*\d+(?:\.\d+)?\.sp',
    r'\1fontSize = 12.5.sp',
    block,
    count=1
)

block = re.sub(
    r'(text\s*=\s*disclaimerText,\s*\n\s*)fontSize\s*=\s*\d+(?:\.\d+)?\.sp,\s*\n\s*color\s*=\s*TextMuted,\s*\n\s*lineHeight\s*=\s*\d+(?:\.\d+)?\.sp',
    r'\1fontSize = 11.5.sp,\n                    color = TextMuted,\n                    lineHeight = 16.sp',
    block,
    count=1
)

text = text[:start] + block + text[end:]

TARGET_FILE.write_text(text, encoding="utf-8")


# ============================================================
# 3) Workflow debug APK ZIP artifact name timestamp only
#    Inside ZIP remains app-debug.apk.
# ============================================================
workflow_dir = Path(".github/workflows")
if not workflow_dir.exists():
    raise SystemExit("ERROR: .github/workflows directory not found.")

workflow_files = list(workflow_dir.glob("*.yml")) + list(workflow_dir.glob("*.yaml"))

candidates = []
for wf in workflow_files:
    wf_text = wf.read_text(encoding="utf-8")
    if "assembleDebug" in wf_text or "Build debug APK" in wf_text or "crypto-oracle-nexus-debug-apk" in wf_text:
        candidates.append(wf)

if not candidates:
    raise SystemExit("ERROR: No debug APK workflow found.")

workflow_file = candidates[0]
wf_text = workflow_file.read_text(encoding="utf-8")


def find_step(source: str, step_name: str):
    pattern = re.compile(r"^(\s*)-\s+name:\s*" + re.escape(step_name) + r"\s*$", re.MULTILINE)
    match = pattern.search(source)
    if not match:
        return None

    indent = match.group(1)
    start = match.start()
    next_step = re.compile(r"^" + re.escape(indent) + r"-\s+name:\s+.*$", re.MULTILINE)
    next_match = next_step.search(source, match.end())
    end = next_match.start() if next_match else len(source)

    return start, end, indent


def remove_step(source: str, step_name: str):
    found = find_step(source, step_name)
    if not found:
        return source

    start, end, _ = found
    return source[:start] + source[end:]


def insert_after_step(source: str, step_name: str, new_step: str):
    found = find_step(source, step_name)
    if not found:
        return None

    _, end, _ = found
    if not new_step.endswith("\n"):
        new_step += "\n"

    return source[:end] + "\n" + new_step + source[end:]


def replace_step(source: str, step_name: str, new_step: str):
    found = find_step(source, step_name)
    if not found:
        return None

    start, end, _ = found
    if not new_step.endswith("\n"):
        new_step += "\n"

    return source[:start] + new_step + source[end:]


# Remove old wrong workflow steps, if any.
wf_text = remove_step(wf_text, "Prepare timestamped debug APK")
wf_text = remove_step(wf_text, "Generate timestamped artifact name")

build_step = find_step(wf_text, "Build debug APK")
if not build_step:
    raise SystemExit("ERROR: Build debug APK step not found.")

_, _, indent = build_step

timestamp_step = f'''{indent}- name: Generate timestamped artifact name
{indent}  id: artifact_name
{indent}  shell: bash
{indent}  run: |
{indent}    FILE_TS="$(TZ='Asia/Dhaka' date '+%H-%M-%S-%d-%B-%y')"
{indent}    ARTIFACT_NAME="crypto-oracle-nexus-debug-[${{FILE_TS}}]-apk"
{indent}    echo "artifact_name=$ARTIFACT_NAME" >> "$GITHUB_OUTPUT"
{indent}    echo "ZIP download name will be: $ARTIFACT_NAME.zip"
'''

wf_text = insert_after_step(wf_text, "Build debug APK", timestamp_step)
if wf_text is None:
    raise SystemExit("ERROR: Could not insert timestamp artifact name step.")

upload_step = f'''{indent}- name: Upload debug APK artifact
{indent}  uses: actions/upload-artifact@v4
{indent}  with:
{indent}    name: ${{{{ steps.artifact_name.outputs.artifact_name }}}}
{indent}    path: app/build/outputs/apk/debug/app-debug.apk
{indent}    if-no-files-found: error
{indent}    retention-days: 30
'''

updated = replace_step(wf_text, "Upload debug APK artifact", upload_step)

if updated is None:
    updated = insert_after_step(wf_text, "Generate timestamped artifact name", upload_step)

if updated is None:
    raise SystemExit("ERROR: Could not update Upload debug APK artifact step.")

workflow_file.write_text(updated, encoding="utf-8")

print("OK: Final three exact modifications applied.")
print("1) Toolbar visible gap: Spacer 16.dp")
print("2) AI Decision Brief text size increased")
print("3) Downloaded ZIP artifact timestamped")
print("Expected downloaded ZIP:")
print("crypto-oracle-nexus-debug-[20-40-00-07-June-26]-apk.zip")
print("Inside ZIP remains:")
print("app-debug.apk")
