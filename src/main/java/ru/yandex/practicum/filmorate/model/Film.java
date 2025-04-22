package ru.yandex.practicum.filmorate.model;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import ru.yandex.practicum.filmorate.annotation.ReleaseDate;

import java.time.LocalDate;

@Data
@AllArgsConstructor
public class Film {

    private Long id;

    @NotNull
    @NotBlank(message = "Название фильма не может быть пустым")
    private String name;

    @Size(max = 200, message = "Описание не должно превышать 200 символов")
    private String description;

    @NotNull
    @ReleaseDate
    private LocalDate releaseDate;

    @Positive(message = "Продолжительность фильма не может быть отрицательной")
    private long duration;
}