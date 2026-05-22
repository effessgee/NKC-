package com.example.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.Complaint
import com.example.ui.theme.*

/**
 * Signature Drawing Pad Canvas.
 * Let's capture actual touch drawings.
 */
@Composable
fun SignaturePad(
    modifier: Modifier = Modifier,
    onSignatureCaptured: (String) -> Unit
) {
    var points by remember { mutableStateOf(listOf<Offset>()) }
    var lines by remember { mutableStateOf(listOf<List<Offset>>()) }

    Column(
        modifier = modifier
            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f), RoundedCornerShape(12.dp))
            .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.5f), RoundedCornerShape(12.dp))
            .padding(8.dp)
    ) {
        Text(
            text = "Digital Signature (Branch Incharge Sign Panel)",
            style = MaterialTheme.typography.titleSmall,
            color = MaterialTheme.colorScheme.onSurface,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 6.dp)
        )

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(140.dp)
                .background(Color.White, RoundedCornerShape(8.dp))
                .border(2.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(8.dp))
                .pointerInput(Unit) {
                    detectDragGestures(
                        onDragStart = { offset ->
                            points = listOf(offset)
                        },
                        onDrag = { change, dragAmount ->
                            change.consume()
                            val nextPoint = change.position
                            points = points + nextPoint
                        },
                        onDragEnd = {
                            if (points.isNotEmpty()) {
                                lines = lines + listOf(points)
                                points = emptyList()
                                onSignatureCaptured("Captured_Sig_${lines.size}_Points")
                            }
                        }
                    )
                }
        ) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                // Draw historic line strokes
                lines.forEach { linePoints ->
                    if (linePoints.size > 1) {
                        val path = Path().apply {
                            moveTo(linePoints.first().x, linePoints.first().y)
                            for (i in 1 until linePoints.size) {
                                lineTo(linePoints[i].x, linePoints[i].y)
                            }
                        }
                        drawPath(
                            path = path,
                            color = Color.Black,
                            style = Stroke(width = 6f, cap = StrokeCap.Round)
                        )
                    }
                }

                // Draw active stroke
                if (points.size > 1) {
                    val path = Path().apply {
                        moveTo(points.first().x, points.first().y)
                        for (i in 1 until points.size) {
                            lineTo(points[i].x, points[i].y)
                        }
                    }
                    drawPath(
                        path = path,
                        color = Color.Blue,
                        style = Stroke(width = 6f, cap = StrokeCap.Round)
                    )
                }

                if (lines.isEmpty() && points.isEmpty()) {
                    // Instruction text inside white canvas
                    // Drawn on canvas explicitly
                }
            }

            if (lines.isEmpty() && points.isEmpty()) {
                Text(
                    text = "Sign here with your finger",
                    color = Color.LightGray,
                    fontSize = 14.sp,
                    modifier = Modifier.align(Alignment.Center)
                )
            }
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 6.dp),
            horizontalArrangement = Arrangement.End
        ) {
            TextButton(
                onClick = {
                    lines = emptyList()
                    points = emptyList()
                    onSignatureCaptured("")
                }
            ) {
                Icon(Icons.Default.Clear, contentDescription = "Clear", modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("Clear Signature")
            }
        }
    }
}

/**
 * Camera Capture & Photo Selector simulation.
 */
