/*
 * Genetic Conflict Seeker
 *
 * Copyright (c) 2023-2026
 *
 * @author: Viet-Man Le (vietman.le@ist.tugraz.at)
 */

package at.tugraz.ist.ase.conflict.common;

import lombok.Setter;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.time.Duration;
import java.time.Instant;

/**
 * Writes statistics and summary data for genetic algorithm runs.
 * <p>
 * Records per-generation statistics (population ID, generation ID, conflicts found)
 * to a CSV file, and optionally writes a summary with totals and runtime.
 */
public class StatisticsWriter {

    private final String[] header = new String[]{"population", "generation", "total_cs", "generated_cs", "new_cs"};
    private final String[] summaryHeaders = new String[]{"total_populations", "total_gens", "found_cs", "runtime[ms]"};

    @Setter
    private String summaryPath = "";
    private int totalConflicts = 0;
    private final BufferedWriter logWriter;
    
    private int populationCount = 0, generationCount = 0;

    private final Instant start;
    private Instant finish;

    public StatisticsWriter(String path) throws IOException {
        try {
            // check if summary path directory tree exists or create directory tree
            File parentFile = new File(path).getParentFile();
            if (parentFile != null && !parentFile.exists()) {
                parentFile.mkdirs();
            }

            Files.deleteIfExists(Paths.get(path));

            start = Instant.now();
            logWriter = new BufferedWriter(new FileWriter(path));
            logWriter.write(String.join(",", header));
            logWriter.newLine();
            logWriter.flush();
        } catch (IOException e) {
            e.printStackTrace();
            throw e;
        }
    }

    public void write(int populationID,int generationID, int generatedConflicts, int newConflicts, int totalConflicts) {
        assert (this.totalConflicts + newConflicts) == totalConflicts;
        this.totalConflicts = totalConflicts;
        
        populationCount = populationID + 1;
        generationCount += 1;

        String line = String.join(",", new String[]{
                String.valueOf(populationID),
                String.valueOf(generationID),
                String.valueOf(totalConflicts),
                String.valueOf(generatedConflicts),
                String.valueOf(newConflicts)
        });

        try {
            logWriter.write(line);
            logWriter.newLine();
        } catch (IOException e) {
            e.printStackTrace();
            //throw e;
        }
    }

    private void writeSummary() {

        String line = String.join(",", new String[]{
                String.valueOf(populationCount),
                String.valueOf(generationCount),
                String.valueOf(totalConflicts),
                String.valueOf(Duration.between(start, finish).toMillis())
        });

        try {
            Path summaryFilePath = Paths.get(summaryPath);
            if (!Files.exists(summaryFilePath)) {
                Files.createFile(summaryFilePath);
                try (BufferedWriter writer = Files.newBufferedWriter(summaryFilePath, StandardOpenOption.APPEND)) {
                    writer.write(String.join(",", summaryHeaders));
                    writer.newLine();
                }
            }

            try (BufferedWriter writer = Files.newBufferedWriter(summaryFilePath, StandardOpenOption.APPEND)) {
                writer.write(line);
                writer.newLine();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void close() {
        finish = Instant.now();
        if (!summaryPath.isEmpty()) writeSummary();

        try {
            if (logWriter != null) {
                logWriter.flush();
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                if (logWriter != null) {
                    logWriter.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
