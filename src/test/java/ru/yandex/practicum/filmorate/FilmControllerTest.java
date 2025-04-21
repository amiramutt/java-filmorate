package ru.yandex.practicum.filmorate;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ru.yandex.practicum.filmorate.controller.FilmController;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.Film;

import java.time.LocalDate;
import java.util.Collection;

import static org.junit.jupiter.api.Assertions.*;

class FilmControllerTest {

    private FilmController filmController;
    private Film film;

    @BeforeEach
    void setUp() {
        filmController = new FilmController();
        film = new Film(1L, "The Notebook", "The Notebook is a 2004 American romantic drama film directed by Nick Cassavetes", LocalDate.of(2004, 5, 20), 124L);
    }

    @Test
    void shouldAddFilm() {
        Film added = filmController.addFilm(film);
        assertNotNull(added.getId());
        assertEquals("The Notebook", added.getName());
    }

    @Test
    void shouldThrowValidationExceptionForTooOldDate() {
        film.setReleaseDate(LocalDate.of(1800, 1, 1));
        ValidationException ex = assertThrows(ValidationException.class,
                () -> filmController.addFilm(film));
        assertEquals("Год выпуска фильма не может быть раньше 28.12.1895", ex.getMessage());
    }

    @Test
    void shouldReturnAllFilms() {
        filmController.addFilm(film);
        Collection<Film> films = filmController.getFilms();
        assertEquals(1, films.size());
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
        film.setId(999L);
        NotFoundException ex = assertThrows(NotFoundException.class,
                () -> filmController.updateFilm(film));
        assertEquals("Фильм с ID:999 не найден", ex.getMessage());
    }
}