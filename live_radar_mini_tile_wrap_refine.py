from pathlib import Path
import re

target = Path("app/src/main/java/com/example/ui/MarketRadarScreen.kt")
if not target.exists():
    raise SystemExit("ERROR: MarketRadarScreen.kt not found.")

text = target.read_text(encoding="utf-8")


def replace_composable_function(source: str, function_name: str, replacement: str) -> str:
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


def remove_function(source: str, function_name: str) -> str:
    pattern = re.compile(
        r"\n(?:@Composable\s+)?fun\s+"
        + re.escape(function_name)
        + r"\s*\("
    )

    match = pattern.search(source)
    if not match:
        return source

    start = match.start() + 1
    brace_start = source.find("{", match.end())
    if brace_start == -1:
        return source

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
        return source

    while end < len(source) and source[end].isspace():
        end += 1

    return source[:start] + source[end:]


# Remove old helper if script is rerun.
text = remove_function(text, "LiveRadarAdornmentMiniTile")

replacement = '''@Composable
fun OpportunisticSignalAdornmentSection(
    basePrice: Double,
    isLong: Boolean,
    potential: Double,
    isBengali: Boolean,
    themeColor: Color
) {
    val tp1 = if (isLong) basePrice * (1.0 + potential * 0.25 / 100.0) else basePrice * (1.0 - potential * 0.25 / 100.0)
    val tp2 = if (isLong) basePrice * (1.0 + potential * 0.50 / 100.0) else basePrice * (1.0 - potential * 0.50 / 100.0)
    val tp3 = if (isLong) basePrice * (1.0 + potential * 1.00 / 100.0) else basePrice * (1.0 - potential * 1.00 / 100.0)

    val formatPrice = { price: Double ->
        when {
            price < 0.01 -> String.format("%.6f", price)
            price < 1.0 -> String.format("%.4f", price)
            else -> String.format("%,.2f", price)
        }
    }

    Spacer(modifier = Modifier.height(10.dp))
    HorizontalDivider(color = BorderColor.copy(alpha = 0.4f))
    Spacer(modifier = Modifier.height(10.dp))

    // 1. Take Profit Matrix — Signal Pro style mini tiles
    Text(
        text = if (isBengali) "টার্গেট প্রফিট ম্যাট্রিক্স" else "TAKE PROFIT TARGET MATRIX",
        fontSize = 9.sp,
        fontWeight = FontWeight.Bold,
        color = themeColor,
        letterSpacing = 1.sp
    )

    Spacer(modifier = Modifier.height(7.dp))

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        LiveRadarAdornmentMiniTile(
            label = "TP1 (25%)",
            value = "$${formatPrice(tp1)}",
            valueColor = TextPrimary,
            labelColor = CryptoGreen,
            borderColor = themeColor,
            modifier = Modifier.weight(1f)
        )

        LiveRadarAdornmentMiniTile(
            label = "TP2 (50%)",
            value = "$${formatPrice(tp2)}",
            valueColor = TextPrimary,
            labelColor = CryptoGreen,
            borderColor = themeColor,
            modifier = Modifier.weight(1f)
        )

        LiveRadarAdornmentMiniTile(
            label = "TP3 (100%)",
            value = "$${formatPrice(tp3)}",
            valueColor = TextPrimary,
            labelColor = CryptoGreen,
            borderColor = themeColor,
            modifier = Modifier.weight(1f)
        )
    }

    Spacer(modifier = Modifier.height(10.dp))
    HorizontalDivider(color = BorderColor.copy(alpha = 0.4f))
    Spacer(modifier = Modifier.height(10.dp))

    // 2. Multi-AI Consensus Engines — mini tile wrapped for visual consistency
    Text(
        text = if (isBengali) "মাল্টি-এআই কনসেনসাস স্কোর" else "MULTI-AI CONSENSUS ENGINES",
        fontSize = 9.sp,
        fontWeight = FontWeight.Bold,
        color = themeColor,
        letterSpacing = 1.sp
    )

    Spacer(modifier = Modifier.height(7.dp))

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        LiveRadarAdornmentMiniTile(
            label = "Gemini Pro AI",
            value = "94/100",
            valueColor = TextPrimary,
            labelColor = TextMuted,
            borderColor = themeColor,
            modifier = Modifier.weight(1f)
        )

        LiveRadarAdornmentMiniTile(
            label = "GPT-4Q Quant",
            value = "90/100",
            valueColor = TextPrimary,
            labelColor = TextMuted,
            borderColor = themeColor,
            modifier = Modifier.weight(1f)
        )

        LiveRadarAdornmentMiniTile(
            label = "Claude Sentient",
            value = "93/100",
            valueColor = TextPrimary,
            labelColor = TextMuted,
            borderColor = themeColor,
            modifier = Modifier.weight(1f)
        )
    }

    Spacer(modifier = Modifier.height(10.dp))
    HorizontalDivider(color = BorderColor.copy(alpha = 0.4f))
    Spacer(modifier = Modifier.height(10.dp))

    // 3. Recommended Position Sizing — Signal Pro style mini tiles
    Text(
        text = if (isBengali) "পজিশন সাইজিং পোর্টফোলিও কন্ট্রোল" else "RECOMMENDED POSITION ALLOCATION SIZING",
        fontSize = 9.sp,
        fontWeight = FontWeight.Bold,
        color = themeColor,
        letterSpacing = 1.sp
    )

    Spacer(modifier = Modifier.height(7.dp))

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        LiveRadarAdornmentMiniTile(
            label = if (isBengali) "Conservative" else "Conservative",
            value = "2.0% Cap",
            valueColor = AccentGold,
            labelColor = TextPrimary,
            borderColor = themeColor,
            modifier = Modifier.weight(1f)
        )

        LiveRadarAdornmentMiniTile(
            label = if (isBengali) "Balanced" else "Balanced",
            value = "5.0% Cap",
            valueColor = AccentGold,
            labelColor = TextPrimary,
            borderColor = themeColor,
            modifier = Modifier.weight(1f)
        )

        LiveRadarAdornmentMiniTile(
            label = if (isBengali) "Aggressive" else "Aggressive",
            value = "10.0% Max",
            valueColor = AccentGold,
            labelColor = TextPrimary,
            borderColor = themeColor,
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
fun LiveRadarAdornmentMiniTile(
    label: String,
    value: String,
    valueColor: Color,
    labelColor: Color,
    borderColor: Color,
    modifier: Modifier = Modifier
) {
    val valueFontSize = when {
        value.length >= 12 -> 8.0.sp
        value.length >= 10 -> 8.6.sp
        else -> 9.6.sp
    }

    Column(
        modifier = modifier
            .heightIn(min = 52.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(
                Brush.linearGradient(
                    listOf(
                        Color(0xFF02050D),
                        Color(0xFF050B15),
                        Color(0xFF02050D)
                    )
                )
            )
            .border(
                0.75.dp,
                borderColor.copy(alpha = 0.46f),
                RoundedCornerShape(8.dp)
            )
            .padding(horizontal = 5.dp, vertical = 7.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = label,
            fontSize = 7.6.sp,
            fontWeight = FontWeight.Black,
            color = labelColor,
            textAlign = TextAlign.Center,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )

        Spacer(modifier = Modifier.height(3.dp))

        Text(
            text = value,
            fontSize = valueFontSize,
            fontWeight = FontWeight.Black,
            color = valueColor,
            textAlign = TextAlign.Center,
            maxLines = 1,
            softWrap = false,
            overflow = TextOverflow.Ellipsis
        )
    }
}'''

text = replace_composable_function(text, "OpportunisticSignalAdornmentSection", replacement)

target.write_text(text, encoding="utf-8")

print("OK: Live Radar TP / Multi-AI / Position Sizing rows wrapped with mini tiles.")
print("Only MarketRadarScreen.kt modified.")
