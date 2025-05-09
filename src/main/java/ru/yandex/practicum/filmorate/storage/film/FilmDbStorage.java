package ru.yandex.practicum.filmorate.storage.film;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.mappers.FilmRowMapper;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.model.Mpa;

import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.Statement;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Component("FilmDbStorage")
public class FilmDbStorage implements FilmStorage {

    private final JdbcTemplate jdbc;
    private final FilmRowMapper mapper;

    @Autowired
    public FilmDbStorage(JdbcTemplate jdbc, FilmRowMapper mapper) {
        this.jdbc = jdbc;
        this.mapper = mapper;
    }

    @Override
    public Film addFilm(Film film) {
        String query = """
        INSERT INTO movies (name, description, release_date, duration, rating_id)
        VALUES (?, ?, ?, ?, ?)
    """;

        KeyHolder keyHolder = new GeneratedKeyHolder();

        jdbc.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(query, Statement.RETURN_GENERATED_KEYS);
            ps.setString(1, film.getName());
            ps.setString(2, film.getDescription());
            ps.setDate(3, Date.valueOf(film.getReleaseDate()));
            ps.setLong(4, film.getDuration());
            ps.setObject(5, getRatingIdIfExists(film.getMpa()));
            return ps;
        }, keyHolder);

        Number key = keyHolder.getKey();
        if (key == null) {
            throw new DataAccessException("Не удалось получить id фильма после вставки") {};
        }
        int filmId = key.intValue();

        String genreQuery = "INSERT INTO movie_genre (movie_id, genre_id) VALUES (?, ?)";

        Set<Genre> sortedGenres = film.getGenres().stream()
                .sorted(Comparator.comparing(Genre::getId))
                .collect(Collectors.toCollection(LinkedHashSet::new));

        for (Genre genre : sortedGenres) {
            jdbc.update(genreQuery, filmId, getGenreIdIfExists(genre));
        }
        film.setId(filmId);
        film.setGenres(sortedGenres);
        return film;
    }

    @Override
    public Film updateFilm(Film film) {
        String checkSql = "SELECT COUNT(*) FROM movies WHERE movie_id = ?";
        Integer count = jdbc.queryForObject(checkSql, Integer.class, film.getId());
        if (count == null || count == 0) {
            throw new NotFoundException("Фильм с ID: " + film.getId() + " не найден");
        }

        Integer ratingId = getRatingIdIfExists(film.getMpa());

        String updateSql = """
        UPDATE movies
        SET name = ?, description = ?, release_date = ?, duration = ?, rating_id = ?
        WHERE movie_id = ?
    """;

        jdbc.update(updateSql,
                film.getName(),
                film.getDescription(),
                film.getReleaseDate(),
                film.getDuration(),
                ratingId,
                film.getId()
        );

        String deleteGenresSql = "DELETE FROM movie_genre WHERE movie_id = ?";
        jdbc.update(deleteGenresSql, film.getId());

        insertGenres(film.getId(), film.getGenres());

        return getFilmById(film.getId());
    }

    @Override
    public Film getFilmById(int id) {
        String query = """
            SELECT
                m.movie_id,
                m.name,
                m.description,
                m.release_date,
                m.duration,
                r.rating_id,
                r.name AS rating_name
            FROM movies m
            LEFT JOIN rating r ON m.rating_id = r.rating_id
            WHERE m.movie_id = ?
        """;
        Film film = jdbc.queryForObject(query, mapper, id);

        String genreSql = """
            SELECT g.genre_id, g.name
            FROM movie_genre mg
            JOIN genres g ON mg.genre_id = g.genre_id
            WHERE mg.movie_id = ?
            ORDER by g.genre_id
        """;

        jdbc.query(genreSql, (rs) -> {
            if (film != null) {
                try {
                    Genre genre = new Genre(rs.getInt("genre_id"), rs.getString("name"));
                    film.getGenres().add(genre);
                } catch (IllegalArgumentException e) {
                    throw new NotFoundException("Такого жанра не существует");
                }
            } else {
                throw new NotFoundException("Фильм с ID: " + id + " не найден");
            }
        }, id);

        String likeSql = "SELECT user_id FROM likes WHERE movie_id = ?";
        jdbc.query(likeSql, (rs) -> {
            if (film != null) {
                film.getLikes().add(rs.getInt("user_id"));
            }
        }, id);

        return film;
    }

    @Override
    public ArrayList<Film> getAllFilms() {
        String query = """
            SELECT
                m.movie_id,
                m.name,
                m.description,
                m.release_date,
                m.duration,
                r.rating_id,
                r.name AS rating_name
            FROM movies m
            LEFT JOIN rating r ON m.rating_id = r.rating_id
        """;
        ArrayList<Film> films = new ArrayList<>(jdbc.query(query, mapper));

        Map<Integer, Film> filmMap = films.stream()
                .collect(Collectors.toMap(Film::getId, Function.identity()));

        String genreSql = """
            SELECT mg.movie_id, g.genre_id, g.name
            FROM movie_genre mg
            JOIN genres g ON mg.genre_id = g.genre_id
            ORDER BY g.genre_id
        """;

        jdbc.query(genreSql, (rs) -> {
            int movieId = rs.getInt("movie_id");
            Film film = filmMap.get(movieId);
            if (film != null) {
                try {
                    Genre genre = new Genre(rs.getInt("genre_id"), rs.getString("name"));
                    film.getGenres().add(genre);
                } catch (IllegalArgumentException e) {
                    throw new NotFoundException("Такого жанра не существует");
                }
            }
        });

        String likeSql = "SELECT movie_id, user_id FROM likes";
        jdbc.query(likeSql, (rs) -> {
            int movieId = rs.getInt("movie_id");
            Film film = filmMap.get(movieId);
            if (film != null) {
                film.getLikes().add(rs.getInt("user_id"));
            }
        });

        return films;
    }

    @Override
    public void deleteFilm(int id) {
        String deleteLikes = "DELETE FROM likes WHERE movie_id = ?";
        jdbc.update(deleteLikes, id);

        String deleteGenres = "DELETE FROM movie_genre WHERE movie_id = ?";
        jdbc.update(deleteGenres, id);

        String deleteMovie = "DELETE FROM movies WHERE movie_id = ?";
        int rowsAffected = jdbc.update(deleteMovie, id);

        if (rowsAffected == 0) {
            throw new NotFoundException("Фильм с ID: " + id + " не найден");
        }
    }

    @Override
    public void deleteAllFilms() {
        jdbc.update("DELETE FROM likes");
        jdbc.update("DELETE FROM movie_genre");
        jdbc.update("DELETE FROM movies");
    }

    @Override
    public void addLike(int filmId, int userId) {
        String sql = "INSERT INTO likes (movie_id, user_id) VALUES (?, ?)";
        jdbc.update(sql, filmId, userId);
    }

    @Override
    public void removeLike(int filmId, int userId) {
        String sql = "DELETE FROM likes WHERE movie_id = ? AND user_id = ?";
        jdbc.update(sql, filmId, userId);
    }

    @Override
    public List<Film> getPopularFilms(int count) {
        String sql = """
        SELECT m.movie_id, m.name, m.description, m.release_date, m.duration, r.rating_id, r.name AS rating_name,
               COUNT(l.user_id) AS likes_count
        FROM movies m
        LEFT JOIN rating r ON m.rating_id = r.rating_id
        LEFT JOIN likes l ON m.movie_id = l.movie_id
        GROUP BY m.movie_id, r.rating_id, r.name
        ORDER BY likes_count DESC
        LIMIT ?
    """;

        List<Film> films = jdbc.query(sql, mapper, count);

        Map<Integer, Film> filmMap = films.stream()
                .collect(Collectors.toMap(Film::getId, Function.identity()));

        String genreSql = "SELECT mg.movie_id, g.genre_id, g.name FROM movie_genre mg JOIN genres g ON mg.genre_id = g.genre_id ORDER BY g.genre_id";
        jdbc.query(genreSql, (rs) -> {
            int movieId = rs.getInt("movie_id");
            Film film = filmMap.get(movieId);
            if (film != null) {
                Genre genre = new Genre(rs.getInt("genre_id"), rs.getString("name"));
                film.getGenres().add(genre);
            }
        });

        String likeSql = "SELECT movie_id, user_id FROM likes";
        jdbc.query(likeSql, (rs) -> {
            int movieId = rs.getInt("movie_id");
            Film film = filmMap.get(movieId);
            if (film != null) {
                film.getLikes().add(rs.getInt("user_id"));
            }
        });

        return films;
    }

    private Integer getGenreIdIfExists(Genre genre) {
        if (genre == null) return null;

        try {
            String sqlById = "SELECT genre_id FROM genres WHERE genre_id = ?";
            return jdbc.queryForObject(sqlById, Integer.class, genre.getId());
        } catch (EmptyResultDataAccessException e) {
            try {
                String sqlByName = "SELECT genre_id FROM genres WHERE name = ?";
                return jdbc.queryForObject(sqlByName, Integer.class, genre.getName());
            } catch (EmptyResultDataAccessException ex) {
                throw new NotFoundException("Жанр '" + genre.getName() + "' не найден");
            }
        }
    }

    private Integer getRatingIdIfExists(Mpa mpa) {
        if (mpa == null) return null;
        try {
            String sqlById = "SELECT rating_id FROM rating WHERE rating_id = ?";
            return jdbc.queryForObject(sqlById, Integer.class, mpa.getId());
        } catch (EmptyResultDataAccessException e) {
            try {
                String sqlByName = "SELECT rating_id FROM rating WHERE name = ?";
                return jdbc.queryForObject(sqlByName, Integer.class, mpa.getName());
            } catch (EmptyResultDataAccessException ex) {
                throw new NotFoundException("Рейтинг '" + mpa.getName() + "' не найден");
            }
        }
    }

    private void insertGenres(int movieId, Set<Genre> genres) {
        if (genres == null || genres.isEmpty()) return;

        String sql = "INSERT INTO movie_genre (movie_id, genre_id) VALUES (?, ?)";
        List<Object[]> batchArgs = genres.stream()
                .sorted(Comparator.comparing(Genre::getId))
                .map(genre -> new Object[]{movieId, genre.getId()})
                .collect(Collectors.toList());

        jdbc.batchUpdate(sql, batchArgs);
    }

}
