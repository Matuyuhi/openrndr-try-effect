import org.openrndr.application
import org.openrndr.color.ColorRGBa
import org.openrndr.extra.olive.oliveProgram
import org.openrndr.math.Vector2
import org.openrndr.shape.ShapeContour
import org.openrndr.shape.contour
import kotlin.math.sin

/*
*
 */
fun main() = application {
    configure {
        width = 700
        height = 700
    }

    oliveProgram {
        // Shape of the letter 'K'
        val shape = contour {
            val size = 100.0
            val padding = 10.0
            moveTo(padding, padding)
            lineTo(padding, size + padding)
            lineTo(padding + size, padding + size)
            lineTo(padding + size / 2, padding + size / 2)
            lineTo(padding + size, padding)
            close()
        }
        var t = 0.0
        extend {
            drawer.clear(ColorRGBa.BLACK)

            // default
            drawer.fill = ColorRGBa.WHITE
            drawer.stroke = null
            drawer.translate(0.0, 0.0)
            drawer.contour(shape)

            // Top left pattern: dots
            drawer.fill = ColorRGBa.WHITE
            drawer.stroke = null
            drawer.translate(0.0, 200.0)
            drawDots(drawer, shape, 10.0)

            // Top middle pattern: blocks
            drawer.translate(120.0, -5.0)
            drawBlocks(drawer, shape, 5.0)

            // Top right pattern: triangles
            drawer.translate(120.0, 0.0)
            drawTriangles(drawer, shape, 10.0)

            // Bottom left pattern: outline
            drawer.translate(120.0, 0.0)
            drawer.stroke = ColorRGBa.WHITE
            drawer.fill = ColorRGBa.PINK
            drawer.contour(shape)

            // Bottom middle pattern: wavy outline
            drawer.translate(-360.0, 130.0)
            drawWavyOutline(drawer, shape, 10.0)

            // Bottom right pattern: splatter
            drawer.translate(120.0, 0.0)
            drawSplatter(drawer, shape, 150.0)


            drawer.translate(120.0, 0.0)
            drawer.stroke = ColorRGBa.WHITE
            drawer.strokeWeight = 2.0

            // 波打つ輪郭を作成
            val wavyShape = shape.adaptivePositions().mapIndexed { i, pos ->
                val angle = (i * 0.5 + t) * 5.0 * Math.PI
                val offset = Vector2(sin(angle) * 10.0, sin(angle) * 10.0)
                pos + offset
            }.let { ShapeContour.fromPoints(it, shape.closed) }

            drawer.contour(wavyShape)

            // 時間を進める
            t += 0.01
        }
    }
}

