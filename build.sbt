ThisBuild / version := "0.1.0-SNAPSHOT"

ThisBuild / scalaVersion := "2.13.10"

lazy val http4sVersion     = "1.0.0-M36"
lazy val circeVersion      = "0.14.3"
lazy val prometheusVersion = "0.16.0"

lazy val root = (project in file("."))
  .settings(
    name := "kpitracker",
    libraryDependencies ++= Seq(
      "org.http4s"    %% "http4s-dsl"              % http4sVersion,
      "org.http4s"    %% "http4s-blaze-server"     % http4sVersion,
      "org.http4s"    %% "http4s-blaze-client"     % http4sVersion,
      "org.http4s"    %% "http4s-circe"            % http4sVersion,
      "io.prometheus"  % "simpleclient"            % prometheusVersion,
      "io.prometheus"  % "simpleclient_httpserver" % prometheusVersion,
      "io.circe"      %% "circe-core"              % circeVersion,
      "io.circe"      %% "circe-generic"           % circeVersion,
      "io.circe"      %% "circe-parser"            % circeVersion,
      "ch.qos.logback" % "logback-classic"         % "1.4.3"
    )
  )
