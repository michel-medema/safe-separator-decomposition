package nl.rug.ds.experiments.safe.separators.main

import cats.implicits.*
import com.typesafe.scalalogging.LazyLogging
import nl.rug.ds.common.{FileHelper, Timer}
import nl.rug.ds.experiments.safe.separators.decomposer.{DecompositionAlgorithm, HeuristicDecomposition, NoDecomposition, PredefinedSeparators}
import nl.rug.ds.experiments.safe.separators.triangulation.*
import nl.rug.ds.graph.common.Graph
import nl.rug.ds.graph.format.parser.Pace
import play.api.libs.json.{Json, OWrites}

import java.nio.file.{Path, Paths}
import scala.language.postfixOps
import scala.util.{Failure, Success}


object Main extends LazyLogging with FileHelper {
  private def printUsage(): Unit = {
    println("Usage: decomposition triangulation graph outputFile")
    println("\tdecomposition: 0 (no decomposition), 1 (PredefinedSeparators), 2 (HeuristicDecomposition).")
    println("\ttriangulation: 1 (BZTreeWidth), 2 (TamakiTreeWidth), 3 (Min-Fill), 4 (Minimum Degree).")
    System.exit(2)
  }

  private def parseDecompositionAlgorithm(algorithm: Option[Int], graph: Path): Option[DecompositionAlgorithm] = {
    algorithm.collect {
      case 0 => NoDecomposition
      case 1 => PredefinedSeparators(graph)
      case 2 => HeuristicDecomposition
    }
  }

  private def parseTriangulationAlgorithm(algorithm: Option[Int], tempPath: Path): Option[TriangulationAlgorithm] = {
    algorithm.collect {
      case 1 => new BZTreeWidth(tempPath)
      case 2 => TamakiTreeWidth
      case 3 => MinFill
      case 4 => MinDegree
    }
  }

  def main(args: Array[String]): Unit = {
    if (args.length != 4) {
      printUsage()
    }

    println(args.toList)

    val graphFile: Path = Paths.get(args(2))
    val outputFile: Path = Paths.get(args(3))

    // This temporary directory is used by BZTreewidth to store the input and output file, since
    // the graph cannot be passed to this algorithm directly (it is an external application). Defaults
    // to the current working directory.
    val tempPath: Path = Paths.get(sys.env.getOrElse("TMPDIR", "./"))

    implicit val resultsWrites: OWrites[TriangulationResult] = Json.writes[TriangulationResult]

    parseDecompositionAlgorithm(args(0).toIntOption, graphFile) match {
      case Some(decompositionAlgorithm: DecompositionAlgorithm) =>
        parseTriangulationAlgorithm(args(1).toIntOption, tempPath) match {
          case Some(triangulationAlgorithm: TriangulationAlgorithm) =>
            val timer: Timer = Timer.start()

            Pace.read(graphFile.toFile).flatMap { (g: Graph[Int]) =>
              // The default result values.
              val result: TriangulationResult = TriangulationResult(
                getFileName(graphFile),
                decompositionAlgorithm.name,
                triangulationAlgorithm.name,
                treeWidth = 0,
                executionTime = "0ms",
                finished = false,
                vertices = g.n,
                edges = g.m
              )
              // Save the default values to disk. This ensures that an output file exists even if
              // there is an external program that enforces a certain time limit and aborts the triangulation
              // process if it takes too long.
              writeToFile(outputFile.toFile, Json.toJson(result).toString())

              // Process the graph and overwrite the result values.
              for {
                graphs: Set[Graph[Int]] <- decompositionAlgorithm.decompose(g)
                treeWidth: Int <- graphs.toList.traverse(triangulationAlgorithm.triangulate).map(_.max)
                _ <- writeToFile(outputFile.toFile, Json.toJson(result.copy(executionTime = s"${timer.elapsedMillis()}ms", finished = true, treeWidth = treeWidth)).toString)
              } yield treeWidth
            } match {
              case Success(tw: Int) =>
                logger.debug(s"The treewidth of the graph in the file $graphFile is $tw.")

              case Failure(e: Throwable) =>
                logger.error(e.getMessage)
                System.exit(1)
            }
          case None =>
            logger.error("Incorrect triangulation algorithm specified.")
            printUsage()
        }
      case None =>
        logger.error("Incorrect decomposition algorithm specified.")
        printUsage()
    }
  }
}
