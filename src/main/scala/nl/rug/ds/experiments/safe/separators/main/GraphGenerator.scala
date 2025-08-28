package nl.rug.ds.experiments.safe.separators.main

import com.typesafe.scalalogging.LazyLogging
import nl.rug.ds.common.{FileHelper, Helper}
import nl.rug.ds.graph.common.{Graph, GraphConnectivity}
import nl.rug.ds.graph.format.parser.{Pace, SeparatorFile}
import nl.rug.ds.graph.generator.SafeSeparatorGraph

import java.nio.file.{Path, Paths}
import scala.util.{Failure, Success}

object GraphGenerator extends LazyLogging with FileHelper {
  // TODO: Make this an argument.
  // The absolute path of the directory in which the graphs should be stored.
  private val outputPath: Path = Paths.get("C:\\Users\\P278366\\Downloads\\")

  private val combinations: List[(Int, Int, Int, Int, Int, Double)] = for (
    numSeparators <- List(1, 5, 10);
    communitiesPerSeparator <- List(2, 3);
    separatorSize <- List(3, 4, 5, 12);
    communitySize <- List(20, 25, 30);
    connectivity <- List( 3, 4, 5 );
    separatorConnectivity <- List( 0.00, 0.33, 0.66, 1.00 )
  ) yield (numSeparators, communitiesPerSeparator, separatorSize, communitySize, connectivity, separatorConnectivity)

  def main(args: Array[String]): Unit = {
    // TODO: Collect failures.
    combinations.zipWithIndex.foreach {
      case ((numSeps, communitiesPerSeparator, sepSize, communitySize, connectivity, separatorConnectivity), i) =>
        logger.info(s"Generating graph ${i + 1} of ${combinations.size}: $numSeps separator(s) of size $sepSize, $separatorConnectivity edges for the separator, $communitiesPerSeparator communities per separator, and $communitySize vertices per community with an average degree of $connectivity.")

        val (graph: Graph[Int], separators: List[Set[Int]]) = new SafeSeparatorGraph(numSeps, sepSize, communitiesPerSeparator, communitySize, connectivity, separatorConnectivity).generate()

        // Remap the vertices to 1..n instead of 0..n-1, as expected by the PACE format.
        val g: Graph[Int] = Graph(graph.V.map(_ + 1), graph.E.map(e => (e._1 + 1) -> (e._2 + 1)))
        val seps: List[Set[Int]] = separators.map(_.map(_ + 1))

        val fileName: String = s"${numSeps}_${communitiesPerSeparator}_${sepSize}_${communitySize}_${connectivity}_$separatorConnectivity.gr"

        Pace.write(outputPath.resolve(fileName).toFile, g) flatMap { _ =>
          SeparatorFile.write(outputPath.resolve(s"$fileName.seps").toFile, seps)
        } match {
          case Success(_) =>
          case Failure(e: Throwable) =>
            logger.error(e.toString)
            e.printStackTrace()
            System.exit(-1)
        }

        seps.foreach { (s: Set[Int]) =>
          val clique: Set[Int] = GraphConnectivity.largestClique(g.subgraph(s))
          val requiredConnectivity: Int = s.size - clique.size
          val largestCliques: Set[Set[Int]] = GraphConnectivity.maximalCliques(g.subgraph(s)).filter(_.size == clique.size)

          val components: Set[Set[Int]] = (g \ s).components()

          // Since the cliques are not always returned in the same order and it is not clear which clique was used to compute the vertex connectivity,
          // the connectivity needs to be correct for at least one of the largest cliques.
          val hasRequiredConnectivity: Boolean = largestCliques.exists { (c: Set[Int]) =>
            components.forall { (component: Set[Int]) =>
              Helper.pairs(s).forall {
                case (u: Int, v: Int) =>
                  val excluded: Set[Int] = (component ++ c) -- Set(u, v)

                  GraphConnectivity.vertexConnectivity(g \ excluded, u, v) >= requiredConnectivity
              }
            }
          }

          if (!hasRequiredConnectivity) {
            logger.error(s"Separator vertices are not ${requiredConnectivity}-connected!")
            System.exit(-1)
          }
        }
    }
  }
}
