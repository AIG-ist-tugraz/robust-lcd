# GeneticConflictSeeker

A Java tool that identifies conflicts in configuration knowledge bases and feature models using a genetic algorithm. It supports three runnable variants:
- Feature Model variant (FM)
- APM knowledge base variant (APM)
- Camera knowledge base variant (CameraKB)

The project produces runnable fat JARs for each variant via the Maven Assembly Plugin.

## Stack
- Language: Java (JDK 21)
- Build/Package manager: Apache Maven
- Packaging: JAR (fat JARs with dependencies via maven-assembly-plugin)
- Key dependencies:
  - `at.tugraz.ist.ase.hiconfit:configurator:${hiconfit.version}` (installed from the local `lib/` JAR during Maven `validate`)
  - `ch.qos.logback:logback-classic`
  - `org.junit.jupiter:junit-jupiter` (tests)

See `pom.xml` for full details. The build enables Java preview features at compile time (`--enable-preview`). TODO: Confirm whether the runtime requires `--enable-preview` when launching the JARs.

## Requirements
- JDK 21 (set `JAVA_HOME` accordingly)
- Maven 3.9+ (earlier versions may work, but were not verified)
- Internet access to resolve Maven dependencies

## Setup
This project ships a required library JAR in `lib/` which is installed into your local Maven repository during the Maven `validate` phase.

1) Install the provided library (happens automatically on `validate`):
```bash
mvn validate
```
This will install `lib/configurator-1.0.1-alpha-40.jar` into your local Maven repository under the coordinates:
`at.tugraz.ist.ase.hiconfit:configurator:${hiconfit.version}`.

2) Build the runnable JARs:
```bash
mvn clean package
```
Artifacts are created under `target/`:
- `gc_seeker-jar-with-dependencies.jar` (FM)
- `gc_seeker_apm-jar-with-dependencies.jar` (APM)
- `gc_seeker_camera-jar-with-dependencies.jar` (CameraKB)

## Running
Each variant reads options from a configuration file. Use the `-cfg` option to provide a path.

### Feature Model variant (FM)
Designed for feature models (e.g., `.xml`, `.splx`).
```bash
java -jar target/gc_seeker-jar-with-dependencies.jar -cfg ./conf/gc_seeker.cfg
```

### APM variant
Designed for the APM knowledge base.
```bash
java -jar target/gc_seeker_apm-jar-with-dependencies.jar -cfg ./conf/gc_seeker_apm.cfg
```

### CameraKB variant
Designed for the Camera knowledge base.
```bash
java -jar target/gc_seeker_camera-jar-with-dependencies.jar -cfg ./conf/gc_seeker_camera.cfg
```

Notes:
- If Java preview features are required at runtime, add `--enable-preview` after `java` in the above commands. TODO: Verify necessity.
- When no `-cfg` is provided, a default config path from code may be used (see `ConfigManager.defaultConfigFile_GeneticConflictSeeker`).

## Running with Docker
You can build and run the application and the accompanying Python experiment scripts using Docker. The image builds the Java fat JARs with Maven, and the final runtime image only contains JRE 21 and Python 3 with the required packages.

- Build the image:
```bash
docker build -t genetic-conflict-seeker .
```

- Run the Java FM app (default CMD). Mount your local `conf/` and `data/` so outputs are written on the host:
```bash
docker run --rm -it \
  -v "$PWD/conf":/app/conf \
  -v "$PWD/data":/app/data \
  genetic-conflict-seeker
```
This uses the default command in the image: `java -jar /app/target/gc_seeker-jar-with-dependencies.jar -cfg /app/conf/gc_seeker.cfg`.

- Run the APM variant explicitly:
```bash
docker run --rm -it \
  -v "$PWD/conf":/app/conf \
  -v "$PWD/data":/app/data \
  genetic-conflict-seeker \
  java -jar /app/target/gc_seeker_apm-jar-with-dependencies.jar -cfg /app/conf/gc_seeker_apm.cfg
```

- Run a Python evaluation script from `test-scripts/aaai` (example: BusyBox workflow):
```bash
docker run --rm -it \
  -v "$PWD/conf":/app/conf \
  -v "$PWD/data":/app/data \
  -w /app \
  genetic-conflict-seeker \
  python3 test-scripts/aaai/eval_script.py -cfg test-scripts/aaai/conf/busybox_eval_script.toml
```
Outputs from Java (CSV, .da files, logs) and script-generated summaries/figures will appear under the mounted `/app/data` and the respective `test-scripts/**/results/` paths inside the container (which map to your working directory when mounted).

Reproducibility tips:
- Keep your experiment configs in version control (e.g., TOML under `test-scripts/aaai/**/conf/`).
- Always mount `conf/` and `data/` so runs are deterministic and outputs persist.
- For large-scale runs, prefer the TOML-based `aaai/eval_script.py` and then `summary.py`/`viz.py` inside the same image to generate summaries and figures.

## Configuration
Sample configuration files live in `conf/`:
- `gc_seeker.cfg` (FM variant)
- `gc_seeker_apm.cfg` (APM variant)
- `gc_seeker_camera.cfg` (Camera variant)

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

Security note: Configuration files in this repo contain example email credentials. Treat these as placeholders only and replace with your own or leave blank. Credentials are read from the config file; environment-variable support is not implemented. TODO: Consider adding environment variable overrides for secrets.

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
- `pom.xml` — Maven project file (Java 21, assembly plugin builds the three fat JARs)
- `src/main/java/` — source code
  - Main classes (entry points):
    - `at.tugraz.ist.ase.conflict.fm.GeneticConflictForFM`
    - `at.tugraz.ist.ase.conflict.kb.apm.GeneticConflictForAPM`
    - `at.tugraz.ist.ase.conflict.kb.camera.GeneticConflictForCamera`
- `src/test/java/` — tests (JUnit 5)
- `conf/` — sample configuration files
- `data/` — sample datasets, existing conflict sets, results, statistics
- `lib/` — provided dependency JAR(s) installed during `mvn validate`
- `target/` — build outputs (created after packaging)
- `logs.log` — example log output (if present)
- `LICENSE`, `README.md`

## License
This project is licensed under the MIT License — see `LICENSE` for details.

## Changelog
- 2025-11-17: README overhauled with stack, build/run instructions, configuration, tests, and structure.

## TODOs
- Verify whether running with `--enable-preview` is required at runtime.
- Add environment variable support for sensitive config values (e.g., `emailPass`).
- Provide more end-to-end examples for FM input formats and datasets.