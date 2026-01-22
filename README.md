# GeneticConflictSeeker

A Java tool that identifies conflicts in configuration knowledge bases and feature models using a genetic algorithm.

The project produces a runnable fat JAR via the Maven Assembly Plugin.

## Stack
- Language: Java (JDK 21)
- Build/Package manager: Apache Maven
- Packaging: JAR (fat JAR with dependencies via maven-assembly-plugin)
- Key dependencies:
  - `at.tugraz.ist.ase.hiconfit:configurator` (installed from local `lib/` JAR during Maven `validate`)
  - `io.github.cdimascio:dotenv-java` (environment variable / `.env` file support)
  - `ch.qos.logback:logback-classic` (logging)
  - `org.junit.jupiter:junit-jupiter` (tests)

See `pom.xml` for full details.

## Requirements
- JDK 21 (set `JAVA_HOME` accordingly)
- Maven 3.9+ (earlier versions may work, but were not verified)
- Internet access to resolve Maven dependencies

## Setup
This project ships a required library JAR in `lib/` which is installed into your local Maven repository during the Maven `validate` phase. The JAR is from the [HiConfiT](https://hiconfit.github.io) project ([source code](https://github.com/HiConfiT/hiconfit-core)).

1. Install the provided library (happens automatically on `validate`):
```bash
mvn validate
```
This will install `lib/configurator-1.0.1-alpha-40.jar` into your local Maven repository under the coordinates:
`at.tugraz.ist.ase.hiconfit:configurator:${hiconfit.version}`.

2. Build the runnable JAR:
```bash
mvn clean package
```
Artifact is created under `target/`:
- `gc_seeker-jar-with-dependencies.jar`

## Running

The application reads options from a configuration file. Use the `-cfg` option to provide a path.

```bash
java -jar target/gc_seeker-jar-with-dependencies.jar -cfg ./conf/gc_seeker.cfg
```

Notes:
- If Java preview features are required at runtime, add `--enable-preview` after `java` in the above commands.
- When no `-cfg` is provided, a default config path from code may be used (see `ConfigManager.defaultConfigFile_GeneticConflictSeeker`).

## Running with Docker

This project provides two Dockerfiles for different use cases:

| Dockerfile | Purpose | Image Size |
|------------|---------|------------|
| `Dockerfile` | Build JAR files only | ~800MB (Maven+JDK) |
| `Dockerfile.eval` | Run evaluations (JAR + Python) | ~600MB (JRE+Python) |

### For Developers: Build JAR Files

Use `Dockerfile` to compile and extract JAR files without installing Java/Maven locally.

```bash
# Build the image
docker build -t gc-seeker-build .

# Extract JAR files to your local target/ directory
docker run --rm -v "$PWD/target":/output gc-seeker-build
```

Output:
```
JAR files copied to /output:
- gc_seeker-jar-with-dependencies.jar
```

### For Researchers: Run Evaluations

Use `Dockerfile.eval` to run experiments with the full environment (Java + Python).

```bash
# Build the image
docker build -f Dockerfile.eval -t gc-seeker-eval .
```

**Run the JAR directly:**
```bash
docker run --rm \
  -v "$PWD/conf":/app/conf \
  -v "$PWD/data":/app/data \
  gc-seeker-eval \
  java -jar /app/target/gc_seeker-jar-with-dependencies.jar -cfg /app/conf/gc_seeker.cfg
```

**Run Python evaluation scripts:**
```bash
docker run --rm \
  -v "$PWD/data":/app/data \
  -v "$PWD/test-scripts":/app/test-scripts \
  gc-seeker-eval \
  python3 test-scripts/aaai/eval_script.py -cfg test-scripts/aaai/conf/busybox_eval_script.toml
```

**Generate summary and visualizations:**
```bash
# Summary statistics
docker run --rm \
  -v "$PWD/test-scripts":/app/test-scripts \
  gc-seeker-eval \
  python3 test-scripts/aaai/summary.py -cfg test-scripts/aaai/conf/summary_conf.toml

# Visualizations
docker run --rm \
  -v "$PWD/test-scripts":/app/test-scripts \
  gc-seeker-eval \
  python3 test-scripts/aaai/viz.py -cfg test-scripts/aaai/conf/viz_conf.toml
```

### Reproducibility Tips

- Mount `data/` and `test-scripts/` so outputs persist on your host machine
- Keep experiment configs (TOML files) in version control
- Use the same Docker image version across all runs for consistency
- Results appear in `test-scripts/**/results/` directories

## Configuration
Sample configuration file: `conf/gc_seeker.cfg`

Common/observed keys (see code for authoritative list):
- Paths and input
  - `nameKB` and `kbPath` (FM): combined to locate the feature model file.
  - `resultPath`: file to write log/status messages during search.
  - `allCSPath`: output file collecting identified conflict sets.
  - `allCSWithoutCFPath`: output file collecting conflict sets without CF constraints.
  - `existingCSPath`: optional file with known conflicts to seed the run.
  - `existingCSWithoutCFPath`: optional file with known conflicts excluding CF constraints.
  - `statisticsPath`, `summaryPath` (FM/APM): CSV outputs for statistics and summary.
- Algorithm parameters
  - `maxNumConflicts`, `cfInConflicts`
  - `populationSize`, `maxNumGenerations`
  - `noPreferenceProbability`, `mutationProbability`, `maxFeaturesInUR`
  - `stopAfterXTimesNoConflict`
  - Legacy/advanced: `limitParentsToResolved`, `extinctAfterXTimesNoConflict`, `stopAfterXExtinctions`, `weightedConflicts`, `weightedCrossover`, `weightedCrossoverFactor`, `avoidSameOriginalConflict`
- Notifications (optional)
  - `emailAfterEachConf` (`yes`/`no`), `emailAddress`, `emailPass`

### Environment Variable Overrides

Sensitive configuration values can be set via environment variables or a `.env` file instead of the config file:

| Config Key | Environment Variable |
|------------|---------------------|
| `emailAddress` | `GC_EMAIL_ADDRESS` |
| `emailPass` | `GC_EMAIL_PASS` |

**Option 1: Using a `.env` file (recommended)**

Copy the example file and fill in your values:
```bash
cp .env.example .env
# Edit .env with your values
```

Then run normally:
```bash
java -jar target/gc_seeker-jar-with-dependencies.jar -cfg ./conf/gc_seeker.cfg
```

**Option 2: Using system environment variables**
```bash
export GC_EMAIL_PASS="your-secret-password"
java -jar target/gc_seeker-jar-with-dependencies.jar -cfg ./conf/gc_seeker.cfg
```

Priority order: `.env` file > system environment variable > config file > default value.

Security note: Add `.env` to `.gitignore` to avoid committing sensitive values.

## Scripts and examples
- See `test-scripts/README.md` for an overview of all experiment scripts, how to run them, and the directory layout.
- Detailed AAAI experiment workflow: `test-scripts/aaai/README.md`
- Example TOML config/scripts and sample results are under `test-scripts/` (e.g., `test-scripts/aaai/...`). These are for experiments/evaluations and not wired into Maven.
- Example data inputs/outputs are under `data/`.

## Tests
This project uses JUnit 5.
- Run all tests:
```bash
mvn test
```
- Example test: `src/test/java/at/tugraz/ist/ase/conflict/genetic/GeneticConflictIdentifierTest.java`

## Project structure
- `pom.xml` — Maven project file (Java 21, assembly plugin builds the fat JAR)
- `src/main/java/` — source code
  - Main class: `at.tugraz.ist.ase.conflict.fm.GeneticConflictForFM`
- `src/test/java/` — tests (JUnit 5)
- `conf/` — sample configuration files
- `data/` — sample datasets and results
- `lib/` — provided dependency JAR(s) installed during `mvn validate`
- `test-scripts/` — Python evaluation scripts (see `test-scripts/README.md`)
- `target/` — build outputs (created after packaging)
- `Dockerfile` — Docker build for JAR compilation
- `Dockerfile.eval` — Docker build for running evaluations
- `.env.example` — template for environment variables

## License
This project is licensed under the MIT License — see `LICENSE` for details.

## Changelog
- 2026-01-22: Added Docker support (two Dockerfiles), `.env` file support for sensitive config values, updated documentation.
- 2025-11-17: README overhauled with stack, build/run instructions, configuration, tests, and structure.