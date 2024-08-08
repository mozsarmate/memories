package hu.mat.life

import android.os.Bundle
import android.util.Log
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
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
                            lines = getCircleCenter2(path)
                            if (path.size > 1) {
                                totalRotationDegrees = calculateRotationDegrees(path, center)
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
                    drawPath(path, lines, center)
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

fun getCircleCenter2(path: List<Offset>): List<Offset> {
    val ans = mutableListOf<Offset>()
    val resolution = 4
    val perpOffset = 60
    
    if (path.size < resolution * 2) return listOf(Offset(0f, 0f))
    val lines: MutableList<Pair<Offset, Offset>> = mutableListOf()
    
    //for until resolution
    for (i in 1 until resolution) {
        val curidx = (path.size - 1) / resolution * i
        ans += path[curidx]
        val left = if ((curidx - perpOffset) < 0) 0 else curidx - perpOffset
        val right =
            if ((curidx + perpOffset) > path.size - 1) path.size - 1 else curidx + perpOffset
        
        val perpDirVec = calculatePerpendicularDirection(path[curidx], path[left], path[right])
        lines += Pair(
            path[curidx],
            Offset(path[curidx].x + perpDirVec.x, path[curidx].y + perpDirVec.y)
        )
        ans += Offset(path[curidx].x + perpDirVec.x, path[curidx].y + perpDirVec.y)
    }
    
    val intersections: MutableList<Offset> = mutableListOf()
    for (i in 0 until lines.size - 2) {
        val inter = findLineIntersection(
            lines[i].first,
            lines[i].second,
            lines[i + 1].first,
            lines[i + 1].second
        )
        if (inter != null) {
            intersections += inter
            ans += inter
        }
    }
    
    val center2x = intersections.map{it.x}.average().toFloat()
    val center2y = intersections.map{it.y}.average().toFloat()
    ans += Offset(center2x, center2y)
    
    return ans
    
}

fun calculateRotationDegrees(path: List<Offset>, center: Offset): Float {
    if (path.size < 10f) return 0f
    
    
    var totalDegrees = 0f
    
    
    //for (i in 1 until path.size) {
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
    //}
    
    return totalDegrees
}

fun DrawScope.drawPath(path: List<Offset>, lines: List<Offset>, center: Offset) {
    for (i in 1 until path.size) {
        drawLine(
            color = Color.Red,
            start = path[i - 1],
            end = path[i],
            strokeWidth = 5f
        )
    }
    //drawCircle(color = Color.Blue, radius = 5f, center = center)
    Log.d("MAIN", lines.size.toString())
    if(lines.size > 20) {
        for (i in 0 until 4) {
            drawCircle(color = Color.Green, radius = 5f, center = lines[2 * i])
            drawLine(color = Color.Cyan, start = lines[i * 2], end = lines[i * 2 + 1], strokeWidth = 3f)
            if (lines.size > 2 * 4 + i) {
                drawCircle(color = Color.Red, radius = 3f, center = lines[2 * 4 + i])
            }
        }
    }
    drawLine(color = Color.Cyan, start = path[0], end = center)
    drawLine(color = Color.Cyan, start = path[path.size - 1], end = center)
}


/**
 * Calculates the intersection point of two lines defined by two points each.
 *
 * @param p1 The first point of the first line.
 * @param p2 The second point of the first line.
 * @param p3 The first point of the second line.
 * @param p4 The second point of the second line.
 * @return The intersection point as an Offset, or null if the lines are parallel.
 */
fun findLineIntersection(p1: Offset, p2: Offset, p3: Offset, p4: Offset): Offset? {
    // Calculate differences
    val dx1 = p2.x - p1.x
    val dy1 = p2.y - p1.y
    val dx2 = p4.x - p3.x
    val dy2 = p4.y - p3.y
    
    // Calculate determinants
    val determinant = dx1 * dy2 - dy1 * dx2
    
    // Check if lines are parallel (determinant is zero)
    if (determinant == 0f) {
        return null // No intersection, lines are parallel
    }
    
    // Calculate the intersection point using Cramer's Rule
    val dx3 = p3.x - p1.x
    val dy3 = p3.y - p1.y
    
    val t1 = (dx3 * dy2 - dy3 * dx2) / determinant
    val x = p1.x + t1 * dx1
    val y = p1.y + t1 * dy1
    
    return Offset(x, y)
}

/**
 * @param p The point through which the perpendicular line passes.
 * @param q The first point defining the original line.
 * @param r The second point defining the original line.
 * @return An Offset representing the direction of the perpendicular line.
 *         The Offset is normalized to indicate direction.
 */
fun calculatePerpendicularDirection(p: Offset, q: Offset, r: Offset): Offset {
    // Calculate the direction vector of the original line QR
    val dx = r.x - q.x
    val dy = r.y - q.y
    
    // Calculate the perpendicular direction
    val perpendicularDx = -dy // Perpendicular vector x-component
    val perpendicularDy = dx  // Perpendicular vector y-component
    
    // Normalize the direction to have a consistent magnitude (optional)
    val magnitude =
        Math.sqrt((perpendicularDx * perpendicularDx + perpendicularDy * perpendicularDy).toDouble())
            .toFloat()
    val normalizedPerpendicularDx = perpendicularDx / magnitude
    val normalizedPerpendicularDy = perpendicularDy / magnitude
    
    return Offset(normalizedPerpendicularDx, normalizedPerpendicularDy)
}
