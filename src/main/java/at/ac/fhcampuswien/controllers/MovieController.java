package at.ac.fhcampuswien.controllers;

import at.ac.fhcampuswien.ApiUtils;
import at.ac.fhcampuswien.models.Movie;
import at.ac.fhcampuswien.services.MovieService;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.google.gson.Gson;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;

public class MovieController implements HttpHandler {

    private final MovieService movieService;
    private final String BASE = "/api/movies/";
    private final Gson gson = new Gson();

    public MovieController() {
        // Initialisierung des Services mit Dummy-Daten
        this.movieService = new MovieService(Movie.generateDummyMovies());
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        String method = exchange.getRequestMethod();
        String path = exchange.getRequestURI().getPath();

        switch (path) {
            case BASE + "getAll" -> handleMethod(method, "GET", exchange, () -> handleGetAllRequest(exchange));
            case BASE + "add" -> handleMethod(method, "POST", exchange, () -> handleAddRequest(exchange));
            case BASE + "delete" -> handleMethod(method, "DELETE", exchange, () -> handleDeleteRequest(exchange));
            case BASE + "update" -> handleMethod(method, "PUT", exchange, () -> handleUpdateRequest(exchange));
            case BASE + "search" -> handleMethod(method, "GET", exchange, () -> handleSearchRequest(exchange));
            default -> ApiUtils.sendResponse(exchange, 404, "{ \"error\": \"Path not found\" }");
        }
    }

    private void handleGetAllRequest(HttpExchange exchange) throws IOException {
        ApiUtils.sendResponse(exchange, 200, formatMovieResponse(movieService.getAllMovies()));
    }

    private void handleSearchRequest(HttpExchange exchange) throws IOException {
        Map<String, String> params = ApiUtils.parseQueryParams(exchange.getRequestURI().getQuery());
        String title = params.get("title");
        String genre = params.get("genre");
        Integer year = params.containsKey("releaseYear") ? Integer.parseInt(params.get("releaseYear")) : null;

        List<Movie> results = movieService.searchMovies(title, genre, year);
        ApiUtils.sendResponse(exchange, 200, formatMovieResponse(results));
    }

    private void handleAddRequest(HttpExchange exchange) throws IOException {
        String body = new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);

        try {
            Movie movie = gson.fromJson(body, Movie.class);

            if (movie == null || movie.getTitle() == null || movie.getGenre() == null || movie.getReleaseYear() <= 0) {
                ApiUtils.sendResponse(exchange, 400, "{ \"message\": \"The request body is malformed\" }");
                return;
            }

            if (movieService.addMovie(new Movie(movie.getTitle(), movie.getGenre(), movie.getReleaseYear()))) {
                ApiUtils.sendResponse(exchange, 201, "{ \"message\": \"Movie added successfully\" }");
            } else {
                ApiUtils.sendResponse(exchange, 400, "{ \"message\": \"Movie already exists\" }");
            }
        } catch (Exception e) {
            ApiUtils.sendResponse(exchange, 400, "{ \"message\": \"The request body is malformed\" }");
        }
    }

    private void handleDeleteRequest(HttpExchange exchange) throws IOException {
        String body = new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);

        try {
            Movie movie = gson.fromJson(body, Movie.class);

            if (movie == null || movie.getTitle() == null || movie.getGenre() == null || movie.getReleaseYear() <= 0) {
                ApiUtils.sendResponse(exchange, 404, "{ \"message\": \"Movie not found\" }");
                return;
            }

            if (movieService.deleteMovie(movie.getTitle(), movie.getGenre(), movie.getReleaseYear())) {
                ApiUtils.sendResponse(exchange, 200, "{ \"message\": \"Movie deleted successfully\" }");
            } else {
                ApiUtils.sendResponse(exchange, 404, "{ \"message\": \"Movie not found\" }");
            }
        } catch (Exception e) {
            ApiUtils.sendResponse(exchange, 404, "{ \"message\": \"Movie not found\" }");
        }
    }

    private void handleUpdateRequest(HttpExchange exchange) throws IOException {
        String body = new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);

        try {
            Movie movie = gson.fromJson(body, Movie.class);

            if (movie == null || movie.getId() == null || movie.getTitle() == null || movie.getGenre() == null || movie.getReleaseYear() <= 0) {
                ApiUtils.sendResponse(exchange, 404, "{ \"message\": \"Movie to be updated not found\" }");
                return;
            }

            if (movieService.updateMovie(movie.getId().toString(), movie.getTitle(), movie.getGenre(), movie.getReleaseYear())) {
                ApiUtils.sendResponse(exchange, 200, "{ \"message\": \"Movie updated successfully\" }");
            } else {
                ApiUtils.sendResponse(exchange, 404, "{ \"message\": \"Movie to be updated not found\" }");
            }
        } catch (Exception e) {
            ApiUtils.sendResponse(exchange, 404, "{ \"message\": \"Movie to be updated not found\" }");
        }
    }

    // --- Hilfsmethoden für Parsing und JSON-Formatierung ---

    private String formatMovieResponse(List<Movie> movieList) {
        return gson.toJson(Map.of("movies", movieList));
    }

    private void handleMethod(String actual, String expected, HttpExchange ex, RequestAction action) throws IOException {
        if (!actual.equals(expected)) {
            ApiUtils.sendResponse(ex, 405, "{ \"error\": \"Method not allowed\" }");
            return;
        }
        action.execute();
    }

    @FunctionalInterface private interface RequestAction { void execute() throws IOException; }

}