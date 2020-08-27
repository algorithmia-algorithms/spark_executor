package spark_executor.main.models.algorithmia

import play.api.libs.json._

case class EventManager(ALGO_A: List[Event], ALGO_B: List[Event], HOST: List[Event])

object EventManager {
  implicit val rem: Reads[EventManager] = Json.reads[EventManager]
  implicit val wem: Writes[EventManager] = Json.writes[EventManager]
}
