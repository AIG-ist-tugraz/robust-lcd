/*
 * Genetic Conflict Seeker
 *
 * Copyright (c) 2023
 *
 * @author: Viet-Man Le (vietman.le@ist.tugraz.at)
 */

package at.tugraz.ist.ase.conflict.genetic;

/**
 * @param <E> element type
 * @param <C> individual type
 */
public interface Individual<E, C extends Individual<E, C>> extends Cloneable, Iterable<E> {

    int size();

    C clone() throws CloneNotSupportedException;
}
