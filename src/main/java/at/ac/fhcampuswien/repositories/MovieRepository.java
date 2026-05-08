package at.ac.fhcampuswien.repositories;

import at.ac.fhcampuswien.models.Movie;
import at.ac.fhcampuswien.utils.DatabaseUtil;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class MovieRepository {

    public void add(Movie movie) {

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
            e.printStackTrace();
        }
    }

    public List<Movie> findAll() {

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
            e.printStackTrace();
        }

        return movies;
    }

    public boolean delete(Movie movie) {

        String sql = "DELETE FROM movies WHERE title = ? AND genre = ? AND release_year = ?;";

        try (
                Connection connection = DatabaseUtil.getConnection();
                PreparedStatement preparedStatement = connection.prepareStatement(sql)
                ) {

            preparedStatement.setString(1, movie.getTitle());
            preparedStatement.setString(2, movie.getGenre());
            preparedStatement.setInt(3, movie.getReleaseYear());

            int rowsAffected = preparedStatement.executeUpdate();

            return rowsAffected > 0;

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return false;
    }

    public boolean update(Movie movie) {

        String sql = "UPDATE movies SET title = ?, genre = ?, release_year = ? WHERE id = ?;";

        try (
                Connection connection = DatabaseUtil.getConnection();
                PreparedStatement preparedStatement = connection.prepareStatement(sql);
                ) {

            preparedStatement.setString(1, movie.getTitle());
            preparedStatement.setString(2, movie.getGenre());
            preparedStatement.setInt(3, movie.getReleaseYear());
            preparedStatement.setObject(4, movie.getId());

            int rowsAffected = preparedStatement.executeUpdate();

            return rowsAffected > 0;

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return false;
    }

}
