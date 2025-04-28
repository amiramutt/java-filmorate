package ru.yandex.practicum.filmorate.test;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import ru.yandex.practicum.filmorate.controller.UserController;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.User;

import java.time.LocalDate;
import java.util.Collection;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class UserControllerTest {

    @Autowired
    private UserController userController;
    private User user;

    @BeforeEach
    void setUp() {
        user = new User(1, "user@email.com", "user", "User", LocalDate.of(1999, 1, 6));
    }

    @Test
    void shouldAdduser() {
        User added = userController.addUser(user);
        assertNotNull(added.getId());
        assertEquals("User", added.getName());
    }

    @Test
    void shouldSetNameToLoginIfNameIsNull() {
        user.setName(null);
        User added = userController.addUser(user);
        assertEquals("user", added.getName());
    }

    @Test
    void shouldSetNameToLoginIfNameIsBlank() {
        user.setName("   ");
        User added = userController.addUser(user);
        assertEquals("user", added.getName());
    }

    @Test
    void shouldUpdateExistingUser() {
        User added = userController.addUser(user);
        added.setName("User Userov");
        User updated = userController.updateUser(added);

        assertEquals("User Userov", updated.getName());
        assertEquals(added.getId(), updated.getId());
    }

    @Test
    void shouldThrowNotFoundWhenUpdatingNonExistentUser() {
        user.setId(999);
        NotFoundException ex = assertThrows(NotFoundException.class,
                () -> userController.updateUser(user));
        assertEquals("Пользователь с ID: 999 не найден", ex.getMessage());
    }

    @Test
    void shouldReturnAllUsers() {
        userController.addUser(user);
        Collection<User> users = userController.getUsers();
        assertFalse(users.isEmpty());
    }
}