package nl.rug.ds.experiments.safe.separators.decomposer

import nl.rug.ds.graph.common.Graph

import scala.util.{Success, Try}

case object NoDecomposition extends DecompositionAlgorithm {
  override val name: String = "NoDecomposition"

  override def decompose(g: Graph[Int]): Try[Set[Graph[Int]]] = Success(Set(g))
}
