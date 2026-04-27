package at.ac.fhcampuswien.controllers;

import at.ac.fhcampuswien.ApiUtils;
import at.ac.fhcampuswien.models.Movie;
import at.ac.fhcampuswien.services.MovieService;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;

public class MovieController implements HttpHandler {

    private final MovieService movieService;
    private final String BASE = "/api/movies/";

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
        ParsedMovieData data = parseMovieData(body);

        if (data == null) {
            ApiUtils.sendResponse(exchange, 400, "{ \"message\": \"The request body is malformed\" }");
            return;
        }

        if (movieService.addMovie(new Movie(data.title, data.genre, data.releaseYear))) {
            ApiUtils.sendResponse(exchange, 201, "{ \"message\": \"Movie added successfully\" }");
        } else {
            ApiUtils.sendResponse(exchange, 400, "{ \"message\": \"Movie already exists\" }");
        }
    }

    private void handleDeleteRequest(HttpExchange exchange) throws IOException {
        String body = new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);
        ParsedMovieData data = parseMovieData(body);

        if (data != null && movieService.deleteMovie(data.title, data.genre, data.releaseYear)) {
            ApiUtils.sendResponse(exchange, 200, "{ \"message\": \"Movie deleted successfully\" }");
        } else {
            ApiUtils.sendResponse(exchange, 404, "{ \"message\": \"Movie not found\" }");
        }
    }

    private void handleUpdateRequest(HttpExchange exchange) throws IOException {
        String body = new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);
        ParsedMovieData data = parseMovieDataWithId(body);

        if (data != null && movieService.updateMovie(data.id, data.title, data.genre, data.releaseYear)) {
            ApiUtils.sendResponse(exchange, 200, "{ \"message\": \"Movie updated successfully\" }");
        } else {
            ApiUtils.sendResponse(exchange, 404, "{ \"message\": \"Movie to be updated not found\" }");
        }
    }

    // --- Hilfsmethoden für Parsing und JSON-Formatierung ---

    private String formatMovieResponse(List<Movie> movieList) {
        StringBuilder sb = new StringBuilder("{ \"movies\": [");
        for (int i = 0; i < movieList.size(); i++) {
            Movie m = movieList.get(i);
            sb.append(String.format("{\"id\":\"%s\",\"title\":\"%s\",\"genre\":\"%s\",\"releaseYear\":%d}",
                    m.getId(), m.getTitle(), m.getGenre(), m.getReleaseYear()));
            if (i < movieList.size() - 1) sb.append(",");
        }
        sb.append("]}");
        return sb.toString();
    }

    private void handleMethod(String actual, String expected, HttpExchange ex, RequestAction action) throws IOException {
        if (!actual.equals(expected)) {
            ApiUtils.sendResponse(ex, 405, "{ \"error\": \"Method not allowed\" }");
            return;
        }
        action.execute();
    }

    private ParsedMovieData parseMovieData(String json) {
        String title = extractValue(json, "title");
        String genre = extractValue(json, "genre");
        Integer year = extractInt(json, "releaseYear");
        return (title != null && genre != null && year != null) ? new ParsedMovieData(null, title, genre, year) : null;
    }

    private ParsedMovieData parseMovieDataWithId(String json) {
        String id = extractValue(json, "id");
        ParsedMovieData data = parseMovieData(json);
        return (id != null && data != null) ? new ParsedMovieData(id, data.title, data.genre, data.releaseYear) : null;
    }

    private String extractValue(String json, String key) {
        String pattern = "\"" + key + "\"\\s*:\\s*\"";
        if (!json.contains(pattern)) return null;
        return json.split(pattern)[1].split("\"")[0];
    }

    private Integer extractInt(String json, String key) {
        String pattern = "\"" + key + "\"\\s*:\\s*";
        if (!json.contains(pattern)) return null;
        try {
            return Integer.parseInt(json.split(pattern)[1].split("[,}]")[0].trim());
        } catch (Exception e) { return null; }
    }

    @FunctionalInterface private interface RequestAction { void execute() throws IOException; }
    private static class ParsedMovieData {
        String id, title, genre; int releaseYear;
        ParsedMovieData(String i, String t, String g, int y) { id=i; title=t; genre=g; releaseYear=y; }
    }
}