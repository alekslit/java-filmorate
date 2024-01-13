package ru.yandex.practicum.filmorate.controller;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.model.director.Director;
import ru.yandex.practicum.filmorate.service.DirectorDbService;

import javax.validation.Valid;
import java.util.List;
import java.util.Optional;

@RestController
@AllArgsConstructor
@Slf4j
@RequestMapping("/directors")
public class DirectorController {
    private final DirectorDbService directorDbService;

    @GetMapping
    public List<Director> getAllDirectors() {
        return directorDbService.getAllDirectors();
    }

    @GetMapping("/{id}")
    public Director getDirectorById(@PathVariable Integer id) {
        return directorDbService.getByIdDirector(id);
    }

    @PostMapping
    public Director addDirector(@Valid @RequestBody Director director) {
        return directorDbService.addDirector(director);
    }

    @PutMapping
    public Director updateDirector(@Valid @RequestBody Director director) {
        return directorDbService.updateDirector(director);
    }

    @DeleteMapping("/{id}")
    public boolean deleteDirector(@PathVariable Integer id) {
        return directorDbService.deleteDirector(id);
    }
}