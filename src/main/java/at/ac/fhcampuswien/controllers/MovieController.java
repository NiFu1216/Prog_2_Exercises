package at.ac.fhcampuswien.controllers;

import at.ac.fhcampuswien.ApiUtils;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import java.io.IOException;

public class MovieController implements HttpHandler {

    private final String BASE = "/api/movies/";
    @Override
    public void handle(HttpExchange exchange) throws IOException {
        // Get the HTTP method (GET, POST, etc.)
        String method = exchange.getRequestMethod();

        // Get the requested URI path (e.g. /api/hello/greet)
        String path = exchange.getRequestURI().getPath();

        // Route based on the path
        switch (path) {
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

}
