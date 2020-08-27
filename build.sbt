name := "spark_executor"

version := "0.1"

scalaVersion := "2.12.12"


libraryDependencies += "org.apache.spark" %% "spark-core" % "3.0.0"
libraryDependencies += "org.apache.spark" %% "spark-streaming" % "3.0.0"
libraryDependencies += "com.typesafe.play" %% "play-json" % "2.9.0"
libraryDependencies += "com.algorithmia" %% "algorithmia-scala" % "1.0.1"
