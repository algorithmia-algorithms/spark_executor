package spark_executor.main.models.algorithmia

import play.api.libs.json._

case class AlgoInput(features: List[List[Float]], device_type: String)


object AlgoInput{
  implicit val ra: Reads[AlgoInput] = Json.reads[AlgoInput]
  implicit val wa: Writes[AlgoInput] = Json.writes[AlgoInput]
}