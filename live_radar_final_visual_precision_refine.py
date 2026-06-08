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


# ------------------------------------------------------------
# 1) Remove old helper duplicates before adding refined helpers.
# ------------------------------------------------------------
for fn in [
    "LiveRadarAiOracleMetadataTile",
    "LiveRadarMetadataMiniTile",
    "LiveRadarAdornmentMiniTile",
]:
    text = remove_function(text, fn)


# ------------------------------------------------------------
# 2) Replace metadata block in expanded cards:
#    Hot Spot + Futures Long + Futures Short.
# ------------------------------------------------------------
metadata_call = '''                    LiveRadarAiOracleMetadataTile(
                        patternText = if (isBengali) details["pattern_bn"]!! else details["pattern"]!!,
                        orderbookText = if (isBengali) details["bid_ask_bn"]!! else details["bid_ask"]!!,
                        stopLossText = details["sl"]!!,
                        probabilityText = details["prob"]!!,
                        descriptionText = if (isBengali) details["desc_bn"]!! else details["desc"]!!,
                        isBengali = isBengali,
                        themeColor = if (target >= basePrice) CryptoGreen else Color(0xFFFF3F60)
                    )

'''

metadata_pattern = re.compile(
    r'''                    Text\(
                        text = if \(isBengali\) "[^"]*"\s*else\s*"ORACLE[^"]*METADATA",
[\s\S]*?                    Text\(
                        text = if \(isBengali\) details\["desc_bn"\]!! else details\["desc"\]!!,
                        fontSize = 11\.sp,
                        color = TextSecondary,
                        lineHeight = 16\.sp
                    \)

''',
    re.MULTILINE
)

text, metadata_count = metadata_pattern.subn(metadata_call, text)

if metadata_count < 3:
    raise SystemExit(f"ERROR: Expected to replace 3 metadata blocks, replaced {metadata_count}.")

print(f"OK: Replaced {metadata_count} metadata blocks with AI Oracle major tile.")


# ------------------------------------------------------------
# 3) Replace OpportunisticSignalAdornmentSection:
#    TP / Multi-AI / Position Sizing mini tile sizes refined.
# ------------------------------------------------------------
opportunistic_replacement = '''@Composable
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
            valueColor = Color.White,
            labelColor = CryptoGreen,
            borderColor = themeColor,
            labelSize = 8.5f,
            valueSize = 10.8f,
            minHeight = 58,
            modifier = Modifier.weight(1f)
        )

        LiveRadarAdornmentMiniTile(
            label = "TP2 (50%)",
            value = "$${formatPrice(tp2)}",
            valueColor = Color.White,
            labelColor = CryptoGreen,
            borderColor = themeColor,
            labelSize = 8.5f,
            valueSize = 10.8f,
            minHeight = 58,
            modifier = Modifier.weight(1f)
        )

        LiveRadarAdornmentMiniTile(
            label = "TP3 (100%)",
            value = "$${formatPrice(tp3)}",
            valueColor = Color.White,
            labelColor = CryptoGreen,
            borderColor = themeColor,
            labelSize = 8.5f,
            valueSize = 10.8f,
            minHeight = 58,
            modifier = Modifier.weight(1f)
        )
    }

    Spacer(modifier = Modifier.height(10.dp))
    HorizontalDivider(color = BorderColor.copy(alpha = 0.4f))
    Spacer(modifier = Modifier.height(10.dp))

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
            valueColor = Color.White,
            labelColor = TextPrimary,
            borderColor = themeColor,
            labelSize = 8.2f,
            valueSize = 10.6f,
            minHeight = 58,
            modifier = Modifier.weight(1f)
        )

        LiveRadarAdornmentMiniTile(
            label = "GPT-4Q Quant",
            value = "90/100",
            valueColor = Color.White,
            labelColor = TextPrimary,
            borderColor = themeColor,
            labelSize = 8.2f,
            valueSize = 10.6f,
            minHeight = 58,
            modifier = Modifier.weight(1f)
        )

        LiveRadarAdornmentMiniTile(
            label = "Claude Sentient",
            value = "93/100",
            valueColor = Color.White,
            labelColor = TextPrimary,
            borderColor = themeColor,
            labelSize = 8.2f,
            valueSize = 10.6f,
            minHeight = 58,
            modifier = Modifier.weight(1f)
        )
    }

    Spacer(modifier = Modifier.height(10.dp))
    HorizontalDivider(color = BorderColor.copy(alpha = 0.4f))
    Spacer(modifier = Modifier.height(10.dp))

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
            label = "Conservative",
            value = "2.0% Cap",
            valueColor = AccentGold,
            labelColor = Color.White,
            borderColor = themeColor,
            labelSize = 8.9f,
            valueSize = 10.6f,
            minHeight = 62,
            modifier = Modifier.weight(1f)
        )

        LiveRadarAdornmentMiniTile(
            label = "Balanced",
            value = "5.0% Cap",
            valueColor = AccentGold,
            labelColor = Color.White,
            borderColor = themeColor,
            labelSize = 8.9f,
            valueSize = 10.6f,
            minHeight = 62,
            modifier = Modifier.weight(1f)
        )

        LiveRadarAdornmentMiniTile(
            label = "Aggressive",
            value = "10.0% Max",
            valueColor = AccentGold,
            labelColor = Color.White,
            borderColor = themeColor,
            labelSize = 8.9f,
            valueSize = 10.6f,
            minHeight = 62,
            modifier = Modifier.weight(1f)
        )
    }
}'''

