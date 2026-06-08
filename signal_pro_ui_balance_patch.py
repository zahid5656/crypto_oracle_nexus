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
        raise SystemExit(f"ERROR: Function end not found: {function_name}")

    return source[:start] + replacement + source[end:]


def replace_row_containing(source: str, scope_start: int, scope_end: int, anchor: str, replacement: str) -> str:
    anchor_pos = source.find(anchor, scope_start, scope_end)
    if anchor_pos == -1:
        print(f"WARNING: Row anchor not found: {anchor}")
        return source

    row_start = source.rfind("Row(", scope_start, anchor_pos)
    if row_start == -1:
        print("WARNING: Row start not found.")
        return source

    line_start = source.rfind("\n", 0, row_start)
    line_start = 0 if line_start == -1 else line_start + 1

    brace_start = source.find("{", row_start)
    if brace_start == -1:
        print("WARNING: Row opening brace not found.")
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
        print("WARNING: Row end not found.")
        return source

    return source[:line_start] + replacement + source[end:]


def add_named_arg_to_calls(source: str, function_name: str, arg_text: str, skip_if_contains: str) -> str:
    pattern = re.compile(r"\b" + re.escape(function_name) + r"\s*\(")
    out = []
    last = 0

    for match in pattern.finditer(source):
        line_start = source.rfind("\n", 0, match.start()) + 1
        same_line_prefix = source[line_start:match.start()]

        if "fun " in same_line_prefix:
            continue

        open_paren = source.find("(", match.start())
        depth = 0
        end = None

        for i in range(open_paren, len(source)):
            if source[i] == "(":
                depth += 1
            elif source[i] == ")":
                depth -= 1
                if depth == 0:
                    end = i
                    break

        if end is None:
            continue

        call_text = source[match.start():end + 1]
        if skip_if_contains in call_text:
            continue

        inner = source[open_paren + 1:end].strip()
        insertion = (",\n                        " + arg_text) if inner else arg_text

        out.append(source[last:end])
        out.append(insertion)
        last = end

    out.append(source[last:])
    return "".join(out)


# ------------------------------------------------------------
# 1) Top toolbar: reload + language button balanced like Live Radar
# ------------------------------------------------------------
pd_start = text.find("fun PredictionDashboard(")
toolbar_scope_end = text.find("        Spacer(modifier = Modifier.height(8.dp))", pd_start)
if toolbar_scope_end == -1:
    toolbar_scope_end = text.find("        // Condensed AI Modality and Scan Button", pd_start)

toolbar_replacement = '''            Row(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.wrapContentWidth()
            ) {
                IconButton(
                    onClick = { viewModel.runScanner() },
                    modifier = Modifier
                        .size(36.dp)
                        .background(
                            brush = Brush.radialGradient(
                                colors = listOf(
                                    CryptoCyan.copy(alpha = 0.18f),
                                    Color(0xFF050A13)
                                )
                            ),
                            shape = CircleShape
                        )
                        .border(1.dp, BorderColor, CircleShape)
                ) {
                    Icon(
                        imageVector = Icons.Default.Refresh,
                        contentDescription = "Re-run scanner",
                        tint = CryptoCyan,
                        modifier = Modifier.size(18.dp)
                    )
                }

                SignalProLanguageSwitchButton(
                    isBengali = isBengali,
                    onClick = { viewModel.toggleLanguage() }
                )
            }'''

if pd_start != -1 and toolbar_scope_end != -1:
    text = replace_row_containing(
        source=text,
        scope_start=pd_start,
        scope_end=toolbar_scope_end,
        anchor="SignalProLanguageSwitchButton(",
        replacement=toolbar_replacement
    )


language_button_code = '''@Composable
fun SignalProLanguageSwitchButton(
    isBengali: Boolean,
    onClick: () -> Unit
) {
    Button(
        onClick = onClick,
        colors = ButtonDefaults.buttonColors(
            containerColor = DarkSurface,
            contentColor = CryptoCyan
        ),
        shape = RoundedCornerShape(8.dp),
        border = BorderStroke(1.dp, BorderColor),
        modifier = Modifier
            .height(36.dp)
            .width(82.dp),
        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp)
    ) {
        Text(
            text = if (isBengali) "English" else "বাংলা",
            fontWeight = FontWeight.Bold,
            fontSize = 11.sp,
            maxLines = 1,
            softWrap = false,
            overflow = androidx.compose.ui.text.style.TextOverflow.Clip
        )
    }
}'''

