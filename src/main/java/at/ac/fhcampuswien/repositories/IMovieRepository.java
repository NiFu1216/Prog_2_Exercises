package at.ac.fhcampuswien.repositories;

import at.ac.fhcampuswien.models.Movie;
import at.ac.fhcampuswien.exceptions.DatabaseException;
import at.ac.fhcampuswien.exceptions.MovieNotFoundException;
import java.util.List;

//Interface entkoppelt Schichten voneinander (Dependency Inversion).
//Jede Klasse, die sich in unserem Projekt um das Speichern von Filmen kümmern möchte, MUSS diese vier Funktionen anbieten
public interface IMovieRepository {
    void add(Movie movie) throws DatabaseException;
    List<Movie> findAll() throws DatabaseException;
    boolean delete(Movie movie) throws DatabaseException, MovieNotFoundException;
    boolean update(Movie movie) throws DatabaseException, MovieNotFoundException;
}