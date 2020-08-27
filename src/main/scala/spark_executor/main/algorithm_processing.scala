package spark_executor.main

import org.apache.spark.streaming.{Seconds, StreamingContext}
import org.apache.spark.{SparkConf, SparkContext}
import org.apache.spark.streaming.dstream.DStream
import play.api.libs.json._
import spark_executor.main.models.algorithmia.{AlgoInput, AlgoOutput}
import com.algorithmia._
import com.algorithmia.algo.{AlgoFailure, AlgoSuccess}

import scala.collection.mutable.Queue
import org.apache.spark.rdd.RDD

import scala.collection.mutable

object algorithm_processing {

  val ALGO_NAME = "network_anomaly_detection/orchestrator/0.1.0"
  val CLIENT: AlgorithmiaClient = Algorithmia.client()
  val CONF: SparkConf = new SparkConf().setMaster("local[1]").setAppName("NetworkAnomalyDetector")
  val SC = new SparkContext(CONF)
  val SSC = new StreamingContext(SC, Seconds(10))

  def algorithm_request(input: AlgoInput, client: AlgorithmiaClient): AlgoOutput = {
    val result = client.algo(ALGO_NAME).pipe(input)
    result match {
      case AlgoSuccess(result, _, _) => result.as[AlgoOutput]
      case AlgoFailure(message, _, _) => throw new RuntimeException(message)
    }
  }

  def main(args: Array[String]): Unit = {
    val inputData: RDD[String] = SC.textFile("src/main/resources/example_input.txt")
    val inputQueue: mutable.Queue[RDD[String]] = mutable.Queue()
    inputQueue += inputData
    val inputStream = SSC.queueStream(inputQueue, oneAtATime = true, defaultRDD = inputData)
    val algorithmInputs: DStream[AlgoInput] = inputStream
      .map(j => Json.parse(j).as[AlgoInput])
    val algorithmOutputs: DStream[AlgoOutput] = algorithmInputs
      .map(req => algorithm_request(req, CLIENT))
    var i = 0
    algorithmOutputs.map(l => {println(l); l})
        .foreachRDD(rdd => {
          rdd.saveAsTextFile(s"src/main/resources/output/output_file_$i.txt")
          i += 1
        })
    SSC.start()
    SSC.awaitTermination()
    Console.println("finished")
  }
}
