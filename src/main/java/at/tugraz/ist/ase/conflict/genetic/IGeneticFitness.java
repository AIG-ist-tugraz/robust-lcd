/*
 * Genetic Conflict Seeker
 *
 * Copyright (c) 2023-2026
 *
 * @author: Viet-Man Le (vietman.le@ist.tugraz.at)
 */

package at.tugraz.ist.ase.conflict.genetic;

/**
 * Interface for calculating fitness values of individuals in a genetic algorithm.
 * Lower fitness values indicate better individuals.
 *
 * @param <E> the element type
 * @param <C> the individual type
 * @param <T> the fitness value type (must be Comparable)
 */
public interface IGeneticFitness<E, C extends Individual<E, C>, T extends Comparable<T>>{

    /**
     * Assume that individual1 is better than individual2 <br/>
     * fit1 = calculate(individual1) <br/>
     * fit2 = calculate(individual2) <br/>
     * So the following condition must be true <br/>
     * fit1.compareTo(fit2) <= 0 <br/>
     */
    T calculate(C individual);

}
