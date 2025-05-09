package ru.yandex.practicum.filmorate.storage.genre;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.Genre;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

@Component
public class GenreDbStorage implements GenreStorage {

    private final JdbcTemplate jdbc;

    @Autowired
    public GenreDbStorage(JdbcTemplate jdbcTemplate) {
        this.jdbc = jdbcTemplate;
    }

    @Override
    public List<Genre> getAllGenres() {
        String sql = "SELECT genre_id, name FROM genres ORDER BY genre_id";
        return jdbc.query(sql, this::mapRowToGenre);
    }

    @Override
    public Genre getGenreById(int id) {
        String sql = "SELECT genre_id, name FROM genres WHERE genre_id = ?";
        try {
            Genre genre = jdbc.queryForObject(sql, this::mapRowToGenre, id);
            return genre;
        } catch (EmptyResultDataAccessException e) {
            throw new NotFoundException("Жанр с ID: " + id + " не найден");
        }
    }

    private Genre mapRowToGenre(ResultSet rs, int rowNum) throws SQLException {
        return new Genre(rs.getInt("genre_id"), rs.getString("name"));
    }
}

