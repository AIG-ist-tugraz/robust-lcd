/*
 * Genetic Conflict Seeker
 *
 * Copyright (c) 2023-2026
 *
 * @author: Viet-Man Le (vietman.le@ist.tugraz.at)
 */

package at.tugraz.ist.ase.conflict.cli;

import at.tugraz.ist.ase.hiconfit.common.LoggerUtils;
import io.github.cdimascio.dotenv.Dotenv;
import lombok.Getter;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

/**
 * A class to manage the input configuration of the application.
 */
@Getter
@ToString
@Slf4j
public final class ConfigManager {
    public static String defaultConfigFile_GeneticConflictSeeker = "./conf/gc_seeker.cfg";

    private final String fullnameKB;
    private final String nameKB;
    private final String kbPath;

    private final String allConflictsPath;
    private final String allConflictsWithoutCFPath;

    private final int maxNumConflicts;
    private final String cfInConflicts;

    private final String machine;
    private final String emailAfterEachConf;
    private final String emailAddress;
    private final String emailPass;

    // used by GeneticConflictSeeker
    private final String resultPath;
    private final int populationSize;
    private final double noPreferenceProbability;
    private final double mutationProbability;
    private final int maxNumGenerations;
    private final int stopAfterXTimesNoConflict;
    private final int maxFeaturesInUR;
    private final boolean printResult;
    private final String existingCSPath;
    private final String existingCSWithoutCFPath;
    // extinction settings
    private final int extinctAfterXTimesNoConflict;
    private final int stopAfterXExtinctions;
    // weighting settings
    private final boolean weightedConflicts;
    private final boolean avoidSameOriginalConflict;
    private final boolean weightedCrossover;
    private final double weightedCrossoverFactor;
    // legacy settings
    private final boolean limitParentsToResolved; // used to limit the parents to the resolved conflicts (as in Uran's version)

    // used to log statistics
    private final String statisticsPath;
    private final String summaryPath;

    private static ConfigManager instance = null;

    // Dotenv instance for loading .env file and environment variables
    private static final Dotenv dotenv = Dotenv.configure()
            .ignoreIfMissing()  // Don't throw if .env file is missing
            .load();

    public static ConfigManager getInstance(String configFile) {
        if (instance == null) {
            instance = new ConfigManager(configFile);
        }
        return instance;
    }

    /**
     * Gets a config value with environment variable override support.
     * Priority: .env file > system environment variable > config file > default value.
     *
     * @param props the properties from config file
     * @param key the property key
     * @param envVar the environment variable name (or null to skip env lookup)
     * @param defaultValue the default value if neither env nor config is set
     * @return the resolved value
     */
    private static String getPropertyWithEnvOverride(Properties props, String key, String envVar, String defaultValue) {
        if (envVar != null) {
            String envValue = dotenv.get(envVar);
            if (envValue != null && !envValue.isEmpty()) {
                return envValue;
            }
        }
        return props.getProperty(key, defaultValue);
    }

    private ConfigManager(String configFile) {
        Properties appProps = new Properties();
        try {
            appProps.load(new FileInputStream(configFile));
        } catch (IOException e) {
            log.error("{}{}", LoggerUtils.tab(), e.getMessage());
        }

        kbPath = appProps.getProperty("kbPath", "./kb/");

        allConflictsPath = appProps.getProperty("allCSPath", "./results/allConflictSets.da");
        allConflictsWithoutCFPath = appProps.getProperty("allCSWithoutCFPath", "./results/allConflictSetsWithoutCF.da");

        fullnameKB = appProps.getProperty("nameKB", null);

        if (fullnameKB != null) {
            int index = fullnameKB.lastIndexOf(".");
            if (index != -1) {
                nameKB = fullnameKB.substring(0, index);
            } else {
                nameKB = fullnameKB;
            }
        } else {
            nameKB = null;
        }

        maxNumConflicts = Integer.parseInt(appProps.getProperty("maxNumConflicts", "0")); // 0 - no limit
        cfInConflicts = appProps.getProperty("cfInConflicts", "no");

        machine = appProps.getProperty("machine", "may moi");
        emailAfterEachConf = appProps.getProperty("emailAfterEachConf", "no");
        // Sensitive values support environment variable overrides
        emailAddress = getPropertyWithEnvOverride(appProps, "emailAddress", "GC_EMAIL_ADDRESS", "");
        emailPass = getPropertyWithEnvOverride(appProps, "emailPass", "GC_EMAIL_PASS", "");

        resultPath = appProps.getProperty("resultPath", "./results/result.txt");
        populationSize = Integer.parseInt(appProps.getProperty("populationSize", "100"));
        noPreferenceProbability = Double.parseDouble(appProps.getProperty("noPreferenceProbability", "0.7"));
        mutationProbability = Double.parseDouble(appProps.getProperty("mutationProbability", "0.1"));
        maxNumGenerations = Integer.parseInt(appProps.getProperty("maxNumGenerations", "30"));
        stopAfterXTimesNoConflict = Integer.parseInt(appProps.getProperty("stopAfterXTimesNoConflict", "0"));
        maxFeaturesInUR = Integer.parseInt(appProps.getProperty("maxFeaturesInUR", "100"));
        printResult = appProps.getProperty("printResult", "yes").equals("yes");
        existingCSPath = appProps.getProperty("existingCSPath", "");
        existingCSWithoutCFPath = appProps.getProperty("existingCSWithoutCFPath", "");

        extinctAfterXTimesNoConflict = Integer.parseInt(appProps.getProperty("extinctAfterXTimesNoConflict", "0"));
        stopAfterXExtinctions = Integer.parseInt(appProps.getProperty("stopAfterXExtinctions", "0"));

        weightedConflicts = appProps.getProperty("weightedConflicts", "no").equals("yes");
        avoidSameOriginalConflict = appProps.getProperty("avoidSameOriginalConflict", "no").equals("yes");
        weightedCrossover = appProps.getProperty("weightedCrossover", "no").equals("yes");
        weightedCrossoverFactor = Double.parseDouble(appProps.getProperty("weightedCrossoverFactor", "2"));

        limitParentsToResolved = appProps.getProperty("limitParentsToResolved", "no").equals("yes");

        statisticsPath = appProps.getProperty("statisticsPath", "");
        summaryPath = appProps.getProperty("summaryPath", "");

        log.trace("{}<<< Read configurations [fullnameKB={}]", LoggerUtils.tab(), fullnameKB);
    }

    public String getKBFilepath() {
        return kbPath + fullnameKB;
    }
}
