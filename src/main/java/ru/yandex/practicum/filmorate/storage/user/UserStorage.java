package ru.yandex.practicum.filmorate.storage.user;

import ru.yandex.practicum.filmorate.model.User;

import java.util.ArrayList;

public interface UserStorage {
    User addUser(User user);

    User updateUser(User user);

    User getUserById(int id);

    ArrayList<User> getAllUsers();

    void deleteUser(int id);
}