package at.ac.fhcampuswien.models;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.UUID;

public class Movie {

    private UUID id;
    private String title;
    private String genre;
    private int releaseYear;

    public Movie() {

        id = UUID.randomUUID();

    }

    public Movie(String title, String genre, int releaseYear) {

        this();
        this.title = title;
        this.genre = genre;
        this.releaseYear = releaseYear;

    }
        //method to generate movies
    public static List<Movie> generateDummyMovies() {
        List<Movie> movies = new ArrayList<>();
        String[] titles = {"Attack of the killer toasters", "The avocado that saved Christmas", "Aliens vs. Grandmas",
                "Harry Potter and the midlife crisis", "The cat that new too much",
                "Escape from Monday", "The great coffee", "Dude,who took the Wi-Fi password?",
                "Pineapple Pizza: a love story","Just one more episode","The last Donut", "Galactic traffic jam",
                "Matrix reloaded...again", "The data wars","A web page story", "Breaking algorithms",
                "Bazinga", "The final exam", "2AM study session","Major decisions, minor regrets"};

        String[] genres = {"Action", "Drama", "Sci-Fi", "Comedy",
                "Thriller", "Horror", "Fantasy", "Adventure", "Romance", "Anime"};

        for (int i = 0; i < titles.length; i++) {
            movies.add(new Movie(titles[i], genres[i / 2], new Random().nextInt(1960, 2026)));
        }

        return movies;
    }

    @Override
    public String toString() {
        return "Movie{id=" + id + ", title='" + title + "', genre='" + genre + "', releaseYear=" + releaseYear + "}";
    }

    // Getters
    public UUID getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public String getGenre() {
        return genre;
    }

    public int getReleaseYear() {
        return releaseYear;
    }

    // Setters
    public void setID(UUID id) {
        this.id = id;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public void setGenre(String genre) {
        this.genre = genre;
    }

    public void setReleaseYear(int releaseYear) {
        this.releaseYear = releaseYear;
    }

}
