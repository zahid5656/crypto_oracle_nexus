package com.example.feature.signal_pro

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.Spring
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.ui.draw.scale
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.*
import com.example.viewmodel.AppScreen
import com.example.viewmodel.CryptoViewModel

private val T_Surface = Color(0xFF111112)
private val T_Bg = DarkBackground
private val T_BorderMedium = Color(0xFF3A3A3C)
private val T_TextPrimary = Color(0xFFFFFFFF)
private val T_TextSecondary = Color(0xFF8E8E93)
private val T_TextMuted = Color(0xFF636366)

private val T_Green = CryptoGreen
private val T_Red = CryptoRedText
private val T_Cyan = CryptoCyan
private val T_Gold = TitanGold
private val T_Orange = TitanOrange

@Composable
fun SignalProMockupScreen(
    viewModel: CryptoViewModel,
    modifier: Modifier = Modifier
) {
    androidx.activity.compose.BackHandler {
        viewModel.navigateTo(AppScreen.Home)
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(T_Bg)
            .padding(horizontal = 12.dp),
        contentPadding = PaddingValues(bottom = 24.dp)
    ) {
        item {
            SignalProHeader(onBack = { viewModel.navigateTo(AppScreen.Home) })
            Spacer(modifier = Modifier.height(16.dp))
        }

        item {
            PriceMatrixBlock()
            Spacer(modifier = Modifier.height(16.dp))
        }

        item {
            CQISurface()
            Spacer(modifier = Modifier.height(16.dp))
        }

        item {
             Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                 Box(modifier = Modifier.weight(1f)) { RiskScoreSurface() }
                 Box(modifier = Modifier.weight(1f)) { ExecutionReadinessSurface() }
             }
             Spacer(modifier = Modifier.height(16.dp))
        }

        item {
            ConsensusSummarySurface()
            Spacer(modifier = Modifier.height(16.dp))
        }
        
        item {
            MultiAIConsensusSurface()
            Spacer(modifier = Modifier.height(16.dp))
        }

        item {
            DirectionValidationSurface()
            Spacer(modifier = Modifier.height(16.dp))
        }

        item {
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Box(modifier = Modifier.weight(1f)) { RRValidationSurface() }
                Box(modifier = Modifier.weight(1f)) { SLSanitySurface() }
            }
            Spacer(modifier = Modifier.height(16.dp))
        }

        item {
            TPMatrixSurface()
            Spacer(modifier = Modifier.height(16.dp))
        }

        item {
            PositionAllocationSurface()
            Spacer(modifier = Modifier.height(16.dp))
        }

        item {
            DecisionGateSurface()
            Spacer(modifier = Modifier.height(16.dp))
        }

        item {
            ConflictFlagSurface()
            Spacer(modifier = Modifier.height(16.dp))
        }

        item {
            AuditRow()
            Spacer(modifier = Modifier.height(24.dp))
        }

        item {
            ActionButtonsSurface()
        }
    }
}

@Composable
fun SignalProHeader(onBack: () -> Unit) {
    Column(modifier = Modifier.fillMaxWidth().padding(top = 12.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onBack, modifier = Modifier.size(28.dp)) {
                Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = T_TextPrimary)
            }
            Spacer(modifier = Modifier.width(8.dp))
            Column {
                Text("SIGNAL PRO", color = T_TextPrimary, fontSize = 16.sp, fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold)
                Text("Validation Cockpit", color = T_Cyan, fontSize = 10.sp, fontFamily = FontFamily.Monospace)
            }
        }
        
        Spacer(modifier = Modifier.height(12.dp))
        
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Column {
                Text("Symbol: BTC", color = T_TextPrimary, fontSize = 12.sp, fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold)
                Text("Mode: FUTURES LONG", color = T_Green, fontSize = 11.sp, fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold)
            }
            Column(horizontalAlignment = Alignment.End) {
                Text("Signal State: ACTIVE", color = T_Green, fontSize = 11.sp, fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold)
                Text("Validity: 42m", color = T_TextSecondary, fontSize = 10.sp, fontFamily = FontFamily.Monospace)
            }
        }
        Spacer(modifier = Modifier.height(8.dp))
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text("Timeframe: 24H", color = T_TextSecondary, fontSize = 10.sp, fontFamily = FontFamily.Monospace)
            Text("Freshness: 12s", color = T_Green, fontSize = 10.sp, fontFamily = FontFamily.Monospace)
        }
    }
}

