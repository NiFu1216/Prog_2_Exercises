package at.ac.fhcampuswien.controllers;

import at.ac.fhcampuswien.ApiUtils;
import at.ac.fhcampuswien.exceptions.DatabaseException;
import at.ac.fhcampuswien.exceptions.MovieNotFoundException;
import at.ac.fhcampuswien.models.Movie;
import at.ac.fhcampuswien.repositories.MovieRepository;
import at.ac.fhcampuswien.services.MovieService;
import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;

public class MovieController implements HttpHandler {

    private final MovieService movieService;
    private final String BASE = "/api/movies/";
    private final Gson gson = new Gson();

    public MovieController() {
        this.movieService = new MovieService(new MovieRepository());
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {

        String method = exchange.getRequestMethod();
        String path = exchange.getRequestURI().getPath();

        try {
            switch (path) {
                case BASE + "getAll" -> handleMethod(method, "GET", exchange, () -> handleGetAllRequest(exchange));
                case BASE + "add" -> handleMethod(method, "POST", exchange, () -> handleAddRequest(exchange));
                case BASE + "delete" -> handleMethod(method, "DELETE", exchange, () -> handleDeleteRequest(exchange));
                case BASE + "update" -> handleMethod(method, "PUT", exchange, () -> handleUpdateRequest(exchange));
                case BASE + "search" -> handleMethod(method, "GET", exchange, () -> handleSearchRequest(exchange));
                default -> ApiUtils.sendResponse(exchange, 404, "{ \"error\": \"Path not found\" }");
            }

        } catch (JsonSyntaxException e) {
            ApiUtils.sendResponse(exchange, 400,
                    "{ \"error\": \"Malformed JSON syntax\" }");

        } catch (MovieNotFoundException e) {
            ApiUtils.sendResponse(exchange, 404,
                    "{ \"error\": \"" + e.getMessage() + "\" }");

        } catch (DatabaseException e) {
            ApiUtils.sendResponse(exchange, 500,
                    "{ \"error\": \"Internal Server Error: Database issue\" }");

        } catch (Exception e) {
            ApiUtils.sendResponse(exchange, 500,
                    "{ \"error\": \"An unexpected error occurred\" }");
        }
    }

    // ---------------- HANDLERS ----------------

    private void handleGetAllRequest(HttpExchange exchange) throws IOException {
        List<Movie> movies = movieService.getAllMovies();
        ApiUtils.sendResponse(exchange, 200, formatMovieResponse(movies));
    }

    private void handleSearchRequest(HttpExchange exchange) throws IOException {
        Map<String, String> params =
                ApiUtils.parseQueryParams(exchange.getRequestURI().getQuery());

        String title = params.get("title");
        String genre = params.get("genre");
        Integer year = params.containsKey("releaseYear")
                ? Integer.parseInt(params.get("releaseYear"))
                : null;

        List<Movie> results = movieService.searchMovies(title, genre, year);
        ApiUtils.sendResponse(exchange, 200, formatMovieResponse(results));
    }

    private void handleAddRequest(HttpExchange exchange) throws IOException {

        String body = new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);

        Movie movie = gson.fromJson(body, Movie.class);

        if (movie == null || movie.getTitle() == null ||
                movie.getGenre() == null || movie.getReleaseYear() <= 0) {

            ApiUtils.sendResponse(exchange, 400,
                    "{ \"error\": \"Invalid movie data\" }");
            return;
        }

        boolean success = movieService.addMovie(
                new Movie(movie.getTitle(), movie.getGenre(), movie.getReleaseYear())
        );

        if (success) {
            ApiUtils.sendResponse(exchange, 201,
                    "{ \"message\": \"Movie added successfully\" }");
        } else {
            ApiUtils.sendResponse(exchange, 400,
                    "{ \"message\": \"Movie already exists\" }");
        }
    }

    private void handleDeleteRequest(HttpExchange exchange) throws IOException {

        String body = new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);

        Movie movie = gson.fromJson(body, Movie.class);

        if (movieService.deleteMovie(
                movie.getTitle(),
                movie.getGenre(),
                movie.getReleaseYear())) {

            ApiUtils.sendResponse(exchange, 200,
                    "{ \"message\": \"Movie deleted successfully\" }");

        } else {
            ApiUtils.sendResponse(exchange, 404,
                    "{ \"message\": \"Movie not found\" }");
        }
    }

    private void handleUpdateRequest(HttpExchange exchange) throws IOException {

        String body = new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);

        Movie movie = gson.fromJson(body, Movie.class);

        if (movieService.updateMovie(
                movie.getId().toString(),
                movie.getTitle(),
                movie.getGenre(),
                movie.getReleaseYear())) {

            ApiUtils.sendResponse(exchange, 200,
                    "{ \"message\": \"Movie updated successfully\" }");

        } else {
            ApiUtils.sendResponse(exchange, 404,
                    "{ \"message\": \"Movie not found\" }");
        }
    }

    // ---------------- HELPERS ----------------

    private String formatMovieResponse(List<Movie> movieList) {
        return gson.toJson(Map.of("movies", movieList));
    }

    private void handleMethod(String actual, String expected,
                              HttpExchange ex, RequestAction action) throws IOException {

        if (!actual.equals(expected)) {
            ApiUtils.sendResponse(ex, 405,
                    "{ \"error\": \"Method not allowed\" }");
            return;
        }

        action.execute();
    }

    @FunctionalInterface
    private interface RequestAction {
        void execute() throws IOException;
    }
}