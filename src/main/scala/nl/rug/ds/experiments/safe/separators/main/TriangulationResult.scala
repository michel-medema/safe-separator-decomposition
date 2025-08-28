package nl.rug.ds.experiments.safe.separators.main

final case class TriangulationResult(
  graph: String,
  decompositionAlgorithm: String,
  triangulationAlgorithm: String,
  treeWidth: Int,
  executionTime: String,
  finished: Boolean,
  vertices: Int,
  edges: Int
)
