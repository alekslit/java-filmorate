package ru.yandex.practicum.filmorate.converters;

import org.springframework.core.convert.converter.Converter;
import ru.yandex.practicum.filmorate.model.SortBy;

public class SortByConverter implements Converter<String, SortBy> {
    @Override
    public SortBy convert(String source) {
        if (source.equalsIgnoreCase("director")) {
            return SortBy.DIRECTOR;
        } else if (source.equalsIgnoreCase("title")) {
            return SortBy.TITLE;
        } else if (source.equalsIgnoreCase("director,title")) {
            return SortBy.DIRECTOR_TITLE;
        } else if (source.equalsIgnoreCase("title,director")) {
            return SortBy.TITLE_DIRECTOR;
        } else {
            throw new IllegalArgumentException("Invalid 'by' parameter. Supported values are 'director', 'title', 'director,title' and 'title,director'");
        }
    }
}
