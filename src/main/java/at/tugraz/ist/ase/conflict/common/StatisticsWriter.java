package at.tugraz.ist.ase.conflict.common;


import lombok.Setter;

import java.io.*;
import java.time.Duration;
import java.time.Instant;

public class StatisticsWriter {

    private final String[] header = new String[]{"population", "generation", "total_cs", "generated_cs", "new_cs"};

    @Setter
    private String summaryPath = "";
    private int totalConflicts = 0;
    private final BufferedWriter logWriter;
    
    private int populationCount = 0, generationCount = 0;

    private Instant start, finish;

    public StatisticsWriter(String path) throws IOException {
        try {
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
        
        populationCount = populationID;
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
            FileWriter writer = new FileWriter(summaryPath, true);
            writer.write(line);
            writer.write("\r\n");
            writer.flush();
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
            //throw e
        }
    }

    public void close() {
        finish = Instant.now();
        if (!summaryPath.equals("")) writeSummary();

        try {
            if (logWriter != null) {
                logWriter.flush();
                logWriter.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
