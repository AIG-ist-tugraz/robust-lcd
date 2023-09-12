/*
 * Genetic Conflict Seeker
 *
 * Copyright (c) 2023
 *
 * @author: Viet-Man Le (vietman.le@ist.tugraz.at)
 */

package at.tugraz.ist.ase.genetic;

public interface IterationListener<E, C extends Individual<E, C>, T extends Comparable<T>> {
    void update(IGeneticAlgorithm<E, C, T> environment);
}
