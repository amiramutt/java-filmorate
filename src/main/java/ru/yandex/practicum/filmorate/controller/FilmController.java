package ru.yandex.practicum.filmorate.controller;

import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.Film;

import java.time.LocalDate;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/films")
@Validated
public class FilmController {
    private long filmId = 0;
    private final Map<Long, Film> films = new HashMap<>();
    private static final LocalDate MIN_RELEASE_DATE = LocalDate.of(1895, 12, 28);

    @GetMapping
    public Collection<Film> getFilms() {
        return films.values();
    }

    @PostMapping
    public Film addFilm(@Valid @RequestBody Film film) {
        if (film.getReleaseDate().isBefore(MIN_RELEASE_DATE)) {
            throw new ValidationException("Год выпуска фильма не может быть раньше 28.12.1895");
        }

        film.setId(++filmId);
        films.put(film.getId(), film);
        log.debug("Фильм {} успешно добавлен", film.getName());
        return film;
    }

    @PutMapping
    public Film updateFilm(@Valid @RequestBody Film film) {
        if (film.getReleaseDate().isBefore(MIN_RELEASE_DATE)) {
            throw new ValidationException("Год выпуска фильма не может быть раньше 28.12.1895");
        }

        if (films.containsKey(film.getId())) {
            films.put(film.getId(), film);
            log.debug("Фильм {} успешно обновлен", film.getName());
            return film;
        }

        log.debug("Фильм с ID {} не найден", film.getId());
        throw new NotFoundException("Фильм с ID:" + film.getId() + " не найден");
    }
}