@Composable
fun InteractivePhotoSelector(
    title: String,
    currentSelection: String?,
    category: String, // "Mechanical", "Electrical", "Plumbing", "Janitorial"
    isResult: Boolean = false, // false = Before, true = After
    onPhotoSelected: (String) -> Unit
) {
    // Elegant predefined high contrast canvas images designed for cleaning/MEP
    val dirtyTemplates = when (category) {
        "Janitorial" -> listOf("DIRTY_LOUNGE_STAIN", "UNSANITIZED_LOUNGE_TILE")
        "MEP - Electrical" -> listOf("MOCK_ELECTRICAL_SHORT", "DISORGANIZED_BREAKER")
        "MEP - Plumbing" -> listOf("CLOGGED_DRAIN_FLOOD", "LEAKING_SEWAGE_COUPLE")
        else -> listOf("AC_CHILLER_LEAK_ICE", "HVAC_DUCTOR_RUST")
    }

    val cleanTemplates = when (category) {
        "Janitorial" -> listOf("SPARKLING_LOUNGE_POLISHED", "SANITIZED_LOUNGE_COMPLETED")
        "MEP - Electrical" -> listOf("REPAIRED_ELECTRICAL_BOX", "REORGANIZED_SECURE_BREAKER")
        "MEP - Plumbing" -> listOf("CLEARED_DRAIN_SEWAGE", "SEALED_WELL_COUPLED_PIPE")
        else -> listOf("AC_CHILLER_FRESH_GAS", "HVAC_AIRFLOW_CLEAN")
    }

    val selectedTemplates = if (isResult) cleanTemplates else dirtyTemplates
    var activeInkPoints by remember { mutableStateOf(listOf<Offset>()) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceColorAtElevation(2.dp))
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(4.dp))

            if (currentSelection != null) {
                // Photo showing card
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(160.dp)
                        .background(MaterialTheme.colorScheme.inverseOnSurface, RoundedCornerShape(8.dp))
                        .border(1.dp, MaterialTheme.colorScheme.primary, RoundedCornerShape(8.dp))
                ) {
                    // Draw template graphic
                    Canvas(modifier = Modifier.fillMaxSize()) {
                        val drawColor = if (isResult) Color(0xFF2ECC71) else Color(0xFFE74C3C)
                        val caption = currentSelection.replace("_", " ")

                        // Artistic representation
                        drawRect(
                            brush = Brush.linearGradient(listOf(drawColor.copy(0.15f), drawColor.copy(0.02f))),
                            size = size
                        )

                        // Draw visual schematic circles representing components
                        drawCircle(
                            color = drawColor.copy(alpha = 0.3f),
                            radius = size.width / 5f,
                            center = Offset(size.width / 3f, size.height / 2f),
                            style = Stroke(width = 4f)
                        )
                        drawCircle(
                            color = drawColor.copy(alpha = 0.5f),
                            radius = size.width / 8f,
                            center = Offset(size.width * 2/3f, size.height * 1/3f)
                        )

                        // Draw lines
                        drawLine(
                            color = drawColor.copy(0.4f),
                            start = Offset(0f, size.height / 2),
                            end = Offset(size.width, size.height / 2),
                            strokeWidth = 3f
                        )
                    }

                    // Overlay details
                    Column(
                        modifier = Modifier
                            .align(Alignment.BottomStart)
                            .padding(8.dp)
                    ) {
                        Text(
                            text = if (isResult) "✅ WORK RESOLVED PHOTO" else "⚠️ BEFORE RESOLUTION PHOTO",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (isResult) Color(0xFF2ECC71) else Color(0xFFE74C3C)
                        )
                        Text(
                            text = "Reference: ${currentSelection.replace("_", " ")}",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }

                    // Reset button
                    IconButton(
                        onClick = { onPhotoSelected("") },
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .padding(4.dp)
                            .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.8f), CircleShape)
                    ) {
                        Icon(Icons.Default.Refresh, contentDescription = "Retake", tint = MaterialTheme.colorScheme.primary)
                    }
                }
            } else {
                // Interactive Camera Simulator doodle pad
                Text(
                    text = "No photo captured yet. Select standard camera capture preset below or sketch the problem site outline:",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(bottom = 6.dp)
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    selectedTemplates.forEach { template ->
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .height(54.dp)
                                .background(
                                    MaterialTheme.colorScheme.primary.copy(alpha = 0.08f),
                                    RoundedCornerShape(8.dp)
                                )
                                .border(
                                    1.dp,
                                    MaterialTheme.colorScheme.primary.copy(alpha = 0.3f),
                                    RoundedCornerShape(8.dp)
                                )
                                .clickable { onPhotoSelected(template) }
                                .padding(6.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = template.replace("DIRTY_", "").replace("CLEAN_", "").replace("_", " ").take(22) + if (template.length > 22) ".." else "",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary,
                                maxLines = 2
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Sketch Option
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(100.dp)
                        .background(
                            MaterialTheme.colorScheme.surfaceColorAtElevation(1.dp),
                            RoundedCornerShape(8.dp)
                        )
                        .border(1.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(8.dp))
                        .pointerInput(Unit) {
                            detectDragGestures(
                                onDragEnd = {
                                    if (activeInkPoints.isNotEmpty()) {
                                        onPhotoSelected("CUSTOM_FIELD_DOODLE_${activeInkPoints.hashCode()}")
                                    }
                                },
                                onDrag = { change, dragAmount ->
                                    change.consume()
                                    activeInkPoints = activeInkPoints + change.position
                                }
                            )
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Canvas(modifier = Modifier.fillMaxSize()) {
                        if (activeInkPoints.size > 1) {
                            val path = Path().apply {
                                moveTo(activeInkPoints.first().x, activeInkPoints.first().y)
                                for (i in 1 until activeInkPoints.size) {
                                    lineTo(activeInkPoints[i].x, activeInkPoints[i].y)
                                }
                            }
                            drawPath(
                                path = path,
                                color = Color.Gray,
                                style = Stroke(width = 4f)
                            )
                        }
                    }

                    if (activeInkPoints.isEmpty()) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(Icons.Default.CameraAlt, contentDescription = "Doodle", tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(0.5f))
                            Text(
                                "Or draw freehand outlines to capture site",
                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(0.6f),
                                fontSize = 11.sp
                            )
                        }
                    } else {
                        Text(
                            "DRAWING REGISTERED! Click preset to overwrite, or lift finger to finalize capture.",
                            color = MaterialTheme.colorScheme.primary,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.align(Alignment.BottomCenter).padding(4.dp)
                        )
                    }
                }
            }
        }
    }
}