if "fun SignalProLanguageSwitchButton(" in text:
    text = replace_composable_function(text, "SignalProLanguageSwitchButton", language_button_code)
else:
    insert_point = text.find("\n@Composable\nfun TabButton(")
    if insert_point == -1:
        insert_point = text.find("\n@Composable\nfun RealTimeCountdown(")
    if insert_point == -1:
        raise SystemExit("ERROR: Could not find insertion point for SignalProLanguageSwitchButton.")
    text = text[:insert_point] + "\n" + language_button_code + "\n\n" + text[insert_point:]


# ------------------------------------------------------------
# 2) Multi-AI Consensus: proportional text + balanced boxes
# ------------------------------------------------------------
ai_score_tile_code = '''@Composable
fun AiScoreTile(title: String, score: Int, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .height(58.dp)
            .clip(RoundedCornerShape(9.dp))
            .background(
                Brush.linearGradient(
                    listOf(
                        Color(0xFF02050D),
                        Color(0xFF080E18),
                        Color(0xFF02050D)
                    )
                )
            )
            .border(0.75.dp, CryptoCyan.copy(alpha = 0.34f), RoundedCornerShape(9.dp))
            .padding(horizontal = 6.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = title,
                fontSize = 9.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFFDCE5F5),
                maxLines = 1,
                textAlign = TextAlign.Center,
                overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
            )

            Spacer(modifier = Modifier.height(3.dp))

            Text(
                text = "$score/100",
                fontSize = 15.sp,
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.Black,
                color = CryptoGreen,
                maxLines = 1,
                softWrap = false,
                textAlign = TextAlign.Center
            )
        }
    }
}'''

if "fun AiScoreTile(" in text:
    text = replace_composable_function(text, "AiScoreTile", ai_score_tile_code)


consensus_metric_code = '''@Composable
fun ConsensusMetricColumn(
    label: String,
    value: String,
    valueColor: Color,
    modifier: Modifier = Modifier
) {
    val valueSize = when {
        value.length >= 9 -> 12.sp
        value.length >= 6 -> 13.sp
        else -> 15.sp
    }

    Column(
        modifier = modifier
            .heightIn(min = 50.dp)
            .padding(horizontal = 2.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = label,
            fontSize = 7.5.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFFD0D8E8),
            maxLines = 2,
            lineHeight = 8.5.sp,
            textAlign = TextAlign.Center,
            overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
        )

        Spacer(modifier = Modifier.height(3.dp))

        Text(
            text = value,
            fontSize = valueSize,
            fontWeight = FontWeight.Black,
            color = valueColor,
            maxLines = 1,
            softWrap = false,
            textAlign = TextAlign.Center,
            overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
        )
    }
}'''

if "fun ConsensusMetricColumn(" in text:
    text = replace_composable_function(text, "ConsensusMetricColumn", consensus_metric_code)
else:
    insert_point = text.find("\n@Composable\nfun MultiAiConsensusModule(")
    if insert_point != -1:
        text = text[:insert_point] + "\n" + consensus_metric_code + "\n\n" + text[insert_point:]