@Composable
fun PriceMatrixBlock() {
    SurfaceBlock("PRICE MATRIX") {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            PriceItem("Entry", "$63,920", T_Cyan)
            PriceItem("Current", "$63,994", T_TextPrimary)
            PriceItem("Expected", "$65,400", T_Gold)
        }
        Spacer(modifier = Modifier.height(12.dp))
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            PriceItem("Predicted", "$66,200", T_Green)
            PriceItem("TP", "$66,500", T_Green)
            PriceItem("SL", "$62,780", T_Red)
        }
    }
}

@Composable
fun PriceItem(label: String, value: String, valueColor: Color) {
    Column(horizontalAlignment = Alignment.Start) {
        Text(label, color = T_TextMuted, fontSize = 10.sp, fontFamily = FontFamily.Monospace)
        Text(value, color = valueColor, fontSize = 14.sp, fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold, style = androidx.compose.ui.text.TextStyle(fontFeatureSettings = "tnum"))
    }
}

@Composable
fun CQISurface() {
    SurfaceBlock("SIGNAL QUALITY ENGINE (CQI)") {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            Column {
                Text("CQI Score", color = T_TextMuted, fontSize = 10.sp, fontFamily = FontFamily.Monospace)
                Text("88", color = T_Green, fontSize = 24.sp, fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold)
            }
            Column(horizontalAlignment = Alignment.End) {
                Text("Classification", color = T_TextMuted, fontSize = 10.sp, fontFamily = FontFamily.Monospace)
                Text("HIGH CONFIDENCE", color = T_Green, fontSize = 12.sp, fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold)
            }
        }
        Spacer(modifier = Modifier.height(8.dp))
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text("Probability: 82%", color = T_TextSecondary, fontSize = 11.sp, fontFamily = FontFamily.Monospace)
            Text("Status: VALIDATED", color = T_Green, fontSize = 11.sp, fontFamily = FontFamily.Monospace)
        }
    }
}

@Composable
fun RiskScoreSurface() {
    SurfaceBlock("RISK SCORE / ঝুঁকির পরিমান") {
        Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
            Text("24", color = T_Gold, fontSize = 24.sp, fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold)
            Text("MEDIUM", color = T_Gold, fontSize = 12.sp, fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
fun ExecutionReadinessSurface() {
    SurfaceBlock("EXECUTION READINESS") {
        Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
            Text("ACCEPTABLE", color = T_Cyan, fontSize = 16.sp, fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(4.dp))
            Text("Spread: 0.04% | Liq: High", color = T_TextSecondary, fontSize = 9.sp, fontFamily = FontFamily.Monospace)
            Text("Slip: Low | Latency: 180ms", color = T_TextSecondary, fontSize = 9.sp, fontFamily = FontFamily.Monospace)
        }
    }
}

@Composable
fun ConsensusSummarySurface() {
    SurfaceBlock("CONSENSUS SUMMARY") {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Column {
                Text("Consensus Confidence", color = T_TextMuted, fontSize = 9.sp, fontFamily = FontFamily.Monospace)
                Text("HIGH (84%)", color = T_Green, fontSize = 12.sp, fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(8.dp))
                Text("Consensus Bias", color = T_TextMuted, fontSize = 9.sp, fontFamily = FontFamily.Monospace)
                Text("AGGRESSIVE", color = T_Gold, fontSize = 12.sp, fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold)
            }
            Column(horizontalAlignment = Alignment.End) {
                Text("Direction", color = T_TextMuted, fontSize = 9.sp, fontFamily = FontFamily.Monospace)
                Text("BULLISH", color = T_Green, fontSize = 12.sp, fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(8.dp))
                Text("Vote Split", color = T_TextMuted, fontSize = 9.sp, fontFamily = FontFamily.Monospace)
                Text("4-0-1 (1 Outlier)", color = T_TextSecondary, fontSize = 11.sp, fontFamily = FontFamily.Monospace)
            }
        }
        Spacer(modifier = Modifier.height(6.dp))
        Text("Model Spread: 14%", color = T_TextSecondary, fontSize = 9.sp, fontFamily = FontFamily.Monospace)
    }
}

@Composable
fun MultiAIConsensusSurface() {
    SurfaceBlock("MULTI-AI CONSENSUS ENGINES") {
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            EngineCard("Oracle Quant Engine", "91", "BULLISH", "ACTIVE", T_Green)
            EngineCard("Deterministic Rules Core", "88", "BULLISH", "ACTIVE", T_Green)
            EngineCard("Sentiment Engine", "76", "NEUTRAL", "WATCH", T_Gold)
            EngineCard("Market Structure Engine", "82", "BULLISH", "ACTIVE", T_Green)
            EngineCard("Risk Guard Engine", "85", "SAFE", "VALIDATED", T_Cyan)
        }
    }
}

