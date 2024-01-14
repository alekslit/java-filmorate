package ru.yandex.practicum.filmorate.service;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.dao.director.DirectorStorage;
import ru.yandex.practicum.filmorate.model.director.Director;

import java.util.List;

@Service
@AllArgsConstructor
public class DirectorDbService {
    private final DirectorStorage directorStorage;

    public List<Director> getAllDirectors() {
        return directorStorage.getAllDirectors();
    }

    public Director getByIdDirector(Integer id) {
        return directorStorage.getByIdDirector(id);
    }

    public Director addDirector(Director director) {
        return directorStorage.addDirector(director);
    }

    public Director updateDirector(Director director) {
        return directorStorage.updateDirector(director);
    }

    public boolean deleteDirector(Integer id) {
        directorStorage.deleteDirector(id);
        return true;
    }
}