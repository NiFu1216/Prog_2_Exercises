package at.ac.fhcampuswien.services;

import at.ac.fhcampuswien.exceptions.DatabaseException;
import at.ac.fhcampuswien.exceptions.MovieNotFoundException;
import at.ac.fhcampuswien.models.Movie;
import at.ac.fhcampuswien.repositories.MovieRepository;

import java.util.List;
import java.util.stream.Collectors;

public class MovieService {

    private final MovieRepository movieRepository;

    public MovieService(MovieRepository movieRepository) {
        this.movieRepository = movieRepository;
    }

    public List<Movie> getAllMovies() throws DatabaseException {
        return movieRepository.findAll();
    }

    // ---------------- SEARCH ----------------
    public List<Movie> searchMovies(String title, String genre, Integer releaseYear)
            throws DatabaseException {

        return movieRepository.findAll().stream()
                .filter(m -> title == null ||
                        m.getTitle().toLowerCase().contains(title.toLowerCase()))
                .filter(m -> genre == null ||
                        m.getGenre().toLowerCase().contains(genre.toLowerCase()))
                .filter(m -> releaseYear == null ||
                        m.getReleaseYear() == releaseYear)
                .collect(Collectors.toList());
    }

    // ---------------- ADD ----------------
    public boolean addMovie(Movie newMovie) throws DatabaseException {

        boolean exists = movieRepository.findAll().stream()
                .anyMatch(m ->
                        m.getTitle().equalsIgnoreCase(newMovie.getTitle()) &&
                                m.getReleaseYear() == newMovie.getReleaseYear());

        if (exists) return false;

        movieRepository.add(newMovie);
        return true;
    }

    // ---------------- DELETE ----------------
    public boolean deleteMovie(String title, String genre, int year)
            throws DatabaseException, MovieNotFoundException {

        List<Movie> movies = movieRepository.findAll();

        Movie movieToDelete = movies.stream()
                .filter(m ->
                        m.getTitle().equalsIgnoreCase(title) &&
                                m.getGenre().equalsIgnoreCase(genre) &&
                                m.getReleaseYear() == year)
                .findFirst()
                .orElseThrow(() ->
                        new MovieNotFoundException("Movie not found for deletion"));

        return movieRepository.delete(movieToDelete);
    }

    // ---------------- UPDATE ----------------
    public boolean updateMovie(String id, String title, String genre, int year)
            throws DatabaseException, MovieNotFoundException {

        List<Movie> movies = movieRepository.findAll();

        Movie movieToUpdate = movies.stream()
                .filter(m -> m.getId().toString().equals(id))
                .findFirst()
                .orElseThrow(() ->
                        new MovieNotFoundException("Movie not found for update"));

        movieToUpdate.setTitle(title);
        movieToUpdate.setGenre(genre);
        movieToUpdate.setReleaseYear(year);

        return movieRepository.update(movieToUpdate);
    }
}