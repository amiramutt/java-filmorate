package ru.yandex.practicum.filmorate.controller;

import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.service.FilmService;

import java.util.Collection;
import java.util.List;

@Slf4j
@RestController
@RequestMapping("/films")
@Validated
public class FilmController {
    private final FilmService filmService;

    @Autowired
    public FilmController(FilmService filmService) {
        this.filmService = filmService;
    }

    @GetMapping
    public Collection<Film> getFilms() {
        return filmService.getAllFilms();
    }

    @PostMapping
    public Film addFilm(@Validated @RequestBody Film film) {
        return filmService.addFilm(film);
    }

    @PutMapping
    public Film updateFilm(@Valid @RequestBody Film film) {
        return filmService.updateFilm(film);
    }

    @PutMapping("/{id}/like/{userId}")
    private void addLike(@PathVariable int id, @PathVariable int userId) {
        filmService.addLike(id, userId);
    }

    @DeleteMapping("/{id}/like/{userId}")
    private void removeLike(@PathVariable int id, @PathVariable int userId) {
        filmService.removeLike(id, userId);
    }

    @GetMapping("/popular")
    private List<Film> getPopularFilms(@RequestParam(defaultValue = "10") int count) {
        return filmService.getPopularFilms(count);
    }

    @GetMapping("/{id}")
    private Film getFilmById(@PathVariable int id) {
        log.info("=== START getFilmById(" + id + ") ===");
        log.info("FilmService instance: " + filmService.getClass().getName());
        //return filmService.getFilmById(id);
        try {
            Film film = filmService.getFilmById(id);
            log.info("Found film: " + film);
            return film;
        } catch (Exception e) {
            log.error("ERROR in getFilmById: ", e);
            throw e; // Re-throw to preserve original behavior
        }
    }

    @GetMapping("/check-di")
    public String checkDependencyInjection() {
        return "FilmService is " + (filmService != null ? "INJECTED" : "NULL");
    }
}