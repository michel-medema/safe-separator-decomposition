package nl.rug.ds.experiments.safe.separators.decomposer

import nl.rug.ds.graph.common.Graph
import nl.rug.ds.graph.triangulation.decomposition.HDC

import scala.util.{Success, Try}

case object HeuristicDecomposition extends DecompositionAlgorithm {
  override val name: String = "HeuristicDecomposition"

  override def decompose(g: Graph[Int]): Try[Set[Graph[Int]]] = Success(new HDC().decompose(g)._1)
}
