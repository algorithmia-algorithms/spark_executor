package spark_executor.main.models.algorithmia

import play.api.libs.json._

case class Event(message: String, timestamp: String)

object Event{
  implicit val re: Reads[Event] = Json.reads[Event]
  implicit val we: Writes[Event] = Json.writes[Event]
}