@Composable
fun EngineCard(name: String, score: String, vote: String, status: String, color: Color) {
    Row(
        modifier = Modifier.fillMaxWidth().background(T_Surface).border(0.5.dp, T_BorderMedium, RoundedCornerShape(4.dp)).padding(8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column {
            Text(name, color = T_TextPrimary, fontSize = 11.sp, fontFamily = FontFamily.Monospace)
            Text("Status: $status", color = T_TextSecondary, fontSize = 9.sp, fontFamily = FontFamily.Monospace)
        }
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(score, color = color, fontSize = 14.sp, fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.width(8.dp))
            Text(vote, color = color, fontSize = 11.sp, fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
fun DirectionValidationSurface() {
    SurfaceBlock("DIRECTION / TRADE LOGIC VALIDATION") {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Column {
                Text("Direction Logic", color = T_TextMuted, fontSize = 10.sp, fontFamily = FontFamily.Monospace)
                Text("LONG", color = T_Green, fontSize = 12.sp, fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold)
            }
            Column(horizontalAlignment = Alignment.End) {
                Text("Entry State", color = T_TextMuted, fontSize = 10.sp, fontFamily = FontFamily.Monospace)
                Text("VALID", color = T_Green, fontSize = 12.sp, fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold)
            }
        }
        Spacer(modifier = Modifier.height(12.dp))
        Text("StopLoss < Entry : VALID", color = T_Green, fontSize = 11.sp, fontFamily = FontFamily.Monospace)
        Text("Target > Entry : VALID", color = T_Green, fontSize = 11.sp, fontFamily = FontFamily.Monospace)
    }
}

@Composable
fun RRValidationSurface() {
    SurfaceBlock("RISK / REWARD") {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text("RR: 2.4R", color = T_Cyan, fontSize = 16.sp, fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold)
            Text("VALID", color = T_Green, fontSize = 12.sp, fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold)
        }
        Spacer(modifier = Modifier.height(4.dp))
        Text("Risk Path: Standard", color = T_TextSecondary, fontSize = 10.sp, fontFamily = FontFamily.Monospace)
    }
}

@Composable
fun SLSanitySurface() {
    SurfaceBlock("STOP-LOSS SANITY") {
        Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
            Text("3.8%", color = T_Green, fontSize = 16.sp, fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(4.dp))
            Text("State: Standard", color = T_TextSecondary, fontSize = 10.sp, fontFamily = FontFamily.Monospace)
        }
    }
}

@Composable
fun TPMatrixSurface() {
    SurfaceBlock("TAKE PROFIT MATRIX") {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            TPItem("TP1", "+3%")
            TPItem("TP2", "+7%")
            TPItem("TP3", "+10%")
            TPItem("Final Target", "+12.5%")
        }
    }
}

