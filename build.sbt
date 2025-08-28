import scala.collection.Seq

ThisBuild / version := "0.1.0-SNAPSHOT"

ThisBuild / scalaVersion := "3.7.2"

lazy val root = (project in file("."))
  .settings(
    name := "safe-separator-decomposition",
    githubTokenSource := TokenSource.Environment("GITHUB_TOKEN"),
    resolvers += Resolver.githubPackages("michel-medema", "scala-graph-tools"),
    libraryDependencies ++= Seq(
      "scala-graph-tools" % "scala-graph-tools_2.13" % "0.1.0-SNAPSHOT" changing(),
      "com.typesafe.scala-logging" %% "scala-logging" % "3.9.5",
      "ch.qos.logback" % "logback-classic" % "1.5.18",
      "com.typesafe.play" %% "play-json" % "2.10.7",
      "org.typelevel" %% "cats-core" % "2.13.0"
    ),
    assembly / assemblyMergeStrategy := {
      case PathList("META-INF", _*) => MergeStrategy.discard
      case "module-info.class" => MergeStrategy.discard
      case x =>
        val oldStrategy = (assembly / assemblyMergeStrategy).value
        oldStrategy(x)
    }
  )
