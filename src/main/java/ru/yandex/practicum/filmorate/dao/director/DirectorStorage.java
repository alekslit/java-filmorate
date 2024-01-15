package ru.yandex.practicum.filmorate.dao.director;

import ru.yandex.practicum.filmorate.model.director.Director;

import java.util.List;

public interface DirectorStorage {
    List<Director> getAllDirectors();

    Director getByIdDirector(Integer id);

    Director addDirector(Director director);

    Director updateDirector(Director director);

    void deleteDirector(Integer id);
}
