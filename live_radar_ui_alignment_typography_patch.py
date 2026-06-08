from pathlib import Path
import re

target = Path("app/src/main/java/com/example/ui/MarketRadarScreen.kt")
if not target.exists():
    raise SystemExit("ERROR: MarketRadarScreen.kt not found.")

text = target.read_text(encoding="utf-8")
original_text = text


def remove_function(source: str, name: str) -> str:
    pattern = re.compile(r"\n(?:@Composable\s+)?(?:private\s+)?fun\s+" + re.escape(name) + r"\s*\(")
    match = pattern.search(source)
    if not match:
        return source

    start = match.start() + 1
    brace_start = source.find("{", match.end())
    if brace_start == -1:
        raise SystemExit(f"ERROR: opening brace not found for function {name}")

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
        raise SystemExit(f"ERROR: could not remove function {name}")

    while end < len(source) and source[end].isspace():
        end += 1

    return source[:start] + source[end:]


def remove_data_class(source: str, name: str) -> str:
    pattern = re.compile(r"\n(?:private\s+)?data\s+class\s+" + re.escape(name) + r"\s*\(")
    match = pattern.search(source)
    if not match:
        return source

    start = match.start() + 1
    paren_start = source.find("(", match.start())
    depth = 0
    end = None

    for i in range(paren_start, len(source)):
        if source[i] == "(":
            depth += 1
        elif source[i] == ")":
            depth -= 1
            if depth == 0:
                end = i + 1
                break

    if end is None:
        raise SystemExit(f"ERROR: could not remove data class {name}")

    while end < len(source) and source[end].isspace():
        end += 1

    return source[:start] + source[end:]


def find_lambda_end(source: str, start: int) -> int:
    brace_start = source.find("{", start)
    if brace_start == -1:
        raise SystemExit("ERROR: lambda opening brace not found.")

    depth = 0
    for i in range(brace_start, len(source)):
        if source[i] == "{":
            depth += 1
        elif source[i] == "}":
            depth -= 1
            if depth == 0:
                return i + 1

    raise SystemExit("ERROR: lambda closing brace not found.")


marker = "// ============================================================================\n// LIVE RADAR UI ALIGNMENT + TYPOGRAPHY PATCH"
marker_index = text.find(marker)
if marker_index != -1:
    text = text[:marker_index].rstrip() + "\n"

for function_name in [
    "OracleAnalyticMetadataGrid",
    "OracleAnalyticMetadataRow",
    "OracleAnalyticMetadataTile",
    "LiveRadarBetaDivergenceGuard",
    "BetaDivergenceGuardGrid",
    "BetaGuardAiImpactTile",
    "BetaGuardMiniTile",
    "AllocationSizingTile",
    "ecosystemLeaderNameFor",
    "stableMetricSeed",
    "guardImpactPenaltyFor",
]:
    text = remove_function(text, function_name)

text = remove_data_class(text, "LiveRadarBetaGuardUiState")

for old, new in [
    (
        "val details = spotDetails[index]",
        "val details = spotDetails[index]\n            val ecosystemLeaderName = ecosystemLeaderNameFor(symbol)",
    ),
    (
        "val details = longDetails[index]",
        "val details = longDetails[index]\n            val ecosystemLeaderName = ecosystemLeaderNameFor(symbol)",
    ),
    (
        "val details = shortDetails[index]",
        "val details = shortDetails[index]\n            val ecosystemLeaderName = ecosystemLeaderNameFor(symbol)",
    ),
]:
    if new not in text:
        text = text.replace(old, new, 1)


