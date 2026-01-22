/*
 * Genetic Conflict Seeker
 *
 * Copyright (c) 2023-2026
 *
 * @author: Viet-Man Le (vietman.le@ist.tugraz.at)
 */

package at.tugraz.ist.ase.conflict.genetic;

/**
 * Callback interface for receiving notifications after each iteration of the genetic algorithm.
 * Implementations can use this to log progress, update UI, or perform other actions.
 *
 * @param <E> the element type
 * @param <C> the individual type
 * @param <T> the fitness value type
 */
public interface IterationListener<E, C extends Individual<E, C>, T extends Comparable<T>> {
    void update(IGeneticAlgorithm<E, C, T> environment);
}
