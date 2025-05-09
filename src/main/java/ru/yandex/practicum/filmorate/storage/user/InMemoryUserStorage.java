package ru.yandex.practicum.filmorate.storage.user;

import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.User;

import java.util.*;

@Component("InMemoryUserStorage")
public class InMemoryUserStorage implements UserStorage {
    private final Map<Integer, User> users = new HashMap<>();

    @Override
    public User addUser(User user) {
        user.setId(generateId());

        if (user.getName() == null || user.getName().isBlank()) {
            user.setName(user.getLogin());
        }

        users.put(user.getId(), user);
        return user;
    }

    @Override
    public User updateUser(User user) {
        if (users.containsKey(user.getId())) {
            if (user.getName() == null || user.getName().isBlank()) {
                user.setName(user.getLogin());
            }

            users.put(user.getId(), user);
            return user;
        }

        throw new NotFoundException("Пользователь с ID: " + user.getId() + " не найден");
    }

    @Override
    public User getUserById(int id) throws NotFoundException {
        User user = users.get(id);
        if (user == null) {
            throw new NotFoundException("Пользователь с ID: " + id + " не найден");
        }
        return user;
    }

    @Override
    public ArrayList<User> getAllUsers() {
        return new ArrayList<>(users.values());
    }

    @Override
    public void deleteUser(int id) {
        users.remove(id);
    }

    @Override
    public void addFriend(int userId, int friendId) {
        User user = users.get(userId);
        User friend = users.get(friendId);
        user.getFriends().add(friendId);
        friend.getFriends().add(userId);
    }

    @Override
    public void removeFriend(int userId, int friendId) {
        User user = users.get(userId);
        User friend = users.get(friendId);
        user.getFriends().remove(friendId);
        friend.getFriends().remove(userId);
    }

    @Override
    public void deleteAllUsers(){
        users.clear();
    }

    private int generateId() {
        return users.keySet()
                .stream()
                .mapToInt(id -> id)
                .max()
                .orElse(0) + 1;
    }
}
