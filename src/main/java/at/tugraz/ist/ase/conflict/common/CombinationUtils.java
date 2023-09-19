/*
 * Genetic Conflict Seeker
 *
 * Copyright (c) 2023
 *
 * @author: Viet-Man Le (vietman.le@ist.tugraz.at)
 */

package at.tugraz.ist.ase.conflict.common;

import at.tugraz.ist.ase.hiconfit.common.RandomUtils;
import at.tugraz.ist.ase.hiconfit.fm.core.AbstractRelationship;
import at.tugraz.ist.ase.hiconfit.fm.core.CTConstraint;
import at.tugraz.ist.ase.hiconfit.fm.core.Feature;
import at.tugraz.ist.ase.hiconfit.kb.core.Variable;
import at.tugraz.ist.ase.hiconfit.kb.fm.FMKB;
import com.google.common.collect.Lists;
import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@UtilityClass
@Slf4j
public class CombinationUtils {
    public static Integer[] createIndexesArray(int numVar) {
        return IntStream.range(0, numVar).mapToObj(i -> i + 1).toArray(Integer[]::new);
    }

//    public static void convertCSVtoValueComb(String file, List<String> combsList) {
//        BufferedReader reader;
//        try {
//            // Open file
//            reader = new BufferedReader(new FileReader(file));
//
//            String line = reader.readLine(); // read the first line, ignore it
//
//            // Read all combs
//            while ((line = reader.readLine()) != null) {
//                String comb = "";
//                String[] tokens = line.split(",");
//
//                int indexVariable = 0;
//                for (String token : tokens) {
//                    indexVariable++;
//                    boolean boolValue = Boolean.parseBoolean(token);
//
//                    if (comb.isEmpty()) {
//                        comb = indexVariable + "=" + boolValue;
//                    } else {
//                        comb = comb + "," + indexVariable + "=" + boolValue;
//                    }
//                }
//
//                combsList.add(comb);
//            }
//
//            // Close file
//            reader.close();
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//    }
//
//    // enumerate through all files in old_combs folder
//    public static int readAllCombFilesToStrList(String path, List<String> combsList) {
//        File folder = new File(path);
//        int fileCounter = 0;
//        if (folder.exists()) { // exists
//            for (final File file : Objects.requireNonNull(folder.listFiles())) {
//                if (file.getName().contains(".da")) {
//                    fileCounter++;
//                    System.out.println("Reading " + file.getName());
//                    readCombFileToStrList(file, combsList);
//                }
//            }
//        }
//        return fileCounter;
//    }
//

//    /**
//     * Removed
//     */
//    public static int readAllCombFilesCombList(String path, List<Combination> combsList) {
//        File folder = new File(path);
//        int fileCounter = 0;
//        if (folder.exists()) { // exists
//            for (final File file : Objects.requireNonNull(folder.listFiles())) {
//                if (file.getName().contains(".da")) {
//                    fileCounter++;
//                    System.out.println("Reading " + file.getName());
//                    readCombFileToCombList(file, combsList);
//                }
//            }
//        }
//        return fileCounter;
//    }

//
//    // Read combs_#_#.da
//    private static void readCombFileToStrList(File file, List<String> combsList) {
//        BufferedReader reader;
//
//        try {
//            // Open file
//            reader = new BufferedReader(new FileReader(file));
//
//            String line = reader.readLine(); // read the first line, ignore it
//
//            // Read all combinations
//            while ((line = reader.readLine()) != null) {
//                String[] tokens = line.split(" - ");
//
//                combsList.add(tokens[1]);
//            }
//
//            // Close file
//            reader.close();
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//    }
//

//    /**
//     * Removed
//     */
//    private static void readCombFileToCombList(File file, List<Combination> combsList) {
//        BufferedReader reader;
//
//        try {
//            // Open file
//            reader = new BufferedReader(new FileReader(file));
//
//            String line = reader.readLine(); // read the first line, ignore it
//
//            // Read all conflict sets
//            while ((line = reader.readLine()) != null) {
//                Combination comb = new Combination(file, line);
//
//                if (!comb.isConsistent() && comb.getCardCD() > 1) {
//                    combsList.add(comb);
//                }
//            }
//
//            // Close file
//            reader.close();
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//    }




//    public static List<String> generatePCValueCombinations(PCKB pckb,
//                                                           int card,
//                                                           Set<Integer> comb,
//                                                           boolean reduce) {
//        int[][] domains = new int[card][];
//        int[] domainSizes = new int[card];
//
//        // get the vars, domains
//        int j = 0;
//        for (Integer t : comb) {
//            int indexVar = t - 1; // indexVar in comb is (real variable index + 1)
//            domains[j] = pckb.getDomains()[indexVar];
//            domainSizes[j] = pckb.getDomainSizes()[indexVar];
//            j = j + 1;
//        }
//
//        // variable-value assignment
//        List<String> value_combs = new LinkedList<>();
//        value_combs.add("");
//        List<Integer> original_index = Lists.newArrayList(comb);
//        Collections.shuffle(original_index);
//
//        for (j = 0; j < card; j++) {
//            value_combs = generateValueCombinations(value_combs,
//                    domains, domainSizes, original_index, j, reduce);
////                    System.out.println(combs);
//        }
//        return value_combs;
//    }

