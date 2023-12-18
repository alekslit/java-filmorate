package ru.yandex.practicum.filmorate.storage.dao;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.jdbc.core.JdbcTemplate;
import ru.yandex.practicum.filmorate.model.Genre;

import java.util.List;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

@RequiredArgsConstructor(onConstructor_ = @Autowired)
@JdbcTest
public class GenreDbStorageTest {
    private final JdbcTemplate jdbcTemplate;
    private GenreDbStorage genreStorage;
    private Genre genreForCheck;

    public void init() {
        genreStorage = new GenreDbStorage(jdbcTemplate);
        genreForCheck = Genre.builder()
                .id(3)
                .name("Мультфильм")
                .build();
    }

    @BeforeEach
    public void setUp() {
        init();
    }

    @Test
    public void testGetGenreById() {
        Genre dataBaseGenre = genreStorage.getGenreById(genreForCheck.getId());

        assertThat(genreForCheck)
                .usingRecursiveComparison()
                .isEqualTo(dataBaseGenre);
    }

    @Test
    public void testGetAllGenres() {
        List<Genre> genreList = genreStorage.getAllGenres();

        assertThat(6).isEqualTo(genreList.size());
    }
}
