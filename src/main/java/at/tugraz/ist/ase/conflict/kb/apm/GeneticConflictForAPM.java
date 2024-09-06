/*
 * Genetic Conflict Seeker
 *
 * Copyright (c) 2023
 *
 * @author: Viet-Man Le (vietman.le@ist.tugraz.at)
 */

package at.tugraz.ist.ase.conflict.kb.apm;

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
import at.tugraz.ist.ase.conflict.kb.KBURResolveStrategyWeighted;
import at.tugraz.ist.ase.conflict.kb.KBCDRModelFactory;
import at.tugraz.ist.ase.conflict.kb.KBConflictCrossOverStrategy;
import at.tugraz.ist.ase.conflict.kb.KBConflictCrossOverStrategyWeighted;
import at.tugraz.ist.ase.conflict.kb.KBURMutationStrategy;
import at.tugraz.ist.ase.hiconfit.cacdr_core.Assignment;
import at.tugraz.ist.ase.hiconfit.common.LoggerUtils;
import at.tugraz.ist.ase.hiconfit.common.MailService;
import at.tugraz.ist.ase.hiconfit.fm.parser.FeatureModelParserException;
import at.tugraz.ist.ase.hiconfit.kb.core.Constraint;
import lombok.Cleanup;
import lombok.val;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import static at.tugraz.ist.ase.hiconfit.common.ConstraintUtils.convertToStringWithMessage;

public class GeneticConflictForAPM {
    public static void main(String[] args) throws IOException, FeatureModelParserException {
        val programTitle = "Genetic Conflict Seeker for APM";
        val usage = "Usage: java -jar gc_seeker_apm.jar [options]";

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

        boolean cfInConflicts = cfg.getCfInConflicts().equals("yes");

        ApmKB apmKB = new ApmKB(false);

        // get variables
        val variables = apmKB.getVariableList();

        System.out.println("Number of variables: " + variables.size());
        System.out.println("Number of constraints: " + apmKB.getNumConstraints());

        // init the population
        String message = String.format("\nGENERATION 0: Randomizing a starting population of %d individuals with a no-preference-probability of %f ...", cfg.getPopulationSize(), cfg.getNoPreferenceProbability());
        ConflictUtils.printMessage(resultWriter, message);

        val mutationStrategy = new KBURMutationStrategy(variables, cfg.getNoPreferenceProbability(), cfg.getMutationProbability(), cfg.getMaxFeaturesInUR());
        val population = Populations.newPopulations(cfg.getPopulationSize(), mutationStrategy);

        // print the population
        printPopulation(resultWriter, population);

        // create CDRModelFactory
        val cdrModelFactory = new KBCDRModelFactory(apmKB, null);

        // create the genetic algorithm
        val gci = GeneticConflictIdentifier.builder()
                .population(population)
//                .featureModel(featureModel)
                .modelFactory(cdrModelFactory)
                .cfInConflicts(cfInConflicts)
                .numMaxConflicts(cfg.getMaxNumConflicts())
                .stopAfterXTimesNoConflict(cfg.getStopAfterXTimesNoConflict())
                .allConflictSets(allConflictSets)
                .allConflictSetsWithoutCF(allConflictSetsWithoutCF)
                .build();

        gci.setMutationStrategy(mutationStrategy);

        gci.setExtinctAfterXTimesNoConflict(cfg.getExtinctAfterXTimesNoConflict());
        gci.setStopAfterXExtinctions(cfg.getStopAfterXExtinctions());

        if (cfg.isWeightedConflicts() || cfg.isAvoidSameOriginalConflict()) {
            gci.setResolveStrategy(new KBURResolveStrategyWeighted());
            gci.setCrossOverStrategy(new KBConflictCrossOverStrategyWeighted(
                    variables,
                    cfg.getPopulationSize(),
                    true,
                    cfg.isAvoidSameOriginalConflict(),
                    cfg.isWeightedCrossover(),
                    cfg.getWeightedCrossoverFactor()
            ));
        }
        else {
            gci.setResolveStrategy(new URResolveStrategy());
            gci.setCrossOverStrategy(new KBConflictCrossOverStrategy(variables, cfg.getPopulationSize()));
        }

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

}