multi_ai_code = '''@Composable
fun MultiAiConsensusModule(
    coinSymbol: String,
    oracleScore: Int,
    isLong: Boolean,
    isBengali: Boolean = false
) {
    val geminiScore = (oracleScore - 4).coerceIn(60, 99)
    val gptScore = (oracleScore - 8).coerceIn(60, 99)
    val claudeScore = (oracleScore - 5).coerceIn(60, 99)
    val consensusScore = ((geminiScore + gptScore + claudeScore) / 3).coerceIn(0, 100)

    val directionText = if (isBengali) {
        if (isLong) "দাম বাড়ছে" else "দাম কমছে"
    } else {
        if (isLong) "BULLISH" else "BEARISH"
    }

    val riskText = if (isBengali) {
        if (oracleScore >= 85) "কম" else "মাঝারি"
    } else {
        if (oracleScore >= 85) "LOW" else "MEDIUM"
    }

    Card(
        colors = CardDefaults.cardColors(containerColor = Color(0xFF030712)),
        modifier = Modifier
            .fillMaxWidth()
            .border(0.95.dp, CryptoCyan.copy(alpha = 0.62f), RoundedCornerShape(14.dp))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Brush.linearGradient(
                        listOf(
                            Color(0xFF03111B),
                            Color(0xFF0B1220),
                            Color(0xFF02050D)
                        )
                    )
                )
                .padding(14.dp)
        ) {
            Text(
                text = if (isBengali) "মাল্টি-এআই ঐকমত্য ইঞ্জিন" else "MULTI-AI CONSENSUS ENGINES",
                fontSize = 11.sp,
                fontWeight = FontWeight.Black,
                color = CryptoCyan,
                letterSpacing = if (isBengali) 0.sp else 1.2.sp,
                maxLines = 1,
                overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
            )

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                AiScoreTile("Gemini Pro AI", geminiScore, Modifier.weight(1f))
                AiScoreTile("GPT-4o Quant", gptScore, Modifier.weight(1f))
                AiScoreTile("Claude Sentient", claudeScore, Modifier.weight(1f))
            }

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(10.dp))
                    .background(
                        Brush.horizontalGradient(
                            listOf(
                                Color(0xFF04111A),
                                Color(0xFF0B1824),
                                Color(0xFF04111A)
                            )
                        )
                    )
                    .border(0.8.dp, CryptoCyan.copy(alpha = 0.62f), RoundedCornerShape(10.dp))
                    .padding(vertical = 8.dp, horizontal = 6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                ConsensusMetricColumn(
                    label = if (isBengali) "ঐকমত্যের আস্থা" else "CONSENSUS CONFIDENCE",
                    value = "$consensusScore%",
                    valueColor = CryptoCyan,
                    modifier = Modifier.weight(1.25f)
                )

                ConsensusMetricColumn(
                    label = if (isBengali) "দিকনির্দেশ" else "DIRECTION",
                    value = directionText,
                    valueColor = CryptoGreen,
                    modifier = Modifier.weight(1.05f)
                )

                ConsensusMetricColumn(
                    label = if (isBengali) "রিস্ক প্রোফাইল" else "RISK PROFILE",
                    value = riskText,
                    valueColor = CryptoGreen,
                    modifier = Modifier.weight(1.0f)
                )
            }
        }
    }
}'''

if "fun MultiAiConsensusModule(" in text:
    text = replace_composable_function(text, "MultiAiConsensusModule", multi_ai_code)