    public static List<String> generateFMValueCombinations(int card,
                                                           Set<Integer> comb,
                                                           FMKB<Feature, AbstractRelationship<Feature>, CTConstraint> fmkb,
                                                           boolean reduce) {
//        boolean[][] domains = new boolean[card][];
//        int[] domainSizes = new int[card];
//
//        // get the vars, domains
//        int j = 0;
//        for (Integer t : comb) {
////            int indexVar = t; // // ignore f0
//            domains[j] = new boolean[2];
//            domains[j][0] = false;
//            domains[j][1] = true;
//            domainSizes[j] = 2;
//            j = j + 1;
//        }

        // variable-value assignment
        List<String> value_combs = new LinkedList<>();
        value_combs.add("");
        List<Integer> original_index = Lists.newArrayList(comb);
        Collections.shuffle(original_index);

        for (int j = 0; j < card; j++) {
            Variable var = fmkb.getVariable(original_index.get(j));
            value_combs = generateValueCombinations(value_combs, var, reduce);
//            value_combs = generateValueCombinations(value_combs,
//                    domains, domainSizes, original_index, j, var, reduce);
//                    System.out.println(combs);
        }
        return value_combs;
    }

    public static List<String> generateFMValueCombinationsV3(int card,
                                                             Set<Integer> comb,
                                                             FMKB<Feature, AbstractRelationship<Feature>, CTConstraint> fmkb,
                                                             List<Feature> leafFeatures,
                                                             boolean reduce) {
        // variable-value assignment
        List<String> value_combs = new LinkedList<>();
        value_combs.add("");
        List<Integer> original_index = Lists.newArrayList(comb);
        Collections.shuffle(original_index);

        for (int j = 0; j < card; j++) {
//            Variable var = fmkb.getVariable(original_index.get(j));
            Variable var = fmkb.getVariable(leafFeatures.get(original_index.get(j)).getName());
            value_combs = generateValueCombinations(value_combs, var, reduce);
//            value_combs = generateValueCombinations(value_combs,
//                    domains, domainSizes, original_index, j, var, reduce);
//                    System.out.println(combs);
        }
        return value_combs;
    }
//
//    private static List<String> generateValueCombinations(List<String> combs,
//                                                         int[][] domains,
//                                                         int[] domainSizes,
//                                                         List<Integer> original_index,
//                                                         int index,
//                                                         boolean reduce) {
//        List<String> newCombs = new LinkedList<>();
//
//        for (String comb : combs) {
//            for (int i = 0; i < domainSizes[index]; i++) {
//                String n_comb;
//                if (comb.isEmpty()) {
//                    n_comb = original_index.get(index) + "=" + domains[index][i];
//                } else {
//                    n_comb = comb + "," + original_index.get(index) + "=" + domains[index][i];
//                }
//
//                newCombs.add(n_comb);
//            }
//
//            if (reduce) {
//                Random rand = new Random();
//                while (newCombs.size() > 50000) {
//                    int i = rand.nextInt(newCombs.size()); //getRandom(0, newCombs.size() - 1);
//                    newCombs.remove(i);
//                }
//            }
//        }
//
//        return newCombs;
//    }