def replace_metadata_by_pattern_rows(source: str) -> tuple[str, int]:
    configs = [
        ("AccentGold", "স্টপ লস"),
        ("CryptoGreen", "স্টপ লস টার্গেট"),
        ("Color(0xFFFF3F60)", "স্টপ লস টার্গেট"),
    ]

    completed = 0
    search_pos = 0

    for theme_color, stop_bn in configs:
        pattern_index = source.find('\"PATTERN DETECTED\"', search_pos)
        if pattern_index == -1:
            available = re.findall(r'\"[A-Z][A-Z0-9 /+()_.:-]*METADATA\"', source)
            unique_available = []
            for item in available:
                if item not in unique_available:
                    unique_available.append(item)
            raise SystemExit(
                "ERROR: metadata pattern row not found. Available metadata headers: "
                + ", ".join(unique_available[:12])
            )

        row1_start = source.rfind("\n                    Row(", 0, pattern_index)
        if row1_start != -1:
            row1_start += 1
        else:
            row1_start = source.rfind("Row(", max(0, pattern_index - 2600), pattern_index)

        if row1_start == -1 or pattern_index - row1_start > 2600:
            raise SystemExit("ERROR: metadata first row anchor not found near PATTERN DETECTED.")

        row1_end = find_lambda_end(source, row1_start)

        row2_start = source.find("Row(", row1_end, row1_end + 1200)
        if row2_start == -1:
            raise SystemExit("ERROR: metadata second row anchor not found after PATTERN DETECTED row.")

        row2_end = find_lambda_end(source, row2_start)

        replacement = f'''OracleAnalyticMetadataGrid(
                        patternLabel = if (isBengali) "প্যাটার্ন" else "PATTERN DETECTED",
                        patternValue = if (isBengali) details["pattern_bn"]!! else details["pattern"]!!,
                        ratioLabel = if (isBengali) "অর্ডারবুক ইমব্যালেন্স" else "ORDERBOOK RATIO",
                        ratioValue = if (isBengali) details["bid_ask_bn"]!! else details["bid_ask"]!!,
                        stopLossLabel = if (isBengali) "{stop_bn}" else "SUGGESTED STOP LOSS",
                        stopLossValue = details["sl"]!!,
                        probabilityLabel = if (isBengali) "সম্ভাব্যতা স্কোর" else "PROBABILITY SCORE",
                        probabilityValue = details["prob"]!!,
                        themeColor = {theme_color}
                    )'''

        source = source[:row1_start] + replacement + source[row2_end:]
        search_pos = row1_start + len(replacement)
        completed += 1

    return source, completed


text, metadata_replacements = replace_metadata_by_pattern_rows(text)
if metadata_replacements != 3:
    raise SystemExit(f"ERROR: expected 3 metadata-grid replacements, found {metadata_replacements}.")

desc_pattern = re.compile(
    r'''(                    Text\(\n                        text = if \(isBengali\) details\["desc_bn"\]!! else details\["desc"\]!!,\n                        fontSize = 11\.sp,\n                        color = TextSecondary,\n                        lineHeight = 16\.sp\n                    \)\n)''',
    re.MULTILINE,
)

is_long_sequence = [True, True, False]
inserted = 0


def insert_guard(match: re.Match) -> str:
    global inserted
    is_long = is_long_sequence[inserted] if inserted < len(is_long_sequence) else True
    inserted += 1
    return match.group(1) + f'''

                    Spacer(modifier = Modifier.height(10.dp))

                    LiveRadarBetaDivergenceGuard(
                        symbol = symbol,
                        timeframe = timeframe,
                        isLong = {str(is_long).lower()},
                        ecosystemLeaderName = ecosystemLeaderName,
                        isBengali = isBengali
                    )
'''

text = desc_pattern.sub(insert_guard, text)
if inserted != 3:
    raise SystemExit(f"ERROR: expected 3 Beta Guard insertions, inserted {inserted}")

match = re.search(r"\n@Composable\s+fun\s+OpportunisticSignalAdornmentSection\s*\(", text)
if not match:
    raise SystemExit("ERROR: OpportunisticSignalAdornmentSection not found.")

start = match.start() + 1
end = find_lambda_end(text, match.start())

