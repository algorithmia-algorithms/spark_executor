package spark_executor.main.models.algorithmia

import play.api.libs.json.{Json, Reads, Writes}

case class Outcomes(ALGO_A: Float, ALGO_B: Float)

object Outcomes{
  implicit val rou: Reads[Outcomes] = Json.reads[Outcomes]
  implicit val wou: Writes[Outcomes] = Json.writes[Outcomes]
}