@Composable
fun TPItem(label: String, value: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(label, color = T_TextMuted, fontSize = 10.sp, fontFamily = FontFamily.Monospace)
        Text(value, color = T_Green, fontSize = 12.sp, fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold)
    }
}

@Composable
fun PositionAllocationSurface() {
    SurfaceBlock("POSITION ALLOCATION") {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            Text("Posture", color = T_TextMuted, fontSize = 12.sp, fontFamily = FontFamily.Monospace)
            Text("Moderate", color = T_Cyan, fontSize = 14.sp, fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold)
            Text("5.0% Cap", color = T_TextSecondary, fontSize = 12.sp, fontFamily = FontFamily.Monospace)
        }
    }
}

@Composable
fun DecisionGateSurface() {
    SurfaceBlock("DECISION GATE SUMMARY") {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text("Decision Gate:", color = T_TextPrimary, fontSize = 14.sp, fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold)
            Text("REVIEW", color = T_Gold, fontSize = 14.sp, fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold)
        }
        Spacer(modifier = Modifier.height(8.dp))
        Text("Blocked: None", color = T_TextSecondary, fontSize = 11.sp, fontFamily = FontFamily.Monospace)
        Text("Warnings: Spread elevated", color = T_Orange, fontSize = 11.sp, fontFamily = FontFamily.Monospace)
        
        Spacer(modifier = Modifier.height(8.dp))
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Column {
                Text("Signal Dir: PASS", color = T_Green, fontSize = 9.sp, fontFamily = FontFamily.Monospace)
                Text("Risk Score: PASS", color = T_Green, fontSize = 9.sp, fontFamily = FontFamily.Monospace)
            }
            Column(horizontalAlignment = Alignment.End) {
                Text("Readiness: WARN", color = T_Gold, fontSize = 9.sp, fontFamily = FontFamily.Monospace)
                Text("Consensus: PASS", color = T_Green, fontSize = 9.sp, fontFamily = FontFamily.Monospace)
            }
        }
    }
}

@Composable
fun ConflictFlagSurface() {
    SurfaceBlock("CONFLICT FLAG") {
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            ChipLabel("REVIEW LIQUIDITY", T_Orange)
            ChipLabel("CONSENSUS ALIGNED", T_Green)
        }
    }
}

