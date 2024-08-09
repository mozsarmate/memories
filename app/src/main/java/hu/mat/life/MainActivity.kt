package hu.mat.life

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.DrawStyle
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.*
import androidx.compose.ui.unit.dp
import java.lang.Math.toDegrees
import kotlin.math.*

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            RotationDegreeDetector()
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RotationDegreeDetector() {
    var path by remember { mutableStateOf(listOf<Offset>()) }
    var center by remember { mutableStateOf(Offset(0f, 0f)) }
    var lines by remember { mutableStateOf(listOf<Offset>()) }
    var totalRotationDegrees by remember { mutableStateOf(0f) }
    
    
    
    Scaffold(
        topBar = {
            TopAppBar(title = { Text("Rotation Degree Detector") })
        },
        content = { paddingValues ->
            Canvas(modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .pointerInput(Unit) {
                    detectDragGestures(
                        onDragStart = { offset ->
                            path = listOf(offset)
                            center = Offset(0f, 0f)
                        },
                        onDrag = { change, _ ->
                            path = path + change.position
                            val centerX = path
                                .map { it.x }
                                .average()
                                .toFloat()
                            val centerY = path
                                .map { it.y }
                                .average()
                                .toFloat()
                            center = Offset(centerX, centerY)
                            if (path.size > 1) {
                                totalRotationDegrees = calculateRotationDegrees2(path)
                            }
                        },
                        onDragEnd = {
                            path = emptyList()
                            totalRotationDegrees = 0f
                        }
                    )
                }
            ) {
                if (path.isNotEmpty()) {
                    drawPath(path, lines, totalRotationDegrees)
                }
            }
        }
    )
    
    Text(
        text = "Rotation: ${"%.2f".format(totalRotationDegrees)}°",
        modifier = Modifier.padding(64.dp),
        color = Color.Black
    )
}


fun calculateRotationDegrees(path: List<Offset>, center: Offset): Float {
    if (path.size < 10f) return 0f
    
    var totalDegrees = 0f
    
    
    val start = path[0]
    val end = path[path.size - 1]
    
    // Calculate angles from the center
    val angle1 = atan2(start.y - center.y, start.x - center.x)
    val angle2 = atan2(end.y - center.y, end.x - center.x)
    
    // Calculate the angle difference in degrees
    var degrees = toDegrees((angle1 - angle2).toDouble())
    
    // Normalize the degrees to be within -180 to 180
    degrees = (degrees + 360) % 360
    if (degrees > 180) {
        degrees -= 360
    }
    
    totalDegrees = degrees.toFloat()
    
    
    return totalDegrees
}

fun calculateRotationDegrees2(path: List<Offset>): Float {
    if (path.size < 10f) return 0f
    
    var totalDegrees = 0f
    
    val start0 = path[0]
    val start1 = path[((path.size - 1) * 0.1).toInt()]
    val end0 = path[((path.size - 1) * 0.9).toInt()]
    val end1 = path[path.size - 1]
    
    val startV = start1 - start0
    val endV = end1 - end0
    
    var degrees = (atan2(endV.y, endV.x) - atan2(startV.y, startV.x)).toFloat()
    degrees = degrees * 180 / Math.PI.toFloat()
    if (degrees < 0) degrees += 360
    return degrees
}

fun DrawScope.drawPath(path: List<Offset>, lines: List<Offset>, rotation: Float) {
    for(i in 0 until path.size-1){
        drawLine(start = path[i], end = path[i+1], color = getColor(rotation, i / path.size.toFloat()), strokeWidth = 10f)
    }
    
    /*    drawPath(
            color = Color.Green,
            path = ,
            style = Stroke(
                width = 3.dp.toPx(),
                pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 10f))
            )
        )*/
    
    drawLine(color = Color.Cyan, start = path[0], end = center)
    drawLine(color = Color.Cyan, start = path[path.size - 1], end = center)
}
fun getColor(rotation: Float, curProgress : Float) : Color {
    val colors = listOf<Color>(Color.Red, Color.Green, Color.Blue, Color.Yellow)
    var offsettedCurPogress = curProgress + (rotation/360)
    offsettedCurPogress *= colors.size
    offsettedCurPogress %= colors.size
    val lowerBound = offsettedCurPogress.toInt()
    val upperBound = (lowerBound + 1) % colors.size
    val colorFrom = colors[lowerBound]
    val colorTo = colors[upperBound]
    val mixCoef = offsettedCurPogress - lowerBound
    return mixColors(colorFrom, colorTo, mixCoef)
}

fun mixColors(a : Color, b : Color, coef : Float) : Color{
    return Color(red = (a.red * coef + b.red * (1-coef)), blue = (a.blue * coef + b.blue * (1-coef)), green = (a.green * coef + b.green * (1-coef)), alpha = (a.alpha * coef + b.alpha * (1-coef)), colorSpace = a.colorSpace)
}

/*
fun convertOffsetsToPath(offsets: List<Offset>): Path {
    val path = Path()
    
    if (offsets.isNotEmpty()) {
        // Move to the first point
        val startPoint = offsets.first()
        path.moveTo(startPoint.x, startPoint.y)
        
        // Iterate over the rest of the points
        for (i in 1 until offsets.size) {
            val point = offsets[i]
            path.lineTo(point.x, point.y)
        }
    }
    
    return path
}*/
