
import org.openrndr.application
import org.openrndr.color.ColorRGBa
import org.openrndr.draw.colorBuffer
import org.openrndr.draw.isolatedWithTarget
import org.openrndr.draw.renderTarget
import org.openrndr.extra.gui.GUI
import org.openrndr.extra.noise.uniform
import org.openrndr.extra.parameters.BooleanParameter
import org.openrndr.extra.parameters.DoubleParameter
import org.openrndr.math.Vector2
import kotlin.math.cos
import kotlin.math.sin
import kotlin.random.Random

data class Agent(var position: Vector2, var velocity: Vector2, var angle: Double)
/*
* Physarumシミュレーションは、粘菌（Physarum polycephalum）の動きを模倣したシミュレーション。
* 粘菌は、食物源を探索し、それに向かって成長する特徴があります。
* 粘菌シミュレーションでは、エージェント（粘菌の胞子）が環境内を動き、食物源や化学物質の濃度を感知して方向を変えながら成長していきます。

主なルール:

	1.	移動（Movement）:
	•	エージェントはランダムな方向に動く。
	•	環境の情報（化学物質の濃度など）に基づいて方向を調整する。
	2.	方向の変更（Direction Change）:
	•	エージェントはセンサーを使って周囲の環境を検知し、化学物質の濃度が高い方向に向かう。
	3.	トレイル（Trail）:
	•	エージェントが通過した経路はトレイルとして残る。
	•	他のエージェントはこのトレイルを感知して、同じ経路をたどることがある。
目的:

	•	自然界における粘菌の食物探索行動を理解する。
	•	ネットワーク形成や最短経路問題の解決などのアルゴリズムに応用する。
	•	バイオインスパイアードコンピューティング（生物模倣計算）の研究。

 */
fun main() = application {
    configure {
        width = 800
        height = 800
    }

    program {
        val numAgents = 1000
        val agents = MutableList(numAgents) {
            Agent(
                position = Vector2.uniform(Vector2(0.0), Vector2(width.toDouble(), height.toDouble())),
                velocity = Vector2(cos(Random.nextDouble(2.0 * Math.PI)), sin(Random.nextDouble(2.0 * Math.PI))),
                angle = Random.nextDouble(2.0 * Math.PI)
            )
        }

        val gui = GUI()

        val params = object {
            @DoubleParameter("Move Speed", 0.1, 10.0)
            var moveSpeed = 1.0

            @DoubleParameter("Turn Speed", 0.1, 5.0)
            var turnSpeed = 0.3

            @BooleanParameter("Show Agents")
            var showAgents = true
        }

        gui.add(params, "Settings")

        extend(gui)

        val trailMap = colorBuffer(width, height)
        val renderTarget = renderTarget(width, height) {
            colorBuffer(trailMap)
        }

        extend {
            drawer.isolatedWithTarget(renderTarget) {
                drawer.fill = ColorRGBa(0.0, 0.0, 0.0, 0.1)
                drawer.stroke = null
                drawer.rectangle(0.0, 0.0, width.toDouble(), height.toDouble())

                if (params.showAgents) {
                    drawer.fill = ColorRGBa.WHITE
                    agents.forEach { agent ->
                        drawer.circle(agent.position, 1.0)
                    }
                }
            }

            agents.forEach { agent ->
                val sensorAngle = agent.angle + Random.nextDouble(-params.turnSpeed, params.turnSpeed)
                val sensorDirection = Vector2(cos(sensorAngle), sin(sensorAngle))
                val sensorPosition = agent.position + sensorDirection * 5.0

                if (sensorPosition.x < 0 || sensorPosition.x >= width || sensorPosition.y < 0 || sensorPosition.y >= height) {
                    agent.angle = Random.nextDouble(2.0 * Math.PI)
                } else {
                    val sensorValue = trailMap.shadow.get(sensorPosition.x.toInt(), sensorPosition.y.toInt()).r
                    agent.angle = sensorAngle + (sensorValue * params.turnSpeed)
                }

                val direction = Vector2(cos(agent.angle), sin(agent.angle))
                agent.velocity = direction * params.moveSpeed
                agent.position += agent.velocity

                if (agent.position.x < 0) agent.position = Vector2(width.toDouble(), agent.position.y)
                if (agent.position.x >= width) agent.position = Vector2(0.0, agent.position.y)
                if (agent.position.y < 0) agent.position = Vector2(agent.position.x, height.toDouble())
                if (agent.position.y >= height) agent.position = Vector2(agent.position.x, 0.0)
            }

            drawer.image(trailMap)
        }
    }
}

fun align(agent: Agent, agents: List<Agent>): Vector2 {
    val perceptionRadius = 50.0
    var steering = Vector2.ZERO
    var total = 0

    for (other in agents) {
        if (other != agent && agent.position.distanceTo(other.position) < perceptionRadius) {
            steering += other.velocity
            total++
        }
    }

    if (total > 0) {
        steering /= total.toDouble()
        steering = steering.normalized * 2.0
        steering -= agent.velocity
        steering = steering.limit(0.05)
    }
    return steering
}

fun cohere(agent: Agent, agents: List<Agent>): Vector2 {
    val perceptionRadius = 50.0
    var steering = Vector2.ZERO
    var total = 0

    for (other in agents) {
        if (other != agent && agent.position.distanceTo(other.position) < perceptionRadius) {
            steering += other.position
            total++
        }
    }

    if (total > 0) {
        steering /= total.toDouble()
        steering -= agent.position
        steering = steering.normalized * 2.0
        steering -= agent.velocity
        steering = steering.limit(0.05)
    }
    return steering
}

fun separate(agent: Agent, agents: List<Agent>): Vector2 {
    val perceptionRadius = 24.0
    var steering = Vector2.ZERO
    var total = 0

    for (other in agents) {
        val distance = agent.position.distanceTo(other.position)
        if (other != agent && distance < perceptionRadius) {
            var diff = agent.position - other.position
            diff /= distance
            steering += diff
            total++
        }
    }

    if (total > 0) {
        steering /= total.toDouble()
        steering = steering.normalized * 2.0
        steering -= agent.velocity
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