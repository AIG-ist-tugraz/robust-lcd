package at.tugraz.ist.ase.conflict.common;


import lombok.Setter;

import java.io.*;
import java.time.Duration;
import java.time.Instant;

public class StatisticsWriter {

    private final String[] header = new String[]{"generation", "total_cs", "generated_cs", "new_cs"};

    @Setter
    private String summaryPath = "";
    private int generationCounter = 0;
    private int totalConflicts = 0;
    private final BufferedWriter logWriter;

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

    public void write(int generatedConflicts, int newConflicts) {
        totalConflicts += newConflicts;

        String line = String.join(",", new String[]{
                String.valueOf(generationCounter++),
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

    public void write(int generatedConflicts, int newConflicts, int totalConflicts) {
        assert (this.totalConflicts + newConflicts) == totalConflicts;
        this.totalConflicts = totalConflicts;

        String line = String.join(",", new String[]{
                String.valueOf(generationCounter++),
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
                String.valueOf(generationCounter),
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
