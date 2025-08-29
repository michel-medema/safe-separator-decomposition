package nl.rug.ds.experiments.safe.separators.triangulation

import nl.rug.ds.graph.common.Graph

import scala.util.Try


trait TriangulationAlgorithm {
  val name: String

  protected def remap(g: Graph[Int]): Graph[Int] = {
    val M: Map[Int, Int] = g.V.toList.sorted.zip(Range.inclusive(1, g.n)).toMap

    Graph(g.V.map(v => M(v)), g.E.map(e => M(e._1) -> M(e._2)))
  }

  def triangulate( g: Graph[Int] ): Try[Int]
}
