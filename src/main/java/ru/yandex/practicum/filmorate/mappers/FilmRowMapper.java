package ru.yandex.practicum.filmorate.mappers;

import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Mpa;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashSet;
import java.util.LinkedHashSet;

@Component
public class FilmRowMapper implements RowMapper<Film> {
    @Override
    public Film mapRow(ResultSet resultSet, int rowNum) throws SQLException {
        Mpa rating = null;
        String ratingName = resultSet.getString("rating_name");
        if (ratingName != null) {
            try {
                rating = new Mpa(resultSet.getInt("rating_id"),ratingName);
            } catch (IllegalArgumentException e) {
                throw new NotFoundException("Некорректный рейтинг: " + ratingName);
            }
        }

        return new Film(
                resultSet.getInt("movie_id"),
                resultSet.getString("name"),
                resultSet.getString("description"),
                resultSet.getDate("release_date").toLocalDate(),
                resultSet.getLong("duration"),
                new HashSet<>(),
                rating,
                new LinkedHashSet<>()
        );
    }
}
