package at.ac.fhcampuswien.services;

import at.ac.fhcampuswien.models.Movie;
//Blaupause für das Strategy Design Pattern (Verhaltensmuster) und setzt das Open-Closed Principle (das „O“ in SOLID) um. Es dient als flexibler Regel-Tester für deine Filmsuche.
//Behavioral Pattern: Strategy. Kapselt Such-Kriterien separat ab (Open-Closed Principle).
public interface MovieFilterStrategy {
    boolean matches(Movie movie);
}