text = replace_composable_function(text, "OpportunisticSignalAdornmentSection", opportunistic_replacement)


# ------------------------------------------------------------
# 4) Refine Beta Guard internal tiles.
# ------------------------------------------------------------
beta_ai_impact_replacement = '''@Composable
fun BetaGuardAiImpactTile(
    status: ExecutionGuardStatus,
    readiness: Int,
    penalty: Int,
    isBengali: Boolean
) {
    val color = executionGuardColor(status)

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                Brush.linearGradient(
                    listOf(
                        Color(0xFF02050D),
                        Color(0xFF050B15),
                        Color(0xFF02050D)
                    )
                ),
                RoundedCornerShape(8.dp)
            )
            .border(0.58.dp, color.copy(alpha = 0.32f), RoundedCornerShape(8.dp))
            .padding(horizontal = 8.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = "AI GUARD IMPACT",
                fontSize = 7.6.sp,
                fontWeight = FontWeight.Black,
                color = color,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(modifier = Modifier.height(2.dp))

            Text(
                text = readinessActionLabel(status, isBengali),
                fontSize = 9.4.sp,
                fontWeight = FontWeight.Black,
                color = Color.White,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }

        Spacer(modifier = Modifier.width(7.dp))

        Column(horizontalAlignment = Alignment.End) {
            Text(
                text = "$readiness/100",
                fontSize = 13.2.sp,
                fontWeight = FontWeight.Black,
                color = color,
                maxLines = 1
            )

            if (penalty > 0) {
                Text(
                    text = "-$penalty pts",
                    fontSize = 7.6.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFFE95772),
                    maxLines = 1
                )
            }
        }
    }
}'''

text = replace_composable_function(text, "BetaGuardAiImpactTile", beta_ai_impact_replacement)