# ------------------------------------------------------------
# 3) AI Oracle Analytics heatmap: stacked label/value cards
# ------------------------------------------------------------
ai_explanation_code = '''@Composable
fun AiExplanationModule(
    whyEnglish: String,
    whyBengali: String,
    coinSymbol: String,
    isBengali: Boolean,
    onToggleLanguage: () -> Unit
) {
    val rotation by animateFloatAsState(
        targetValue = if (isBengali) 180f else 0f,
        animationSpec = spring(stiffness = Spring.StiffnessLow)
    )

    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = if (isBengali) "এআই ওরাকলের বিশ্লেষণমূলক তথ্য" else "AI ORACLE ANALYTICS COGNITION",
            fontSize = 11.sp,
            fontWeight = FontWeight.Black,
            color = CryptoCyan,
            letterSpacing = if (isBengali) 0.sp else 1.sp,
            maxLines = 1,
            overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
        )

        Spacer(modifier = Modifier.height(8.dp))

        Card(
            colors = CardDefaults.cardColors(containerColor = Color(0xFF030712)),
            modifier = Modifier
                .fillMaxWidth()
                .border(0.95.dp, CryptoCyan.copy(alpha = 0.62f), RoundedCornerShape(12.dp))
                .graphicsLayer {
                    rotationY = rotation
                    cameraDistance = 14 * density
                    shape = RoundedCornerShape(12.dp)
                }
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        Brush.linearGradient(
                            listOf(
                                Color(0xFF03111B),
                                Color(0xFF0B1220),
                                Color(0xFF02050D)
                            )
                        )
                    )
                    .padding(horizontal = 12.dp, vertical = 12.dp)
            ) {
                if (rotation <= 90f) {
                    Column {
                        Text(
                            text = whyEnglish,
                            fontSize = 13.sp,
                            color = Color(0xFFF4F8FF),
                            lineHeight = 18.sp
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        Text(
                            text = "QUANTITATIVE HEATMAP SIGNALS",
                            fontSize = 9.sp,
                            color = CryptoCyan,
                            fontWeight = FontWeight.Black,
                            maxLines = 1,
                            overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                        )

                        Spacer(modifier = Modifier.height(6.dp))

                        HeatmapSignalsAlignedRow(
                            firstLabel = "Trend",
                            firstValue = "STRONG",
                            firstColor = CryptoGreen,
                            secondLabel = "Momentum",
                            secondValue = "HOT",
                            secondColor = AcceleratorCyanColor(coinSymbol),
                            thirdLabel = "Volume",
                            thirdValue = "ACCUMULATING",
                            thirdColor = AccentGold
                        )
                    }
                } else {
                    Column(
                        modifier = Modifier.graphicsLayer { rotationY = 180f }
                    ) {
                        Text(
                            text = whyBengali,
                            fontSize = 13.sp,
                            color = Color(0xFFF4F8FF),
                            lineHeight = 18.sp
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        Text(
                            text = "পরিমাণগত হিটম্যাপ সিগন্যাল",
                            fontSize = 9.sp,
                            color = CryptoCyan,
                            fontWeight = FontWeight.Black,
                            maxLines = 1,
                            overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                        )

                        Spacer(modifier = Modifier.height(6.dp))

                        HeatmapSignalsAlignedRow(
                            firstLabel = "ট্রেন্ড",
                            firstValue = "শক্তিশালী",
                            firstColor = CryptoGreen,
                            secondLabel = "মতিগতি",
                            secondValue = "তীব্র",
                            secondColor = AcceleratorCyanColor(coinSymbol),
                            thirdLabel = "লেনদেন",
                            thirdValue = "সঞ্চয় হচ্ছে",
                            thirdColor = AccentGold
                        )
                    }
                }
            }
        }
    }
}'''

if "fun AiExplanationModule(" in text:
    text = replace_composable_function(text, "AiExplanationModule", ai_explanation_code)


heatmap_row_code = '''@Composable
fun HeatmapSignalsAlignedRow(
    firstLabel: String,
    firstValue: String,
    firstColor: Color,
    secondLabel: String,
    secondValue: String,
    secondColor: Color,
    thirdLabel: String,
    thirdValue: String,
    thirdColor: Color
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        InsightMetricPill(
            label = firstLabel,
            value = firstValue,
            valueColor = firstColor,
            modifier = Modifier.weight(1f)
        )

        InsightMetricPill(
            label = secondLabel,
            value = secondValue,
            valueColor = secondColor,
            modifier = Modifier.weight(1f)
        )

        InsightMetricPill(
            label = thirdLabel,
            value = thirdValue,
            valueColor = thirdColor,
            modifier = Modifier.weight(1.25f)
        )
    }
}'''

if "fun HeatmapSignalsAlignedRow(" in text:
    text = replace_composable_function(text, "HeatmapSignalsAlignedRow", heatmap_row_code)
else:
    insert_point = text.find("\n@Composable\nfun InsightMetricPill(")
    if insert_point == -1:
        raise SystemExit("ERROR: Could not find insertion point for HeatmapSignalsAlignedRow.")
    text = text[:insert_point] + "\n" + heatmap_row_code + "\n\n" + text[insert_point:]