/**
 * Custom Canvas Charts drawing total completion rates, high urgency tasks, and category ratios.
 */
@Composable
fun CanvasDashboardCharts(
    complaints: List<Complaint>
) {
    val total = complaints.size
    val pending = complaints.count { it.status == "Pending" }
    val assigned = complaints.count { it.status == "Assigned" }
    val inProgress = complaints.count { it.status == "In Progress" }
    val done = complaints.count { it.status == "Done" }
    val completed = complaints.count { it.status == "Completed" }

    val cleaningCount = complaints.count { it.category == "Janitorial" }
    val mepCount = total - cleaningCount

    val highUrgency = complaints.count { it.urgency == "High" }
    val mediumUrgency = complaints.count { it.urgency == "Medium" }
    val lowUrgency = complaints.count { it.urgency == "Low" }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // Completion bar chart card
        Card(
            modifier = Modifier.weight(1.3f),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(modifier = Modifier.padding(12.dp)) {
                Text(
                    text = "Resolution Pipeline Status",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(14.dp))

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(110.dp),
                    horizontalArrangement = Arrangement.SpaceAround,
                    verticalAlignment = Alignment.Bottom
                ) {
                    val states = listOf(
                        StateBarData("Pending", pending, Color(0xFFE74C3C)),
                        StateBarData("Assign", assigned, Color(0xFF3498DB)),
                        StateBarData("Active", inProgress, Color(0xFFF1C40F)),
                        StateBarData("Done", done + completed, Color(0xFF2ECC71))
                    )

                    val maxCount = states.maxOfOrNull { it.count } ?: 1
                    val divisor = if (maxCount == 0) 1 else maxCount

                    states.forEach { state ->
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Bottom,
                            modifier = Modifier.fillMaxHeight()
                        ) {
                            Text(
                                text = state.count.toString(),
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Spacer(modifier = Modifier.height(4.dp))

                            Box(
                                modifier = Modifier
                                    .width(22.dp)
                                    .height(((state.count.toFloat() / divisor.toFloat()) * 70f).dp.coerceAtLeast(4.dp))
                                    .background(state.color, RoundedCornerShape(topStart = 4.dp, topEnd = 4.dp))
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = state.label,
                                fontSize = 10.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        }

        // Division ratio donut canvas card
        Card(
            modifier = Modifier.weight(1f),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(
                modifier = Modifier.padding(12.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Janitorial vs MEP Ratios",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.align(Alignment.Start)
                )
                Spacer(modifier = Modifier.height(16.dp))

                Box(
                    modifier = Modifier.size(80.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Canvas(modifier = Modifier.fillMaxSize()) {
                        val strokeWidth = 16f
                        val cleaningRatio = if (total > 0) cleaningCount.toFloat() / total.toFloat() else 0.5f
                        val mepRatio = 1f - cleaningRatio

                        // Cleaning division sweep
                        drawArc(
                            color = Color(0xFF00AAFF),
                            startAngle = -90f,
                            sweepAngle = cleaningRatio * 360f,
                            useCenter = false,
                            style = Stroke(width = strokeWidth)
                        )

                        // MEP division sweep
                        drawArc(
                            color = Color(0xFFFFA726),
                            startAngle = -90f + (cleaningRatio * 360f),
                            sweepAngle = mepRatio * 360f,
                            useCenter = false,
                            style = Stroke(width = strokeWidth)
                        )
                    }

                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = total.toString(),
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "TOTAL",
                            fontSize = 8.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceAround
                ) {
                    IndicatorLabel("Clean", Color(0xFF00AAFF), cleaningCount)
                    IndicatorLabel("MEP", Color(0xFFFFA726), mepCount)
                }
            }
        }
    }
}

private data class StateBarData(val label: String, val count: Int, val color: Color)

@Composable
private fun IndicatorLabel(text: String, color: Color, count: Int) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            modifier = Modifier
                .size(8.dp)
                .background(color, CircleShape)
        )
        Spacer(modifier = Modifier.width(4.dp))
        Text(text = "$text ($count)", fontSize = 10.sp, fontWeight = FontWeight.Bold)
    }
}


/**
 * Pakistan Map Schematic Board with City Node Indicators.
 */
@Composable
fun PakistanMapSchematicBoard(
    complaints: List<Complaint>,
    onCitySelected: (String) -> Unit
) {
    val cityData = listOf(
        MapCityNode("Karachi", 24.86, 67.00, Offset(50f, 170f)),
        MapCityNode("Lahore", 31.52, 74.35, Offset(175f, 105f)),
        MapCityNode("Islamabad", 33.68, 73.04, Offset(160f, 65f)),
        MapCityNode("Peshawar", 34.01, 71.52, Offset(135f, 65f)),
        MapCityNode("Rawalpindi", 33.59, 73.04, Offset(165f, 75f)),
        MapCityNode("Faisalabad", 31.41, 73.07, Offset(155f, 110f))
    )

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text(
                text = "Pakistan Nationwide Branches Map (4000+ Staff Tracker)",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Interactive geographic service nodes. Red blinking indicates active unresolved complaints.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
                    .background(
                        MaterialTheme.colorScheme.primary.copy(alpha = 0.03f),
                        RoundedCornerShape(8.dp)
                    )
                    .border(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.1f), RoundedCornerShape(8.dp)),
                contentAlignment = Alignment.Center
            ) {
                // Pakistan Outline Art
                Canvas(modifier = Modifier.fillMaxSize()) {
                    // Stylized vector path of boundaries of Pakistan
                    val path = Path().apply {
                        moveTo(40f, 160f)
                        lineTo(30f, 110f)
                        lineTo(60f, 90f)
                        lineTo(80f, 100f)
                        lineTo(110f, 60f)
                        lineTo(130f, 50f)
                        lineTo(170f, 40f)
                        lineTo(190f, 70f)
                        lineTo(210f, 95f)
                        lineTo(180f, 130f)
                        lineTo(150f, 140f)
                        lineTo(120f, 180f)
                        lineTo(70f, 195f)
                        close()
                    }

                    // Scaling the outline to center
                    drawPath(
                        path = path,
                        brush = Brush.linearGradient(
                            listOf(
                                Color.Green.copy(0.04f),
                                Color.LightGray.copy(0.02f)
                            )
                        ),
                        style = Stroke(width = 2f)
                    )
                }

                // Node items rendered as overlays
                cityData.forEach { city ->
                    val cityComplaints = complaints.filter { it.city.lowercase() == city.name.lowercase() }
                    val activePending = cityComplaints.count { it.status == "Pending" || it.status == "Assigned" || it.status == "In Progress" }
                    val statusColor = when {
                        activePending > 1 -> UrgencyHigh
                        activePending == 1 -> UrgencyMedium
                        else -> UrgencyLow
                    }

                    Box(
                        modifier = Modifier
                            .offset(x = city.drawOffset.x.dp, y = city.drawOffset.y.dp)
                            .wrapContentSize()
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .background(
                                    MaterialTheme.colorScheme.surface.copy(alpha = 0.85f),
                                    RoundedCornerShape(6.dp)
                                )
                                .border(1.dp, statusColor.copy(alpha = 0.8f), RoundedCornerShape(6.dp))
                                .clickable { onCitySelected(city.name) }
                                .padding(horizontal = 4.dp, vertical = 2.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(6.dp)
                                    .background(statusColor, CircleShape)
                            )
                            Spacer(modifier = Modifier.width(3.dp))
                            Text(
                                text = "${city.name} (${cityComplaints.size})",
                                fontSize = 8.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }
            }
        }
    }
}

private data class MapCityNode(val name: String, val lat: Double, val lng: Double, val drawOffset: Offset)