beta_mini_tile_replacement = '''@Composable
fun BetaGuardMiniTile(
    label: String,
    state: DivergenceState,
    value: String,
    modifier: Modifier = Modifier
) {
    val color = divergenceStateColor(state)

    Column(
        modifier = modifier
            .heightIn(min = 43.dp)
            .background(
                Brush.linearGradient(
                    listOf(
                        Color(0xFF02050D),
                        Color(0xFF050B15),
                        Color(0xFF02050D)
                    )
                ),
                RoundedCornerShape(7.dp)
            )
            .border(0.56.dp, color.copy(alpha = 0.30f), RoundedCornerShape(7.dp))
            .padding(horizontal = 7.dp, vertical = 5.dp),
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = label,
            fontSize = 7.0.sp,
            fontWeight = FontWeight.Black,
            color = TextMuted,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )

        Spacer(modifier = Modifier.height(2.dp))

        Text(
            text = value,
            fontSize = 8.6.sp,
            fontWeight = FontWeight.Black,
            color = color,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}'''

text = replace_composable_function(text, "BetaGuardMiniTile", beta_mini_tile_replacement)


beta_penalty_tile_replacement = '''@Composable
fun BetaGuardPenaltyTile(
    penalty: Int,
    readiness: Int,
    status: ExecutionGuardStatus,
    isBengali: Boolean,
    modifier: Modifier = Modifier
) {
    val color = executionGuardColor(status)
    val currentScore = readiness.coerceIn(0, 100)

    Column(
        modifier = modifier
            .heightIn(min = 43.dp)
            .background(
                Brush.linearGradient(
                    listOf(
                        Color(0xFF02050D),
                        Color(0xFF050B15),
                        Color(0xFF02050D)
                    )
                ),
                RoundedCornerShape(7.dp)
            )
            .border(0.56.dp, color.copy(alpha = 0.30f), RoundedCornerShape(7.dp))
            .padding(horizontal = 7.dp, vertical = 5.dp),
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "Readiness",
            fontSize = 7.0.sp,
            fontWeight = FontWeight.Black,
            color = TextMuted,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )

        Spacer(modifier = Modifier.height(2.dp))

        Text(
            text = "$currentScore/100",
            fontSize = 8.8.sp,
            fontWeight = FontWeight.Black,
            color = color,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )

        if (penalty > 0) {
            Text(
                text = "-$penalty pts",
                fontSize = 7.6.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFFE95772),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}'''

text = replace_composable_function(text, "BetaGuardPenaltyTile", beta_penalty_tile_replacement)


# ------------------------------------------------------------
# 5) Reduce Beta Guard neon-like intensity where exact modifiers exist.
# ------------------------------------------------------------
text = text.replace(
    ".border(0.75.dp, accentColor.copy(alpha = 0.44f), RoundedCornerShape(11.dp))",
    ".border(0.65.dp, accentColor.copy(alpha = 0.32f), RoundedCornerShape(11.dp))"
)
text = text.replace(
    "accentColor.copy(alpha = 0.038f)",
    "accentColor.copy(alpha = 0.026f)"
)
text = text.replace(
    "accentColor.copy(alpha = 0.060f)",
    "accentColor.copy(alpha = 0.040f)"
)
text = text.replace(
    ".border(0.65.dp, accentColor.copy(alpha = 0.35f), RoundedCornerShape(8.dp))",
    ".border(0.58.dp, accentColor.copy(alpha = 0.30f), RoundedCornerShape(8.dp))"
)


