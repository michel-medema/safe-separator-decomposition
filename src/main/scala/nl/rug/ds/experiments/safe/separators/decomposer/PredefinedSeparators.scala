package nl.rug.ds.experiments.safe.separators.decomposer

import nl.rug.ds.graph.common.Graph
import nl.rug.ds.graph.format.parser.SeparatorFile

import java.nio.file.Path
import scala.util.Try

final case class PredefinedSeparators(graphFile: Path) extends DecompositionAlgorithm {
  override val name: String = "SeparatorDecomposition"

  override def decompose(g: Graph[Int]): Try[Set[Graph[Int]]] = {
    // The separator file is assumed to be in the same directory as the graph file.
    SeparatorFile.read(file = s"${graphFile.toAbsolutePath.toString}.seps") map { (S: List[Separator]) =>
      S.foldLeft(List(g)) {
        case (graphs: List[Graph[Int]], separator: Separator) => graphs.flatMap(g => split(g, separator))
      }.toSet
    }
  }
}
