package ru.yandex.practicum.filmorate.service.dao;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.model.Mpa;
import ru.yandex.practicum.filmorate.storage.dao.MpaDbStorage;

import java.util.List;

@Service
public class MpaDbService {
    private final MpaDbStorage mpaStorage;

    @Autowired
    public MpaDbService(MpaDbStorage mpaStorage) {
        this.mpaStorage = mpaStorage;
    }

    public Mpa getMpaById(Integer mpaId) {
        return mpaStorage.getMpaById(mpaId);
    }

    public List<Mpa> getAllMpa() {
        return mpaStorage.getAllMpa();
    }
}