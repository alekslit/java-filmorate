package ru.yandex.practicum.filmorate.dao.director;

import ru.yandex.practicum.filmorate.model.Director;

import java.util.List;
import java.util.Optional;

public interface DirectorStorage {
    List<Director> getAllDirectors();
    Optional<Director> getByIdDirector(Integer id);
    Director addDirector(Director director);
    Director updateDirector(Director director);

    void deleteDirector(Integer id);

}
