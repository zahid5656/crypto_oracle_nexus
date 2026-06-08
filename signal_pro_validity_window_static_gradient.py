from pathlib import Path
import re

target = Path("app/src/main/java/com/example/ui/AnalysisScreen.kt")
if not target.exists():
    raise SystemExit("ERROR: AnalysisScreen.kt not found.")

text = target.read_text(encoding="utf-8")


def get_composable_block(source: str, function_name: str):
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

    return start, end, source[start:end]


start, end, block = get_composable_block(text, "RealTimeCountdown")

if "ValidityStaticAccentLayer" in block:
    print("OK: Validity static gradient layer already exists. No duplicate inserted.")
else:
    base_background = '''            .background(
                brush = Brush.linearGradient(
                    colors = listOf(
                        baseLeft,
                        Color(0xFF090F1C),
                        Color(0xFF02050D)
                    )
                )
            )'''

    static_gradient_layer = '''            .background(
                brush = Brush.horizontalGradient(
                    colors = listOf(
                        accentColor.copy(alpha = 0.12f),
                        Color.Transparent,
                        accentColor.copy(alpha = 0.075f),
                        Color.White.copy(alpha = 0.035f),
                        accentColor.copy(alpha = 0.055f),
                        Color.Transparent
                    )
                )
            ) // ValidityStaticAccentLayer'''

    if base_background not in block:
        raise SystemExit("ERROR: RealTimeCountdown base background pattern not found. No unsafe change applied.")

    block = block.replace(
        base_background,
        base_background + "\n" + static_gradient_layer,
        1
    )

    text = text[:start] + block + text[end:]
    target.write_text(text, encoding="utf-8")

    print("OK: Added subtle permanent accent-matched gradient layer to Validity Window.")
    print("No text/layout/border/animation speed changes applied.")
