package at.ac.fhcampuswien.repositories;

import at.ac.fhcampuswien.exceptions.DatabaseException;
import at.ac.fhcampuswien.exceptions.MovieNotFoundException;
import at.ac.fhcampuswien.models.Movie;
import at.ac.fhcampuswien.utils.DatabaseUtil;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class MovieRepository implements IMovieRepository {

    public void add(Movie movie) throws DatabaseException {

        String sql = "INSERT INTO movies (id, title, genre, release_year) VALUES (?, ?, ?, ?);";

        try (
                Connection connection = DatabaseUtil.getConnection();
                PreparedStatement preparedStatement = connection.prepareStatement(sql)
        ) {
            preparedStatement.setObject(1, movie.getId());
            preparedStatement.setString(2, movie.getTitle());
            preparedStatement.setString(3, movie.getGenre());
            preparedStatement.setInt(4, movie.getReleaseYear());

            preparedStatement.executeUpdate();

        } catch (SQLException e) {
            throw new DatabaseException("Failed to add movie", e);
        }
    }

    public List<Movie> findAll() throws DatabaseException {

        List<Movie> movies = new ArrayList<>();

        String sql = "SELECT * from movies;";

        try (
                Connection connection = DatabaseUtil.getConnection();
                PreparedStatement preparedStatement = connection.prepareStatement(sql);
                ResultSet resultSet = preparedStatement.executeQuery()
        ) {

            while (resultSet.next()) {
                Movie movie = new Movie();

                movie.setID((UUID) resultSet.getObject("id"));
                movie.setTitle(resultSet.getString("title"));
                movie.setGenre(resultSet.getString("genre"));
                movie.setReleaseYear(resultSet.getInt("release_year"));

                movies.add(movie);
            }

        } catch (SQLException e) {
            throw new DatabaseException("Failed to retrieve movies", e);
        }

        return movies;
    }

    public boolean delete(Movie movie) throws DatabaseException, MovieNotFoundException {

        String sql = "DELETE FROM movies WHERE title = ? AND genre = ? AND release_year = ?;";

        try (
                Connection connection = DatabaseUtil.getConnection();
                PreparedStatement preparedStatement = connection.prepareStatement(sql)
        ) {

            preparedStatement.setString(1, movie.getTitle());
            preparedStatement.setString(2, movie.getGenre());
            preparedStatement.setInt(3, movie.getReleaseYear());

            int rowsAffected = preparedStatement.executeUpdate();

            if (rowsAffected == 0) {
                throw new MovieNotFoundException("Movie not found for deletion");
            }

            return true;

        } catch (SQLException e) {
            throw new DatabaseException("Failed to delete movie", e);
        }
    }

    public boolean update(Movie movie) throws DatabaseException, MovieNotFoundException {

        String sql = "UPDATE movies SET title = ?, genre = ?, release_year = ? WHERE id = ?;";

        try (
                Connection connection = DatabaseUtil.getConnection();
                PreparedStatement preparedStatement = connection.prepareStatement(sql)
        ) {

            preparedStatement.setString(1, movie.getTitle());
            preparedStatement.setString(2, movie.getGenre());
            preparedStatement.setInt(3, movie.getReleaseYear());
            preparedStatement.setObject(4, movie.getId());

            int rowsAffected = preparedStatement.executeUpdate();

            if (rowsAffected == 0) {
                throw new MovieNotFoundException("Movie not found for update");
            }

            return true;

        } catch (SQLException e) {
            throw new DatabaseException("Failed to update movie", e);
        }
    }

}
