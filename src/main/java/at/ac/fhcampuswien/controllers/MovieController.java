package at.ac.fhcampuswien.controllers;

import at.ac.fhcampuswien.ApiUtils;
import at.ac.fhcampuswien.exceptions.DatabaseException;
import at.ac.fhcampuswien.exceptions.MovieNotFoundException;
import at.ac.fhcampuswien.models.Movie;
import at.ac.fhcampuswien.models.MovieFactory;
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

        String path = exchange.getRequestURI().getPath();

        try {
            switch (path) {
                case BASE + "getAll" -> handleGetAllRequest(exchange);
                case BASE + "add" -> handleAddRequest(exchange);
                case BASE + "delete" -> handleDeleteRequest(exchange);
                case BASE + "update" -> handleUpdateRequest(exchange);
                case BASE + "search" -> handleSearchRequest(exchange);
                default -> ApiUtils.sendResponse(exchange, 404, "{ \"error\": \"Path not found\" }");
            }

        } catch (JsonSyntaxException e) {
            ApiUtils.sendResponse(exchange, 400, "{ \"error\": \"Malformed JSON syntax\" }");
        } catch (UncheckedExceptionWrapper e) {
            // Hier entpacken wir die getunnelten Checked Exceptions wieder
            Throwable originalException = e.getCause();
            if (originalException instanceof MovieNotFoundException) {
                ApiUtils.sendResponse(exchange, 404, "{ \"error\": \"" + originalException.getMessage() + "\" }");
            } else if (originalException instanceof DatabaseException) {
                ApiUtils.sendResponse(exchange, 500, "{ \"error\": \"Internal Server Error: Database issue\" }");
            } else {
                ApiUtils.sendResponse(exchange, 500, "{ \"error\": \"An unexpected server error occurred\" }");
            }
        } catch (Exception e) {
            ApiUtils.sendResponse(exchange, 500,
                    "{ \"error\": \"An unexpected error occurred\" }");
        }
    }

    private void handleGetAllRequest(HttpExchange exchange) throws IOException, DatabaseException {
        ApiUtils.sendResponse(exchange, 200, formatMovieResponse(movieService.getAllMovies()));
    }

    private void handleSearchRequest(HttpExchange exchange) throws IOException,DatabaseException {
        Map<String, String> params = ApiUtils.parseQueryParams(exchange.getRequestURI().getQuery());
        String title = params.get("title");
        String genre = params.get("genre");
        Integer year = params.containsKey("releaseYear")
                ? Integer.parseInt(params.get("releaseYear"))
                : null;

        List<Movie> results = movieService.searchMovies(title, genre, year);
        ApiUtils.sendResponse(exchange, 200, formatMovieResponse(results));
    }

    private void handleAddRequest(HttpExchange exchange) throws IOException, DatabaseException {
        String body = new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);
        Movie movie = gson.fromJson(body, Movie.class);

        if (movie == null || movie.getTitle() == null || movie.getGenre() == null || movie.getReleaseYear() <= 0) {
            ApiUtils.sendResponse(exchange, 400, "{ \"message\": \"The request body is malformed\" }");
            return;
        }

        // Use the creational factory method rather than calling Movie constructor directly.
        if (movieService.addMovie(MovieFactory.create(movie.getTitle(), movie.getGenre(), movie.getReleaseYear()))) {
            ApiUtils.sendResponse(exchange, 201, "{ \"message\": \"Movie added successfully\" }");
        } else {
            ApiUtils.sendResponse(exchange, 400, "{ \"message\": \"Movie already exists\" }");
        }
    }
    private void handleDeleteRequest(HttpExchange exchange) throws IOException, DatabaseException, MovieNotFoundException {
        String body = new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);
        Movie movie = gson.fromJson(body, Movie.class);

        if (movie == null || movie.getTitle() == null || movie.getGenre() == null || movie.getReleaseYear() <= 0) {
            ApiUtils.sendResponse(exchange, 400, "{ \"message\": \"Invalid movie data supplied\" }");
            return;
        }

        movieService.deleteMovie(movie.getTitle(), movie.getGenre(), movie.getReleaseYear());
        ApiUtils.sendResponse(exchange, 200, "{ \"message\": \"Movie deleted successfully\" }");
    }

    private void handleUpdateRequest(HttpExchange exchange) throws IOException, DatabaseException, MovieNotFoundException {
        String body = new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);
        Movie movie = gson.fromJson(body, Movie.class);

        if (movie == null || movie.getId() == null || movie.getTitle() == null || movie.getGenre() == null || movie.getReleaseYear() <= 0) {
            ApiUtils.sendResponse(exchange, 400, "{ \"message\": \"Missing or invalid update data\" }");
            return;
        }

        movieService.updateMovie(movie.getId().toString(), movie.getTitle(), movie.getGenre(), movie.getReleaseYear());
        ApiUtils.sendResponse(exchange, 200, "{ \"message\": \"Movie updated successfully\" }");
    }

    // ---------------- HELPERS ----------------

    private String formatMovieResponse(List<Movie> movieList) {
        return gson.toJson(Map.of("movies", movieList));
    }
    // Hilfsklasse zum Tunneln der Exceptions
    private static class UncheckedExceptionWrapper extends RuntimeException {
        public UncheckedExceptionWrapper(Throwable cause) {
            super(cause);
        }
    }
}
