package at.ac.fhcampuswien;

import at.ac.fhcampuswien.controllers.MovieController;
import at.ac.fhcampuswien.models.Movie;
import at.ac.fhcampuswien.models.MovieFactory;
import at.ac.fhcampuswien.repositories.IMovieRepository;
import at.ac.fhcampuswien.repositories.MovieRepository;
import at.ac.fhcampuswien.services.MovieService;
import at.ac.fhcampuswien.utils.DatabaseUtil;
import com.sun.net.httpserver.HttpContext;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.List;

public class Main {
    private final static int SERVER_PORT = 8080;

    public static void main(String[] args) throws IOException {
        // Create an HTTP server listening on defined port
        HttpServer server = HttpServer.create(new InetSocketAddress(SERVER_PORT), 0);

        // Register controllers and their handlers - REST endpoints
        registerController(server, "/api/movies", new MovieController());

        // Start the database
        DatabaseUtil.initializeDatabase();

        // Start the server
        server.setExecutor(null);
        server.start();
        System.out.printf("Server is running on http://localhost:%d", SERVER_PORT);

// befüllt deine Datenbank beim Starten automatisch mit Testdaten, sorgt aber gleichzeitig dafür, dass keine doppelten Filme entstehen.
        try {
            IMovieRepository repo = new MovieRepository();
            MovieService movieService = new MovieService(repo);

           //Überprüfe Dummy-Filme in H2-Datenbank
            List<Movie> dummyMovies = Movie.generateDummyMovies();
            int eingefuegteFilme = 0;

            for (Movie m : dummyMovies) {
                // addMovie prüft über deinen Stream bereits sauber auf Duplikate!
                if (movieService.addMovie(m)) {
                    eingefuegteFilme++;
                }
            }

            if (eingefuegteFilme > 0) {
                System.out.printf("%d neue Dummy-Filme erfolgreich in H2-Datenbank gespeichert!%n", eingefuegteFilme);
            } else {
                System.out.println("Alle 20 Dummy-Filme existieren bereits in der Datenbank.");
            }
        } catch (Exception e) {
            System.err.println("Fehler beim automatischen DB-Befüllen: " + e.getMessage());
        }
        // Testing the Database

        /*
        MovieService movieService = new MovieService(new MovieRepository());
        // Create Movie instances through the factory to keep creation logic centralized.
        Movie grown_ups = MovieFactory.create("Grown Ups", "Comedy", 1990);

        System.out.println("Movies:");
        System.out.println(movieService.getAllMovies());

        movieService.addMovie(MovieFactory.create("Inception", "Sci-Fi", 1990));
        movieService.addMovie(grown_ups);
        System.out.println("Added 2 movies:");
        System.out.println(movieService.getAllMovies());

        System.out.println("Searched for 'row': " + movieService.searchMovies("row", null, null));

        movieService.updateMovie(grown_ups.getId().toString(), "Grown Ups 2", "Comedyyyy", 1990);
        System.out.println("Updated movies: ");
        System.out.println(movieService.getAllMovies());

        Movie grown_ups_2 = movieService.searchMovies("2", null, null).get(0);

        movieService.deleteMovie(grown_ups_2.getTitle(), grown_ups_2.getGenre(), grown_ups_2.getReleaseYear());
        movieService.deleteMovie("Inception", "Sci-Fi", 1990);
        System.out.println("Deleted all movies:");
        System.out.println(movieService.getAllMovies());
        */

    }

    private static void registerController(HttpServer server, String path, HttpHandler handler) {
        HttpContext context = server.createContext(path, handler);
        // Optionally add more configurations to context if needed
    }
}