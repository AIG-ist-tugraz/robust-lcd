/*
 * Genetic Conflict Seeker
 *
 * Copyright (c) 2023-2026
 *
 * @author: Viet-Man Le (vietman.le@ist.tugraz.at)
 */

package at.tugraz.ist.ase.conflict.genetic;

import at.tugraz.ist.ase.conflict.genetic.mutate.IMutationStrategy;
import lombok.experimental.UtilityClass;

/**
 * A factory for creating populations
 */
@UtilityClass
public class Populations {
    public <E, C extends Individual<E, C>>
    Population<E, C> newPopulations(int size, IMutationStrategy<E, C> mutationStrategy) {
        Population<E, C> population = new Population<>();

        if (mutationStrategy == null) {
            return population;
        }

        for (int i = 0; i < size; i++) {
            // create a new individual by mutation
            C individual = (C) mutationStrategy.mutate();
            population.addIndividual(individual);
        }
        return population;
    }
}
