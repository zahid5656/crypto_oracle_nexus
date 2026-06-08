from pathlib import Path
import re

target = Path("app/src/main/java/com/example/ui/MarketRadarScreen.kt")
if not target.exists():
    raise SystemExit("ERROR: MarketRadarScreen.kt not found.")

text = target.read_text(encoding="utf-8")


def replace_function(source: str, function_name: str, replacement: str) -> str:
    pattern = re.compile(
        r"(?:@Composable\s+)?fun\s+"
        + re.escape(function_name)
        + r"\s*\("
    )

    match = pattern.search(source)
    if not match:
        raise SystemExit(f"ERROR: function not found: {function_name}")

    start = match.start()
    brace_start = source.find("{", match.end())
    if brace_start == -1:
        raise SystemExit(f"ERROR: opening brace not found for {function_name}")

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
        raise SystemExit(f"ERROR: function end not found for {function_name}")

    return source[:start] + replacement + source[end:]


replacement = '''@Composable
fun BetaGuardPenaltyTile(
    penalty: Int,
    readiness: Int,
    status: ExecutionGuardStatus,
    isBengali: Boolean,
    modifier: Modifier = Modifier
) {
    val color = executionGuardColor(status)
    val currentScore = (100 - penalty).coerceIn(0, 100)

    Column(
        modifier = modifier
            .heightIn(min = 46.dp)
            .background(Color(0xFF050A13), RoundedCornerShape(7.dp))
            .border(0.58.dp, color.copy(alpha = 0.34f), RoundedCornerShape(7.dp))
            .padding(horizontal = 7.dp, vertical = 5.dp),
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "Readiness",
            fontSize = 6.9.sp,
            fontWeight = FontWeight.Bold,
            color = TextMuted,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )

        Spacer(modifier = Modifier.height(2.dp))

        Text(
            text = "$currentScore/100",
            fontSize = 9.3.sp,
            fontWeight = FontWeight.Black,
            color = color,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )

        if (penalty > 0) {
            Text(
                text = "-$penalty pts",
                fontSize = 7.8.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFFE95772),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}'''

text = replace_function(text, "BetaGuardPenaltyTile", replacement)

target.write_text(text, encoding="utf-8")

print("OK: BetaGuardPenaltyTile stacked Readiness value applied.")
