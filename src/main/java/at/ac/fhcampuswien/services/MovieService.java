package at.ac.fhcampuswien.services;

import at.ac.fhcampuswien.models.Movie;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class MovieService {
    private final List<Movie> movies;

    // Constructor Injection: Die Liste wird von außen übergeben (wichtig für Tests!)
    public MovieService(List<Movie> movies) {
        this.movies = movies;
    }

    public List<Movie> getAllMovies() {
        return movies;
    }

    // Suche mit Streams (case-insensitive)
    public List<Movie> searchMovies(String title, String genre, Integer releaseYear) {
        return movies.stream()
                .filter(m -> title == null || m.getTitle().toLowerCase().contains(title.toLowerCase()))
                .filter(m -> genre == null || m.getGenre().toLowerCase().contains(genre.toLowerCase()))
                .filter(m -> releaseYear == null || m.getReleaseYear() == releaseYear)
                .collect(Collectors.toList());
    }

    public boolean addMovie(Movie newMovie) {
        // Prüfen auf Duplikate mit anyMatch
        boolean exists = movies.stream().anyMatch(m ->
                m.getTitle().equalsIgnoreCase(newMovie.getTitle()) &&
                        m.getReleaseYear() == newMovie.getReleaseYear());

        if (exists) return false;
        return movies.add(newMovie);
    }

    public boolean deleteMovie(String title, String genre, int year) {
        return movies.removeIf(m ->
                m.getTitle().equalsIgnoreCase(title) &&
                        m.getGenre().equalsIgnoreCase(genre) &&
                        m.getReleaseYear() == year);
    }

    // Update basierend auf ID und neuen Daten
    public boolean updateMovie(String id, String title, String genre, int year) {
        return movies.stream()
                .filter(m -> m.getId().toString().equals(id))
                .findFirst()
                .map(m -> {
                    m.setTitle(title);
                    m.setGenre(genre);
                    m.setReleaseYear(year);
                    return true;
                }).orElse(false);
    }
}