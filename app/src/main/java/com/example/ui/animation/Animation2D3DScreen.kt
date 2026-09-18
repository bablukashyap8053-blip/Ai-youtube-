package com.example.ui.animation

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.filled.ViewInAr
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.CyberCyan
import com.example.ui.theme.LuminousAmber
import com.example.ui.theme.NeonPink
import com.example.ui.theme.PrimaryViolet
import com.example.ui.theme.PrimaryVioletLight
import kotlinx.coroutines.delay
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

data class Particle2D(
    var x: Float,
    var y: Float,
    var vx: Float,
    var vy: Float,
    val color: Color,
    val radius: Float
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun Animation2D3DScreen(
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedTab by remember { mutableIntStateOf(1) } // Default to 3D

    Scaffold(
        modifier = modifier
            .fillMaxSize()
            .testTag("animation_screen"),
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "2D & 3D Animation Studio",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Real-time Vector & 3D Math Engine",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack, modifier = Modifier.testTag("anim_back_btn")) {
                        Icon(imageVector = Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.surface)
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            TabRow(
                selectedTabIndex = selectedTab,
                containerColor = MaterialTheme.colorScheme.surface,
                contentColor = PrimaryVioletLight
            ) {
                Tab(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    text = { Text("🌌 2D Physics & Waves", fontWeight = FontWeight.Bold) },
                    modifier = Modifier.testTag("tab_2d_anim")
                )
                Tab(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    text = { Text("🧊 3D Engine & Models", fontWeight = FontWeight.Bold) },
                    modifier = Modifier.testTag("tab_3d_anim")
                )
            }

            if (selectedTab == 0) {
                Animation2DView()
            } else {
                Animation3DView()
            }
        }
    }
}

