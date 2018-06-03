lazy val akkaHttpVersion = "10.1.1"
lazy val akkaVersion    = "2.5.12"

lazy val root = (project in file(".")).
	settings(
		inThisBuild(List(
			organization    := "ozma",
			scalaVersion    := "2.12.5"
		)),
		name := "akka-http",
		libraryDependencies ++= Seq(
			"com.typesafe.akka" %% "akka-http"            % akkaHttpVersion,
			"com.typesafe.akka" %% "akka-http-spray-json" % akkaHttpVersion,
			"com.typesafe.akka" %% "akka-http-xml"        % akkaHttpVersion,
			"com.typesafe.akka" %% "akka-stream"          % akkaVersion,

			"com.google.code.gson" % "gson" % "2.8.5",
			"org.apache.kafka" % "kafka-clients" % "1.1.0",
			"com.typesafe.scala-logging" %% "scala-logging" % "3.9.0",
      "ch.qos.logback" % "logback-classic" % "1.1.2",

			"com.typesafe.akka" %% "akka-http-testkit"    % akkaHttpVersion % Test,
			"com.typesafe.akka" %% "akka-testkit"         % akkaVersion     % Test,
			"com.typesafe.akka" %% "akka-stream-testkit"  % akkaVersion     % Test,
			"org.scalatest"     %% "scalatest"            % "3.0.1"         % Test
		)
	)
