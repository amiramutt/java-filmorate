package ru.yandex.practicum.filmorate.model;


import java.util.Objects;

public class Genre {
    private int id;
    private String name;

    public Genre(int id, String name) {
        this.id = id;
        this.name = name;
    }

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    @Override
    public int hashCode() {
        int hash = 17;
        if (name != null) {
            hash = hash + name.hashCode();
        }
        hash = hash * 31;

        if (id != 0) {
            hash = hash + id;
        }
        hash = hash * 31;

        return hash;
    }
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        Genre otherGenre = (Genre) obj;
        return Objects.equals(id, otherGenre.id);
    }
}