insight_pill_code = '''@Composable
fun InsightMetricPill(
    label: String,
    value: String,
    valueColor: Color,
    modifier: Modifier = Modifier
) {
    val valueSize = when {
        value.length >= 11 -> 8.4.sp
        value.length >= 8 -> 9.2.sp
        else -> 10.5.sp
    }

    Column(
        modifier = modifier
            .height(48.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(
                brush = Brush.linearGradient(
                    colors = listOf(
                        Color(0xFF02050D),
                        Color(0xFF08111C),
                        Color(0xFF02050D)
                    )
                )
            )
            .border(0.75.dp, valueColor.copy(alpha = 0.50f), RoundedCornerShape(8.dp))
            .padding(horizontal = 5.dp, vertical = 6.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = label,
            fontSize = 8.5.sp,
            color = Color(0xFFD3DAE8),
            fontWeight = FontWeight.Bold,
            maxLines = 1,
            softWrap = false,
            textAlign = TextAlign.Center,
            overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
        )

        Spacer(modifier = Modifier.height(2.dp))

        Text(
            text = value,
            fontSize = valueSize,
            fontWeight = FontWeight.Black,
            color = valueColor,
            maxLines = 1,
            softWrap = false,
            textAlign = TextAlign.Center,
            overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
        )
    }
}'''

if "fun InsightMetricPill(" in text:
    text = replace_composable_function(text, "InsightMetricPill", insight_pill_code)


# ------------------------------------------------------------
# 4) Signal Quality Engine Index: centered metrics + Bangla support
# ------------------------------------------------------------
quality_metric_code = '''@Composable
fun QualityMetricColumn(
    label: String,
    value: String,
    valueColor: Color,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.heightIn(min = 42.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = label,
            fontSize = 8.sp,
            color = TextMuted,
            fontWeight = FontWeight.Bold,
            maxLines = 1,
            textAlign = TextAlign.Center,
            overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
        )

        Spacer(modifier = Modifier.height(3.dp))

        Text(
            text = value,
            fontSize = 12.sp,
            color = valueColor,
            fontWeight = FontWeight.Bold,
            fontFamily = FontFamily.Monospace,
            maxLines = 1,
            textAlign = TextAlign.Center,
            overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
        )
    }
}'''

if "fun QualityMetricColumn(" in text:
    text = replace_composable_function(text, "QualityMetricColumn", quality_metric_code)
else:
    insert_point = text.find("\n@Composable\nfun SignalQualitySystemBlock(")
    if insert_point != -1:
        text = text[:insert_point] + "\n" + quality_metric_code + "\n\n" + text[insert_point:]