// -------------------------------------------------------------
// 3D REAL-TIME ENGINE VIEW
// -------------------------------------------------------------
@Composable
fun Animation3DView() {
    var modelType by remember { mutableIntStateOf(0) } // 0: Cube, 1: Sphere/Polyhedron, 2: Torus Donut
    var rotX by remember { mutableFloatStateOf(25f) }
    var rotY by remember { mutableFloatStateOf(45f) }
    var rotZ by remember { mutableFloatStateOf(0f) }
    var autoRotate by remember { mutableStateOf(true) }
    var speed by remember { mutableFloatStateOf(1.0f) }
    var scaleFactor by remember { mutableFloatStateOf(160f) }
    var wireColor by remember { mutableStateOf(CyberCyan) }

    // Auto-rotation loop
    LaunchedEffect(autoRotate, speed) {
        while (autoRotate) {
            delay(16) // ~60fps
            rotY = (rotY + 0.8f * speed) % 360f
            rotX = (rotX + 0.4f * speed) % 360f
        }
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // 3D Interactive Canvas
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(340.dp)
                    .testTag("canvas_3d_card"),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF0A0718))
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .pointerInput(Unit) {
                            detectDragGestures { change, dragAmount ->
                                change.consume()
                                rotY = (rotY + dragAmount.x * 0.5f) % 360f
                                rotX = (rotX - dragAmount.y * 0.5f) % 360f
                            }
                        }
                ) {
                    Canvas3DEngine(
                        modelType = modelType,
                        rotX = rotX,
                        rotY = rotY,
                        rotZ = rotZ,
                        scale = scaleFactor,
                        wireColor = wireColor
                    )

                    // Overlay watermark & drag prompt
                    Surface(
                        color = Color.Black.copy(alpha = 0.5f),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .align(Alignment.BottomCenter)
                            .padding(12.dp)
                    ) {
                        Text(
                            text = "👆 स्क्रीन पर उंगली घुमाकर 3D में घुमाएं (Drag to rotate)",
                            color = Color.White.copy(alpha = 0.85f),
                            fontSize = 11.sp,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                        )
                    }

                    // Reset angle button
                    IconButton(
                        onClick = {
                            rotX = 25f
                            rotY = 45f
                            rotZ = 0f
                        },
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .padding(8.dp)
                            .testTag("reset_3d_angle_btn")
                    ) {
                        Icon(imageVector = Icons.Default.Refresh, contentDescription = "Reset Angle", tint = Color.White)
                    }
                }
            }
        }

        // 3D Model Selector
        item {
            Text(
                text = "🎯 3D मॉडल चुनें (Choose 3D Model)",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(8.dp))
            val models = listOf("3D Cube (घन)", "3D Sphere (गोला)", "3D Torus (डोनट)")
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                models.forEachIndexed { index, name ->
                    val isSelected = modelType == index
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = if (isSelected) PrimaryViolet else MaterialTheme.colorScheme.surfaceVariant,
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(12.dp))
                            .clickable { modelType = index }
                            .testTag("model_btn_$index")
                    ) {
                        Text(
                            text = name,
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(vertical = 10.dp, horizontal = 4.dp),
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                        )
                    }
                }
            }
        }

        // Color & Speed Controls
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("घूर्णन गति (Rotation Speed): ${String.format("%.1f", speed)}x", fontWeight = FontWeight.Medium)
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = if (autoRotate) PrimaryVioletLight else MaterialTheme.colorScheme.surfaceVariant,
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .clickable { autoRotate = !autoRotate }
                                .testTag("toggle_auto_rotate")
                        ) {
                            Text(
                                text = if (autoRotate) "Auto: चालू" else "Auto: बंद",
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                color = if (autoRotate) Color.Black else MaterialTheme.colorScheme.onSurfaceVariant,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    Slider(
                        value = speed,
                        onValueChange = { speed = it },
                        valueRange = 0.2f..3.0f,
                        colors = SliderDefaults.colors(thumbColor = PrimaryViolet, activeTrackColor = PrimaryViolet)
                    )

                    Spacer(modifier = Modifier.height(8.dp))
                    Text("ज़ूम (3D Scale Zoom): ${scaleFactor.toInt()}px", fontWeight = FontWeight.Medium)
                    Slider(
                        value = scaleFactor,
                        onValueChange = { scaleFactor = it },
                        valueRange = 80f..260f,
                        colors = SliderDefaults.colors(thumbColor = CyberCyan, activeTrackColor = CyberCyan)
                    )

                    Spacer(modifier = Modifier.height(8.dp))
                    Text("नियॉन रंग (Neon Glow Color):", fontWeight = FontWeight.Medium)
                    Spacer(modifier = Modifier.height(6.dp))
                    val colorOptions = listOf(CyberCyan, PrimaryVioletLight, NeonPink, LuminousAmber, Color(0xFF00E676))
                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        colorOptions.forEach { col ->
                            Box(
                                modifier = Modifier
                                    .size(32.dp)
                                    .clip(CircleShape)
                                    .background(col)
                                    .clickable { wireColor = col }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun Canvas3DEngine(
    modelType: Int,
    rotX: Float,
    rotY: Float,
    rotZ: Float,
    scale: Float,
    wireColor: Color
) {
    Canvas(modifier = Modifier.fillMaxSize()) {
        val cx = size.width / 2f
        val cy = size.height / 2f

        // Convert angles to radians
        val radX = rotX * (PI.toFloat() / 180f)
        val radY = rotY * (PI.toFloat() / 180f)
        val radZ = rotZ * (PI.toFloat() / 180f)

        // 3D rotation math transformation
        fun rotateAndProject(x: Float, y: Float, z: Float): Triple<Float, Float, Float> {
            // Rot Y
            val x1 = x * cos(radY) + z * sin(radY)
            val y1 = y
            val z1 = -x * sin(radY) + z * cos(radY)

            // Rot X
            val x2 = x1
            val y2 = y1 * cos(radX) - z1 * sin(radX)
            val z2 = y1 * sin(radX) + z1 * cos(radX)

            // Perspective Projection
            val cameraDist = 4.0f
            val distance = (z2 + cameraDist).coerceAtLeast(0.5f)
            val projX = cx + (x2 / distance) * scale * 2.2f
            val projY = cy + (y2 / distance) * scale * 2.2f
            return Triple(projX, projY, z2)
        }

        when (modelType) {
            0 -> {
                // 3D Cube
                val vertices = listOf(
                    Triple(-1f, -1f, -1f), Triple(1f, -1f, -1f), Triple(1f, 1f, -1f), Triple(-1f, 1f, -1f),
                    Triple(-1f, -1f, 1f), Triple(1f, -1f, 1f), Triple(1f, 1f, 1f), Triple(-1f, 1f, 1f)
                )
                val projected = vertices.map { rotateAndProject(it.first, it.second, it.third) }

                val edges = listOf(
                    0 to 1, 1 to 2, 2 to 3, 3 to 0, // front
                    4 to 5, 5 to 6, 6 to 7, 7 to 4, // back
                    0 to 4, 1 to 5, 2 to 6, 3 to 7  // sides
                )

                // Draw connecting edges
                edges.forEach { (a, b) ->
                    val p1 = projected[a]
                    val p2 = projected[b]
                    drawLine(
                        color = wireColor,
                        start = Offset(p1.first, p1.second),
                        end = Offset(p2.first, p2.second),
                        strokeWidth = 2.5.dp.toPx()
                    )
                }

                // Draw vertex glowing nodes
                projected.forEach { p ->
                    drawCircle(
                        color = Color.White,
                        radius = 4.dp.toPx(),
                        center = Offset(p.first, p.second)
                    )
                }
            }

            1 -> {
                // 3D Geodesic Sphere
                val rings = 8
                val segments = 12
                val r = 1.2f

                val grid = mutableListOf<List<Triple<Float, Float, Float>>>()
                for (i in 0..rings) {
                    val theta = i * PI.toFloat() / rings
                    val ringPoints = mutableListOf<Triple<Float, Float, Float>>()
                    for (j in 0 until segments) {
                        val phi = j * 2 * PI.toFloat() / segments
                        val x = r * sin(theta) * cos(phi)
                        val y = r * cos(theta)
                        val z = r * sin(theta) * sin(phi)
                        ringPoints.add(rotateAndProject(x, y, z))
                    }
                    grid.add(ringPoints)
                }

                // Draw latitude & longitude lines
                for (i in 0..rings) {
                    for (j in 0 until segments) {
                        val nextJ = (j + 1) % segments
                        val p1 = grid[i][j]
                        val p2 = grid[i][nextJ]
                        drawLine(
                            color = wireColor.copy(alpha = 0.7f),
                            start = Offset(p1.first, p1.second),
                            end = Offset(p2.first, p2.second),
                            strokeWidth = 1.5.dp.toPx()
                        )
                        if (i < rings) {
                            val pDown = grid[i + 1][j]
                            drawLine(
                                color = wireColor.copy(alpha = 0.5f),
                                start = Offset(p1.first, p1.second),
                                end = Offset(pDown.first, pDown.second),
                                strokeWidth = 1.5.dp.toPx()
                            )
                        }
                    }
                }
            }

            2 -> {
                // 3D Torus Donut
                val majorR = 1.1f
                val minorR = 0.45f
                val stepsU = 10
                val stepsV = 16

                val torusPoints = mutableListOf<List<Triple<Float, Float, Float>>>()
                for (i in 0 until stepsU) {
                    val u = i * 2 * PI.toFloat() / stepsU
                    val ring = mutableListOf<Triple<Float, Float, Float>>()
                    for (j in 0 until stepsV) {
                        val v = j * 2 * PI.toFloat() / stepsV
                        val x = (majorR + minorR * cos(v)) * cos(u)
                        val y = minorR * sin(v)
                        val z = (majorR + minorR * cos(v)) * sin(u)
                        ring.add(rotateAndProject(x, y, z))
                    }
                    torusPoints.add(ring)
                }

                for (i in 0 until stepsU) {
                    val nextI = (i + 1) % stepsU
                    for (j in 0 until stepsV) {
                        val nextJ = (j + 1) % stepsV
                        val p = torusPoints[i][j]
                        val pNextV = torusPoints[i][nextJ]
                        val pNextU = torusPoints[nextI][j]

                        drawLine(
                            color = wireColor,
                            start = Offset(p.first, p.second),
                            end = Offset(pNextV.first, pNextV.second),
                            strokeWidth = 1.5.dp.toPx()
                        )
                        drawLine(
                            color = NeonPink.copy(alpha = 0.6f),
                            start = Offset(p.first, p.second),
                            end = Offset(pNextU.first, pNextU.second),
                            strokeWidth = 1.5.dp.toPx()
                        )
                    }
                }
            }
        }
    }
}

// -------------------------------------------------------------
// 2D PARTICLE & WAVE PHYSICS VIEW
// -------------------------------------------------------------
@Composable
fun Animation2DView() {
    val particles = remember {
        mutableStateListOf<Particle2D>().apply {
            val colors = listOf(PrimaryVioletLight, CyberCyan, NeonPink, LuminousAmber, Color.White)
            repeat(45) {
                add(
                    Particle2D(
                        x = (50..300).random().toFloat(),
                        y = (50..300).random().toFloat(),
                        vx = (-3..3).random().toFloat().coerceAtLeast(1f),
                        vy = (-3..3).random().toFloat().coerceAtLeast(1f),
                        color = colors.random(),
                        radius = (3..8).random().toFloat()
                    )
                )
            }
        }
    }

    var waveFrequency by remember { mutableFloatStateOf(2.5f) }
    var waveAmplitude by remember { mutableFloatStateOf(35f) }

    // Particle physics tick
    LaunchedEffect(Unit) {
        while (true) {
            delay(16)
            particles.forEach { p ->
                p.x += p.vx
                p.y += p.vy
                if (p.x < 10 || p.x > 360) p.vx *= -1
                if (p.y < 10 || p.y > 280) p.vy *= -1
            }
        }
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(300.dp)
                    .testTag("canvas_2d_physics"),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF0C091C))
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .pointerInput(Unit) {
                            detectTapGestures { offset ->
                                // Tap to burst particles towards touch!
                                particles.forEach { p ->
                                    p.vx = (offset.x - p.x) * 0.05f
                                    p.vy = (offset.y - p.y) * 0.05f
                                }
                            }
                        }
                ) {
                    Canvas(modifier = Modifier.fillMaxSize()) {
                        val w = size.width
                        val h = size.height

                        // Harmonic Sine Waves
                        val wavePath = Path()
                        val wavePath2 = Path()
                        wavePath.moveTo(0f, h * 0.75f)
                        wavePath2.moveTo(0f, h * 0.8f)

                        for (x in 0..w.toInt() step 5) {
                            val y = h * 0.75f + sin((x * 0.02f * waveFrequency)) * waveAmplitude
                            val y2 = h * 0.8f + cos((x * 0.025f * waveFrequency)) * (waveAmplitude * 0.7f)
                            wavePath.lineTo(x.toFloat(), y)
                            wavePath2.lineTo(x.toFloat(), y2)
                        }

                        drawPath(wavePath, CyberCyan, style = Stroke(width = 3.dp.toPx()))
                        drawPath(wavePath2, NeonPink.copy(alpha = 0.8f), style = Stroke(width = 2.dp.toPx()))

                        // Draw Particles & connection webs
                        for (i in particles.indices) {
                            val p1 = particles[i]
                            drawCircle(p1.color, p1.radius, Offset(p1.x, p1.y))

                            for (j in i + 1 until particles.size) {
                                val p2 = particles[j]
                                val dx = p1.x - p2.x
                                val dy = p1.y - p2.y
                                val distSq = dx * dx + dy * dy
                                if (distSq < 4500) { // ~67px
                                    val alpha = (1f - distSq / 4500f) * 0.4f
                                    drawLine(
                                        color = CyberCyan.copy(alpha = alpha),
                                        start = Offset(p1.x, p1.y),
                                        end = Offset(p2.x, p2.y),
                                        strokeWidth = 1.dp.toPx()
                                    )
                                }
                            }
                        }
                    }

                    Surface(
                        color = Color.Black.copy(alpha = 0.5f),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .align(Alignment.BottomCenter)
                            .padding(12.dp)
                    ) {
                        Text(
                            text = "✨ स्क्रीन पर टैप करें - पार्टिकल्स गुरुत्वाकर्षण से खिंचेंगे",
                            color = Color.White.copy(alpha = 0.85f),
                            fontSize = 11.sp,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                        )
                    }
                }
            }
        }

        // Wave Controls
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text("तरंग आवृत्ति (Wave Frequency): ${String.format("%.1f", waveFrequency)}", fontWeight = FontWeight.Bold)
                    Slider(
                        value = waveFrequency,
                        onValueChange = { waveFrequency = it },
                        valueRange = 0.5f..6.0f,
                        colors = SliderDefaults.colors(thumbColor = CyberCyan, activeTrackColor = CyberCyan)
                    )

                    Spacer(modifier = Modifier.height(6.dp))
                    Text("तरंग आयाम (Wave Amplitude): ${waveAmplitude.toInt()}px", fontWeight = FontWeight.Bold)
                    Slider(
                        value = waveAmplitude,
                        onValueChange = { waveAmplitude = it },
                        valueRange = 10f..70f,
                        colors = SliderDefaults.colors(thumbColor = NeonPink, activeTrackColor = NeonPink)
                    )
                }
            }
        }
    }
}
