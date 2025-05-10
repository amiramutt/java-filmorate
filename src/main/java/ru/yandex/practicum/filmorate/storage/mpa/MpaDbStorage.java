package ru.yandex.practicum.filmorate.storage.mpa;

import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.Mpa;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

@Component
public class MpaDbStorage implements MpaStorage {

    private final JdbcTemplate jdbc;

    public MpaDbStorage(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    @Override
    public List<Mpa> getAllMpa() {
        String sql = "SELECT rating_id, name FROM rating";
        return jdbc.query(sql, this::mapRowToMpa);
    }

    @Override
    public Mpa getMpaById(int id) {
        String sql = "SELECT rating_id, name FROM rating WHERE rating_id = ?";
        try {
            Mpa mpa = jdbc.queryForObject(sql, this::mapRowToMpa, id);
            return mpa;
        } catch (EmptyResultDataAccessException e) {
            throw new NotFoundException("Рейтинг с ID: " + id + " не найден");
        }
    }

    private Mpa mapRowToMpa(ResultSet rs, int rowNum) throws SQLException {
        return new Mpa(rs.getInt("rating_id"), rs.getString("name"));
    }
}