signal_quality_code = '''@Composable
fun SignalQualitySystemBlock(
    score: Int,
    confidence: Int,
    probability: Int,
    riskGrade: String,
    isBengali: Boolean = false
) {
    val indicator = when {
        score >= 90 -> if (isBengali) "ইনস্টিটিউশনাল মান" else "Institutional Grade"
        score >= 82 -> if (isBengali) "উচ্চ আস্থা" else "High Confidence"
        score >= 70 -> if (isBengali) "শক্তিশালী" else "Strong"
        score >= 55 -> if (isBengali) "মাঝারি" else "Moderate"
        else -> if (isBengali) "দুর্বল" else "Weak"
    }

    val riskText = when (riskGrade.uppercase()) {
        "LOW" -> if (isBengali) "কম" else "LOW"
        "MEDIUM" -> if (isBengali) "মাঝারি" else "MEDIUM"
        "HIGH" -> if (isBengali) "তীব্র" else "HIGH"
        "EXTREME" -> if (isBengali) "খুব বেশি" else "EXTREME"
        else -> riskGrade
    }

    val riskColor = when (riskGrade.uppercase()) {
        "LOW" -> CryptoGreen
        "MEDIUM" -> AccentGold
        "HIGH", "EXTREME" -> Color(0xFFFF3F60)
        else -> AccentGold
    }

    val themeColor = when {
        score >= 82 -> CryptoCyan
        score >= 70 -> CryptoGreen
        score >= 55 -> AccentGold
        else -> Color(0xFFFF3F60)
    }

    Card(
        colors = CardDefaults.cardColors(containerColor = Color(0xFF050A13)),
        modifier = Modifier
            .fillMaxWidth()
            .border(0.95.dp, CryptoCyan.copy(alpha = 0.62f), RoundedCornerShape(12.dp)),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Text(
                text = if (isBengali) "সিগন্যাল মান যাচাই সূচক" else "SIGNAL QUALITY ENGINE INDEX",
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                color = TextSecondary,
                letterSpacing = if (isBengali) 0.sp else 1.sp,
                maxLines = 1,
                overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
            )

            Spacer(modifier = Modifier.height(10.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = if (isBengali) "শ্রেণি" else "CLASSIFICATION",
                        fontSize = 8.sp,
                        color = TextMuted,
                        maxLines = 1
                    )

                    Text(
                        text = if (isBengali) indicator else indicator.uppercase(),
                        fontSize = if (isBengali) 12.sp else 13.sp,
                        fontWeight = FontWeight.Black,
                        color = themeColor,
                        maxLines = 1,
                        overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                    )
                }

                Box(
                    modifier = Modifier
                        .background(themeColor.copy(alpha = 0.12f), RoundedCornerShape(6.dp))
                        .padding(horizontal = 8.dp, vertical = 3.dp)
                ) {
                    Text(
                        text = "CQI: $score/100",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = themeColor,
                        maxLines = 1
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))
            HorizontalDivider(color = BorderColor.copy(alpha = 0.4f))
            Spacer(modifier = Modifier.height(10.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                QualityMetricColumn(
                    label = if (isBengali) "আস্থা" else "CONFIDENCE",
                    value = "$confidence%",
                    valueColor = TextPrimary,
                    modifier = Modifier.weight(1f)
                )

                QualityMetricColumn(
                    label = if (isBengali) "সম্ভাবনা" else "PROBABILITY",
                    value = "$probability%",
                    valueColor = TextPrimary,
                    modifier = Modifier.weight(1f)
                )

                QualityMetricColumn(
                    label = if (isBengali) "রিস্ক" else "RISK SCORE",
                    value = riskText,
                    valueColor = riskColor,
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}'''

if "fun SignalQualitySystemBlock(" in text:
    text = replace_composable_function(text, "SignalQualitySystemBlock", signal_quality_code)


# ------------------------------------------------------------
# 5) Institutional Confirmation Checklist: Bangla support
# ------------------------------------------------------------
checklist_code = '''@Composable
fun TradeChecklistBlock(
    trendConfirmed: Boolean,
    volumeConfirmed: Boolean,
    momentumConfirmed: Boolean,
    liquidityConfirmed: Boolean,
    riskEvaluated: Boolean,
    isBengali: Boolean = false
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = Color(0xFF050A13)),
        modifier = Modifier
            .fillMaxWidth()
            .border(0.95.dp, CryptoCyan.copy(alpha = 0.62f), RoundedCornerShape(12.dp)),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Text(
                text = if (isBengali) "ইনস্টিটিউশনাল নিশ্চিতকরণ তালিকা" else "INSTITUTIONAL CONFIRMATION CHECKLIST",
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                color = TextSecondary,
                letterSpacing = if (isBengali) 0.sp else 1.sp,
                maxLines = 1,
                overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
            )

            Spacer(modifier = Modifier.height(10.dp))

            val items = listOf(
                (if (isBengali) "বাজারের দিক নিশ্চিত" else "Trend Confirmed") to trendConfirmed,
                (if (isBengali) "লেনদেন নিশ্চিত" else "Volume Confirmed") to volumeConfirmed,
                (if (isBengali) "মতিগতির জোর নিশ্চিত" else "Momentum Confirmed") to momentumConfirmed,
                (if (isBengali) "নিরাপদ তহবিল নিশ্চিত" else "Liquidity Confirmed") to liquidityConfirmed,
                (if (isBengali) "ঝুঁকি যাচাইকৃত" else "Risk Evaluated") to riskEvaluated
            )

            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                items.forEach { (label, checked) ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(14.dp)
                                .clip(CircleShape)
                                .background(if (checked) CryptoGreen.copy(alpha = 0.15f) else Color(0xFFFF3F60).copy(alpha = 0.15f))
                                .border(1.dp, if (checked) CryptoGreen else Color(0xFFFF3F60), CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            if (checked) {
                                Icon(
                                    imageVector = Icons.Default.Check,
                                    contentDescription = "Passed",
                                    tint = CryptoGreen,
                                    modifier = Modifier.size(9.dp)
                                )
                            } else {
                                Box(modifier = Modifier.size(4.dp).background(Color(0xFFFF3F60), CircleShape))
                            }
                        }

                        Spacer(modifier = Modifier.width(8.dp))

                        Text(
                            text = label,
                            fontSize = if (isBengali) 11.5.sp else 11.sp,
                            fontWeight = FontWeight.Medium,
                            color = if (checked) TextPrimary else TextMuted,
                            maxLines = 1,
                            overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                        )
                    }
                }
            }
        }
    }
}'''