    private static List<String> generateValueCombinations(List<String> combs,
//                                                          boolean[][] domains,
//                                                          int[] domainSizes,
//                                                          List<Integer> original_index,
//                                                          int index,
                                                          Variable var,
                                                          boolean reduce) {
        List<String> newCombs = new LinkedList<>();


        for (String comb : combs) {
            for (String value : var.getDomain().getValues()) {
//            for (int i = 0; i < var.getDomain().size(); i++) {
                String n_comb;

                if (comb.isEmpty()) {
                    n_comb = var.getName() + "=" + value;
//                    n_comb = original_index.get(index) + "=" + domains[index][i];
                } else {
                    n_comb = comb + "," + var.getName() + "=" + value;
//                    n_comb = comb + "," + original_index.get(index) + "=" + domains[index][i];
                }

                newCombs.add(n_comb);
            }

            if (reduce) {
//                Random rand = new Random();
                while (newCombs.size() > 50000) {
                    int i = RandomUtils.getRandomInt(newCombs.size()); //getRandom(0, newCombs.size() - 1);
                    newCombs.remove(i);
                }
            }
        }

        return newCombs;
    }

////    public static List<Set<Integer>> selectCombinations(Set<Set<Integer>> combinations, int max_items) {
////        List<Set<Integer>> selected_combinations = new LinkedList<>();
////        Random rand = new Random();
////
////        int size = combinations.size();
////
////        for (int i = 0; i < max_items; i++) {
////
////            Set<Integer> combination;
////
////            do {
////                int index = rand.nextInt(size); //getRandom(0, size - 1);
////
////                combination = IteratorUtils.get(combinations.iterator(), index);
////
////            } while (existsInSelectedCombinations(combination, selected_combinations));
////
////            selected_combinations.add(combination);
////        }
////        return selected_combinations;
////    }

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

////    private static boolean existsInSelectedCombinations(Set<Integer> combination, List<Set<Integer>> selected_combinations) {
////        for (Set<Integer> selected_combination : selected_combinations) {
////            if (selected_combination.hashCode() == combination.hashCode())
////                return true;
////        }
////        return false;
////    }

//    public static boolean containsCombination(int indexComb, List<Combination> combsList) {
//        for (Combination comb : combsList) {
//            if (comb.getIndexComb() == indexComb) {
//                return true;
//            }
//        }
//        return false;
//    }
//
//    public static boolean containsValueCombination(String value_combination, List<Combination> combsList) {
//        for (Combination comb : combsList) {
//            if (comb.getCombination().equals(value_combination)) {
//                return true;
//            }
//        }
//        return false;
//    }
//
//    // FUNCTIONS FOR CombinationSelector.java
//    public static List<Integer> selectCombination(Map<Integer, List<Integer>> histogram, int cardCS, int numSelectedCombs) {
//        List<Integer> selectedList = new LinkedList<>();
//        List<Integer> idList = histogram.get(cardCS);
//
//        int numComb = Math.min(idList.size(), numSelectedCombs);
//
//        List<Integer> selectedIndex = selectIndexes(numComb, idList.size());
//        for (Integer index : selectedIndex) {
//            selectedList.add(idList.get(index));
//        }
//
//        return selectedList;
//    }
//
//    public static void saveSelectedCombination(String filename, List<Combination> combsList, List<Integer> selectedList, int numSelectedCombs) throws IOException {
//        BufferedWriter writer = new BufferedWriter(new FileWriter(filename));
//
//        int cc = 0;
//        double running = 0;
//        for (Integer index: selectedList) {
//            Combination comb = combsList.get(index);
//
//            cc += comb.getNumCC();
//            running += comb.getRunningtime();
//        }
//
//        writer.write( ((double)cc / numSelectedCombs) + " " + (running / numSelectedCombs) + "\n");
//        for (Integer index: selectedList) {
//            Combination comb = combsList.get(index);
//
//            writer.write(comb.toString() + "\n");
//        }
//
//        writer.close();
//    }
}
