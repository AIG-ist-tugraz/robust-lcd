/*
 * Genetic Conflict Seeker
 *
 * Copyright (c) 2023-2026
 *
 * @author: Viet-Man Le (vietman.le@ist.tugraz.at)
 */

package at.tugraz.ist.ase.conflict.common;

import at.tugraz.ist.ase.hiconfit.common.RandomUtils;
import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * Utility methods for generating random combinations and index selections.
 * Used for randomly selecting features during mutation and crossover operations.
 */
@UtilityClass
@Slf4j
public class CombinationUtils {

    /**
     * randomly select #numIndexes combinations from #indexesSize combinations
     * @param numIndexes the number of combinations to be selected
     * @param indexesSize the size of the list of combinations
     * @param sort        true if the selected indexes are sorted
     * @return list of combination indexes
     */
    public static List<Integer> selectIndexes(int numIndexes, int indexesSize, boolean sort) {
        List<Integer> selectedIndexes = new ArrayList<>();

        if (indexesSize > numIndexes) {
            int index;
            for (int i = 0; i < numIndexes; i++) {
                do {
                    index = RandomUtils.getRandomInt(indexesSize); // 0 - indexes.size() - 1
                } while (selectedIndexes.contains(index));
                selectedIndexes.add(index);

                if (sort) {
                    Collections.sort(selectedIndexes);
                }
            }
        } else {
            selectedIndexes = IntStream.range(0, indexesSize).boxed().collect(Collectors.toList());

            if (!sort) {
                Collections.shuffle(selectedIndexes);
            }
        }
        return selectedIndexes;
    }
}
