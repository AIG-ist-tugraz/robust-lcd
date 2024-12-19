/*
 * Genetic Conflict Seeker
 *
 * Copyright (c) 2023
 *
 * @author: Viet-Man Le (vietman.le@ist.tugraz.at)
 */

package at.tugraz.ist.ase.conflict.fm;

import at.tugraz.ist.ase.conflict.cli.CmdLineOptions;
import at.tugraz.ist.ase.conflict.cli.ConfigManager;
import at.tugraz.ist.ase.conflict.common.ConflictSetReader;
import at.tugraz.ist.ase.conflict.common.ConflictUtils;
import at.tugraz.ist.ase.conflict.common.StatisticsWriter;
import at.tugraz.ist.ase.conflict.genetic.GeneticConflictIdentifier;
import at.tugraz.ist.ase.conflict.genetic.Population;
import at.tugraz.ist.ase.conflict.genetic.Populations;
import at.tugraz.ist.ase.conflict.genetic.UserRequirement;
import at.tugraz.ist.ase.conflict.genetic.resolve.URResolveStrategy;
import at.tugraz.ist.ase.conflict.kb.KBConflictCrossOverStrategy;
import at.tugraz.ist.ase.conflict.kb.KBConflictCrossOverStrategyWeighted;
import at.tugraz.ist.ase.conflict.kb.KBURResolveStrategyWeighted;
import at.tugraz.ist.ase.hiconfit.cacdr_core.Assignment;
import at.tugraz.ist.ase.hiconfit.cdrmodel.fm.FMModelWithRequirement;
import at.tugraz.ist.ase.hiconfit.common.LoggerUtils;
import at.tugraz.ist.ase.hiconfit.common.MailService;
import at.tugraz.ist.ase.hiconfit.fm.core.AbstractRelationship;
import at.tugraz.ist.ase.hiconfit.fm.core.CTConstraint;
import at.tugraz.ist.ase.hiconfit.fm.core.Feature;
import at.tugraz.ist.ase.hiconfit.fm.core.FeatureModel;
import at.tugraz.ist.ase.hiconfit.fm.parser.FMParserFactory;
import at.tugraz.ist.ase.hiconfit.fm.parser.FeatureModelParserException;
import at.tugraz.ist.ase.hiconfit.kb.core.Constraint;
import lombok.Cleanup;
import lombok.extern.slf4j.Slf4j;
import lombok.val;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import static at.tugraz.ist.ase.hiconfit.common.ConstraintUtils.convertToStringWithMessage;

/**
 * Identify conflicts using genetic algorithm
 * Configurations:
 * - nameKB: name of the knowledge base
 * - kbPath: path to the knowledge base
 * - resultPath: path to the result file - store messages during the identification process
 * - allCSPath: path to the file that stores identified conflicts
 * - allCSWithoutCFPath: path to the file that stores identified conflicts without CF constraints
 * - existingCSPath: path to the file that stores known conflicts
 * - existingCSWithoutCFPath: path to the file that stores known conflicts without CF constraints
 * - maxNumConflicts: maximum number of conflicts to be identified for each individual
 * - cfInConflicts: whether CF constraints are included in conflicts
 * - machine: name of the machine that runs the identification process
 * - emailAfterEachConf: whether to send an email after finishing a conflict identification
 * - emailAddress: email address to send an email
 * - emailPass: password of the email address
 * - populationSize: size of the population
 * - noPreferenceProbability: probability of a feature to be a no-preference feature
 * - mutationProbability: probability of a feature to be mutated
 * - maxNumGenerations: maximum number of generations
 * - stopAfterXTimesNoConflict: stop the identification process after X times of no new conflict
 * - maxFeaturesInUR: maximum number of features in a user requirement
 * - printResult: whether to print the result
 */
@Slf4j
public class GeneticConflictForFM {

