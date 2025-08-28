package nl.rug.ds.experiments.safe.separators.triangulation

import nl.rug.ds.graph.common.Graph
import nl.rug.ds.graph.triangulation.Triangulation

import scala.util.{Success, Try}

case object MinDegree extends TriangulationAlgorithm {
  override val name: String = "MinDegree"

  override def triangulate(g: Graph[Int]): Try[Int] = Success(Triangulation.minDegree(g).cliqueNumber - 1)
}
