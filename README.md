# Safe Separator Decomposition

This repository contains the code that was used for the experiments described in the paper

> M. Medema and A. Lazovik, ‘A safeness condition for minimal separators based on vertex connectivity’, Discrete Mathematics, vol. 348, no. 9, p. 114524, Sep. 2025, doi: 10.1016/j.disc.2025.114524.

Its purpose is to compute a triangulation of a graph, possibly by first decomposing the graph into smaller subgraphs. It offers several decomposition and triangulation algorithms, where the decomposition is mainly based on so-called safe separators.

## Data

The [data](./data) directory contains the graphs that were used for the paper, along with the corresponding result files. The graphs are split into two types: synthetic graphs, which have been generated with the `nl.rug.ds.experiments.safe.separators.main.GraphGenerator`, and the graphs from the PACE Treewidth challenge of 2017. Each synthetic graph is accompanied by an additional file that lists its safe separators. The results directory follows the same separation.

## Usage

The class `nl.rug.ds.experiments.safe.separators.main.Main` is the main entrypoint. It expects four command line arguments:

`decomposition: 0 (no decomposition), 1 (PredefinedSeparators), 2 (HeuristicDecomposition)`\
`triangulation: 1 (BZTreeWidth), 2 (TamakiTreeWidth), 3 (Min-Fill), 4 (Minimum Degree)`\
`graph: the path to the input graph`\
`outputFile: the path to the file in which the results should be stored`

For the decomposition algorithm, `no decomposition` leaves the graph unchanged, `PredefinedSeparators` reads the vertex separators from a file (which can be generated along with synthetic graphs using the class `nl.rug.ds.experiments.safe.separators.main.GraphGenerator`), and `HeuristicDecomposition` uses the Heuristic Decomposition with Community Dectection algorithm described in the above-mentioned paper.

The triangulation algorithms are [BZTreewidth](https://github.com/TomvdZanden/BZTreewidth/tree/master), [TamakiTreeWidth](https://github.com/twalgor/tw), the Minimum Fill-In heuristic, and the Minimum Degree heuristic. Note that the BZTreewidth algorithm relies on an external executable, meaning it will not work as such.

### Docker

For convenience, the [Dockerfile](./Dockerfile) can be used to build a Docker container that provides a runtime environment with all necessary dependencies. The Docker container compiles the source code of this project as well as the source code of the BZTreewidth algorithm, ensuring that this algorithm can be used inside the container. The GitHub workflow automatically builds the container and publishes it to the GitHub container registry of this repository. Alternatively, it can be built locally, or the container can be pulled from the GitHub registry.