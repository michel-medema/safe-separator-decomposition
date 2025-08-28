package nl.rug.ds.experiments.safe.separators.decomposer

import nl.rug.ds.graph.common.Graph

import scala.util.Try

trait DecompositionAlgorithm {
  type Separator = Set[Int]

  val name: String

  def decompose(g: Graph[Int]): Try[Set[Graph[Int]]]

  protected def split[V](g: Graph[V], separator: Set[V]): List[Graph[V]] = {
    if (separator.subsetOf(g.V)) {
      //(g \ s).components().toList.map( c => g.subgraph( c ++ s ) ++ Graph.clique( s ) )
      g.components(separator).toList.map(c => g.subgraph(c._1 ++ separator) ++ Graph.clique(separator))
    } else {
      List(g)
    }
  }
}
