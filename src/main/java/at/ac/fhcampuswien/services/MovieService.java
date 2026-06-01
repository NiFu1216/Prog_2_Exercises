package at.ac.fhcampuswien.services;

import at.ac.fhcampuswien.exceptions.DatabaseException;
import at.ac.fhcampuswien.exceptions.MovieNotFoundException;
import at.ac.fhcampuswien.models.Movie;
import at.ac.fhcampuswien.models.MovieFactory;
import at.ac.fhcampuswien.repositories.IMovieRepository; // NEU: Interface importieren!

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class MovieService {
    //MovieRepository in IMovieRepository gewchselt
    //Code flexibler, austauschbarer und viel einfacher testbar gemacht
    private final IMovieRepository movieRepository;

    // Constructor Injection: Die Liste wird von außen übergeben
    public MovieService(IMovieRepository movieRepository) {
        this.movieRepository = movieRepository;
    }

    public List<Movie> getAllMovies() throws DatabaseException {
        return movieRepository.findAll();
    }
    //new method old in commend
    public List<Movie> searchMovies(String title, String genre, Integer releaseYear) throws DatabaseException {
        List<MovieFilterStrategy> strategies = new ArrayList<>();

        if (title != null && !title.isBlank()) {
            strategies.add(m -> m.getTitle().toLowerCase().contains(title.toLowerCase()));
        }
        if (genre != null && !genre.isBlank()) {
            strategies.add(m -> m.getGenre().toLowerCase().contains(genre.toLowerCase()));
        }
        if (releaseYear != null) {
            strategies.add(m -> m.getReleaseYear() == releaseYear);
        }

        return movieRepository.findAll().stream()
                .filter(movie -> strategies.stream().allMatch(strategy -> strategy.matches(movie)))
                .collect(Collectors.toList());
    }

    public boolean addMovie(Movie newMovie) throws DatabaseException {
        // Prüfen auf Duplikate mit anyMatch
        boolean exists = movieRepository.findAll().stream().anyMatch(m ->
                m.getTitle().equalsIgnoreCase(newMovie.getTitle()) &&
                        m.getReleaseYear() == newMovie.getReleaseYear());

        if (exists) return false;

        movieRepository.add(newMovie);
        return true;
    }
    // REFACTORED: Altes Stream-Parsing LÖSCHEN. Das Repository wirft jetzt die Fehler selbst!
    public boolean deleteMovie(String title, String genre, int year) throws DatabaseException, MovieNotFoundException {
        // Create the Movie instance through the factory for consistency.
        Movie movieToDelete = MovieFactory.create(title, genre, year);
        return movieRepository.delete(movieToDelete);
    }

    // REFACTORED: Altes Stream-Parsing LÖSCHEN. Übergabe des befüllten Movie-Objekts direkt ans Repo.
    public boolean updateMovie(String id, String title, String genre, int year) throws DatabaseException, MovieNotFoundException {
        // Use factory creation for the updated movie instance.
        Movie updatedMovie = MovieFactory.create(title, genre, year);
        updatedMovie.setID(java.util.UUID.fromString(id));
        return movieRepository.update(updatedMovie);
    }}
    /*public boolean deleteMovie(String title, String genre, int year) {
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


  // Suche mit Streams (case-insensitive)
    public List<Movie> searchMovies(String title, String genre, Integer releaseYear) throws DatabaseException {
        return movieRepository.findAll().stream()
                .filter(m -> title == null || m.getTitle().toLowerCase().contains(title.toLowerCase()))
                .filter(m -> genre == null || m.getGenre().toLowerCase().contains(genre.toLowerCase()))
                .filter(m -> releaseYear == null || m.getReleaseYear() == releaseYear)
                .collect(Collectors.toList());
    }

*/