package nl.rug.ds.experiments.safe.separators.triangulation

import nl.rug.ds.graph.common.Graph
import nl.rug.ds.graph.format.parser.Pace
import nl.rug.ds.graph.tree.decomposition.format.PaceTD

import java.io.File
import java.nio.file.{Files, Path}
import scala.util.{Failure, Try}

trait ExternalAlgorithm extends TriangulationAlgorithm {
  val tempPath: Path

  protected def run(input: File, output: File): Int

  override def triangulate(g: Graph[Int]): Try[Int] = {
    val inputFile: Path = Files.createTempFile(tempPath, null, ".gr")
    val outputFile: Path = Files.createTempFile(tempPath, null, ".td")

    Pace.write(inputFile.toFile, remap(g))

    val exitCode: Int = run(inputFile.toFile, outputFile.toFile)

    if (exitCode != 0) {
      return Failure(new RuntimeException(s"The process exited unexpectedly with exit code $exitCode."))
    }

    for {
      treeWidth: Int <- PaceTD(outputFile.toFile).map(_.tw)
      _ <- Try(Files.delete(inputFile))
      _ <- Try(Files.delete(outputFile))
    } yield treeWidth
  }
}