@Composable
fun AuditRow() {
    SurfaceBlock("SOURCE / PROVENANCE / AUDIT") {
        Column {
            Text("Signal ID: SIG-BTC-24H-001", color = T_TextSecondary, fontSize = 10.sp, fontFamily = FontFamily.Monospace)
            Text("Source: Local Mock", color = T_TextSecondary, fontSize = 10.sp, fontFamily = FontFamily.Monospace)
            Text("Rules Fired: 8", color = T_TextSecondary, fontSize = 10.sp, fontFamily = FontFamily.Monospace)
            Text("Audit: Pending", color = T_TextMuted, fontSize = 10.sp, fontFamily = FontFamily.Monospace)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ActionButtonsSurface() {
    var step by remember { mutableStateOf(0) }
    var showDecisionBrief by remember { mutableStateOf(false) }
    
    val decisionBriefSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val setupSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

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

    if (showDecisionBrief) {
        ModalBottomSheet(
            onDismissRequest = { showDecisionBrief = false },
            sheetState = decisionBriefSheetState,
            containerColor = Color(0xFF030712),
            contentColor = T_TextPrimary
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .fillMaxHeight(0.92f)
                    .padding(horizontal = 18.dp, vertical = 10.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text("AI Decision Brief", fontSize = 20.sp, fontWeight = FontWeight.Black, color = T_Cyan)
                Text("Compact signal summary for faster decision-making", fontSize = 12.5.sp, color = T_TextSecondary)
                
                SurfaceBlock("Signal Verdict") { Text("Signal is strong, but entry confirmation is still required.", color = T_Green, fontSize = 12.sp) }
                SurfaceBlock("Why It Matters") { Text("This setup combines trend, momentum, volume, AI consensus, and risk profile signals.", color = T_Cyan, fontSize = 12.sp) }
                SurfaceBlock("Risk Warning") { Text("Risk is low to medium. Follow stop loss and position sizing.", color = T_Gold, fontSize = 12.sp) }
                SurfaceBlock("Suggested Action") { Text("Verify entry price, stop loss, and target before accepting the signal.", color = T_Green, fontSize = 12.sp) }
                
                Text("AI assists decision-making; the final trading decision is yours.", fontSize = 11.5.sp, color = T_TextMuted)

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    Button(
                        onClick = { showDecisionBrief = false; step = 2 },
                        colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent, contentColor = T_Cyan),
                        border = androidx.compose.foundation.BorderStroke(1.dp, T_Cyan.copy(alpha=0.72f)),
                        modifier = Modifier.weight(1f).height(46.dp)
                    ) { Text("SIGNAL SETUP", fontWeight = FontWeight.Black, fontSize = 11.sp) }
                    
                    Button(
                        onClick = { showDecisionBrief = false; step = 1 },
                        colors = ButtonDefaults.buttonColors(containerColor = T_Green, contentColor = T_Bg),
                        modifier = Modifier.weight(1f).height(46.dp)
                    ) { Text("ACCEPT SIGNAL", fontWeight = FontWeight.Black, fontSize = 11.sp) }
                }
                
                TextButton(onClick = { showDecisionBrief = false }, modifier = Modifier.align(Alignment.CenterHorizontally)) {
                    Text("CLOSE", color = T_TextSecondary, fontWeight = FontWeight.Bold, fontSize = 11.sp)
                }
            }
        }
    }

    if (step == 2) {
        ModalBottomSheet(
            onDismissRequest = { step = 0 },
            sheetState = setupSheetState,
            containerColor = Color(0xFF030712),
            contentColor = T_TextPrimary
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .fillMaxHeight(0.92f)
                    .padding(horizontal = 18.dp, vertical = 10.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text("SIGNAL SETUP", fontSize = 20.sp, fontWeight = FontWeight.Black, color = T_Cyan)
                Text("Auto-filled from the current signal. Review before accepting.", fontSize = 12.sp, color = T_TextSecondary)
                
                // Use Recommended Signal Button Placeholder that user requested
                Button(
                    onClick = { /* Auto-fill logic mock */ },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1E293B)),
                    modifier = Modifier.fillMaxWidth().height(40.dp),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text("USE RECOMMENDED SIGNAL SETUP", color = T_Cyan, fontWeight = FontWeight.Bold, fontSize = 11.sp)
                }
                
                OutlinedTextField(value = "66500", onValueChange = {}, label = { Text("TARGET") }, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = "62780", onValueChange = {}, label = { Text("SL1 / STOP LOSS") }, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = "5.0% Cap", onValueChange = {}, label = { Text("ALLOCATION") }, modifier = Modifier.fillMaxWidth())
                
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    TextButton(onClick = { step = 0 }, modifier = Modifier.weight(1f)) {
                        Text("CLOSE", color = T_TextSecondary, fontWeight = FontWeight.Bold)
                    }
                    Button(
                        onClick = { step = 1 },
                        colors = ButtonDefaults.buttonColors(containerColor = T_Green, contentColor = T_Bg),
                        modifier = Modifier.weight(1f).height(46.dp)
                    ) { Text("ACCEPT SIGNAL", fontWeight = FontWeight.Black) }
                }
            }
        }
    }

    if (step == 1) {
        AlertDialog(
            onDismissRequest = { step = 0 },
            title = { Text("Verify Trade Details", color = T_Cyan, fontSize = 18.sp, fontWeight = FontWeight.Bold) },
            text = {
                Column {
                    Text("Direction: LONG (FUTURES)", color = T_TextSecondary, fontSize = 14.sp)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("Locked Entry Price:", color = T_TextSecondary, fontSize = 12.sp)
                    Text("$63,920.00", color = T_Green, fontSize = 24.sp, fontWeight = FontWeight.Black, fontFamily = FontFamily.Monospace) // The bright green dollar value user requested
                    Spacer(modifier = Modifier.height(16.dp))
                    Text("Once accepted, this entry will activate personal mission tracking.", color = T_Gold, fontSize = 10.sp)
                }
            },
            confirmButton = {
                Button(onClick = { step = 0 }, colors = ButtonDefaults.buttonColors(containerColor = T_Green)) {
                    Text("CONFIRM MISSION", fontWeight = FontWeight.Black, color = T_Bg)
                }
            },
            dismissButton = {
                TextButton(onClick = { step = 0 }) { Text("Cancel", color = T_TextSecondary) }
            },
            containerColor = Color(0xFF030712)
        )
    }

    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
        Box(
            modifier = Modifier
                .weight(1f)
                .height(48.dp)
                .clip(RoundedCornerShape(10.dp))
                .background(androidx.compose.ui.graphics.Brush.linearGradient(listOf(Color(0xFF02050D), Color(0xFF0B1220), Color(0xFF02050D))))
                .background(androidx.compose.ui.graphics.Brush.horizontalGradient(listOf(T_Cyan.copy(alpha=0.10f), T_Green.copy(alpha=0.08f), T_Cyan.copy(alpha=0.10f))))
                .background(androidx.compose.ui.graphics.Brush.horizontalGradient(listOf(Color.Transparent, T_Cyan.copy(alpha=0.42f), Color.White.copy(alpha=0.12f), T_Green.copy(alpha=0.26f), Color.Transparent), startX = recoSweepX, endX = recoSweepX + 520f))
                .border(0.8.dp, T_Cyan.copy(alpha=0.66f), RoundedCornerShape(10.dp))
                .clickable { showDecisionBrief = true }
                .padding(horizontal = 8.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center, modifier = Modifier.fillMaxWidth()) {
                Text("HIGH CONFIDENCE", fontSize = 10.sp, fontWeight = FontWeight.Black, color = Color(0xFFF4F8FF), maxLines = 1)
                Text("| VERIFY ENTRY |", fontSize = 10.sp, fontWeight = FontWeight.Black, color = Color(0xFFF4F8FF), maxLines = 1)
            }
        }

        Box(
            modifier = Modifier
                .scale(scale)
                .weight(1f)
                .height(48.dp)
                .background(androidx.compose.ui.graphics.Brush.linearGradient(listOf(T_Cyan.copy(alpha = pulseAlpha), T_Green.copy(alpha = pulseAlpha))), RoundedCornerShape(10.dp))
                .border(0.8.dp, Color.White.copy(alpha = 0.28f), RoundedCornerShape(10.dp))
                .clickable(interactionSource = interactionSource, indication = androidx.compose.foundation.LocalIndication.current, onClick = { step = 1 })
                .padding(horizontal = 10.dp),
            contentAlignment = Alignment.Center
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Check, contentDescription = null, tint = Color.White, modifier = Modifier.size(15.dp))
                Spacer(modifier = Modifier.width(5.dp))
                Text("ACCEPT SIGNAL", fontWeight = FontWeight.Black, fontSize = 11.sp, color = Color.White, letterSpacing = 0.8.sp)
            }
        }
    }
}

@Composable
fun ChipLabel(text: String, color: Color) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(4.dp))
            .background(color.copy(alpha = 0.15f))
            .border(0.5.dp, color, RoundedCornerShape(4.dp))
            .padding(horizontal = 8.dp, vertical = 4.dp)
    ) {
        Text(text, color = color, fontSize = 9.sp, fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold)
    }
}

@Composable
fun SurfaceBlock(title: String, content: @Composable () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .border(0.5.dp, T_BorderMedium, RoundedCornerShape(6.dp))
            .background(T_Surface)
            .padding(12.dp)
    ) {
        Text(title, color = T_TextMuted, fontSize = 10.sp, fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(12.dp))
        content()
    }
}

