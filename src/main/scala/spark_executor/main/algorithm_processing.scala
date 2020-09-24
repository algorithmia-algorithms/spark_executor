package spark_executor.main

import org.apache.spark.streaming.{Milliseconds, StreamingContext}
import org.apache.spark.{SparkConf, SparkContext}
import org.apache.spark.streaming.dstream.DStream
import play.api.libs.json._
import spark_executor.main.models.algorithmia.{AlgoInput, AlgoOutput}
import com.algorithmia._
import com.algorithmia.algo.{AlgoFailure, AlgoSuccess}
import org.apache.spark.rdd.RDD
import scala.collection.mutable

object algorithm_processing {

  val ALGO_NAME = "network_anomaly_detection/orchestrator/0.1.0"
  val CLIENT: AlgorithmiaClient = Algorithmia.client()
  val CONF: SparkConf = new SparkConf().setMaster("local[1]").setAppName("NetworkAnomalyDetector")
  val SC = new SparkContext(CONF)
  val SSC = new StreamingContext(SC, Milliseconds(1000))

  def algorithm_request(input: AlgoInput, client: AlgorithmiaClient): AlgoOutput = {
    val result = client.algo(ALGO_NAME).pipe(input)
    result match {
      case AlgoSuccess(result, _, _) => result.as[AlgoOutput]
      case AlgoFailure(message, _, _) => throw new RuntimeException(message)
    }
  }

  def main(args: Array[String]): Unit = {
    // lets get our input file loaded, this "Input" will be replaced with either an HDFS, Kafka, or Kinesis connection in a production environment
    val inputData: RDD[String] = SC.textFile("src/main/resources/example_input.txt").flatMap(s => s.split('\n'))

    // lets convert this RDD into a Stream that wrpe can use for processing
    val inputQueue: mutable.Queue[RDD[String]] = mutable.Queue()
    inputQueue += inputData
    val inputStream: DStream[String] = SSC.queueStream(inputQueue, oneAtATime = true)

    // Now that we have the stream loaded, lets convert it from a list of strings into our actual algorithm input format
    val algorithmInputs: DStream[AlgoInput] = inputStream
      .map(j => Json.parse(j).as[AlgoInput])
    // And lets call our orchestrator algorithm on each input in our DStream
    val algorithmOutputs: DStream[AlgoOutput] = algorithmInputs
      .map(req => algorithm_request(req, CLIENT))
    // Now lets print the results to console as a basic Sink, this could be stored to disk as well.
    // If our stream runs out of data, let's kill the stream and terminate the process.

    algorithmOutputs.foreachRDD(rdd => {
          if (rdd.isEmpty()) {
            Console.println("terminated job")
            SSC.stop(stopSparkContext = true, stopGracefully = false)
          } else {
            rdd.foreach(l => {Console.println(Json.toJson(l))})
          }})
    SSC.start()
    SSC.awaitTermination()
    Console.println("finished")
  }
}
