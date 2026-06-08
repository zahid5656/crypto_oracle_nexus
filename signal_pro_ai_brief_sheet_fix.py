from pathlib import Path
import re

target = Path("app/src/main/java/com/example/ui/AnalysisScreen.kt")
if not target.exists():
    raise SystemExit("ERROR: AnalysisScreen.kt not found.")

text = target.read_text(encoding="utf-8")


def replace_composable_function(source: str, function_name: str, replacement: str) -> str:
    pattern = re.compile(r"(?:@OptIn\([^\n]+\)\s*)?@Composable\s+fun\s+" + re.escape(function_name) + r"\s*\(")
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


# Extract current StartTradeFlow and patch only sheet behavior.
pattern = re.compile(r"(?:@OptIn\([^\n]+\)\s*)?@Composable\s+fun\s+StartTradeFlow\s*\(")
match = pattern.search(text)
if not match:
    raise SystemExit("ERROR: StartTradeFlow not found.")

start = match.start()
brace_start = text.find("{", match.end())
depth = 0
end = None

for i in range(brace_start, len(text)):
    if text[i] == "{":
        depth += 1
    elif text[i] == "}":
        depth -= 1
        if depth == 0:
            end = i + 1
            break

if end is None:
    raise SystemExit("ERROR: StartTradeFlow end not found.")

block = text[start:end]

# 1) Ensure sheet state exists.
if "decisionBriefSheetState" not in block:
    anchor = "    val isBengali by viewModel.isBengali.collectAsState()\n"
    if anchor not in block:
        raise SystemExit("ERROR: isBengali anchor not found in StartTradeFlow.")

    block = block.replace(
        anchor,
        anchor + "    val decisionBriefSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)\n",
        1
    )

# 2) Attach sheetState to ModalBottomSheet.
if "sheetState = decisionBriefSheetState" not in block:
    block = block.replace(
        '''        ModalBottomSheet(
            onDismissRequest = { showDecisionBrief = false },
            containerColor = Color(0xFF030712),
            contentColor = TextPrimary
        ) {''',
        '''        ModalBottomSheet(
            onDismissRequest = { showDecisionBrief = false },
            sheetState = decisionBriefSheetState,
            containerColor = Color(0xFF030712),
            contentColor = TextPrimary
        ) {''',
        1
    )

# 3) Make content open near-full and keep buttons visible/safe.
old_modifier = '''                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 18.dp, vertical = 10.dp),'''

new_modifier = '''                modifier = Modifier
                    .fillMaxWidth()
                    .fillMaxHeight(0.92f)
                    .verticalScroll(rememberScrollState())
                    .navigationBarsPadding()
                    .padding(horizontal = 18.dp, vertical = 10.dp),'''

if old_modifier in block:
    block = block.replace(old_modifier, new_modifier, 1)
elif ".verticalScroll(rememberScrollState())" not in block:
    print("WARNING: Bottom sheet content modifier pattern not found. Sheet state was still applied.")

# 4) Give bottom buttons extra safe bottom space.
if "Spacer(modifier = Modifier.height(24.dp))" not in block:
    block = block.replace(
        "                Spacer(modifier = Modifier.height(12.dp))",
        "                Spacer(modifier = Modifier.height(24.dp))",
        1
    )

text = text[:start] + block + text[end:]

target.write_text(text, encoding="utf-8")

print("OK: AI Decision Brief bottom sheet now skips partial expansion and opens near-full height.")