new_adornment = r'''@Composable
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
        fontSize = 10.8.sp,
        fontWeight = FontWeight.Black,
        color = themeColor,
        letterSpacing = 1.sp
    )
    Spacer(modifier = Modifier.height(7.dp))
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.Top
    ) {
        Column(modifier = Modifier.weight(1f), horizontalAlignment = Alignment.CenterHorizontally) {
            Text(text = "TP1 (25%)", fontSize = 10.4.sp, fontWeight = FontWeight.Black, color = CryptoGreen, textAlign = TextAlign.Center)
            Text(text = "$${formatPrice(tp1)}", fontSize = 11.4.sp, fontWeight = FontWeight.Bold, color = CryptoGreen, textAlign = TextAlign.Center)
        }
        Column(modifier = Modifier.weight(1f), horizontalAlignment = Alignment.CenterHorizontally) {
            Text(text = "TP2 (50%)", fontSize = 10.4.sp, fontWeight = FontWeight.Black, color = CryptoGreen, textAlign = TextAlign.Center)
            Text(text = "$${formatPrice(tp2)}", fontSize = 11.4.sp, fontWeight = FontWeight.Bold, color = CryptoGreen, textAlign = TextAlign.Center)
        }
        Column(modifier = Modifier.weight(1f), horizontalAlignment = Alignment.CenterHorizontally) {
            Text(text = "TP3 (100%)", fontSize = 10.4.sp, fontWeight = FontWeight.Black, color = CryptoGreen, textAlign = TextAlign.Center)
            Text(text = "$${formatPrice(tp3)}", fontSize = 11.4.sp, fontWeight = FontWeight.Bold, color = CryptoGreen, textAlign = TextAlign.Center)
        }
    }

    Spacer(modifier = Modifier.height(10.dp))
    HorizontalDivider(color = BorderColor.copy(alpha = 0.4f))
    Spacer(modifier = Modifier.height(10.dp))

    Text(
        text = if (isBengali) "মাল্টি-এআই কনসেনসাস স্কোর" else "MULTI-AI CONSENSUS ENGINES",
        fontSize = 10.2.sp,
        fontWeight = FontWeight.Black,
        color = themeColor,
        letterSpacing = 1.sp
    )
    Spacer(modifier = Modifier.height(7.dp))
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.Top
    ) {
        Column(modifier = Modifier.weight(1f), horizontalAlignment = Alignment.CenterHorizontally) {
            Text(text = "Gemini Pro AI", fontSize = 9.6.sp, fontWeight = FontWeight.Bold, color = CryptoGreen, textAlign = TextAlign.Center)
            Text(text = "94 / 100", fontSize = 13.sp, fontWeight = FontWeight.Black, color = TextPrimary, textAlign = TextAlign.Center)
        }
        Column(modifier = Modifier.weight(1f), horizontalAlignment = Alignment.CenterHorizontally) {
            Text(text = "GPT-4Q Quant", fontSize = 9.6.sp, fontWeight = FontWeight.Bold, color = CryptoGreen, textAlign = TextAlign.Center)
            Text(text = "90 / 100", fontSize = 13.sp, fontWeight = FontWeight.Black, color = TextPrimary, textAlign = TextAlign.Center)
        }
        Column(modifier = Modifier.weight(1f), horizontalAlignment = Alignment.CenterHorizontally) {
            Text(text = "Claude Sentient", fontSize = 9.6.sp, fontWeight = FontWeight.Bold, color = CryptoGreen, textAlign = TextAlign.Center)
            Text(text = "93 / 100", fontSize = 13.sp, fontWeight = FontWeight.Black, color = TextPrimary, textAlign = TextAlign.Center)
        }
    }

    Spacer(modifier = Modifier.height(10.dp))
    HorizontalDivider(color = BorderColor.copy(alpha = 0.4f))
    Spacer(modifier = Modifier.height(10.dp))

    Text(
        text = if (isBengali) "পজিশন সাইজিং পোর্টফোলিও কন্ট্রোল" else "RECOMMENDED POSITION ALLOCATION SIZING",
        fontSize = 10.2.sp,
        fontWeight = FontWeight.Black,
        color = themeColor,
        letterSpacing = 1.sp
    )
    Spacer(modifier = Modifier.height(7.dp))
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        verticalAlignment = Alignment.Top
    ) {
        AllocationSizingTile(
            title = "CONSERVATIVE",
            value = "2.0% Cap",
            color = AccentGold,
            modifier = Modifier.weight(1f)
        )
        AllocationSizingTile(
            title = "BALANCED",
            value = "5.0% Cap",
            color = AccentGold,
            modifier = Modifier.weight(1f)
        )
        AllocationSizingTile(
            title = "AGGRESSIVE",
            value = "10.0% Max",
            color = AccentGold,
            modifier = Modifier.weight(1f)
        )
    }
}
'''

