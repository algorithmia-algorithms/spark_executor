package spark_executor.main.models.algorithmia

import play.api.libs.json._

case class AlgoOutput(outcomes: Outcomes, events: EventManager)


object AlgoOutput{
  implicit val ro: Reads[AlgoOutput] = Json.reads[AlgoOutput]
  implicit val wo: Writes[AlgoOutput] = Json.writes[AlgoOutput]
}