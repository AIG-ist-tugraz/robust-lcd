# Feature Model Testing Scripts

This directory contains scripts and resources used for testing and evaluating the GeneticConflictSeeker tool. The scripts automate the process of running evaluations with different configurations, processing results, and generating visualizations.

## Overview

The test scripts are organized as follows:

- **eval_script.py**: Main script for running evaluations with different configurations
- **summary.py**: Processes evaluation results and generates summary statistics
- **viz.py**: Creates visualizations from evaluation results
- **csv2latex.py**: Converts CSV files to LaTeX format for publication

Each knowledge base (KB) has its own directory with configuration files and results:
- arcade
- b2c
- busybox
- ea
- fqa
- ide

## Requirements

The scripts in this directory require:

- Python 3.10+
- Java 21+

And the following Python packages:

- pandas >= 1.0.0
- numpy >= 1.0.0
- matplotlib >= 3.0.0
- tomli >= 2.0.0
- pydantic >= 1.0.0

You can install all required dependencies using pip:

```bash
pip install -r requirements.txt
```

## Usage

### Running Evaluations

To run evaluations, use the `eval_script.py` script with a configuration file:

```bash
python eval_script.py -cfg path/to/config.toml
```

Example configuration files are provided in the `conf` directory:
- arcade_eval_script.toml
- b2c_eval_script.toml
- busybox_eval_script.toml
- ea_eval_script.toml
- fqa_eval_script.toml
- ide_eval_script.toml

### Generating Summaries

To generate summaries from evaluation results, use the `summary.py` script:

```bash
python summary.py -cfg path/to/summary_config.toml
```

### Creating Visualizations

To create visualizations from evaluation results, use the `viz.py` script:

```bash
python viz.py -cfg path/to/viz_config.toml
```

### Converting CSV to LaTeX

To convert CSV files to LaTeX format, use the `csv2latex.py` script:

```bash
python csv2latex.py -cfg path/to/csv2latex_config.toml
```

## Configuration Files

### Evaluation Configuration

The evaluation configuration files (e.g., `arcade_eval_script.toml`) specify:

```toml
[runner]
gc_seeker       = "path/to/gc_seeker.jar"  # Path to the GeneticConflictSeeker JAR
working_dir     = "test-scripts/ecai"      # Working directory
kb_name         = "arcade"                 # Knowledge base name
conf_dirname    = "conf"                   # Configuration directory
result_dirname  = "results"                # Results directory
runs_per_config = 5                        # Number of runs per configuration
verbose         = false                    # Verbose output

[runner.confs]
include = [                                # Configuration files to include
    'original.toml',
    'fullPop.toml',
    # ...
]
```

### GeneticConflictSeeker Configuration

The GeneticConflictSeeker configuration files (e.g., `fullPop.toml`) specify:

```toml
include = "common.toml"                    # Include common configuration

enabled_features = ["base"]                # Enabled features

[base.cs]
maxNumConflicts        = 10                # Maximum number of conflicts
limitParentsToResolved = false             # Limit parents to resolved
```

Common configuration (`common.toml`) includes:

```toml
[base.kb]
nameKB = "arcade-game.splx"                # Knowledge base name
kbPath = "./data/kb/"                      # Knowledge base path

[base.output]
printResult = false                        # Print result

[base.ga]
noPreferenceProbability = 0.7              # No preference probability
mutationProbability     = 0.1              # Mutation probability
maxFeaturesInUR         = 100              # Maximum features in UR

[base.population]
populationSize    = 100                    # Population size
maxNumGenerations = 100                    # Maximum number of generations

[base.cs]
maxNumConflicts        = 1                 # Maximum number of conflicts
limitParentsToResolved = true              # Limit parents to resolved

[extinction]
extinctAfterXTimesNoConflict = 5           # Extinct after X times no conflict
stopAfterXExtinctions        = 5           # Stop after X extinctions

[weighting]
weightedConflicts         = false          # Weighted conflicts
avoidSameOriginalConflict = false          # Avoid same original conflict
weightedCrossover         = false          # Weighted crossover
weightedCrossoverFactor   = 2              # Weighted crossover factor
```

## Output Files

The evaluation scripts generate several types of output files:

1. **Summary Files**: CSV files with summary statistics for each configuration
2. **Statistics Files**: CSV files with detailed statistics for each run
3. **Result Files**: Text files with the results of each run
4. **Conflict Set Files**: Files containing the identified conflict sets

## Workflow

The typical workflow for using these scripts is:

1. Configure the evaluation parameters in the configuration files
2. Run evaluations using `eval_script.py`
3. Generate summaries using `summary.py`
4. Create visualizations using `viz.py`
5. Convert results to LaTeX format using `csv2latex.py` (if needed for publication)
