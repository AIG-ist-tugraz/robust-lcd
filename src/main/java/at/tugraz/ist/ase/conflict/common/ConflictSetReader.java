/*
 * Genetic Conflict Seeker
 *
 * Copyright (c) 2023-2026
 *
 * @author: Viet-Man Le (vietman.le@ist.tugraz.at)
 */

package at.tugraz.ist.ase.conflict.common;

import at.tugraz.ist.ase.hiconfit.kb.core.Constraint;
import lombok.Cleanup;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Set;

/**
 * Reads conflict sets from a file.
 * Each line in the file represents one conflict set in the format
 * "Feature1=value --- Feature2=value --- ...".
 */
public class ConflictSetReader {
    public static List<Set<Constraint>> read(File file) throws IOException {

        @Cleanup InputStream is = new FileInputStream(file);
        @Cleanup BufferedReader br = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8));

        // Read all conflict sets
        ConflictSetBuilder builder = new ConflictSetBuilder();

        return br.lines().map(builder::build).toList();
    }
}
