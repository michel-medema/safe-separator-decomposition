package nl.rug.ds.experiments.safe.separators.triangulation

import nl.rug.ds.graph.common.Graph
import nl.rug.ds.graph.triangulation.Triangulation

import scala.util.{Success, Try}

case object MinFill extends TriangulationAlgorithm {
  override val name: String = "MinFill"

  override def triangulate(g: Graph[Int]): Try[Int] = Success( Triangulation.minFill(g).cliqueNumber - 1 )
}
