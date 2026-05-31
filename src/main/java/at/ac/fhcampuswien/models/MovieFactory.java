package at.ac.fhcampuswien.models;

// Creational pattern: Factory
// This class centralizes Movie object creation so the rest of the application
// does not instantiate Movie directly with "new" everywhere.
public final class MovieFactory {

    private MovieFactory() {
        // Private constructor prevents instantiation of the factory class.
    }

    // Static factory method for creating Movie instances.
    // This is the concrete implementation of the creational design pattern.
    public static Movie create(String title, String genre, int releaseYear) {
        return new Movie(title, genre, releaseYear);
    }
}
