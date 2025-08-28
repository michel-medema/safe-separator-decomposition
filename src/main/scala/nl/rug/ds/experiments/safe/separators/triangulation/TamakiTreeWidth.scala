package nl.rug.ds.experiments.safe.separators.triangulation

import nl.rug.ds.graph.common.Graph
import nl.rug.ds.graph.tree.decomposition.Tamaki

import scala.language.postfixOps
import scala.util.{Success, Try}

case object TamakiTreeWidth extends TriangulationAlgorithm {
  override val name: String = "TamakiTreeWidth"

  override def triangulate( g: Graph[Int] ): Try[Int] = Success( new Tamaki().treeWidth(g) )
}
