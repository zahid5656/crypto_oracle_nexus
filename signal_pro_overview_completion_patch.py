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


def replace_balanced_row_containing(source: str, scope_start: int, scope_end: int, anchor: str, replacement: str) -> str:
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


def append_arg_to_calls(source: str, function_name: str, arg_text: str, skip_if_contains: str) -> str:
    pattern = re.compile(r"(?<!fun\s)\b" + re.escape(function_name) + r"\s*\(")
    out = []
    last = 0

    for match in pattern.finditer(source):
        prefix = source[max(0, match.start() - 30):match.start()]
        if "fun " in prefix:
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


def remove_textbutton_containing(source: str, phrase: str) -> str:
    pos = source.find(phrase)
    if pos == -1:
        return source

    start = source.rfind("TextButton(", 0, pos)
    if start == -1:
        start = source.rfind("Button(", 0, pos)
    if start == -1:
        return source

    line_start = source.rfind("\n", 0, start)
    line_start = 0 if line_start == -1 else line_start + 1

    brace_start = source.find("{", start)
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

    if end < len(source) and source[end] == "\n":
        end += 1

    return source[:line_start] + source[end:]


# ------------------------------------------------------------
# 1) Toolbar refinement: Live Radar style language button + compact reload alignment
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
                        .border(0.8.dp, CryptoCyan.copy(alpha = 0.60f), CircleShape)
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
    text = replace_balanced_row_containing(
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
# 2) AI Recommendation tile + AI Decision Brief bottom popup
# ------------------------------------------------------------
start_trade_flow_code = '''@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StartTradeFlow(
    viewModel: CryptoViewModel,
    mission: com.example.model.Mission,
    livePrice: Double = mission.entryPrice
) {
    var step by remember { mutableStateOf(0) }
    var showDecisionBrief by remember { mutableStateOf(false) }
    val isBengali by viewModel.isBengali.collectAsState()

    val highConfidence = mission.confidence >= 85
    val isLong = mission.type.uppercase() == "LONG"

    val recommendationText = when {
        isBengali && highConfidence && isLong -> "উচ্চ আস্থা | এন্ট্রি যাচাই"
        isBengali && highConfidence && !isLong -> "উচ্চ আস্থা | শর্ট যাচাই"
        isBengali && !highConfidence -> "সতর্কভাবে যাচাই করুন"
        !isBengali && highConfidence -> "High Confidence | Verify Entry"
        else -> "Review Carefully | Verify Entry"
    }

    val verdictText = when {
        isBengali && highConfidence -> "সিগন্যাল শক্তিশালী, তবে এন্ট্রি যাচাই করে নিন।"
        isBengali -> "সিগন্যাল কার্যকর, তবে ঝুঁকি যাচাই করা জরুরি।"
        highConfidence -> "Signal is strong, but entry confirmation is still required."
        else -> "Signal is active, but risk review is required before action."
    }

    val whyText = if (isBengali) {
        "ট্রেন্ড, মতিগতি, লেনদেন, AI consensus এবং risk profile মিলিয়ে এই setup তৈরি হয়েছে।"
    } else {
        "This setup combines trend, momentum, volume, AI consensus, and risk profile signals."
    }

    val riskText = if (isBengali) {
        if (highConfidence) "রিস্ক কম থেকে মাঝারি। Stop loss এবং position size মেনে চলুন।"
        else "রিস্ক মাঝারি। দেরিতে entry নিলে signal quality কমতে পারে।"
    } else {
        if (highConfidence) "Risk is low to medium. Follow stop loss and position sizing."
        else "Risk is medium. Late entry may reduce signal quality."
    }

    val actionText = if (isBengali) {
        if (isLong) "এন্ট্রি price, stop loss এবং target মিলিয়ে তারপর Accept Signal করুন।"
        else "শর্ট এন্ট্রি, stop loss এবং target মিলিয়ে তারপর Accept Signal করুন।"
    } else {
        if (isLong) "Verify entry price, stop loss, and target before accepting the signal."
        else "Verify short entry, stop loss, and target before accepting the signal."
    }

    val disclaimerText = if (isBengali) {
        "এআই সিদ্ধান্তে সহায়তা করে; চূড়ান্ত ট্রেডিং সিদ্ধান্ত আপনার।"
    } else {
        "AI assists decision-making; the final trading decision is yours."
    }

    if (showDecisionBrief) {
        ModalBottomSheet(
            onDismissRequest = { showDecisionBrief = false },
            containerColor = Color(0xFF030712),
            contentColor = TextPrimary
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 18.dp, vertical = 10.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text(
                    text = if (isBengali) "AI সিদ্ধান্ত সংক্ষেপ" else "AI Decision Brief",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Black,
                    color = CryptoCyan
                )

                Text(
                    text = if (isBengali) "দ্রুত সিদ্ধান্ত নেওয়ার জন্য সংক্ষিপ্ত সারাংশ" else "Compact signal summary for faster decision-making",
                    fontSize = 11.sp,
                    color = TextSecondary
                )

                DecisionBriefBlock(
                    title = if (isBengali) "সিগন্যাল রায়" else "Signal Verdict",
                    value = verdictText,
                    accentColor = if (highConfidence) CryptoGreen else AccentGold
                )

                DecisionBriefBlock(
                    title = if (isBengali) "কেন গুরুত্বপূর্ণ" else "Why It Matters",
                    value = whyText,
                    accentColor = CryptoCyan
                )

                DecisionBriefBlock(
                    title = if (isBengali) "ঝুঁকির সতর্কতা" else "Risk Warning",
                    value = riskText,
                    accentColor = AccentGold
                )

                DecisionBriefBlock(
                    title = if (isBengali) "প্রস্তাবিত কাজ" else "Suggested Action",
                    value = actionText,
                    accentColor = CryptoGreen
                )

                Text(
                    text = disclaimerText,
                    fontSize = 10.sp,
                    color = TextMuted,
                    lineHeight = 14.sp
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    TextButton(
                        onClick = { showDecisionBrief = false },
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(
                            text = if (isBengali) "বন্ধ করুন" else "Close",
                            color = TextSecondary
                        )
                    }

                    Button(
                        onClick = {
                            showDecisionBrief = false
                            step = 1
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = CryptoGreen),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(
                            text = if (isBengali) "সিগন্যাল নিন" else "Accept Signal",
                            color = DarkBackground,
                            fontWeight = FontWeight.Black
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))
            }
        }
    }

    if (step == 1) {
        val verifiedEntryLocked = remember { livePrice }

        AlertDialog(
            onDismissRequest = { step = 0 },
            title = {
                Text(
                    text = if (isBengali) "ট্রেড যাচাই করুন" else "Verify Trade Details",
                    color = CryptoCyan,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Column {
                    Text(
                        text = if (isBengali) "দিক: ${mission.type} (${mission.marketType})" else "Direction: ${mission.type} (${mission.marketType})",
                        color = TextSecondary,
                        fontSize = 14.sp
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = if (isBengali) "লকড এন্ট্রি প্রাইস:" else "Locked Entry Price:",
                        color = TextSecondary,
                        fontSize = 12.sp
                    )
                    Text(
                        text = String.format("$%.4f", verifiedEntryLocked),
                        color = TextPrimary,
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Black,
                        fontFamily = FontFamily.Monospace
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = if (isBengali) {
                            "Accept করার পর এই entry personal mission হিসেবে track হবে।"
                        } else {
                            "Once accepted, this entry will activate personal mission tracking."
                        },
                        color = AccentGold,
                        fontSize = 10.sp
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.startMission(
                            mission.copy(
                                id = java.util.UUID.randomUUID().toString(),
                                entryPrice = verifiedEntryLocked,
                                startTime = System.currentTimeMillis()
                            )
                        )
                        viewModel.sendLocalAlert("Mission Started", "AI intelligence system successfully started monitoring ${mission.coinSymbol}")
                        step = 0
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = CryptoGreen)
                ) {
                    Text(
                        text = if (isBengali) "মিশন চালু করুন" else "CONFIRM MISSION",
                        fontWeight = FontWeight.Black,
                        color = DarkBackground
                    )
                }
            },
            dismissButton = {
                TextButton(onClick = { step = 0 }) {
                    Text(
                        text = if (isBengali) "বাতিল" else "Cancel",
                        color = TextSecondary
                    )
                }
            },
            containerColor = Color(0xFF030712),
            titleContentColor = TextPrimary,
            textContentColor = TextSecondary
        )
    }

    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.97f else 1f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessLow),
        label = "ButtonScale"
    )

    val infiniteTransition = rememberInfiniteTransition(label = "AcceptFlowPulse")
    val pulseAlpha by infiniteTransition.animateFloat(
        initialValue = 0.48f,
        targetValue = 0.84f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "PulseAlpha"
    )

    val recoSweepX by infiniteTransition.animateFloat(
        initialValue = -650f,
        targetValue = 650f,
        animationSpec = infiniteRepeatable(
            animation = tween(7200, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "RecommendationSweepX"
    )

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .weight(1f)
                .height(40.dp)
                .clip(RoundedCornerShape(10.dp))
                .background(
                    brush = Brush.linearGradient(
                        colors = listOf(
                            Color(0xFF02050D),
                            Color(0xFF0B1220),
                            Color(0xFF02050D)
                        )
                    )
                )
                .background(
                    brush = Brush.horizontalGradient(
                        colors = listOf(
                            CryptoCyan.copy(alpha = 0.10f),
                            CryptoGreen.copy(alpha = 0.08f),
                            CryptoCyan.copy(alpha = 0.10f)
                        )
                    )
                )
                .background(
                    brush = Brush.horizontalGradient(
                        colors = listOf(
                            Color.Transparent,
                            CryptoCyan.copy(alpha = 0.42f),
                            Color.White.copy(alpha = 0.12f),
                            CryptoGreen.copy(alpha = 0.26f),
                            Color.Transparent
                        ),
                        startX = recoSweepX,
                        endX = recoSweepX + 520f
                    )
                )
                .border(0.8.dp, CryptoCyan.copy(alpha = 0.66f), RoundedCornerShape(10.dp))
                .clickable { showDecisionBrief = true }
                .padding(horizontal = 8.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = recommendationText,
                fontSize = 10.sp,
                fontWeight = FontWeight.Black,
                color = Color(0xFFF4F8FF),
                textAlign = TextAlign.Center,
                maxLines = 2,
                lineHeight = 12.sp,
                overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
            )
        }

        Box(
            modifier = Modifier
                .scale(scale)
                .height(40.dp)
                .widthIn(min = 128.dp, max = 164.dp)
                .background(
                    brush = Brush.linearGradient(
                        colors = listOf(
                            CryptoCyan.copy(alpha = pulseAlpha),
                            CryptoGreen.copy(alpha = pulseAlpha)
                        )
                    ),
                    shape = RoundedCornerShape(10.dp)
                )
                .border(0.8.dp, Color.White.copy(alpha = 0.28f), RoundedCornerShape(10.dp))
                .clickable(
                    interactionSource = interactionSource,
                    indication = LocalIndication.current,
                    onClick = { step = 1 }
                )
                .padding(horizontal = 10.dp),
            contentAlignment = Alignment.Center
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.PlayArrow,
                    contentDescription = "Start Trade",
                    tint = Color.White,
                    modifier = Modifier.size(15.dp)
                )

                Spacer(modifier = Modifier.width(5.dp))

                Text(
                    text = "ACCEPT SIGNAL",
                    fontWeight = FontWeight.Black,
                    fontSize = 11.sp,
                    color = Color.White,
                    letterSpacing = 1.sp,
                    maxLines = 1,
                    softWrap = false
                )
            }
        }
    }
}'''

text = replace_composable_function(text, "StartTradeFlow", start_trade_flow_code)


decision_brief_block_code = '''@Composable
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
            .padding(horizontal = 10.dp, vertical = 8.dp)
    ) {
        Text(
            text = title,
            fontSize = 10.sp,
            fontWeight = FontWeight.Black,
            color = accentColor,
            maxLines = 1
        )

        Spacer(modifier = Modifier.height(3.dp))

        Text(
            text = value,
            fontSize = 12.sp,
            color = TextPrimary,
            lineHeight = 16.sp
        )
    }
}'''

if "fun DecisionBriefBlock(" not in text:
    insert_point = text.find("\n@OptIn(ExperimentalMaterial3Api::class)\n@Composable\nfun StartTradeFlow(")
    if insert_point == -1:
        insert_point = text.find("\n@Composable\nfun StartTradeFlow(")
    if insert_point == -1:
        raise SystemExit("ERROR: Could not find insertion point for DecisionBriefBlock.")
    text = text[:insert_point] + "\n" + decision_brief_block_code + "\n\n" + text[insert_point:]
else:
    text = replace_composable_function(text, "DecisionBriefBlock", decision_brief_block_code)


# ------------------------------------------------------------
# 3) AI Oracle Analytics heatmap: stacked readable layout
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

text = replace_composable_function(text, "AiExplanationModule", ai_explanation_code)


heatmap_helper = '''@Composable
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
    text = replace_composable_function(text, "HeatmapSignalsAlignedRow", heatmap_helper)
else:
    insert_point = text.find("\n@Composable\nfun InsightMetricPill(")
    if insert_point == -1:
        raise SystemExit("ERROR: Could not find InsightMetricPill insertion point.")
    text = text[:insert_point] + "\n" + heatmap_helper + "\n\n" + text[insert_point:]


insight_pill_code = '''@Composable
fun InsightMetricPill(
    label: String,
    value: String,
    valueColor: Color,
    modifier: Modifier = Modifier
) {
    val valueSize = if (value.length >= 11) 8.4.sp else 10.5.sp

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

text = replace_composable_function(text, "InsightMetricPill", insight_pill_code)


# ------------------------------------------------------------
# 4) Signal Quality Engine Index: center alignment + Bangla labels
# ------------------------------------------------------------
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
        else -> if (isBengali) riskGrade else riskGrade
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
                letterSpacing = if (isBengali) 0.sp else 1.sp
            )

            Spacer(modifier = Modifier.height(10.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = if (isBengali) "শ্রেণি" else "CLASSIFICATION",
                        fontSize = 8.sp,
                        color = TextMuted
                    )

                    Text(
                        text = if (isBengali) indicator else indicator.uppercase(),
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Black,
                        color = themeColor
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
                        color = themeColor
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

text = replace_composable_function(text, "SignalQualitySystemBlock", signal_quality_code)


quality_metric_code = '''@Composable
fun QualityMetricColumn(
    label: String,
    value: String,
    valueColor: Color,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally
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

        Spacer(modifier = Modifier.height(2.dp))

        Text(
            text = value,
            fontSize = 12.sp,
            color = valueColor,
            fontWeight = FontWeight.Bold,
            fontFamily = FontFamily.Monospace,
            maxLines = 1,
            textAlign = TextAlign.Center
        )
    }
}'''

if "fun QualityMetricColumn(" not in text:
    insert_point = text.find("\n@Composable\nfun SignalQualitySystemBlock(")
    if insert_point == -1:
        raise SystemExit("ERROR: Could not find insertion point for QualityMetricColumn.")
    text = text[:insert_point] + "\n" + quality_metric_code + "\n\n" + text[insert_point:]
else:
    text = replace_composable_function(text, "QualityMetricColumn", quality_metric_code)


# ------------------------------------------------------------
# 5) Institutional Confirmation Checklist Bangla support
# ------------------------------------------------------------
trade_checklist_code = '''@Composable
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
                letterSpacing = if (isBengali) 0.sp else 1.sp
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

text = replace_composable_function(text, "TradeChecklistBlock", trade_checklist_code)


# ------------------------------------------------------------
# 6) Dynamic Persisted Regime Trace Bangla support
# ------------------------------------------------------------
market_regime_code = '''@Composable
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
                letterSpacing = if (isBengali) 0.sp else 1.sp
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
                        fontSize = if (isBengali) 11.sp else 11.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = tint
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

text = replace_composable_function(text, "MarketRegimeTraceModule", market_regime_code)


# ------------------------------------------------------------
# 7) Pass language state into blocks
# ------------------------------------------------------------
text = append_arg_to_calls(text, "SignalQualitySystemBlock", "isBengali = isBengali", "isBengali")
text = append_arg_to_calls(text, "TradeChecklistBlock", "isBengali = isBengali", "isBengali")
text = append_arg_to_calls(text, "MarketRegimeTraceModule", "isBengali = isBengali", "isBengali")


# ------------------------------------------------------------
# 8) Remove old lower/per-card language toggle if any exists
# ------------------------------------------------------------
for phrase in [
    'text = if (isBengali) "Show EN" else "বাংলায় দেখুন',
    'text = if (isBengali) "Show EN" else "বাংলায় দেখুন ➔"',
    'onClick = onToggleLanguage',
]:
    before = text
    text = remove_textbutton_containing(text, phrase)
    if text != before:
        print("Removed old language toggle containing:", phrase)


target.write_text(text, encoding="utf-8")


# ------------------------------------------------------------
# 9) Update project Bengali glossary
# ------------------------------------------------------------
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
| Signal Verdict | সিগন্যাল রায় |
| Why It Matters | কেন গুরুত্বপূর্ণ |
| Risk Warning | ঝুঁকির সতর্কতা |
| Suggested Action | প্রস্তাবিত কাজ |
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

print("OK: Signal Pro overview completion patch applied.")
