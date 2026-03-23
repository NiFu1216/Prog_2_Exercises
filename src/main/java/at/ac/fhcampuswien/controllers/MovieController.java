package at.ac.fhcampuswien.controllers;

import at.ac.fhcampuswien.ApiUtils;
import at.ac.fhcampuswien.models.Movie;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;


import java.io.IOException;
import java.util.List;

public class MovieController implements HttpHandler {

    private final String BASE = "/api/movies/";
    private List<Movie> movies = Movie.generateDummyMovies();
    @Override
    public void handle(HttpExchange exchange) throws IOException {
        // Get the HTTP method (GET, POST, etc.)
        String method = exchange.getRequestMethod();

        // Get the requested URI path (e.g. /api/hello/greet)
        String path = exchange.getRequestURI().getPath();

        // Route based on the path
        switch (path) {
            case BASE + "getAll" -> handleGetAll(method, exchange);
            // case BASE -> handleBaseRequest(method, exchange);
            // case BASE + "getAll" -> handleGetAllRequest(method, exchange);
            // case BASE + "add" -> handleAddRequest(method, exchange);
            // case BASE + "delete" -> handleDeleteRequest(method, exchange);
            // case BASE + "update" -> handleUpdateRequest(method, exchange);
            default -> {
                // Path not found
                String response = "{ \"error\": \"Path not found\" }";
                ApiUtils.sendResponse(exchange, 404, response);
            }
        }
    }
    private void handleGetAll(String method, HttpExchange exchange) throws IOException {
        if (!method.equals("GET")) {
            ApiUtils.sendResponse(exchange, 405, "{ \"error\": \"Method not allowed\" }");
            return;
        }

        StringBuilder json = new StringBuilder("[");
        for (int i = 0; i < movies.size(); i++) {
            Movie m = movies.get(i);
            json.append("{")
                    .append("\"id\":\"").append(m.getID()).append("\",")   // matches your getter
                    .append("\"title\":\"").append(m.getTitle()).append("\",")
                    .append("\"genre\":\"").append(m.getGenre()).append("\",")
                    .append("\"releaseYear\":").append(m.getReleaseYear())
                    .append("}");

            if (i < movies.size() - 1) json.append(",");
        }
        json.append("]");

        ApiUtils.sendResponse(exchange, 200, json.toString());
    }


}
