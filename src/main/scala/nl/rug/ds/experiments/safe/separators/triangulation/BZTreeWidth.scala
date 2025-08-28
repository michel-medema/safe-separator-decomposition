package nl.rug.ds.experiments.safe.separators.triangulation

import java.io.File
import java.nio.file.Path
import scala.language.postfixOps
import scala.sys.process.Process

class BZTreeWidth(override val tempPath: Path) extends ExternalAlgorithm {
  override val name: String = "BZTreeWidth"

  override def run(input: File, output: File): Int = {
    Process( "bztreewidth.sh" ) #< input #> output !
  }
}