    public static void main(String[] args) throws IOException, FeatureModelParserException {
        val programTitle = "Genetic Conflict Seeker";
        val usage = "Usage: java -jar gc_seeker.jar [options]";

        // Parse command line arguments
        val cmdLineOptions = new CmdLineOptions(null, programTitle, null, usage);
        cmdLineOptions.parseArgument(args);

        if (cmdLineOptions.isHelp()) {
            cmdLineOptions.printUsage();
            System.exit(0);
        }

        cmdLineOptions.printWelcome();

        // Read configurations
        val confFile = cmdLineOptions.getConfFile() == null ? ConfigManager.defaultConfigFile_GeneticConflictSeeker : cmdLineOptions.getConfFile();
        val cfg = ConfigManager.getInstance(confFile);

        printConf(cfg);
        MailService mailService = null;
        if (cfg.getEmailAfterEachConf().equals("yes") && !cfg.getEmailAddress().isBlank() && !cfg.getEmailPass().isBlank()) {
            mailService = new MailService(cfg.getEmailAddress(), cfg.getEmailPass());
        }

        List<Set<Constraint>> allConflictSets = new ArrayList<>();
        List<Set<Constraint>> allConflictSetsWithoutCF = new ArrayList<>();

        BufferedWriter resultWriter;
        if (cfg.isPrintResult()) {
            resultWriter = new BufferedWriter(new FileWriter(cfg.getResultPath()));
        } else {
            resultWriter = null;
        }
        @Cleanup val allConflictSetsWriter = new BufferedWriter(new FileWriter(cfg.getAllConflictsPath()));
        @Cleanup val allConflictSetsWithoutCFWriter = new BufferedWriter(new FileWriter(cfg.getAllConflictsWithoutCFPath()));
        LoggerUtils.setUseThreadInfo(false);

        if (!cfg.getExistingCSPath().isBlank()) {
            allConflictSets.addAll(ConflictSetReader.read(new File(cfg.getExistingCSPath())));
        }
        if (!cfg.getExistingCSWithoutCFPath().isBlank()) {
            allConflictSetsWithoutCF.addAll(ConflictSetReader.read(new File(cfg.getExistingCSWithoutCFPath())));
        }

        // loads feature model
        // TODO: how to read a feature model from a file
        val file = new File(cfg.getKBFilepath());
        @Cleanup("dispose") val parser = FMParserFactory.getInstance().getParser(file.getName());
        val featureModel = parser.parse(file);

        boolean cfInConflicts = cfg.getCfInConflicts().equals("yes");

        val model = new FMModelWithRequirement<>(featureModel, null, false, true, cfInConflicts, false);
        model.initialize();
        // TODO: how to read a feature model from a file

        // TODO: take into account all features in the feature model
        // get leaf features
        val leafFeatures = getLeafFeatures(featureModel);

        System.out.println("Number of variables: " + model.getKB().getNumVariables());
        System.out.println("Number of leaf features: " + featureModel.getNumOfLeaf());
        System.out.println("Number of leaf features 2: " + leafFeatures.size());
        System.out.println("Number of constraints: " + model.getKB().getNumConstraints());

        // init the population
        String message = String.format("\nGENERATION 0: Randomizing a starting population of %d individuals with a no-preference-probability of %f ...", cfg.getPopulationSize(), cfg.getNoPreferenceProbability());
        ConflictUtils.printMessage(resultWriter, message);

        val mutationStrategy = new FMURMutationStrategy(leafFeatures, cfg.getNoPreferenceProbability(), cfg.getMutationProbability(), cfg.getMaxFeaturesInUR());
        val population = Populations.newPopulations(cfg.getPopulationSize(), mutationStrategy);

        // print the population
        printPopulation(resultWriter, population);

        // create a CDRModelFactory
        val cdrModelFactory = new FMCDRModelFactory(featureModel, null, cfInConflicts);

        // create the genetic algorithm
        val gci = GeneticConflictIdentifier.builder()
                .population(population)
                .modelFactory(cdrModelFactory)
                .cfInConflicts(cfInConflicts)
                .numMaxConflicts(cfg.getMaxNumConflicts())
                .stopAfterXTimesNoConflict(cfg.getStopAfterXTimesNoConflict())
                .allConflictSets(allConflictSets)
                .allConflictSetsWithoutCF(allConflictSetsWithoutCF)
                .build();

        gci.setMutationStrategy(mutationStrategy);

        //--------------------------------------------------------
        // legacy settings
        gci.setLimitParentsToResolved(cfg.isLimitParentsToResolved());
        // extinction settings
        gci.setExtinctAfterXTimesNoConflict(cfg.getExtinctAfterXTimesNoConflict());
        gci.setStopAfterXExtinctions(cfg.getStopAfterXExtinctions());

        // weighted resolver and crossover
        if (cfg.isWeightedConflicts() || cfg.isAvoidSameOriginalConflict()) {
            gci.setResolveStrategy(new KBURResolveStrategyWeighted());
            gci.setCrossOverStrategy(new FMConflictCrossOverStrategyWeighted(
                    leafFeatures,
                    cfg.getPopulationSize(),
                    true,
                    cfg.isAvoidSameOriginalConflict(),
                    cfg.isWeightedCrossover(),
                    cfg.getWeightedCrossoverFactor()
            ));
        }
        else {
            gci.setResolveStrategy(new URResolveStrategy());
            gci.setCrossOverStrategy(new FMConflictCrossOverStrategy(leafFeatures));
        }

        //--------------------------------------------------------
        gci.setResolveStrategy(new URResolveStrategy());
        gci.setCrossOverStrategy(new FMConflictCrossOverStrategy(leafFeatures));

        //--------------------------------------------------------

        gci.setResultWriter(resultWriter);
        gci.setAllConflictSetsWriter(allConflictSetsWriter);
        gci.setAllConflictSetsWithoutCFWriter(allConflictSetsWithoutCFWriter);
        // TODO: add already known conflicts to gci
        gci.addIterationListener(gci1 -> {
            try {
                printPopulation(resultWriter, gci1.getPopulation());
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });

        // Add statistics writer
        if (!cfg.getStatisticsPath().isEmpty()){
            StatisticsWriter sw = new StatisticsWriter(cfg.getStatisticsPath());
            sw.setSummaryPath(cfg.getSummaryPath());
            gci.setStatisticsWriter(sw);
        }

        // start the genetic algorithm
        gci.evolve(cfg.getMaxNumGenerations());

        // print the results
        printResults(allConflictSets, allConflictSetsWithoutCF, resultWriter);

        System.out.println("DONE");

        if (resultWriter != null) {
            resultWriter.close();
        }

        if (mailService != null) {
            mailService.sendMail(cfg.getEmailAddress(), cfg.getEmailAddress(), "DONE qc_seeker - " + cfg.getMachine(), "Conflict detection finished");
        }
    }

    private static void printResults(List<Set<Constraint>> allConflictSets, List<Set<Constraint>> allConflictSetsWithoutCF, BufferedWriter resultWriter) {
        String message;
        message = "=========================================";
        ConflictUtils.printMessage(resultWriter, message);

        message = String.format("%sConflict sets:", LoggerUtils.tab());
        ConflictUtils.printMessage(resultWriter, message);

        message = String.format("%s%s", LoggerUtils.tab(), convertToStringWithMessage(allConflictSets, "Conflict", "\t", ", ", true));
        ConflictUtils.printMessage(resultWriter, message);

        message = String.format("%sConflict sets without CF:", LoggerUtils.tab());
        ConflictUtils.printMessage(resultWriter, message);

        message = String.format("%s%s", LoggerUtils.tab(), convertToStringWithMessage(allConflictSetsWithoutCF, "Conflict", "\t", ", ", true));
        ConflictUtils.printMessage(resultWriter, message);

        message = "=========================================";
        ConflictUtils.printMessage(resultWriter, message);
    }

    private static void printPopulation(BufferedWriter resultWriter, Population<Assignment, UserRequirement> population) throws IOException {
        String message;
        LoggerUtils.indent();
        int index = 0;
        for (UserRequirement userRequirement : population) {
            message = String.format("%s%s. %s", LoggerUtils.tab(), ++index, userRequirement);

            ConflictUtils.printMessage(resultWriter, message);
        }
        LoggerUtils.outdent();
    }

    private static void printConf(ConfigManager config) {
        System.out.println("Configurations:");
        System.out.println("\tnameKB: " + config.getNameKB());
        System.out.println("\tkbPath: " + config.getKbPath());
        System.out.println("\tresultPath: " + config.getResultPath());
        System.out.println("\tallCSPath: " + config.getAllConflictsPath());
        System.out.println("\tmaxNumConflicts: " + config.getMaxNumConflicts());
        System.out.println("\tcfInConflicts: " + config.getCfInConflicts());
        System.out.println("\temailAfterEachConf: " + config.getEmailAfterEachConf());
        System.out.println("\tpopulationSize: " + config.getPopulationSize());
        System.out.println("\tnoPreferenceProbability: " + config.getNoPreferenceProbability());
        System.out.println("\tmutationProbability: " + config.getMutationProbability());
        System.out.println("\tmaxNumGenerations: " + config.getMaxNumGenerations());
    }

    private static List<Feature> getLeafFeatures(FeatureModel<Feature, AbstractRelationship<Feature>, CTConstraint> fm) {
        List<Feature> leafFeatures = new ArrayList<>();
        for (Feature feature : fm.getBfFeatures()) {
            if (feature.isLeaf()) {
                leafFeatures.add(feature);
            }
        }
        return leafFeatures;
    }
}
