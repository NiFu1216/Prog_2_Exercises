package at.ac.fhcampuswien.services;

import at.ac.fhcampuswien.exceptions.DatabaseException;
import at.ac.fhcampuswien.exceptions.MovieNotFoundException;
import at.ac.fhcampuswien.models.Movie;
import at.ac.fhcampuswien.repositories.MovieRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class MovieServiceTest {

    private MovieService movieService;
    private MovieRepository movieRepository;
    private List<Movie> movies;

    private Movie matrix;
    private Movie inception;
    private Movie grownUps;
    private Movie avatar;

    @BeforeEach
    void setUp() {
        movies = new ArrayList<>();

        matrix = new Movie("The Matrix", "Sci-Fi", 1999);
        inception = new Movie("Inception", "Sci-Fi", 2010);
        grownUps = new Movie("Grown Ups", "Comedy", 2010);
        avatar = new Movie("Avatar", "Action", 2009);

        movies.add(matrix);
        movies.add(inception);
        movies.add(grownUps);
        movies.add(avatar);

        movieRepository = mock(MovieRepository.class);

        // ---- BASIC READ ----
        when(movieRepository.findAll()).thenAnswer(invocation -> movies);

        // ---- ADD ----
        doAnswer(invocation -> {
            Movie m = invocation.getArgument(0);
            movies.add(m);
            return null;
        }).when(movieRepository).add(any(Movie.class));

        // ---- DELETE ----
        when(movieRepository.delete(any(Movie.class))).thenAnswer(invocation -> {
            Movie m = invocation.getArgument(0);
            return movies.removeIf(movie ->
                    movie.getTitle().equalsIgnoreCase(m.getTitle()) &&
                            movie.getGenre().equalsIgnoreCase(m.getGenre()) &&
                            movie.getReleaseYear() == m.getReleaseYear()
            );
        });

        // ---- UPDATE ----
        when(movieRepository.update(any(Movie.class))).thenReturn(true);

        movieService = new MovieService(movieRepository);
    }

    // -------------------------
    // getAllMovies()
    // -------------------------

    @Test
    @DisplayName("Given movies exist When getAllMovies is called Then all movies are returned")
    void givenMoviesExist_whenGetAllMovies_thenReturnAllMovies() {
        List<Movie> result = movieService.getAllMovies();

        assertEquals(4, result.size());
    }

    @Test
    @DisplayName("Given repository returns empty list When getAllMovies is called Then empty list is returned")
    void givenNoMoviesExist_whenGetAllMovies_thenReturnNoMovies() {
        when(movieRepository.findAll()).thenReturn(new ArrayList<>());

        List<Movie> result = movieService.getAllMovies();

        assertTrue(result.isEmpty());
    }

    // -------------------------
    // searchMovies()
    // -------------------------

    @Test
    void givenExactTitle_whenSearchMovies_thenReturnMatchingMovie() {
        List<Movie> result = movieService.searchMovies("Inception", null, null);

        assertEquals(1, result.size());
    }

    @Test
    void givenGenreFilter_whenSearchMovies_thenReturnMatchingGenreMovies() {
        List<Movie> result = movieService.searchMovies(null, "Sci-Fi", null);

        assertEquals(2, result.size());
    }

    @Test
    void givenReleaseYear_whenSearchMovies_thenReturnMatchingYearMovies() {
        List<Movie> result = movieService.searchMovies(null, null, 2010);

        assertEquals(2, result.size());
    }

    // -------------------------
    // addMovie()
    // -------------------------

    @Test
    void givenNewMovie_whenAddMovie_thenMovieIsAdded() {
        Movie newMovie = new Movie("Dune", "Sci-Fi", 2021);

        boolean result = movieService.addMovie(newMovie);

        assertTrue(result);
        assertEquals(5, movieService.getAllMovies().size());
    }

    @Test
    void givenDuplicateMovie_whenAddMovie_thenMovieIsNotAdded() {
        Movie duplicate = new Movie("The Matrix", "Sci-Fi", 1999);

        boolean result = movieService.addMovie(duplicate);

        assertFalse(result);
    }

    // -------------------------
    // deleteMovie()
    // -------------------------

    @Test
    void givenExistingMovie_whenDeleteMovie_thenMovieIsRemoved() {
        boolean result = movieService.deleteMovie("Avatar", "Action", 2009);

        assertTrue(result);
        assertEquals(3, movieService.getAllMovies().size());
    }

    @Test
    void givenNonExistingMovie_whenDeleteMovie_thenReturnFalse() {
        boolean result = movieService.deleteMovie("Titanic", "Drama", 1997);

        assertFalse(result);
    }

    // -------------------------
    // updateMovie()
    // -------------------------

    @Test
    void givenExistingMovieId_whenUpdateMovie_thenMovieIsUpdated() {
        String id = inception.getId().toString();

        boolean result = movieService.updateMovie(id, "Inception Updated", "Thriller", 2011);

        assertTrue(result);
        assertEquals("Inception Updated", inception.getTitle());
    }

    @Test
    void givenNonExistingMovieId_whenUpdateMovie_thenReturnFalse() {
        boolean result = movieService.updateMovie("invalid-id", "Test", "Test", 2000);

        assertFalse(result);
    }

    // -------------------------
    // Exception tests
    // -------------------------

    @Test
    void should_throw_database_exception_when_delete_fails() {
        Movie m = new Movie("X", "Y", 2000);

        when(movieRepository.findAll()).thenReturn(List.of(m));

        when(movieRepository.delete(any(Movie.class)))
                .thenThrow(new DatabaseException("DB error", new SQLException()));

        assertThrows(DatabaseException.class,
                () -> movieService.deleteMovie("X", "Y", 2000));
    }

    @Test
    void should_throw_movie_not_found_exception_when_update_fails() {
        Movie m = inception;

        when(movieRepository.update(any(Movie.class)))
                .thenThrow(new MovieNotFoundException("Not found"));

        assertThrows(MovieNotFoundException.class,
                () -> movieService.updateMovie(
                        m.getId().toString(),
                        m.getTitle(),
                        m.getGenre(),
                        m.getReleaseYear()
                ));
    }
}