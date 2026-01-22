/*
 * Genetic Conflict Seeker
 *
 * Copyright (c) 2023-2026
 *
 * @author: Viet-Man Le (vietman.le@ist.tugraz.at)
 */

package at.tugraz.ist.ase.conflict.genetic;

import at.tugraz.ist.ase.conflict.genetic.crossover.ICrossOverStrategy;
import at.tugraz.ist.ase.conflict.genetic.mutate.IMutationStrategy;
import at.tugraz.ist.ase.conflict.genetic.resolve.IResolveStrategy;

/**
 * Interface for genetic algorithm implementations.
 * Defines the contract for evolving populations of individuals to identify conflicts.
 *
 * @param <E> the element type (e.g., Assignment)
 * @param <C> the individual type extending {@link Individual}
 * @param <T> the fitness value type (must be Comparable)
 */
public interface IGeneticAlgorithm<E, C extends Individual<E, C>, T extends Comparable<T>> {
    void evolve();
    void evolve(int maxIterations);

    Population<E, C> getPopulation();
    int getCurrentIteration();

    void setMutationStrategy(IMutationStrategy<E, C> mutationStrategy);
    void setCrossOverStrategy(ICrossOverStrategy<E, C> crossOverStrategy);
    void setResolveStrategy(IResolveStrategy<E, C> resolveStrategy);

    void addIterationListener(IterationListener<E, C, T> listener);
    void removeIterationListener(IterationListener<E, C, T> listener);
}