text = text[:start] + new_adornment + text[end:]

helpers = r'''

// ============================================================================
// LIVE RADAR UI ALIGNMENT + TYPOGRAPHY PATCH
// Scope: MarketRadarScreen.kt expanded Live Radar cards only.
// Signal Pro, Mission Center and trade execution logic are intentionally untouched.
// ============================================================================

@Composable
private fun OracleAnalyticMetadataGrid(
    patternLabel: String,
    patternValue: String,
    ratioLabel: String,
    ratioValue: String,
    stopLossLabel: String,
    stopLossValue: String,
    probabilityLabel: String,
    probabilityValue: String,
    themeColor: Color
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                Brush.linearGradient(
                    listOf(
                        themeColor.copy(alpha = 0.075f),
                        Color(0xFF06111C),
                        Color(0xFF02050D)
                    )
                ),
                RoundedCornerShape(10.dp)
            )
            .border(0.85.dp, themeColor.copy(alpha = 0.48f), RoundedCornerShape(10.dp))
            .padding(horizontal = 8.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(7.dp)
    ) {
        OracleAnalyticMetadataRow(
            leftLabel = patternLabel,
            leftValue = patternValue,
            leftColor = TextPrimary,
            rightLabel = ratioLabel,
            rightValue = ratioValue,
            rightColor = TextPrimary,
            themeColor = themeColor
        )

        OracleAnalyticMetadataRow(
            leftLabel = stopLossLabel,
            leftValue = stopLossValue,
            leftColor = Color(0xFFE00022),
            rightLabel = probabilityLabel,
            rightValue = probabilityValue,
            rightColor = CryptoGreen,
            themeColor = themeColor,
            leftIsCritical = true,
            rightIsProbability = true
        )
    }
}

@Composable
private fun OracleAnalyticMetadataRow(
    leftLabel: String,
    leftValue: String,
    leftColor: Color,
    rightLabel: String,
    rightValue: String,
    rightColor: Color,
    themeColor: Color,
    leftIsCritical: Boolean = false,
    rightIsProbability: Boolean = false
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.Top
    ) {
        OracleAnalyticMetadataTile(
            label = leftLabel,
            value = leftValue,
            valueColor = leftColor,
            borderColor = if (leftIsCritical) Color(0xFFE00022) else themeColor,
            isCritical = leftIsCritical,
            isLargeValue = leftIsCritical,
            modifier = Modifier.weight(1f)
        )

        OracleAnalyticMetadataTile(
            label = rightLabel,
            value = rightValue,
            valueColor = rightColor,
            borderColor = themeColor,
            showIndicator = rightIsProbability,
            isLargeValue = rightIsProbability,
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
private fun OracleAnalyticMetadataTile(
    label: String,
    value: String,
    valueColor: Color,
    borderColor: Color,
    modifier: Modifier = Modifier,
    isCritical: Boolean = false,
    showIndicator: Boolean = false,
    isLargeValue: Boolean = false
) {
    Column(
        modifier = modifier
            .heightIn(min = 58.dp)
            .background(
                Brush.linearGradient(
                    listOf(
                        borderColor.copy(alpha = if (isCritical) 0.16f else 0.09f),
                        Color(0xFF050A13)
                    )
                ),
                RoundedCornerShape(8.dp)
            )
            .border(
                width = if (isCritical) 1.1.dp else 0.75.dp,
                color = borderColor.copy(alpha = if (isCritical) 0.88f else 0.46f),
                shape = RoundedCornerShape(8.dp)
            )
            .padding(horizontal = 8.dp, vertical = 7.dp),
        verticalArrangement = Arrangement.Top
    ) {
        Text(
            text = label,
            fontSize = 9.8.sp,
            fontWeight = FontWeight.Black,
            color = if (isCritical) Color(0xFFE00022) else TextMuted,
            letterSpacing = 0.35.sp,
            lineHeight = 11.sp,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis
        )

        Spacer(modifier = Modifier.height(4.dp))

        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Start
        ) {
            Text(
                text = value,
                fontSize = if (isLargeValue) 14.2.sp else 11.3.sp,
                fontWeight = FontWeight.Black,
                color = valueColor,
                lineHeight = if (isLargeValue) 15.sp else 13.sp,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
            if (showIndicator) {
                Spacer(modifier = Modifier.width(5.dp))
                Box(
                    modifier = Modifier
                        .size(7.dp)
                        .background(valueColor, CircleShape)
                )
            }
        }
    }
}

@Composable
private fun LiveRadarBetaDivergenceGuard(
    symbol: String,
    timeframe: String,
    isLong: Boolean,
    ecosystemLeaderName: String,
    isBengali: Boolean
) {
    val state = remember(symbol, timeframe, isLong, ecosystemLeaderName) {
        val seed = stableMetricSeed(symbol, timeframe, isLong)
        val latencyDelayed = seed % 5 == 0
        LiveRadarBetaGuardUiState(
            dataSyncValue = if (latencyDelayed) "642ms Delay" else "128ms OK",
            dataSyncDelayed = latencyDelayed,
            btcDelta = if (isLong) "+0.${seed % 9}%" else "-0.${seed % 9}%",
            ecosystemLeaderName = ecosystemLeaderName,
            ecosystemDelta = if (isLong) "+0.${(seed / 3) % 9}%" else "-0.${(seed / 3) % 9}%",
            marketFlow = if (seed % 4 == 0) "Outflow" else "Neutral",
            derivatives = if (seed % 6 == 0) "Crowded" else "Normal",
            spreadRisk = if (seed % 7 == 0) "Wide" else "Tight",
            assetShock = if (seed % 8 == 0) "Fast" else "Clear",
            readiness = (94 - (seed % 9)).coerceIn(80, 96),
            penalty = guardImpactPenaltyFor(seed)
        )
    }

    val guardColor = if (state.dataSyncDelayed) Color(0xFFFF9F0A) else CryptoGreen
    val darkRed = Color(0xFFB00020)

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                Brush.linearGradient(
                    listOf(
                        Color(0xFF02050D),
                        guardColor.copy(alpha = 0.07f),
                        Color(0xFF02050D)
                    )
                ),
                RoundedCornerShape(11.dp)
            )
            .border(0.85.dp, guardColor.copy(alpha = 0.46f), RoundedCornerShape(11.dp))
            .padding(horizontal = 9.dp, vertical = 9.dp)
    ) {
        BetaGuardAiImpactTile(
            readiness = state.readiness,
            penalty = state.penalty,
            guardColor = guardColor,
            penaltyColor = darkRed
        )

        Spacer(modifier = Modifier.height(8.dp))

        BetaDivergenceGuardGrid(
            state = state,
            delayedColor = Color(0xFFFF9F0A),
            safeColor = CryptoGreen,
            riskColor = darkRed
        )
    }
}

@Composable
private fun BetaGuardAiImpactTile(
    readiness: Int,
    penalty: Int,
    guardColor: Color,
    penaltyColor: Color
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                Brush.linearGradient(
                    listOf(
                        guardColor.copy(alpha = 0.12f),
                        Color(0xFF06111C),
                        Color(0xFF02050D)
                    )
                ),
                RoundedCornerShape(9.dp)
            )
            .border(0.75.dp, guardColor.copy(alpha = 0.44f), RoundedCornerShape(9.dp))
            .padding(horizontal = 9.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = "AI GUARD IMPACT",
                fontSize = 12.4.sp,
                fontWeight = FontWeight.Black,
                color = guardColor,
                letterSpacing = 0.85.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = "Validity Entry",
                fontSize = 12.0.sp,
                fontWeight = FontWeight.Black,
                color = TextPrimary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }

        Column(horizontalAlignment = Alignment.End) {
            Text(
                text = "$readiness/100",
                fontSize = 15.4.sp,
                fontWeight = FontWeight.Black,
                color = guardColor,
                maxLines = 1
            )
            Text(
                text = "-$penalty pts",
                fontSize = 11.8.sp,
                fontWeight = FontWeight.Black,
                color = penaltyColor,
                maxLines = 1
            )
        }
    }
}

@Composable
private fun BetaDivergenceGuardGrid(
    state: LiveRadarBetaGuardUiState,
    delayedColor: Color,
    safeColor: Color,
    riskColor: Color
) {
    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            verticalAlignment = Alignment.Top
        ) {
            BetaGuardMiniTile("Data Sync", state.dataSyncValue, if (state.dataSyncDelayed) delayedColor else safeColor, Modifier.weight(1f))
            BetaGuardMiniTile("BTC Delta", state.btcDelta, safeColor, Modifier.weight(1f))
        }
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            verticalAlignment = Alignment.Top
        ) {
            BetaGuardMiniTile(state.ecosystemLeaderName, state.ecosystemDelta, safeColor, Modifier.weight(1f))
            BetaGuardMiniTile("Market Flow", state.marketFlow, if (state.marketFlow == "Outflow") delayedColor else safeColor, Modifier.weight(1f))
        }
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            verticalAlignment = Alignment.Top
        ) {
            BetaGuardMiniTile("Derivatives", state.derivatives, if (state.derivatives == "Crowded") delayedColor else safeColor, Modifier.weight(1f))
            BetaGuardMiniTile("Spread Risk", state.spreadRisk, if (state.spreadRisk == "Wide") delayedColor else safeColor, Modifier.weight(1f))
        }
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            verticalAlignment = Alignment.Top
        ) {
            BetaGuardMiniTile("Asset Shock", state.assetShock, if (state.assetShock == "Fast") delayedColor else safeColor, Modifier.weight(1f))
            BetaGuardMiniTile("Readiness", "${state.readiness}/100", if (state.penalty >= 6) riskColor else safeColor, Modifier.weight(1f))
        }
    }
}

@Composable
private fun BetaGuardMiniTile(
    label: String,
    value: String,
    color: Color,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .heightIn(min = 53.dp)
            .background(Color(0xFF050A13), RoundedCornerShape(8.dp))
            .border(0.8.dp, color.copy(alpha = 0.62f), RoundedCornerShape(8.dp))
            .padding(horizontal = 7.dp, vertical = 7.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = label,
            fontSize = 9.7.sp,
            fontWeight = FontWeight.Black,
            color = TextMuted,
            textAlign = TextAlign.Center,
            lineHeight = 11.sp,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(3.dp))
        Text(
            text = value,
            fontSize = 11.8.sp,
            fontWeight = FontWeight.Black,
            color = color,
            textAlign = TextAlign.Center,
            lineHeight = 13.sp,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.fillMaxWidth()
        )
    }
}

@Composable
private fun AllocationSizingTile(
    title: String,
    value: String,
    color: Color,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .heightIn(min = 48.dp)
            .background(Color(0xFF050A13), RoundedCornerShape(8.dp))
            .border(0.72.dp, color.copy(alpha = 0.40f), RoundedCornerShape(8.dp))
            .padding(horizontal = 6.dp, vertical = 6.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = title,
            fontSize = 8.7.sp,
            fontWeight = FontWeight.Black,
            color = TextMuted,
            textAlign = TextAlign.Center,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(3.dp))
        Text(
            text = value,
            fontSize = 11.2.sp,
            fontWeight = FontWeight.Black,
            color = color,
            textAlign = TextAlign.Center,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.fillMaxWidth()
        )
    }
}

private data class LiveRadarBetaGuardUiState(
    val dataSyncValue: String,
    val dataSyncDelayed: Boolean,
    val btcDelta: String,
    val ecosystemLeaderName: String,
    val ecosystemDelta: String,
    val marketFlow: String,
    val derivatives: String,
    val spreadRisk: String,
    val assetShock: String,
    val readiness: Int,
    val penalty: Int
)

private fun ecosystemLeaderNameFor(symbol: String): String {
    val upper = symbol.uppercase()
    return when {
        upper.contains("ETH") || upper.contains("ARB") || upper.contains("OP") || upper.contains("MATIC") || upper.contains("LINK") || upper.contains("UNI") || upper.contains("AAVE") || upper.contains("SHIB") || upper.contains("PEPE") -> "ETH Leader"
        upper.contains("SOL") || upper.contains("JUP") || upper.contains("PYTH") || upper.contains("WIF") || upper.contains("BONK") || upper.contains("RNDR") || upper.contains("RENDER") -> "SOL Leader"
        upper.contains("BNB") || upper.contains("CAKE") || upper.contains("FLOKI") -> "BNB Leader"
        upper.contains("ADA") -> "ADA Leader"
        upper.contains("DOGE") -> "DOGE Leader"
        upper.contains("AVAX") -> "AVAX Leader"
        upper.contains("NEAR") -> "NEAR Leader"
        else -> "BTC Leader"
    }
}

private fun stableMetricSeed(symbol: String, timeframe: String, isLong: Boolean): Int {
    val raw = "${symbol}_${timeframe}_${isLong}".hashCode()
    return if (raw == Int.MIN_VALUE) 0 else if (raw < 0) -raw else raw
}

private fun guardImpactPenaltyFor(seed: Int): Int {
    return when (seed % 5) {
        0 -> 8
        1 -> 4
        2 -> 6
        else -> 3
    }
}
'''

