package ru.yandex.practicum.filmorate.exception;

public class TaskException extends RuntimeException {
    String s;
    String s1;
    String s2;
    public TaskException(String s, String s1, String s2) {
        this.s = s;
        this.s2 = s2;
        this.s1 = s1;
    }
}
