package at.ac.fhcampuswien.services;

import at.ac.fhcampuswien.models.Movie;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class MovieServiceTest {

    private MovieService movieService;
    private List<Movie> movies;

    private Movie matrix;
    private Movie inception;
    private Movie interstellar;
    private Movie avatar;

    @BeforeEach
    void setUp() {
        movies = new ArrayList<>();

        matrix = new Movie("The Matrix", "Sci-Fi", 1999);
        inception = new Movie("Inception", "Sci-Fi", 2010);
        interstellar = new Movie("Interstellar", "Sci-Fi", 2014);
        avatar = new Movie("Avatar", "Action", 2009);

        movies.add(matrix);
        movies.add(inception);
        movies.add(interstellar);
        movies.add(avatar);

        movieService = new MovieService(movies);
    }

    // -------------------------
    // getAllMovies()
    // -------------------------

    @Test
    @DisplayName("Given movies exist When getAllMovies is called Then all movies are returned")
    void givenMoviesExist_whenGetAllMovies_thenReturnAllMovies() {
        List<Movie> result = movieService.getAllMovies();

        assertEquals(4, result.size());
        assertTrue(result.contains(matrix));
        assertTrue(result.contains(inception));
        assertTrue(result.contains(interstellar));
        assertTrue(result.contains(avatar));
    }

    // -------------------------
    // searchMovies()
    // -------------------------

    @Test
    @DisplayName("Given no filters When searchMovies is called Then all movies are returned")
    void givenNoFilters_whenSearchMovies_thenReturnAllMovies() {
        List<Movie> result = movieService.searchMovies(null, null, null);

        assertEquals(4, result.size());
    }

    @Test
    @DisplayName("Given exact title When searchMovies is called Then matching movie is returned")
    void givenExactTitle_whenSearchMovies_thenReturnMatchingMovie() {
        List<Movie> result = movieService.searchMovies("Inception", null, null);

        assertEquals(1, result.size());
        assertEquals("Inception", result.get(0).getTitle());
    }

    @Test
    @DisplayName("Given partial title When searchMovies is called Then matching movies are returned")
    void givenPartialTitle_whenSearchMovies_thenReturnMatchingMovies() {
        List<Movie> result = movieService.searchMovies("Inter", null, null);

        assertEquals(1, result.size());
        assertEquals("Interstellar", result.get(0).getTitle());
    }

    @Test
    @DisplayName("Given lowercase title When searchMovies is called Then search is case-insensitive")
    void givenLowercaseTitle_whenSearchMovies_thenSearchIsCaseInsensitive() {
        List<Movie> result = movieService.searchMovies("matrix", null, null);

        assertEquals(1, result.size());
        assertEquals("The Matrix", result.get(0).getTitle());
    }

    @Test
    @DisplayName("Given genre filter When searchMovies is called Then only matching genre movies are returned")
    void givenGenreFilter_whenSearchMovies_thenReturnMatchingGenreMovies() {
        List<Movie> result = movieService.searchMovies(null, "Sci-Fi", null);

        assertEquals(3, result.size());
    }

    @Test
    @DisplayName("Given partial genre When searchMovies is called Then matching genre movies are returned")
    void givenPartialGenre_whenSearchMovies_thenReturnMatchingGenreMovies() {
        List<Movie> result = movieService.searchMovies(null, "Sci", null);

        assertEquals(3, result.size());
    }

    @Test
    @DisplayName("Given release year When searchMovies is called Then matching year movies are returned")
    void givenReleaseYear_whenSearchMovies_thenReturnMatchingYearMovies() {
        List<Movie> result = movieService.searchMovies(null, null, 2010);

        assertEquals(1, result.size());
        assertEquals("Inception", result.get(0).getTitle());
    }

    @Test
    @DisplayName("Given title genre and year When searchMovies is called Then exact filtered movie is returned")
    void givenTitleGenreAndYear_whenSearchMovies_thenReturnExactFilteredMovie() {
        List<Movie> result = movieService.searchMovies("Avatar", "Action", 2009);

        assertEquals(1, result.size());
        assertEquals("Avatar", result.get(0).getTitle());
    }

    @Test
    @DisplayName("Given no matching filter When searchMovies is called Then empty list is returned")
    void givenNoMatchingFilter_whenSearchMovies_thenReturnEmptyList() {
        List<Movie> result = movieService.searchMovies("Nonexistent", null, null);

        assertTrue(result.isEmpty());
    }

    // -------------------------
    // addMovie()
    // -------------------------

    @Test
    @DisplayName("Given new movie When addMovie is called Then movie is added")
    void givenNewMovie_whenAddMovie_thenMovieIsAdded() {
        Movie newMovie = new Movie("Dune", "Sci-Fi", 2021);

        boolean result = movieService.addMovie(newMovie);

        assertTrue(result);
        assertEquals(5, movieService.getAllMovies().size());
        assertTrue(movieService.getAllMovies().contains(newMovie));
    }

    @Test
    @DisplayName("Given duplicate movie When addMovie is called Then movie is not added")
    void givenDuplicateMovie_whenAddMovie_thenMovieIsNotAdded() {
        Movie duplicate = new Movie("The Matrix", "Action", 1999);

        boolean result = movieService.addMovie(duplicate);

        assertFalse(result);
        assertEquals(4, movieService.getAllMovies().size());
    }

    @Test
    @DisplayName("Given same title but different year When addMovie is called Then movie is added")
    void givenSameTitleButDifferentYear_whenAddMovie_thenMovieIsAdded() {
        Movie sequel = new Movie("The Matrix", "Sci-Fi", 2003);

        boolean result = movieService.addMovie(sequel);

        assertTrue(result);
        assertEquals(5, movieService.getAllMovies().size());
    }

    @Test
    @DisplayName("Given same title with different casing When addMovie is called Then duplicate is rejected")
    void givenSameTitleDifferentCasing_whenAddMovie_thenDuplicateIsRejected() {
        Movie duplicate = new Movie("the matrix", "Sci-Fi", 1999);

        boolean result = movieService.addMovie(duplicate);

        assertFalse(result);
        assertEquals(4, movieService.getAllMovies().size());
    }

    // -------------------------
    // deleteMovie()
    // -------------------------

    @Test
    @DisplayName("Given existing movie When deleteMovie is called Then movie is removed")
    void givenExistingMovie_whenDeleteMovie_thenMovieIsRemoved() {
        boolean result = movieService.deleteMovie("Avatar", "Action", 2009);

        assertTrue(result);
        assertEquals(3, movieService.getAllMovies().size());
    }

    @Test
    @DisplayName("Given non-existing movie When deleteMovie is called Then false is returned")
    void givenNonExistingMovie_whenDeleteMovie_thenReturnFalse() {
        boolean result = movieService.deleteMovie("Titanic", "Drama", 1997);

        assertFalse(result);
        assertEquals(4, movieService.getAllMovies().size());
    }

    @Test
    @DisplayName("Given different casing When deleteMovie is called Then delete is case-insensitive")
    void givenDifferentCasing_whenDeleteMovie_thenDeleteIsCaseInsensitive() {
        boolean result = movieService.deleteMovie("avatar", "action", 2009);

        assertTrue(result);
        assertEquals(3, movieService.getAllMovies().size());
    }

    // -------------------------
    // updateMovie()
    // -------------------------

    @Test
    @DisplayName("Given existing movie id When updateMovie is called Then movie is updated")
    void givenExistingMovieId_whenUpdateMovie_thenMovieIsUpdated() {
        String id = inception.getId().toString();

        boolean result = movieService.updateMovie(id, "Inception Updated", "Thriller", 2011);

        assertTrue(result);
        assertEquals("Inception Updated", inception.getTitle());
        assertEquals("Thriller", inception.getGenre());
        assertEquals(2011, inception.getReleaseYear());
    }

    @Test
    @DisplayName("Given non-existing movie id When updateMovie is called Then false is returned")
    void givenNonExistingMovieId_whenUpdateMovie_thenReturnFalse() {
        boolean result = movieService.updateMovie("non-existing-id", "Test", "Test", 2000);

        assertFalse(result);
    }

    @Test
    @DisplayName("Given existing movie id When updateMovie is called Then movie count stays unchanged")
    void givenExistingMovieId_whenUpdateMovie_thenMovieCountRemainsUnchanged() {
        String id = matrix.getId().toString();

        boolean result = movieService.updateMovie(id, "Matrix Reloaded", "Sci-Fi", 2003);

        assertTrue(result);
        assertEquals(4, movieService.getAllMovies().size());
    }
}