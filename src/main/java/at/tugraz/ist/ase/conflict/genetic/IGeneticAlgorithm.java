/*
 * Genetic Conflict Seeker
 *
 * Copyright (c) 2023
 *
 * @author: Viet-Man Le (vietman.le@ist.tugraz.at)
 */

package at.tugraz.ist.ase.conflict.genetic;

import at.tugraz.ist.ase.conflict.genetic.crossover.ICrossOverStrategy;
import at.tugraz.ist.ase.conflict.genetic.mutate.IMutationStrategy;
import at.tugraz.ist.ase.conflict.genetic.resolve.IResolveStrategy;

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
