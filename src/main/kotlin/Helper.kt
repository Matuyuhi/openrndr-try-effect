import org.openrndr.draw.Drawer
import org.openrndr.extra.noise.Random
import org.openrndr.math.Vector2
import org.openrndr.shape.ShapeContour
import org.openrndr.shape.contains
import org.openrndr.shape.contour

fun drawDots(drawer: Drawer, shape: ShapeContour, spacing: Double) {
    val bounds = shape.bounds
    for (y in bounds.y.toInt()..bounds.y.toInt() + bounds.height.toInt() step spacing.toInt()) {
        for (x in bounds.x.toInt()..bounds.x.toInt() + bounds.width.toInt() step spacing.toInt()) {
            if (shape.contains(Vector2(x.toDouble(), y.toDouble()))) {
                drawer.circle(x.toDouble(), y.toDouble(), 2.0)
            }
        }
    }
}

fun drawBlocks(drawer: Drawer, shape: ShapeContour, spacing: Double) {
    val bounds = shape.bounds
    for (y in bounds.y.toInt()..bounds.y.toInt() + bounds.height.toInt() step spacing.toInt()) {
        for (x in bounds.x.toInt()..bounds.x.toInt() + bounds.width.toInt() step spacing.toInt()) {
            if (shape.contains(Vector2(x.toDouble(), y.toDouble()))) {
                drawer.rectangle(x.toDouble(), y.toDouble(), spacing, spacing)
            }
        }
    }
}

fun drawTriangles(drawer: Drawer, shape: ShapeContour, spacing: Double) {
    val bounds = shape.bounds
    for (y in bounds.y.toInt()..bounds.y.toInt() + bounds.height.toInt() step spacing.toInt()) {
        for (x in bounds.x.toInt()..bounds.x.toInt() + bounds.width.toInt() step spacing.toInt()) {
            if (shape.contains(Vector2(x.toDouble(), y.toDouble()))) {
                val triangle = contour {
                    moveTo(x.toDouble(), y.toDouble())
                    lineTo(x + spacing, y.toDouble())
                    lineTo(x.toDouble(), y + spacing)
                    close()
                }
                drawer.contour(triangle)
            }
        }
    }
}

fun drawWavyOutline(drawer: Drawer, shape: ShapeContour, amplitude: Double) {
    val wavyShape = shape.adaptivePositions().map {
        val offset = Random.double(-amplitude, amplitude)
        it + it.normalized * offset
    }.let { ShapeContour.fromPoints(it, shape.closed) }
    drawer.contour(wavyShape)
}

fun drawSplatter(drawer: Drawer, shape: ShapeContour, density: Double) {
    val bounds = shape.bounds
    repeat(density.toInt()) {
        val x = Random.double(bounds.x, bounds.x + bounds.width)
        val y = Random.double(bounds.y, bounds.y + bounds.height)
        if (shape.contains(Vector2(x, y))) {
            drawer.circle(x, y, Random.double(2.0, 10.0))
        }
    }
}