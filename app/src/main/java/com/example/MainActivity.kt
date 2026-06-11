package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.*
import kotlin.math.*

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                Scaffold(
                    modifier = Modifier.fillMaxSize()
                ) { innerPadding ->
                    CalculatorScreen(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(innerPadding)
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CalculatorScreen(modifier: Modifier = Modifier) {
    var expression by remember { mutableStateOf("") }
    var resultText by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var isDegreeMode by remember { mutableStateOf(true) }
    var showFormulaSheet by remember { mutableStateOf(false) }

    // Synchronize calculation on expression change
    LaunchedEffect(expression, isDegreeMode) {
        if (expression.trim().isEmpty()) {
            resultText = ""
            errorMessage = null
        } else {
            try {
                val evaluator = MathEvaluator(isDegreeMode)
                val solved = evaluator.parse(expression)
                resultText = formatResult(solved)
                errorMessage = null
            } catch (e: ArithmeticException) {
                resultText = ""
                errorMessage = e.message ?: "Math Error"
            } catch (e: Exception) {
                resultText = ""
                errorMessage = null // Silent error while typing incomplete formulas
            }
        }
    }

    // Function to append characters with safety rules
    fun appendToExpression(value: String) {
        errorMessage = null
        val operators = listOf("+", "-", "×", "÷", "^")
        
        if (value in operators) {
            // Avoid starting directly with an operator unless it's negative number
            if (expression.isEmpty()) {
                if (value == "-") {
                    expression += value
                }
                return
            }
            // Avoid consecutive operators
            val lastChar = expression.last().toString()
            if (lastChar in operators) {
                expression = expression.dropLast(1) + value
                return
            }
        }
        
        expression += value
    }

    // Backspace action
    fun removeLast() {
        if (expression.isNotEmpty()) {
            errorMessage = null
            expression = expression.dropLast(1)
        }
    }

    // Direct Evaluation on "=" press
    fun onEvaluatePress() {
        if (expression.trim().isEmpty()) return
        try {
            val evaluator = MathEvaluator(isDegreeMode)
            val solved = evaluator.parse(expression)
            val formatted = formatResult(solved)
            expression = formatted
            resultText = ""
            errorMessage = null
        } catch (e: Exception) {
            resultText = ""
            errorMessage = e.message ?: "Invalid Syntax"
        }
    }

    Column(
        modifier = modifier
            .background(ThemeBg)
            .padding(top = 16.dp, bottom = 8.dp),
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        // App Title & Rad/Deg Toggle Row (Geometric Balance style)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Header Circle Icon
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(HeaderPillBg),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Σ",
                        color = HeaderPillText,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
                Column {
                    Text(
                        text = "APEX MATH",
                        color = TextDark,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 0.5.sp
                    )
                    Text(
                        text = "equations & solver suite",
                        color = TextMedium,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }

            // Rad / Deg toggle pill
            Row(
                modifier = Modifier
                    .clip(RoundedCornerShape(20.dp))
                    .background(BtnUtilityBg)
                    .clickable { isDegreeMode = !isDegreeMode }
                    .padding(horizontal = 14.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "DEG",
                    color = if (isDegreeMode) AccentBlue else TextMedium,
                    fontWeight = if (isDegreeMode) FontWeight.Bold else FontWeight.Normal,
                    fontSize = 11.sp
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "|",
                    color = DividerGray.copy(alpha = 0.5f),
                    fontSize = 11.sp
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "RAD",
                    color = if (!isDegreeMode) AccentBlue else TextMedium,
                    fontWeight = if (!isDegreeMode) FontWeight.Bold else FontWeight.Normal,
                    fontSize = 11.sp
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Large Output Displays
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .padding(horizontal = 20.dp, vertical = 12.dp)
        ) {
            Column(
                modifier = Modifier.fillMaxSize(),
                horizontalAlignment = Alignment.End,
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                // Clear & Backspace Action Row inside display
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = {
                            expression = ""
                            resultText = ""
                            errorMessage = null
                        },
                        modifier = Modifier
                            .testTag("btn_clear")
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(BtnClearBg)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = "Clear all",
                            tint = BtnClearText,
                            modifier = Modifier.size(18.dp)
                        )
                    }

                    IconButton(
                        onClick = { removeLast() },
                        modifier = Modifier
                            .testTag("btn_backspace")
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(BtnUtilityBg)
                    ) {
                        Text(
                            text = "⌫",
                            color = BtnUtilityText,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                Column(
                    horizontalAlignment = Alignment.End,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    // Formula Line
                    val formulaScrollState = rememberScrollState()
                    Text(
                        text = expression.ifEmpty { "0" },
                        color = TextMedium,
                        fontSize = 20.sp,
                        fontFamily = FontFamily.Monospace,
                        textAlign = TextAlign.End,
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(formulaScrollState)
                            .testTag("display_expression"),
                        maxLines = 1
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    // Live calculation results (clean, high scale, thin weights)
                    if (errorMessage != null) {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(BtnClearBg)
                                .padding(horizontal = 10.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = errorMessage!!,
                                color = BtnClearText,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    } else if (resultText.isNotEmpty()) {
                        Text(
                            text = "= $resultText",
                            color = TextDark,
                            fontSize = 54.sp,
                            fontFamily = FontFamily.Default,
                            fontWeight = FontWeight.Light,
                            letterSpacing = (-1.5).sp,
                            modifier = Modifier.testTag("display_result")
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Geometric Balanced Keypad Layout within bottom card container
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(topStart = 40.dp, topEnd = 40.dp))
                .background(KeypadBg)
                .padding(start = 20.dp, end = 20.dp, top = 16.dp, bottom = 20.dp)
        ) {
            // Little drag pill/handle bar
            Box(
                modifier = Modifier
                    .width(48.dp)
                    .height(5.dp)
                    .clip(CircleShape)
                    .background(DividerGray.copy(alpha = 0.5f))
                    .align(Alignment.CenterHorizontally)
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Open mathematical formula panel triggering bottom sheet
            Button(
                onClick = { showFormulaSheet = true },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
                    .testTag("equations_menu_trigger"),
                colors = ButtonDefaults.buttonColors(
                    containerColor = HeaderPillBg,
                    contentColor = HeaderPillText
                ),
                shape = RoundedCornerShape(24.dp), // Pill shape matching theme
                contentPadding = PaddingValues(0.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.List,
                        contentDescription = "Formulas",
                        tint = HeaderPillText,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "f(x) EQUATIONS, STATS & SOLVERS",
                        color = HeaderPillText,
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp,
                        letterSpacing = 0.5.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Advanced Quick Panel (Scrollable row with scientific buttons)
            val advancedRowScroll = rememberScrollState()
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(advancedRowScroll)
                    .padding(vertical = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                val advButtons = listOf(
                    "sin(" to "sin(", "cos(" to "cos(", "tan(" to "tan(",
                    "ln(" to "ln(", "log(" to "log(", "√(" to "√(",
                    "^" to "^", "!" to "!", "%" to "%",
                    "π" to "π", "e" to "e", 
                    "C" to "C", "P" to "P"
                )
                for ((label, query) in advButtons) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(16.dp))
                            .background(BtnDigitBg)
                            .clickable { appendToExpression(query) }
                            .padding(horizontal = 14.dp, vertical = 10.dp)
                    ) {
                        Text(
                            text = label,
                            color = TextDark,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Standard Numeric Keypad (containing custom Double Zero "00")
            val keys = listOf(
                listOf("C", "(", ")", "÷"),
                listOf("7", "8", "9", "×"),
                listOf("4", "5", "6", "-"),
                listOf("1", "2", "3", "+"),
                listOf("0", "00", ".", "=")
            )

            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                for (row in keys) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        for (key in row) {
                            val isOperator = key in listOf("÷", "×", "-", "+", "=")
                            val isUtility = key in listOf("C", "(", ")")
                            val isDoubleZero = key == "00"
                            
                            val btnBgColor = when {
                                key == "C" -> BtnClearBg
                                key == "=" -> BtnEqualBg
                                isOperator -> BtnOperatorBg
                                isUtility -> BtnUtilityBg
                                else -> BtnDigitBg
                            }
                            
                            val btnTextColor = when {
                                key == "C" -> BtnClearText
                                key == "=" -> BtnEqualText
                                isOperator -> BtnOperatorText
                                isUtility -> BtnUtilityText
                                else -> BtnDigitText
                            }

                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .aspectRatio(1.2f)
                                    .clip(RoundedCornerShape(24.dp))
                                    .background(btnBgColor)
                                    .clickable {
                                        when (key) {
                                            "C" -> {
                                                expression = ""
                                                resultText = ""
                                                errorMessage = null
                                            }
                                            "=" -> onEvaluatePress()
                                            else -> appendToExpression(key)
                                        }
                                    }
                                    .testTag("btn_${if (isDoubleZero) "00" else key.lowercase()}"),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = key,
                                    color = btnTextColor,
                                    fontSize = if (isUtility) 18.sp else 22.sp,
                                    fontWeight = FontWeight.Bold,
                                    fontFamily = if (Character.isDigit(key[0]) || isDoubleZero) FontFamily.Default else FontFamily.Monospace
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    // Formulas & Solver Bottom Sheet Modal
    if (showFormulaSheet) {
        ModalBottomSheet(
            onDismissRequest = { showFormulaSheet = false },
            sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
            containerColor = ThemeBg,
            dragHandle = { BottomSheetDefaults.DragHandle(color = DividerGray) }
        ) {
            EquationsSuiteWindow(
                isDegreeMode = isDegreeMode,
                onDismiss = { showFormulaSheet = false },
                onInsertToCalculator = { formula ->
                    appendToExpression(formula)
                    showFormulaSheet = false
                }
            )
        }
    }
}

// 3-Tab Equations & Statistical / Algebraic Solver panel
@Composable
fun EquationsSuiteWindow(
    isDegreeMode: Boolean,
    onDismiss: () -> Unit,
    onInsertToCalculator: (String) -> Unit
) {
    var activeTab by remember { mutableStateOf(0) }
    val tabTitles = listOf("📊 STATS", "📐 TRIGONOMETRY", "🧮 ALGEBRA")

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .fillMaxHeight(0.85f)
            .padding(16.dp)
    ) {
        // Sheet Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "APEX FORMULA LAB",
                color = TextDark,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.sp
            )
            IconButton(onClick = onDismiss) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = "Close Formulas modal",
                    tint = TextMedium
                )
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Modern Tab Row
        TabRow(
            selectedTabIndex = activeTab,
            containerColor = KeypadBg,
            contentColor = AccentBlue,
            modifier = Modifier
                .clip(RoundedCornerShape(12.dp))
        ) {
            tabTitles.forEachIndexed { index, title ->
                Tab(
                    selected = activeTab == index,
                    onClick = { activeTab = index },
                    modifier = Modifier.padding(vertical = 12.dp)
                ) {
                    Text(
                        text = title,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (activeTab == index) AccentBlue else TextMedium
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Tab Screen router
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
        ) {
            when (activeTab) {
                0 -> StatsSuiteScreen(onInsertToCalculator)
                1 -> TrigSuiteScreen(onInsertToCalculator)
                2 -> AlgebraSuiteScreen(onInsertToCalculator)
            }
        }
    }
}

// Stats solver window
@Composable
fun StatsSuiteScreen(onInsertToCalculator: (String) -> Unit) {
    var rawInput by remember { mutableStateOf("12.5, 18.0, 7.3, 15.2, 23.1") }
    var parsedNumbers by remember { mutableStateOf<List<Double>>(emptyList()) }
    var parseError by remember { mutableStateOf<String?>(null) }

    // Recompute statistics on input change
    LaunchedEffect(rawInput) {
        if (rawInput.trim().isEmpty()) {
            parsedNumbers = emptyList()
            parseError = null
            return@LaunchedEffect
        }
        try {
            val parsed = rawInput.split(Regex("[,\\s\\n]+"))
                .map { it.trim() }
                .filter { it.isNotEmpty() }
                .map { it.toDouble() }
            parsedNumbers = parsed
            parseError = null
        } catch (e: Exception) {
            parseError = "Invalid number format in dataset"
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
    ) {
        Text(
            text = "Descriptive Statistics Suite",
            color = TextDark,
            fontSize = 16.sp,
            fontWeight = FontWeight.SemiBold
        )
        Text(
            text = "Enter a dataset separated by commas, spaces, or lines. Standard descriptive indicators compute continuously.",
            color = TextMedium,
            fontSize = 12.sp,
            modifier = Modifier.padding(vertical = 6.dp)
        )

        Spacer(modifier = Modifier.height(8.dp))

        OutlinedTextField(
            value = rawInput,
            onValueChange = { rawInput = it },
            placeholder = { Text("e.g. 10.5, 23, 7.5, 9", color = TextMedium) },
            modifier = Modifier
                .fillMaxWidth()
                .height(100.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedTextColor = TextDark,
                unfocusedTextColor = TextDark,
                focusedBorderColor = AccentBlue,
                unfocusedBorderColor = DividerGray,
                focusedContainerColor = BtnDigitBg,
                unfocusedContainerColor = BtnDigitBg
            ),
            shape = RoundedCornerShape(12.dp)
        )

        Spacer(modifier = Modifier.height(8.dp))

        if (parseError != null) {
            Text(
                text = parseError!!,
                color = Color.Red,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold
            )
        } else if (parsedNumbers.isNotEmpty()) {
            val count = parsedNumbers.size
            val sum = MathEvaluator.calculateSum(parsedNumbers)
            val mean = MathEvaluator.calculateMean(parsedNumbers)
            val median = MathEvaluator.calculateMedian(parsedNumbers)
            val variance = MathEvaluator.calculateVariance(parsedNumbers)
            val stdDev = MathEvaluator.calculateStdDev(parsedNumbers)
            val min = parsedNumbers.minOrNull() ?: 0.0
            val max = parsedNumbers.maxOrNull() ?: 0.0

            val statsList = listOf(
                Triple("Data Elements Count (N)", formatResult(count.toDouble()), count.toString()),
                Triple("Sample Mean (μ)", formatResult(mean), mean.toString()),
                Triple("Median (M)", formatResult(median), median.toString()),
                Triple("Sample Variance (s²)", formatResult(variance), variance.toString()),
                Triple("Standard Deviation (σ)", formatResult(stdDev), stdDev.toString()),
                Triple("Sumation (Σx)", formatResult(sum), sum.toString()),
                Triple("Minimum Value (Min)", formatResult(min), min.toString()),
                Triple("Maximum Value (Max)", formatResult(max), max.toString())
            )

            Text(
                text = "Computed Indicators",
                color = AccentBlue,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(top = 10.dp, bottom = 4.dp)
            )

            statsList.forEach { (title, formattedVal, rawValue) ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    colors = CardDefaults.cardColors(containerColor = KeypadBg),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(text = title, color = TextMedium, fontSize = 11.sp)
                            Text(
                                text = formattedVal,
                                color = TextDark,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily.Monospace
                            )
                        }

                        Button(
                            onClick = { onInsertToCalculator(rawValue) },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = BtnUtilityBg,
                                contentColor = BtnUtilityText
                            ),
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 2.dp),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.height(32.dp)
                        ) {
                            Text("Use Val", fontSize = 11.sp, color = BtnUtilityText, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Permutations and combinations help widget
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                colors = CardDefaults.cardColors(containerColor = KeypadBg),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Combinatorics Solver Template",
                        color = TextDark,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Insert combination (nCr) or permutation (nPr) formula indicators easily:",
                        color = TextMedium,
                        fontSize = 12.sp,
                        modifier = Modifier.padding(vertical = 6.dp)
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Button(
                            onClick = { onInsertToCalculator("C") },
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = HeaderPillBg,
                                contentColor = HeaderPillText
                            ),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text("nCr (C)", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                        Button(
                            onClick = { onInsertToCalculator("P") },
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = HeaderPillBg,
                                contentColor = HeaderPillText
                            ),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text("nPr (P)", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        } else {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(40.dp),
                contentAlignment = Alignment.Center
            ) {
                Text("Input dynamic numbers above to compile statistics in real-time.", color = TextMedium, fontSize = 13.sp)
            }
        }
        Spacer(modifier = Modifier.height(30.dp))
    }
}

// Trigonometry templates screen
@Composable
fun TrigSuiteScreen(onInsertToCalculator: (String) -> Unit) {
    val identities = listOf(
        "Pythagorean Theorem Identity" to "sin(x)^2 + cos(x)^2 = 1",
        "Sine Double Angle Formulas" to "sin(2 * x) = 2 * sin(x) * cos(x)",
        "Cosine Double Angle Formulas" to "cos(2 * x) = cos(x)^2 - sin(x)^2",
        "Tangent Double Angle Formulas" to "tan(2 * x) = (2 * tan(x)) / (1 - tan(x)^2)",
        "Euler's Complex Relation" to "e^(i * π) + 1 = 0",
        "Co-Function Shifts" to "sin(90 - x) = cos(x)"
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
    ) {
        Text(
            text = "Trigonometrical Equations Reference",
            color = TextDark,
            fontSize = 16.sp,
            fontWeight = FontWeight.SemiBold
        )
        Text(
            text = "Tap any trigonometric formula to insert its respective functional template builder instantly into the main active workspace.",
            color = TextMedium,
            fontSize = 12.sp,
            modifier = Modifier.padding(vertical = 4.dp)
        )

        Spacer(modifier = Modifier.height(10.dp))

        identities.forEach { (name, equations) ->
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 6.dp),
                colors = CardDefaults.cardColors(containerColor = KeypadBg),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(
                    modifier = Modifier.padding(14.dp)
                ) {
                    Text(
                        text = name,
                        color = AccentBlue,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = equations,
                        color = TextDark,
                        fontSize = 15.sp,
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.SemiBold
                    )
                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = { onInsertToCalculator("sin(") },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = BtnUtilityBg,
                                contentColor = BtnUtilityText
                            ),
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(12.dp),
                            contentPadding = PaddingValues(0.dp)
                        ) {
                            Text("Insert sin()", fontSize = 11.sp, color = BtnUtilityText, fontWeight = FontWeight.Bold)
                        }

                        Button(
                            onClick = { onInsertToCalculator("cos(") },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = BtnUtilityBg,
                                contentColor = BtnUtilityText
                            ),
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(12.dp),
                            contentPadding = PaddingValues(0.dp)
                        ) {
                            Text("Insert cos()", fontSize = 11.sp, color = BtnUtilityText, fontWeight = FontWeight.Bold)
                        }

                        Button(
                            onClick = { onInsertToCalculator("tan(") },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = BtnUtilityBg,
                                contentColor = BtnUtilityText
                            ),
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(12.dp),
                            contentPadding = PaddingValues(0.dp)
                        ) {
                            Text("Insert tan()", fontSize = 11.sp, color = BtnUtilityText, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
        Spacer(modifier = Modifier.height(30.dp))
    }
}

// Algebraic solver with step by step Quadratic Equation and Pythagorean theorem calculator
@Composable
fun AlgebraSuiteScreen(onInsertToCalculator: (String) -> Unit) {
    var selectedSolver by remember { mutableStateOf(0) } // 0 -> Quadratic, 1 -> Pythagorean Leg

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Button(
                onClick = { selectedSolver = 0 },
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (selectedSolver == 0) AccentBlue else BtnUtilityBg,
                    contentColor = if (selectedSolver == 0) Color.White else BtnUtilityText
                ),
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("Quadratic Solver", fontSize = 12.sp, fontWeight = FontWeight.Bold)
            }

            Button(
                onClick = { selectedSolver = 1 },
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (selectedSolver == 1) AccentBlue else BtnUtilityBg,
                    contentColor = if (selectedSolver == 1) Color.White else BtnUtilityText
                ),
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("Pythagorean Solver", fontSize = 12.sp, fontWeight = FontWeight.Bold)
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        if (selectedSolver == 0) {
            // Quadratic Solver Panel (ax² + bx + c = 0)
            var inputA by remember { mutableStateOf("1") }
            var inputB by remember { mutableStateOf("-5") }
            var inputC by remember { mutableStateOf("6") }

            Text(
                text = "Quadratic Equations Solver",
                color = TextDark,
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "Formula: ax² + bx + c = 0",
                color = TextMedium,
                fontSize = 12.sp,
                fontFamily = FontFamily.Monospace
            )

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedTextField(
                    value = inputA,
                    onValueChange = { inputA = it },
                    label = { Text("a", color = TextMedium) },
                    modifier = Modifier.weight(1f),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = TextDark,
                        unfocusedTextColor = TextDark,
                        focusedBorderColor = AccentBlue,
                        unfocusedBorderColor = DividerGray,
                        focusedContainerColor = BtnDigitBg,
                        unfocusedContainerColor = BtnDigitBg
                    ),
                    shape = RoundedCornerShape(12.dp)
                )

                OutlinedTextField(
                    value = inputB,
                    onValueChange = { inputB = it },
                    label = { Text("b", color = TextMedium) },
                    modifier = Modifier.weight(1f),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = TextDark,
                        unfocusedTextColor = TextDark,
                        focusedBorderColor = AccentBlue,
                        unfocusedBorderColor = DividerGray,
                        focusedContainerColor = BtnDigitBg,
                        unfocusedContainerColor = BtnDigitBg
                    ),
                    shape = RoundedCornerShape(12.dp)
                )

                OutlinedTextField(
                    value = inputC,
                    onValueChange = { inputC = it },
                    label = { Text("c", color = TextMedium) },
                    modifier = Modifier.weight(1f),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = TextDark,
                        unfocusedTextColor = TextDark,
                        focusedBorderColor = AccentBlue,
                        unfocusedBorderColor = DividerGray,
                        focusedContainerColor = BtnDigitBg,
                        unfocusedContainerColor = BtnDigitBg
                    ),
                    shape = RoundedCornerShape(12.dp)
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Solve step-by-step
            val a = inputA.toDoubleOrNull()
            val b = inputB.toDoubleOrNull()
            val c = inputC.toDoubleOrNull()

            if (a == null || b == null || c == null) {
                Text("Include valid coefficients above to resolve roots.", color = TextMedium, fontSize = 12.sp)
            } else if (a == 0.0) {
                val root = -c / b
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = KeypadBg),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Text(text = "Linear reduction (since a = 0):", color = TextMedium, fontSize = 12.sp)
                        Text(text = "Formula: bx + c = 0", color = TextDark, fontSize = 14.sp, fontFamily = FontFamily.Monospace)
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(text = "Singular Root x = ${formatResult(root)}", color = AccentBlue, fontSize = 16.sp, fontWeight = FontWeight.Bold)

                        Button(
                            onClick = { onInsertToCalculator(root.toString()) },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = HeaderPillBg,
                                contentColor = HeaderPillText
                            ),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.padding(top = 8.dp)
                        ) {
                            Text("Apply Root to main display", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            } else {
                val d = b * b - 4 * a * c
                val dFormatted = formatResult(d)

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = KeypadBg),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Text(text = "Step 1: Calculate Discriminant (D)", color = TextMedium, fontSize = 11.sp)
                        Text(text = "D = b² - 4ac", color = TextDark, fontSize = 13.sp, fontFamily = FontFamily.Monospace)
                        Text(text = "D = ($b)² - 4*($a)*($c) = $dFormatted", color = TextDark, fontSize = 14.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)

                        Spacer(modifier = Modifier.height(10.dp))
                        Text(text = "Step 2: Resolve Complex or Real Roots", color = TextMedium, fontSize = 11.sp)

                        if (d > 0) {
                            val r1 = (-b + sqrt(d)) / (2 * a)
                            val r2 = (-b - sqrt(d)) / (2 * a)

                            Text(text = "Two Independent Real Roots:", color = TextMedium, fontSize = 12.sp)
                            Text(text = "x₁ = $r1", color = AccentBlue, fontSize = 15.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
                            Text(text = "x₂ = $r2", color = AccentBlue, fontSize = 15.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)

                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(top = 10.dp),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Button(
                                    onClick = { onInsertToCalculator(r1.toString()) },
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = HeaderPillBg,
                                        contentColor = HeaderPillText
                                    ),
                                    shape = RoundedCornerShape(12.dp),
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Text("Apply x₁", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                }
                                Button(
                                    onClick = { onInsertToCalculator(r2.toString()) },
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = HeaderPillBg,
                                        contentColor = HeaderPillText
                                    ),
                                    shape = RoundedCornerShape(12.dp),
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Text("Apply x₂", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        } else if (d == 0.0) {
                            val r = -b / (2 * a)
                            Text(text = "Single Real Repeated Root:", color = TextMedium, fontSize = 12.sp)
                            Text(text = "x = $r", color = AccentBlue, fontSize = 16.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)

                            Button(
                                onClick = { onInsertToCalculator(r.toString()) },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = HeaderPillBg,
                                    contentColor = HeaderPillText
                                ),
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier.padding(top = 8.dp)
                            ) {
                                Text("Apply Root", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }
                        } else {
                            // Complex solutions
                            val realPart = -b / (2 * a)
                            val imagPart = sqrt(-d) / (2 * a)

                            val realPartF = formatResult(realPart)
                            val imagPartF = formatResult(imagPart)

                            Text(text = "Two Complex/Imaginary Roots:", color = TextMedium, fontSize = 12.sp)
                            Text(text = "x₁ = $realPartF + ${imagPartF}i", color = AccentBlue, fontSize = 15.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
                            Text(text = "x₂ = $realPartF - ${imagPartF}i", color = AccentBlue, fontSize = 15.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
                        }
                    }
                }
            }
        } else {
            // Pythagorean side solver
            var sideA by remember { mutableStateOf("3") }
            var sideB by remember { mutableStateOf("4") }
            var sideC by remember { mutableStateOf("") } // Hypotenuse side input

            Text(
                text = "Pythagorean Theorem Solver",
                color = TextDark,
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "Formula: a² + b² = c² (where c is hypotenuse)",
                color = TextMedium,
                fontSize = 12.sp,
                fontFamily = FontFamily.Monospace
            )

            Spacer(modifier = Modifier.height(12.dp))

            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = sideA,
                    onValueChange = { sideA = it },
                    label = { Text("Side a (Leg)", color = TextMedium) },
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = TextDark,
                        unfocusedTextColor = TextDark,
                        focusedBorderColor = AccentBlue,
                        unfocusedBorderColor = DividerGray,
                        focusedContainerColor = BtnDigitBg,
                        unfocusedContainerColor = BtnDigitBg
                    ),
                    shape = RoundedCornerShape(12.dp)
                )

                OutlinedTextField(
                    value = sideB,
                    onValueChange = { sideB = it },
                    label = { Text("Side b (Leg)", color = TextMedium) },
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = TextDark,
                        unfocusedTextColor = TextDark,
                        focusedBorderColor = AccentBlue,
                        unfocusedBorderColor = DividerGray,
                        focusedContainerColor = BtnDigitBg,
                        unfocusedContainerColor = BtnDigitBg
                    ),
                    shape = RoundedCornerShape(12.dp)
                )

                OutlinedTextField(
                    value = sideC,
                    onValueChange = { sideC = it },
                    label = { Text("Side c (Hypotenuse - leave blank to solve)", color = TextMedium) },
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = TextDark,
                        unfocusedTextColor = TextDark,
                        focusedBorderColor = AccentBlue,
                        unfocusedBorderColor = DividerGray,
                        focusedContainerColor = BtnDigitBg,
                        unfocusedContainerColor = BtnDigitBg
                    ),
                    shape = RoundedCornerShape(12.dp)
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            val sa = sideA.toDoubleOrNull()
            val sb = sideB.toDoubleOrNull()
            val sc = sideC.toDoubleOrNull()

            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = KeypadBg),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    if (sc == null) {
                        // Solve hypotenuse c (a^2 + b^2)
                        if (sa != null && sb != null) {
                            val resSquared = sa * sa + sb * sb
                            val res = sqrt(resSquared)
                            Text(text = "Solving Hypotenuse (c):", color = TextMedium, fontSize = 11.sp)
                            Text(text = "c = √(a² + b²)", color = TextDark, fontSize = 13.sp, fontFamily = FontFamily.Monospace)
                            Text(text = "c = √(${sa}² + ${sb}²) = √($resSquared)", color = TextDark, fontSize = 13.sp, fontFamily = FontFamily.Monospace)
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(text = "c = ${formatResult(res)}", color = AccentBlue, fontSize = 18.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)

                            Button(
                                onClick = { onInsertToCalculator(res.toString()) },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = HeaderPillBg,
                                    contentColor = HeaderPillText
                                ),
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier.padding(top = 8.dp)
                            ) {
                                Text("Insert Hypotenuse to Calc", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }
                        } else {
                            Text("Provide standard inputs above to compute sides.", color = TextMedium, fontSize = 12.sp)
                        }
                    } else {
                        // Solve leg side a or b
                        if (sa != null && sb == null) {
                            // Find side b (sqrt(c^2 - a^2))
                            if (sc > sa) {
                                val resSquared = sc * sc - sa * sa
                                val res = sqrt(resSquared)
                                Text(text = "Solving Side Leg (b):", color = TextMedium, fontSize = 11.sp)
                                Text(text = "b = √(c² - a²)", color = TextDark, fontSize = 13.sp, fontFamily = FontFamily.Monospace)
                                Text(text = "b = √(${sc}² - ${sa}²) = √($resSquared)", color = TextDark, fontSize = 13.sp, fontFamily = FontFamily.Monospace)
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(text = "b = ${formatResult(res)}", color = AccentBlue, fontSize = 18.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)

                                Button(
                                    onClick = { onInsertToCalculator(res.toString()) },
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = HeaderPillBg,
                                        contentColor = HeaderPillText
                                    ),
                                    shape = RoundedCornerShape(12.dp),
                                    modifier = Modifier.padding(top = 8.dp)
                                ) {
                                    Text("Insert Leg b to Calc", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                }
                            } else {
                                Text("Error: Hypotenuse c must be larger than side leg a.", color = Color.Red, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            }
                        } else if (sb != null && sa == null) {
                            // Find side a (sqrt(c^2 - b^2))
                            if (sc > sb) {
                                val resSquared = sc * sc - sb * sb
                                val res = sqrt(resSquared)
                                Text(text = "Solving Side Leg (a):", color = TextMedium, fontSize = 11.sp)
                                Text(text = "a = √(c² - b²)", color = TextDark, fontSize = 13.sp, fontFamily = FontFamily.Monospace)
                                Text(text = "a = √(${sc}² - ${sb}²) = √($resSquared)", color = TextDark, fontSize = 13.sp, fontFamily = FontFamily.Monospace)
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(text = "a = ${formatResult(res)}", color = AccentBlue, fontSize = 18.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)

                                Button(
                                    onClick = { onInsertToCalculator(res.toString()) },
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = HeaderPillBg,
                                        contentColor = HeaderPillText
                                    ),
                                    shape = RoundedCornerShape(12.dp),
                                    modifier = Modifier.padding(top = 8.dp)
                                ) {
                                    Text("Insert Leg a to Calc", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                }
                            } else {
                                Text("Error: Hypotenuse c must be larger than side leg b.", color = Color.Red, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            }
                        } else {
                            Text("Leave exactly one of the sides empty (Leg a or Leg b) to resolve its dimension.", color = TextMedium, fontSize = 12.sp)
                        }
                    }
                }
            }
        }
        Spacer(modifier = Modifier.height(30.dp))
    }
}

// Utility to format numbers cleanly (avoiding scientific notation where possible, rounding nicely)
fun formatResult(value: Double): String {
    if (value.isNaN()) return "NaN"
    if (value.isInfinite()) return if (value < 0) "-Infinity" else "Infinity"
    
    // Check if it represents an integer
    if (value % 1 == 0.0 && value < 1e12 && value > -1e12) {
        return value.toLong().toString()
    }
    
    // Smooth decimal constraints
    var formattedVal = String.format("%.6f", value)
    
    // Trim trailing zeroes
    if (formattedVal.contains(".")) {
        while (formattedVal.endsWith("0")) {
            formattedVal = formattedVal.dropLast(1)
        }
        if (formattedVal.endsWith(".")) {
            formattedVal = formattedVal.dropLast(1)
        }
    }
    return formattedVal
}
