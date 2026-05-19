package at.ac.fhcampuswien.services;

import at.ac.fhcampuswien.models.Movie;

import java.util.List;
import java.util.stream.Collectors;
import at.ac.fhcampuswien.repositories.MovieRepository;

public class MovieService {

    private final MovieRepository movieRepository;

    // Constructor Injection: Die Liste wird von außen übergeben (wichtig für Tests!)
    public MovieService(MovieRepository movieRepository) {
        this.movieRepository = movieRepository;
    }

    public List<Movie> getAllMovies() {
        return movieRepository.findAll();
    }

    // Suche mit Streams (case-insensitive)
    public List<Movie> searchMovies(String title, String genre, Integer releaseYear) {
        return movieRepository.findAll().stream()
                .filter(m -> title == null || m.getTitle().toLowerCase().contains(title.toLowerCase()))
                .filter(m -> genre == null || m.getGenre().toLowerCase().contains(genre.toLowerCase()))
                .filter(m -> releaseYear == null || m.getReleaseYear() == releaseYear)
                .collect(Collectors.toList());
    }

    public boolean addMovie(Movie newMovie) {
        // Prüfen auf Duplikate mit anyMatch
        boolean exists = movieRepository.findAll().stream().anyMatch(m ->
                m.getTitle().equalsIgnoreCase(newMovie.getTitle()) &&
                        m.getReleaseYear() == newMovie.getReleaseYear());

        if (exists) return false;

        movieRepository.add(newMovie);
        return true;
    }

    public boolean deleteMovie(String title, String genre, int year) {
        List<Movie> movies = movieRepository.findAll();

        return movies.stream()
                .filter(m ->
                    m.getTitle().equalsIgnoreCase(title) &&
                    m.getGenre().equalsIgnoreCase(genre) &&
                    m.getReleaseYear() == year)
                .findFirst()
                .map(movieRepository::delete)
                .orElse(false);
    }

    // Update basierend auf ID und neuen Daten
    public boolean updateMovie(String id, String title, String genre, int year) {
        List<Movie> movies = movieRepository.findAll();

        return movies.stream()
                .filter(m -> m.getId().toString().equals(id))
                .findFirst()
                .map(m -> {
                    m.setTitle(title);
                    m.setGenre(genre);
                    m.setReleaseYear(year);
                    return movieRepository.update(m);
                }).orElse(false);
    }
}