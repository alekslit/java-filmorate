package ru.yandex.practicum.filmorate.dao;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.jdbc.core.JdbcTemplate;
import ru.yandex.practicum.filmorate.model.Mpa;

import java.util.List;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

@RequiredArgsConstructor(onConstructor_ = @Autowired)
@JdbcTest
public class MpaDbStorageTest {
    private final JdbcTemplate jdbcTemplate;
    private MpaDbStorage mpaStorage;
    private Mpa mpaForCheck;

    public void init() {
        mpaStorage = new MpaDbStorage(jdbcTemplate);
        mpaForCheck = Mpa.builder()
                .id(4)
                .name("R")
                .build();
    }

    @BeforeEach
    public void setUp() {
        init();
    }

    @Test
    public void testGetMpaById() {
        Mpa dataBaseMpa = mpaStorage.getMpaById(mpaForCheck.getId());

        assertThat(mpaForCheck)
                .usingRecursiveComparison()
                .isEqualTo(dataBaseMpa);
    }

    @Test
    public void testGetAllMpa() {
        List<Mpa> mpaList = mpaStorage.getAllMpa();

        assertThat(5).isEqualTo(mpaList.size());
    }
}