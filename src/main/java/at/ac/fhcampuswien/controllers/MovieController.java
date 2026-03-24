package at.ac.fhcampuswien.controllers;

import at.ac.fhcampuswien.ApiUtils;
import at.ac.fhcampuswien.models.Movie;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Iterator;
import java.util.List;

public class MovieController implements HttpHandler {

    private List<Movie> movies = Movie.generateDummyMovies();

    private final String BASE = "/api/movies/";
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
            default -> {
                // Path not found
                String response = "{ \"error\": \"Path not found\" }";
                ApiUtils.sendResponse(exchange, 404, response);
            }
        }
    }

    private void handleAddRequest(String method, HttpExchange exchange) throws IOException {

        // Add a movie through the request body
        switch (method) {
            case "POST" -> {
                String requestBody = new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);                //Receiving the data
                boolean movieExists = false;                                                                                      //Logic for 400 Bad Request
                boolean hasTitle = requestBody.matches("(?s).*\"title\"\\s*:\\s*\".*?\".*");                                //Logic for 400 Bad Request
                boolean hasGenre = requestBody.matches("(?s).*\"genre\"\\s*:\\s*\".*?\".*");                                //Logic for 400 Bad Request
                boolean hasYear  = requestBody.matches("(?s).*\"releaseYear\"\\s*:\\s*\\d+.*");                             //Logic for 400 Bad Request
                if(!hasTitle || !hasGenre || !hasYear) {
                    String response = "{ \"message\": \"The request body is malformed or invalid movie data is provided\" }";     //400 Bad Request Response for malformed format
                    ApiUtils.sendResponse(exchange, 400, response);
                    return;
                }
                String title = requestBody.split("(?i)\"title\"\\s*:\\s*\"")[1].split("\"")[0];                       //Extract title
                String genre = requestBody.split("(?i)\"genre\"\\s*:\\s*\"")[1].split("\"")[0];                       //Extract genre
                int releaseYear;
                try{
                    releaseYear = Integer.parseInt(
                            requestBody.split("\"releaseYear\"\\s*:\\s*")[1].split("[,}]")[0].trim()                 //Extract releaseYear
                    );
                } catch (NumberFormatException e) {
                    String response = "{ \"message:\": \"The request body is malformed or invalid movie data is provided\"";     //400 Bad Request Response for wrong format
                    ApiUtils.sendResponse(exchange, 400, response);
                    return;
                }
                for (Movie movie : movies) {
                    if (movie.getTitle().equals(title) &&
                            movie.getGenre().equals(genre) &&
                            movie.getReleaseYear() == releaseYear) {
                        movieExists = true;
                        break;
                    }
                }
                if (movieExists) {
                    String response = "{ \"message:\": \"Movie already exists\" }";                                         // Response if movie exists
                    ApiUtils.sendResponse(exchange, 400, response);
                }else{
                    movies.add(new Movie(title, genre, releaseYear));                                                       //Add a movie if checks before were OK
                    String response = "{ \"message:\": \"Movie added successfully\" }";

                    System.out.println(movies);
                    ApiUtils.sendResponse(exchange, 201, response);
                }
            }
            default -> {
                String response = "{ \"error\": \"Method not allowed\" }";
                ApiUtils.sendResponse(exchange, 405, response);
            }
        }
    }

    private void handleUpdateRequest(String method, HttpExchange exchange) throws IOException {

        // Update a movie's details using its ID
        switch (method) {
            case "PUT" -> {

                // Logic
                String requestBody = new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);
                boolean movieFound = false;
                int releaseYearAsInt;

                // Get the ID
                String idKey = "\"id\": \"";
                int startIdKey = requestBody.indexOf(idKey) + idKey.length();
                int endIdKey = requestBody.indexOf("\"", startIdKey);
                String ID = requestBody.substring(startIdKey, endIdKey);

                // Get the title
                String titleKey = "\"title\": \"";
                int startTitleKey = requestBody.indexOf(titleKey) + titleKey.length();
                int endTitleKey = requestBody.indexOf("\"", startTitleKey);
                String title = requestBody.substring(startTitleKey, endTitleKey);

                // Get the genre
                String genreKey = "\"genre\": \"";
                int startGenreKey = requestBody.indexOf(genreKey) + genreKey.length();
                int endGenreKey = requestBody.indexOf("\"", startGenreKey);
                String genre = requestBody.substring(startGenreKey, endGenreKey);

                // Get the releaseYear
                String releaseYearKey = "\"releaseYear\": \"";
                int startReleaseYearKey = requestBody.indexOf(releaseYearKey) + releaseYearKey.length();
                int endReleaseYearKey = requestBody.indexOf("\"", startReleaseYearKey);
                String releaseYear = requestBody.substring(startReleaseYearKey, endReleaseYearKey);

                // Check if request body is malformed or if invalid movie data is provided
                if (
                        (!requestBody.contains(idKey)) ||
                        (!requestBody.contains(titleKey)) ||
                        (!requestBody.contains(genreKey)) ||
                        (!requestBody.contains(releaseYearKey)) ||
                        (ID.length() != 36)
                ) {
                    // Response
                    String response = "{ \"message:\": \"The request body is malformed or invalid movie data is provided\"";
                    ApiUtils.sendResponse(exchange, 400, response);
                    return;
                } else {
                    try {
                        releaseYearAsInt = Integer.parseInt(releaseYear.trim());
                    } catch (NumberFormatException e) {
                        // Response
                        String response = "{ \"message:\": \"The request body is malformed or invalid movie data is provided\"";
                        ApiUtils.sendResponse(exchange, 400, response);
                        return;
                    }
                }

                // Update the movie
                for (Movie movie : movies) {
                    if ((movie.getId().toString()).equals(ID)) {
                        movieFound = true;
                        movie.setTitle(title);
                        movie.setGenre(genre);
                        movie.setReleaseYear(releaseYearAsInt);
                    }
                }

                if (!movieFound) {
                    // Response if movie was not found
                    String response = "{ \"message:\": \"Movie to be updated not found\"";
                    ApiUtils.sendResponse(exchange, 404, response);
                    return;
                }

                // Response if movie was found
                String response = "{ \"message:\": \"Movie updated successfully\"";
                ApiUtils.sendResponse(exchange, 200, response);

            }
            default -> {
                String response = "{ \"error\": \"Method not allowed\" }";
                ApiUtils.sendResponse(exchange, 405, response);
            }
        }
    }
    private void handleGetAllRequest(String method, HttpExchange exchange) throws IOException {

        switch (method) {
            case "GET" -> {
                StringBuilder response = new StringBuilder("[");
                for (int i = 0; i < movies.size(); i++) {
                    Movie movie = movies.get(i);
                    response.append("{")
                            .append("\"id\":\"").append(movie.getId()).append("\",")
                            .append("\"title\":\"").append(movie.getTitle()).append("\",")
                            .append("\"genre\":\"").append(movie.getGenre()).append("\",")
                            .append("\"releaseYear\":").append(movie.getReleaseYear())
                            .append("}");

                    if (i < movies.size() - 1) {
                        response.append(",");
                    }
                }
                response.append("]");

                ApiUtils.sendResponse(exchange, 200, response.toString());
            }
            default -> {
                String response = "{ \"error\": \"Method not allowed\" }";
                ApiUtils.sendResponse(exchange, 405, response);
            }
        }
    }

    private void handleDeleteRequest(String method, HttpExchange exchange) throws IOException {

        switch (method) {
            case "DELETE" -> {
                String requestBody = new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);

                boolean hasTitle = requestBody.matches("(?s).*\"title\"\\s*:\\s*\".*?\".*");
                boolean hasGenre = requestBody.matches("(?s).*\"genre\"\\s*:\\s*\".*?\".*");
                boolean hasYear  = requestBody.matches("(?s).*\"releaseYear\"\\s*:\\s*\\d+.*");

                if (!hasTitle || !hasGenre || !hasYear) {
                    String response = "{ \"message\": \"The request body is malformed or invalid movie data is provided\" }";
                    ApiUtils.sendResponse(exchange, 400, response);
                    return;
                }

                String title = requestBody.split("(?i)\"title\"\\s*:\\s*\"")[1].split("\"")[0];
                String genre = requestBody.split("(?i)\"genre\"\\s*:\\s*\"")[1].split("\"")[0];

                int releaseYear;
                try {
                    releaseYear = Integer.parseInt(
                            requestBody.split("\"releaseYear\"\\s*:\\s*")[1].split("[,}]")[0].trim()
                    );
                } catch (NumberFormatException e) {
                    String response = "{ \"message\": \"The request body is malformed or invalid movie data is provided\" }";
                    ApiUtils.sendResponse(exchange, 400, response);
                    return;
                }

                Iterator<Movie> iterator = movies.iterator();
                while (iterator.hasNext()) {
                    Movie movie = iterator.next();
                    if (movie.getTitle().equals(title)
                            && movie.getGenre().equals(genre)
                            && movie.getReleaseYear() == releaseYear) {
                        iterator.remove();

                        String response = "{ \"message\": \"Movie deleted successfully\" }";
                        ApiUtils.sendResponse(exchange, 200, response);
                        return;
                    }
                }

                String response = "{ \"message\": \"Movie not found\" }";
                ApiUtils.sendResponse(exchange, 404, response);
            }
            default -> {
                String response = "{ \"error\": \"Method not allowed\" }";
                ApiUtils.sendResponse(exchange, 405, response);
            }
        }
    }

}
