/*
 * Genetic Conflict Seeker
 *
 * Copyright (c) 2023-2026
 *
 * @author: Viet-Man Le (vietman.le@ist.tugraz.at)
 */

package at.tugraz.ist.ase.conflict.genetic;

import at.tugraz.ist.ase.hiconfit.common.RandomUtils;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Random;

import static com.google.common.base.Preconditions.checkElementIndex;

/**
 * A population of the genetic algorithm
 * @param <E> element type
 * @param <C> individual type
 */
public class Population<E, C extends Individual<E, C>> implements Iterable<C> {

    private static final int DEFAULT_NUMBER_OF_INDIVIDUALS = 32;
    private List<C> individuals = new ArrayList<>(DEFAULT_NUMBER_OF_INDIVIDUALS);
    private final Random random = new Random(RandomUtils.getSEED());

    public void addIndividual(C individual) {
        this.individuals.add(individual);
    }

    public int size() {
        return this.individuals.size();
    }

    public C getRandomIndividual() {
        return this.individuals.get(random.nextInt(size()));
    }

    public C getIndividualByIndex(int index) {
        checkElementIndex(index, this.individuals.size(),"Individual index out of bound!");

        return this.individuals.get(index);
    }

//    public void sortPopulationByFitness(Comparator<C> individualsComparator) {
//        Collections.shuffle(this.individuals);
//        this.individuals.sort(individualsComparator);
//    }

    /**
     * shortening population till specific number
     */
    public void trim(int len) {
        this.individuals = this.individuals.subList(0, len);
    }

    @Override
    public Iterator<C> iterator() {
        return this.individuals.iterator();
    }
}
