package ru.yandex.practicum.filmorate.converters;

import org.jetbrains.annotations.NotNull;
import org.springframework.core.convert.converter.Converter;
import ru.yandex.practicum.filmorate.model.SortBy;

public class SortByConverter implements Converter<String, SortBy> {
    @Override
    public SortBy convert(@NotNull String source) {
        for (SortBy val : SortBy.values()) {
            if (val.getValue().equalsIgnoreCase(source)) {
                return val;
            }
        }
        throw new IllegalArgumentException("Invalid 'by' parameter. Supported values are 'director', " +
                "'title', 'director,title' and 'title,director'");
    }
}