text = text.rstrip() + helpers + "\n"

checks = {
    "metadata grid calls": text.count("OracleAnalyticMetadataGrid(") >= 4,
    "beta guard calls": text.count("LiveRadarBetaDivergenceGuard(") >= 4,
    "top aligned rows": "verticalAlignment = Alignment.Top" in text,
    "AI guard title": "AI GUARD IMPACT" in text,
    "Validity Entry exact text": "Validity Entry" in text,
    "deep amber warning": "Color(0xFFFF9F0A)" in text,
    "dynamic ecosystem label": "ecosystemLeaderName = ecosystemLeaderName" in text and "private fun ecosystemLeaderNameFor" in text,
    "engine names green": "Text(text = \"Gemini Pro AI\", fontSize = 9.6.sp, fontWeight = FontWeight.Bold, color = CryptoGreen" in text,
    "no hardcoded beta BTC leader tile": "BetaGuardMiniTile(\"BTC Leader\"" not in text,
}

failed = [name for name, ok in checks.items() if not ok]
if failed:
    raise SystemExit("ERROR: patch verification failed: " + ", ".join(failed))

if text == original_text:
    raise SystemExit("ERROR: patch produced no changes. Aborting before commit.")

target.write_text(text, encoding="utf-8")

print("OK: MarketRadarScreen.kt Live Radar UI alignment patch applied.")
print(f"OK: Metadata grid call markers: {text.count('OracleAnalyticMetadataGrid(')}")
print(f"OK: Beta Guard call markers: {text.count('LiveRadarBetaDivergenceGuard(')}")
print("OK: No .bak file was created by this patcher.")
