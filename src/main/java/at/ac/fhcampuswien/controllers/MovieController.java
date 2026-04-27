package at.ac.fhcampuswien.controllers;

import at.ac.fhcampuswien.ApiUtils;
import at.ac.fhcampuswien.models.Movie;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.ArrayList;
import java.util.Map;

public class MovieController implements HttpHandler {

    private List<Movie> movies = Movie.generateDummyMovies();

    private final String BASE = "/api/movies/";
    private String readRequestBody(HttpExchange exchange) throws IOException {
        return new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);
    }
    private static class ParsedMovieData {
        String id;
        String title;
        String genre;
        int releaseYear;

        ParsedMovieData(String id, String title, String genre, int releaseYear) {
            this.id = id;
            this.title = title;
            this.genre = genre;
            this.releaseYear = releaseYear;
        }
    }
    private ParsedMovieData parseMovieData(String requestBody) {
        String title = extractStringValue(requestBody, "title");
        String genre = extractStringValue(requestBody, "genre");
        Integer releaseYear = extractIntValue(requestBody, "releaseYear");

        if (title == null || genre == null || releaseYear == null) {
            return null;
        }

        return new ParsedMovieData(null, title, genre, releaseYear);
    }
    private ParsedMovieData parseMovieDataWithId(String requestBody) {
        String id = extractStringValue(requestBody, "id");
        String title = extractStringValue(requestBody, "title");
        String genre = extractStringValue(requestBody, "genre");
        Integer releaseYear = extractIntValue(requestBody, "releaseYear");

        if (id == null || title == null || genre == null || releaseYear == null) {
            return null;
        }

        if (id.length() != 36) {
            return null;
        }

        return new ParsedMovieData(id, title, genre, releaseYear);
    }
    private String extractStringValue(String json, String key) {
        String pattern = "\"" + key + "\"\\s*:\\s*\"";

        if (!json.matches("(?s).*" + pattern + ".*")) {
            return null;
        }

        return json.split("(?s)" + pattern, 2)[1].split("\"", 2)[0];
    }
    private Integer extractIntValue(String json, String key) {
        String pattern = "\"" + key + "\"\\s*:\\s*";

        if (!json.matches("(?s).*" + pattern + "\\d+.*")) {
            return null;
        }

        try {
            return Integer.parseInt(
                    json.split(pattern, 2)[1].split("[,}]", 2)[0].trim()
            );
        } catch (NumberFormatException e) {
            return null;
        }
    }
    @FunctionalInterface
    private interface RequestAction {
        void execute() throws IOException;
    }
    private void handleMethod(
            String actualMethod,
            String expectedMethod,
            HttpExchange exchange,
            RequestAction action
    ) throws IOException {
        if (!actualMethod.equals(expectedMethod)) {
            String response = "{ \"error\": \"Method not allowed\" }";
            ApiUtils.sendResponse(exchange, 405, response);
            return;
        }

        action.execute();
    }
    @Override
    public void handle(HttpExchange exchange) throws IOException {

        // Get the HTTP method (GET, POST, etc.)
        String method = exchange.getRequestMethod();

        // Get the requested URI path (e.g. /api/hello/greet)
        String path = exchange.getRequestURI().getPath();

        // Route based on the path
        switch (path) {
            //case BASE -> handleBaseRequest(method, exchange);
            case BASE + "getAll" -> handleGetAllRequest(method, exchange);
            case BASE + "add" -> handleAddRequest(method, exchange);
            case BASE + "delete" -> handleDeleteRequest(method, exchange);
            case BASE + "update" -> handleUpdateRequest(method, exchange);
            case BASE + "search" -> handleSearchRequest(method, exchange);
            default -> {
                // Path not found
                String response = "{ \"error\": \"Path not found\" }";
                ApiUtils.sendResponse(exchange, 404, response);
            }
        }
    }

    private void handleAddRequest(String method, HttpExchange exchange) throws IOException {
        handleMethod(method, "POST", exchange, () -> {
            String requestBody = new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);
            ParsedMovieData data = parseMovieData(requestBody);
            if (data == null) {
                String response = "{ \"message\": \"The request body is malformed or invalid movie data is provided\" }";
                ApiUtils.sendResponse(exchange, 400, response);
                return;
            }
            boolean movieExists = false;
            for (Movie movie : movies) {
                if (movie.getTitle().equals(data.title) &&
                        movie.getGenre().equals(data.genre) &&
                        movie.getReleaseYear() == data.releaseYear) {
                    movieExists = true;
                    break;
                }
            }
            if (movieExists) {
                String response = "{ \"message\": \"Movie already exists\" }";
                ApiUtils.sendResponse(exchange, 400, response);
            } else {
                movies.add(new Movie(data.title, data.genre, data.releaseYear));

                String response = "{ \"message\": \"Movie added successfully\" }";
                ApiUtils.sendResponse(exchange, 201, response);
            }
        });
    }

    private void handleUpdateRequest(String method, HttpExchange exchange) throws IOException {
        handleMethod(method, "PUT", exchange, () -> {
            String requestBody = new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);

            ParsedMovieData data = parseMovieDataWithId(requestBody);

            if (data == null) {
                String response = "{ \"message\": \"The request body is malformed or invalid movie data is provided\" }";
                ApiUtils.sendResponse(exchange, 400, response);
                return;
            }

            for (Movie movie : movies) {
                if (movie.getId().toString().equals(data.id)) {
                    movie.setTitle(data.title);
                    movie.setGenre(data.genre);
                    movie.setReleaseYear(data.releaseYear);

                    String response = "{ \"message\": \"Movie updated successfully\" }";
                    ApiUtils.sendResponse(exchange, 200, response);
                    return;
                }
            }

            String response = "{ \"message\": \"Movie to be updated not found\" }";
            ApiUtils.sendResponse(exchange, 404, response);
        });
    }

    private void handleGetAllRequest(String method, HttpExchange exchange) throws IOException {
        handleMethod(method, "GET", exchange, () -> {
            StringBuilder response = new StringBuilder("{ \"movies\": [");
            for (int i = 0; i < movies.size(); i++) {
                Movie movie = movies.get(i);
                response.append("{")
                        .append("\"id\":\"").append(movie.getId()).append("\",")
                        .append("\"title\":\"").append(movie.getTitle()).append("\",")
                        .append("\"genre\":\"").append(movie.getGenre()).append("\",")
                        .append("\"releaseYear\":").append(movie.getReleaseYear())
                        .append("}");
                if (i < movies.size() - 1) response.append(",");
            }
            response.append("]}");
            ApiUtils.sendResponse(exchange, 200, response.toString());
        });
    }

    private void handleDeleteRequest(String method, HttpExchange exchange) throws IOException {
        handleMethod(method, "DELETE", exchange, () -> {
            String requestBody = new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);
            ParsedMovieData data = parseMovieData(requestBody);
            if (data == null) {
                String response = "{ \"message\": \"The request body is malformed or invalid movie data is provided\" }";
                ApiUtils.sendResponse(exchange, 400, response);
                return;
            }
            for (int i = 0; i < movies.size(); i++) {
                Movie movie = movies.get(i);

                if (movie.getTitle().equals(data.title) &&
                        movie.getGenre().equals(data.genre) &&
                        movie.getReleaseYear() == data.releaseYear) {

                    movies.remove(i);

                    String response = "{ \"message\": \"Movie deleted successfully\" }";
                    ApiUtils.sendResponse(exchange, 200, response);
                    return;
                }
            }
            String response = "{ \"message\": \"Movie not found\" }";
            ApiUtils.sendResponse(exchange, 404, response);
        });
    }

    private void handleSearchRequest(String method, HttpExchange exchange) throws IOException {
        handleMethod(method, "GET", exchange, () -> {
            String query = exchange.getRequestURI().getQuery();
            Map<String, String> params = ApiUtils.parseQueryParams(query);
            String titleParam = params.get("title");
            String genreParam = params.get("genre");
            String releaseYearParam = params.get("releaseYear");
            List<Movie> results = new ArrayList<>();
            for (Movie movie : movies) {
                boolean matches = true;
                if (titleParam != null) {
                    matches = movie.getTitle()
                            .toLowerCase()
                            .contains(titleParam.toLowerCase());
                }
                if (matches && genreParam != null) {
                    matches = movie.getGenre()
                            .toLowerCase()
                            .contains(genreParam.toLowerCase());
                }
                if (matches && releaseYearParam != null) {
                    try {
                        int releaseYear = Integer.parseInt(releaseYearParam);
                        matches = movie.getReleaseYear() == releaseYear;
                    } catch (NumberFormatException e) {
                        String response = "{ \"message\": \"Invalid releaseYear parameter\" }";
                        ApiUtils.sendResponse(exchange, 400, response);
                        return;
                    }
                }
                if (matches) results.add(movie);
            }
            StringBuilder responseBuilder = new StringBuilder("{ \"movies\": [");
            for (int i = 0; i < results.size(); i++) {
                Movie movie = results.get(i);
                responseBuilder.append("{")
                        .append("\"id\":\"").append(movie.getId()).append("\",")
                        .append("\"title\":\"").append(movie.getTitle()).append("\",")
                        .append("\"genre\":\"").append(movie.getGenre()).append("\",")
                        .append("\"releaseYear\":").append(movie.getReleaseYear())
                        .append("}");

                if (i < results.size() - 1) {
                    responseBuilder.append(",");
                }
            }
            responseBuilder.append("]}");
            String response = responseBuilder.toString();
            ApiUtils.sendResponse(exchange, 200, response);
        });
    }
}
