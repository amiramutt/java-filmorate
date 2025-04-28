package ru.yandex.practicum.filmorate.test;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import ru.yandex.practicum.filmorate.controller.FilmController;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.Film;

import java.time.LocalDate;
import java.util.Collection;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class FilmControllerTest {
    private Validator validator;
    @Autowired
    private FilmController filmController;
    private Film film;

    @BeforeEach
    void setUp() {
        film = new Film(1, "The Notebook", "The Notebook is a 2004 American romantic drama film directed by Nick Cassavetes", LocalDate.of(2004, 5, 20), 124L);
        filmController.getFilms().clear();
        ValidatorFactory validatorFactory = Validation.buildDefaultValidatorFactory();
        validator = validatorFactory.getValidator();
    }

    @Test
    void shouldAddFilm() {
        Film added = filmController.addFilm(film);
        assertNotNull(added.getId());
        assertEquals("The Notebook", added.getName());
    }

    @Test
    void shouldNotValidateOldReleaseDate() {
        film.setReleaseDate(LocalDate.of(1800, 1, 1));
        Set<ConstraintViolation<Film>> violations = validator.validate(film);
        assertFalse(violations.isEmpty());
    }

    @Test
    void shouldReturnAllFilms() {
        filmController.addFilm(film);
        Collection<Film> films = filmController.getFilms();
        assertFalse(films.isEmpty());
    }

    @Test
    void shouldUpdateExistingFilm() {
        Film added = filmController.addFilm(film);
        added.setName("The Notebook 2");

        Film updated = filmController.updateFilm(added);
        assertEquals("The Notebook 2", updated.getName());
    }

    @Test
    void shouldThrowNotFoundExceptionWhenUpdatingNonExistentFilm() {
        film.setId(999);
        NotFoundException ex = assertThrows(NotFoundException.class,
                () -> filmController.updateFilm(film));
        assertEquals("Фильм с ID: 999 не найден", ex.getMessage());
    }
}