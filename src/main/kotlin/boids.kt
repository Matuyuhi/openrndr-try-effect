
import org.openrndr.application
import org.openrndr.color.ColorRGBa
import org.openrndr.extra.gui.GUI
import org.openrndr.extra.noise.uniform
import org.openrndr.extra.olive.oliveProgram
import org.openrndr.extra.parameters.BooleanParameter
import org.openrndr.math.Vector2
import org.openrndr.shape.contour

data class Boid(var position: Vector2, var velocity: Vector2)

/*
 * boids
 * 鳥の群れや魚の群れのような集団行動をシミュレートするためのアルゴリズム。
 * Boidsは、個々の「ボイド（Boid）」と呼ばれるエージェントが以下の3つのルールに基づいて動作します：
	1.	整列（Alignment）:
	•	周囲のBoidの平均的な方向に合わせる。
	•	同じ方向に向かって移動する。
	2.	結合（Cohesion）:
	•	周囲のBoidの中心に向かって移動する。
	•	群れの中心に向かう。
	3.	分離（Separation）:
	•	他のBoidとの衝突を避けるために距離を取る。
	•	他のBoidから離れる。
目的:

	•	集団の自然な動きをシミュレートする。
	•	群れ、学校、群集などの行動パターンを理解するための研究。
	•	コンピュータアニメーションやゲームにおけるリアリティのある群衆シミュレーション。

 */
fun main() = application {
    configure {
        width = 800
        height = 800
    }

    oliveProgram {
        val numBoids = 100
        val boids = MutableList(numBoids) {
            Boid(
                position = Vector2.uniform(Vector2(0.0), Vector2(width.toDouble(), height.toDouble())),
                velocity = Vector2.uniform(-1.0, 1.0).normalized * 2.0
            )
        }

        val maxSpeed = 2.0
        val maxForce = 0.05


        val gui = GUI()

        val params = object {
            @BooleanParameter("Alignment")
            var enableAlignment = true

            @BooleanParameter("Cohesion")
            var enableCohesion = true

            @BooleanParameter("Separation")
            var enableSeparation = true
        }

        gui.add(params, "Settings")

        extend(gui)

        extend {
            drawer.clear(ColorRGBa.BLACK)
            drawer.fill = ColorRGBa.WHITE

            for (boid in boids) {
                var alignment = Vector2.ZERO
                var cohesion = Vector2.ZERO
                var separation = Vector2.ZERO

                if (params.enableAlignment) {
                    alignment = align(boid, boids)
                }
                if (params.enableCohesion) {
                    cohesion = cohere(boid, boids)
                }
                if (params.enableSeparation) {
                    separation = separate(boid, boids)
                }

                boid.velocity += alignment + cohesion + separation
                boid.velocity = boid.velocity.normalized * maxSpeed

                boid.position += boid.velocity

                if (boid.position.x < 0) boid.position = Vector2(width.toDouble(), boid.position.y)
                if (boid.position.x > width) boid.position = Vector2(0.0, boid.position.y)
                if (boid.position.y < 0) boid.position = Vector2(boid.position.x, height.toDouble())
                if (boid.position.y > height) boid.position = Vector2(boid.position.x, 0.0)

                val position = boid.position
                val dir = boid.velocity.normalized
                drawer.contour(
                    contour {
                        moveTo(position + dir * 8.0)
                        lineTo(position + dir.rotate(120.0) * 3.0)
                        lineTo(position + dir.rotate(-120.0) * 3.0)
                        close()
                    }
                )
            }
        }
    }
}

fun align(boid: Boid, boids: List<Boid>): Vector2 {
    val perceptionRadius = 50.0
    var steering = Vector2.ZERO
    var total = 0

    for (other in boids) {
        if (other != boid && boid.position.distanceTo(other.position) < perceptionRadius) {
            steering += other.velocity
            total++
        }
    }

    if (total > 0) {
        steering /= total.toDouble()
        steering = steering.normalized * 2.0
        steering -= boid.velocity
        steering = steering.limit(0.05)
    }
    return steering
}

fun cohere(boid: Boid, boids: List<Boid>): Vector2 {
    val perceptionRadius = 50.0
    var steering = Vector2.ZERO
    var total = 0

    for (other in boids) {
        if (other != boid && boid.position.distanceTo(other.position) < perceptionRadius) {
            steering += other.position
            total++
        }
    }

    if (total > 0) {
        steering /= total.toDouble()
        steering -= boid.position
        steering = steering.normalized * 2.0
        steering -= boid.velocity
        steering = steering.limit(0.05)
    }
    return steering
}

fun separate(boid: Boid, boids: List<Boid>): Vector2 {
    val perceptionRadius = 24.0
    var steering = Vector2.ZERO
    var total = 0

    for (other in boids) {
        val distance = boid.position.distanceTo(other.position)
        if (other != boid && distance < perceptionRadius) {
            var diff = boid.position - other.position
            diff /= distance
            steering += diff
            total++
        }
    }

    if (total > 0) {
        steering /= total.toDouble()
        steering = steering.normalized * 2.0
        steering -= boid.velocity
        steering = steering.limit(0.05)
    }
    return steering
}

private fun Vector2.limit(max: Double): Vector2 {
    return if (this.length > max) {
        this.normalized * max
    } else {
        this
    }
}