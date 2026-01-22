/*
 * Genetic Conflict Seeker
 *
 * Copyright (c) 2023-2026
 *
 * @author: Viet-Man Le (vietman.le@ist.tugraz.at)
 */

package at.tugraz.ist.ase.conflict.genetic;

/**
 * Interface representing an individual in a genetic algorithm population.
 * An individual contains a collection of elements and can be cloned and iterated.
 *
 * @param <E> the element type (e.g., Assignment for feature selections)
 * @param <C> the concrete individual type (for self-referential cloning)
 */
public interface Individual<E, C extends Individual<E, C>> extends Cloneable, Iterable<E> {

    int size();

    C clone() throws CloneNotSupportedException;
}