# ------------------------------------------------------------
# 6) Append refined helpers.
# ------------------------------------------------------------
helpers = '''
@Composable
fun LiveRadarAiOracleMetadataTile(
    patternText: String,
    orderbookText: String,
    stopLossText: String,
    probabilityText: String,
    descriptionText: String,
    isBengali: Boolean,
    themeColor: Color
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                Brush.linearGradient(
                    listOf(
                        Color(0xFF02050D),
                        Color(0xFF050B15),
                        Color(0xFF02050D)
                    )
                ),
                RoundedCornerShape(11.dp)
            )
            .border(0.72.dp, themeColor.copy(alpha = 0.38f), RoundedCornerShape(11.dp))
            .padding(horizontal = 11.dp, vertical = 10.dp)
    ) {
        Text(
            text = if (isBengali) "এআই ওরাকল অ্যানালিটিক্যাল মেটাডেটা" else "AI ORACLE ANALYTICAL METADATA",
            fontSize = 9.sp,
            fontWeight = FontWeight.Black,
            color = themeColor,
            letterSpacing = if (isBengali) 0.sp else 1.sp,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )

        Spacer(modifier = Modifier.height(8.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            LiveRadarMetadataMiniTile(
                label = if (isBengali) "প্যাটার্ন" else "PATTERN DETECTED",
                value = patternText,
                valueColor = Color.White,
                borderColor = themeColor,
                modifier = Modifier.weight(1f)
            )

            LiveRadarMetadataMiniTile(
                label = if (isBengali) "অর্ডারবুক রেশিও" else "ORDERBOOK RATIO",
                value = orderbookText,
                valueColor = Color.White,
                borderColor = themeColor,
                modifier = Modifier.weight(1f)
            )
        }

        Spacer(modifier = Modifier.height(6.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            LiveRadarMetadataMiniTile(
                label = if (isBengali) "স্টপ লস" else "SUGGESTED STOP LOSS",
                value = stopLossText,
                valueColor = Color(0xFFFF6F86),
                borderColor = Color(0xFFFF6F86),
                modifier = Modifier.weight(1f)
            )

            LiveRadarMetadataMiniTile(
                label = if (isBengali) "সম্ভাব্যতা স্কোর" else "PROBABILITY SCORE",
                value = probabilityText,
                valueColor = CryptoGreen,
                borderColor = CryptoGreen,
                modifier = Modifier.weight(1f)
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Brush.linearGradient(
                        listOf(
                            Color(0xFF02050D),
                            Color(0xFF060D18),
                            Color(0xFF02050D)
                        )
                    ),
                    RoundedCornerShape(8.dp)
                )
                .border(0.58.dp, themeColor.copy(alpha = 0.28f), RoundedCornerShape(8.dp))
                .padding(horizontal = 9.dp, vertical = 8.dp)
        ) {
            Text(
                text = descriptionText,
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium,
                color = Color.White,
                lineHeight = 17.sp
            )
        }
    }
}

@Composable
fun LiveRadarMetadataMiniTile(
    label: String,
    value: String,
    valueColor: Color,
    borderColor: Color,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .heightIn(min = 52.dp)
            .background(
                Brush.linearGradient(
                    listOf(
                        Color(0xFF02050D),
                        Color(0xFF050B15),
                        Color(0xFF02050D)
                    )
                ),
                RoundedCornerShape(8.dp)
            )
            .border(0.58.dp, borderColor.copy(alpha = 0.34f), RoundedCornerShape(8.dp))
            .padding(horizontal = 7.dp, vertical = 6.dp),
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = label,
            fontSize = 7.3.sp,
            fontWeight = FontWeight.Black,
            color = TextMuted,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )

        Spacer(modifier = Modifier.height(3.dp))

        Text(
            text = value,
            fontSize = 9.6.sp,
            fontWeight = FontWeight.Black,
            color = valueColor,
            maxLines = 2,
            lineHeight = 12.sp,
            overflow = TextOverflow.Ellipsis
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
    labelSize: Float,
    valueSize: Float,
    minHeight: Int,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .heightIn(min = minHeight.dp)
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
            fontSize = labelSize.sp,
            fontWeight = FontWeight.Black,
            color = labelColor,
            textAlign = TextAlign.Center,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )

        Spacer(modifier = Modifier.height(4.dp))

        Text(
            text = value,
            fontSize = valueSize.sp,
            fontWeight = FontWeight.Black,
            color = valueColor,
            textAlign = TextAlign.Center,
            maxLines = 1,
            softWrap = false,
            overflow = TextOverflow.Ellipsis
        )
    }
}
'''

text = text.rstrip() + "\n\n" + helpers + "\n"

target.write_text(text, encoding="utf-8")

print("OK: Final Live Radar visual precision refinement applied.")
print("Only MarketRadarScreen.kt modified.")
