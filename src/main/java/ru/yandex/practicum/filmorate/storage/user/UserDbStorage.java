package ru.yandex.practicum.filmorate.storage.user;

import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.User;

import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

@Component
public class UserDbStorage implements UserStorage {

    private final JdbcTemplate jdbc;

    public UserDbStorage(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    @Override
    public User addUser(User user) {
        String sql = """
            INSERT INTO users (login, name, birthday, email)
            VALUES (?, ?, ?, ?)
        """;

        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbc.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            ps.setString(1, user.getLogin());
            ps.setString(2, user.getName());
            ps.setDate(3, Date.valueOf(user.getBirthday()));
            ps.setString(4, user.getEmail());
            return ps;
        }, keyHolder);

        user.setId(keyHolder.getKey().intValue());
        return user;
    }

    @Override
    public User updateUser(User user) {
        String sql = """
            UPDATE users
            SET login = ?, name = ?, birthday = ?, email = ?
            WHERE user_id = ?
        """;
        int updated = jdbc.update(sql,
                user.getLogin(),
                user.getName(),
                Date.valueOf(user.getBirthday()),
                user.getEmail(),
                user.getId());

        if (updated == 0) {
            throw new NotFoundException("Пользователь с id=" + user.getId() + " не найден");
        }
        return user;
    }

    @Override
    public User getUserById(int id) {
        String sql = "SELECT * FROM users WHERE user_id = ?";
        try {
            User user = jdbc.queryForObject(sql, userRowMapper(), id);

            for (User friend : getFriends(id)) {
                user.getFriends().add(friend.getId());
            }
            return user;
        } catch (EmptyResultDataAccessException e) {
            throw new NotFoundException("Пользователь с id=" + id + " не найден");
        }
    }

    @Override
    public ArrayList<User> getAllUsers() {
        String sql = "SELECT * FROM users";
        return new ArrayList<>(jdbc.query(sql, userRowMapper()));
    }

    @Override
    public void deleteUser(int id) {
        jdbc.update("DELETE FROM friendship WHERE following_user_id = ?", id);
        jdbc.update("DELETE FROM friendship WHERE followed_user_id = ?", id);
        jdbc.update("DELETE FROM users WHERE user_id = ?", id);
    }

    @Override
    public void deleteAllUsers() {
        jdbc.update("DELETE FROM friendship");
        jdbc.update("DELETE FROM users");
    }

    public void addFriend(int userId, int friendId) {
        String checkSql = """
        SELECT status FROM friendship
        WHERE following_user_id = ? AND followed_user_id = ?
    """;

        try {
            String status = jdbc.queryForObject(checkSql, String.class, friendId, userId);

            if ("PENDING".equals(status)) {
                String updateSql = """
                UPDATE friendship
                SET status = 'CONFIRMED'
                WHERE following_user_id = ? AND followed_user_id = ?
            """;
                jdbc.update(updateSql, friendId, userId);

                String insertMirror = """
                INSERT INTO friendship (following_user_id, followed_user_id, status)
                VALUES (?, ?, 'CONFIRMED')
            """;
                jdbc.update(insertMirror, userId, friendId);
            }

        } catch (EmptyResultDataAccessException e) {
            String insertSql = """
            INSERT INTO friendship (following_user_id, followed_user_id, status)
            VALUES (?, ?, 'PENDING')
        """;
            jdbc.update(insertSql, userId, friendId);
        }
    }

    public void removeFriend(int userId, int friendId) {
        jdbc.update("DELETE FROM friendship WHERE following_user_id = ? AND followed_user_id = ?", userId, friendId);

        String updateSql = """
                UPDATE friendship
                SET status = 'CONFIRMED'
                WHERE following_user_id = ? AND followed_user_id = ?
            """;
        jdbc.update(updateSql, friendId, userId);
    }

    public Set<User> getFriends(int userId) {
        String sql = """
        SELECT f.followed_user_id AS user_id,
        u.NAME,
        u.LOGIN,
        u.EMAIL,
        u.BIRTHDAY
        FROM friendship f
        LEFT JOIN users u on u.user_id = f.followed_user_id
        WHERE f.following_user_id = ?
        """;
        return new HashSet<>(jdbc.query(sql, userRowMapper(), userId));
    }

    private RowMapper<User> userRowMapper() {
        return (rs, rowNum) -> new User(
                rs.getInt("user_id"),
                rs.getString("login"),
                rs.getString("email"),
                rs.getString("name"),
                rs.getDate("birthday").toLocalDate()
        );
    }
}