if "fun TradeChecklistBlock(" in text:
    text = replace_composable_function(text, "TradeChecklistBlock", checklist_code)


# ------------------------------------------------------------
# 6) Dynamic Persisted Regime Trace: Bangla market-state mapping
# ------------------------------------------------------------
regime_code = '''@Composable
fun MarketRegimeTraceModule(
    coinSymbol: String,
    isBengali: Boolean = false
) {
    val seed = coinSymbol.hashCode().absoluteValue
    val regimes = listOf("BULLISH", "BEARISH", "SIDEWAYS", "ACCUMULATION", "DISTRIBUTION")
    val regime = regimes[seed % regimes.size]

    val regimeText = when (regime) {
        "BULLISH" -> if (isBengali) "দাম বাড়ার ভাব" else "BULLISH"
        "BEARISH" -> if (isBengali) "মন্দা ভাব" else "BEARISH"
        "SIDEWAYS" -> if (isBengali) "দাম স্থির ভাব" else "SIDEWAYS"
        "ACCUMULATION" -> if (isBengali) "সঞ্চয় হচ্ছে" else "ACCUMULATION"
        else -> if (isBengali) "বিক্রির চাপ" else "DISTRIBUTION"
    }

    val statusText = if (isBengali) "বর্তমানে সক্রিয়" else "ACTIVE DURING INSIGHT"

    val description = when(regime) {
        "BULLISH" -> if (isBengali) {
            "বাজারে ক্রেতার চাপ বেশি, দাম উপরে যাওয়ার সম্ভাবনা আছে।"
        } else {
            "High liquidity markup phase driven by strong smart money orders."
        }

        "BEARISH" -> if (isBengali) {
            "বাজারে বিক্রির চাপ বেশি, দাম নিচে যাওয়ার ঝুঁকি আছে।"
        } else {
            "Markdown liquidations under persistent offer pressure."
        }

        "SIDEWAYS" -> if (isBengali) {
            "দাম নির্দিষ্ট রেঞ্জে ঘুরছে, বড় ব্রেকের জন্য অপেক্ষা করছে।"
        } else {
            "Range bound bracket with low volatility waiting for core breaks."
        }

        "ACCUMULATION" -> if (isBengali) {
            "বড় ক্রেতারা ধীরে ধীরে পজিশন তৈরি করছে।"
        } else {
            "Institutional accumulation in value brackets."
        }

        else -> if (isBengali) {
            "উচ্চ দামে বিক্রির চাপ তৈরি হচ্ছে।"
        } else {
            "Smart money distribution at premium resistance heights."
        }
    }

    val tint = when(regime) {
        "BULLISH" -> CryptoGreen
        "BEARISH" -> Color(0xFFFF3F60)
        "SIDEWAYS" -> TextMuted
        "ACCUMULATION" -> CryptoCyan
        else -> AccentGold
    }

    Card(
        colors = CardDefaults.cardColors(containerColor = Color(0xFF050A13)),
        modifier = Modifier
            .fillMaxWidth()
            .border(0.95.dp, CryptoCyan.copy(alpha = 0.62f), RoundedCornerShape(12.dp)),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Text(
                text = if (isBengali) "চলতি বাজারের মতিগতি" else "PERSISTED REGIME TRACE",
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                color = TextSecondary,
                letterSpacing = if (isBengali) 0.sp else 1.sp,
                maxLines = 1,
                overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
            )

            Spacer(modifier = Modifier.height(10.dp))

            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .background(tint.copy(alpha = 0.15f), RoundedCornerShape(4.dp))
                        .padding(horizontal = 8.dp, vertical = 3.dp)
                ) {
                    Text(
                        text = regimeText,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = tint,
                        maxLines = 1
                    )
                }

                Spacer(modifier = Modifier.width(8.dp))

                Text(
                    text = "|",
                    fontSize = 10.sp,
                    color = TextMuted,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.width(8.dp))

                Text(
                    text = statusText,
                    fontSize = if (isBengali) 10.sp else 8.sp,
                    color = TextMuted,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = description,
                fontSize = 11.sp,
                color = TextSecondary,
                lineHeight = 15.sp
            )
        }
    }
}'''

