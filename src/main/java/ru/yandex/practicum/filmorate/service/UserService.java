package ru.yandex.practicum.filmorate.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.user.UserStorage;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class UserService {

    private final UserStorage userStorage;

    @Autowired
    public UserService(UserStorage userStorage) {
        this.userStorage = userStorage;
    }

    public User addUser(User user) {
        return userStorage.addUser(user);
    }

    public User updateUser(User user) {
        return userStorage.updateUser(user);
    }

    public User getUserById(int id) {
        return userStorage.getUserById(id);
    }

    public ArrayList<User> getAllUsers() {
        return userStorage.getAllUsers();
    }

    public void addFriend(int userId, int friendId) {
        User user = userStorage.getUserById(userId);
        User friend = userStorage.getUserById(friendId);

        user.getFriends().add(friend);
        friend.getFriends().add(user);
    }

    public void removeFriend(int userId, int friendId) {
        User user = userStorage.getUserById(userId);
        User friend = userStorage.getUserById(friendId);
        user.getFriends().remove(friend);
        friend.getFriends().remove(user);
    }

    public Set<User> getFriends(int userId) {
        User user = userStorage.getUserById(userId);
        return user.getFriends();
    }

    public List<User> getCommonFriends(int userId1, int userId2) {
        Set<User> user1Friends = userStorage.getUserById(userId1).getFriends();
        Set<User> user2Friends = userStorage.getUserById(userId2).getFriends();

        return user1Friends.stream()
                .filter(user2Friends::contains)
                .collect(Collectors.toList());
    }
}