if "fun MarketRegimeTraceModule(" in text:
    text = replace_composable_function(text, "MarketRegimeTraceModule", regime_code)


# ------------------------------------------------------------
# 7) Pass isBengali into language-aware blocks
# ------------------------------------------------------------
text = add_named_arg_to_calls(text, "MultiAiConsensusModule", "isBengali = isBengali", "isBengali")
text = add_named_arg_to_calls(text, "SignalQualitySystemBlock", "isBengali = isBengali", "isBengali")
text = add_named_arg_to_calls(text, "TradeChecklistBlock", "isBengali = isBengali", "isBengali")
text = add_named_arg_to_calls(text, "MarketRegimeTraceModule", "isBengali = isBengali", "isBengali")


# ------------------------------------------------------------
# 8) Save approved Bengali glossary
# ------------------------------------------------------------
target.write_text(text, encoding="utf-8")

glossary = Path("docs/bengali_translation_glossary.md")
glossary.parent.mkdir(parents=True, exist_ok=True)
glossary.write_text("""# Crypto Oracle Nexus Bengali Translation Glossary

Project-level Bengali translation source of truth.

Owner override rule:
Manually selected simple Bengali wording must be preserved.
Use simple Bengali that normal traders can understand.

| English | Bengali |
|---|---|
| VALIDITY WINDOW | বৈধতার নির্দিষ্ট মেয়াদ |
| AI ORACLE ANALYTICS COGNITION | এআই ওরাকলের বিশ্লেষণমূলক তথ্য |
| AI Decision Brief | AI সিদ্ধান্ত সংক্ষেপ |
| High Confidence | উচ্চ আস্থা |
| Verify Entry | এন্ট্রি যাচাই |
| CONFIDENCE | আস্থা |
| PROBABILITY | সম্ভাবনা |
| RISK SCORE | রিস্ক |
| LOW | কম |
| MEDIUM | মাঝারি |
| HIGH | তীব্র |
| EXTREME | খুব বেশি |
| Trend Confirmed | বাজারের দিক নিশ্চিত |
| Volume Confirmed | লেনদেন নিশ্চিত |
| Momentum Confirmed | মতিগতির জোর নিশ্চিত |
| Liquidity Confirmed | নিরাপদ তহবিল নিশ্চিত |
| Risk Evaluated | ঝুঁকি যাচাইকৃত |
| PERSISTED REGIME TRACE | চলতি বাজারের মতিগতি |
| BULLISH | দাম বাড়ার ভাব |
| BEARISH | মন্দা ভাব |
| SIDEWAYS | দাম স্থির ভাব |
| ACCUMULATION | সঞ্চয় হচ্ছে |
| DISTRIBUTION | বিক্রির চাপ |
| ACTIVE DURING INSIGHT | বর্তমানে সক্রিয় |
| Momentum | মতিগতি |
| Volume | লেনদেন |
| ACCUMULATING | সঞ্চয় হচ্ছে |

Keep prices, ROI, TP, SL, score, rank, coin symbols, and percentages unchanged.
""", encoding="utf-8")

print("OK: Signal Pro UI balance patch applied.")
