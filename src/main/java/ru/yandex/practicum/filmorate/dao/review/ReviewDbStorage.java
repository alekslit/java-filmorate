package ru.yandex.practicum.filmorate.dao.review;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.simple.SimpleJdbcInsert;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.exception.AlreadyExistException;
import ru.yandex.practicum.filmorate.exception.IllegalIdException;
import ru.yandex.practicum.filmorate.exception.IncorrectRequestParameterException;
import ru.yandex.practicum.filmorate.model.Review;
import ru.yandex.practicum.filmorate.model.event.EventOperation;
import ru.yandex.practicum.filmorate.model.event.EventType;
import ru.yandex.practicum.filmorate.utility.Events;

import java.util.List;
import java.util.Map;

import static ru.yandex.practicum.filmorate.exception.IllegalIdException.*;
import static ru.yandex.practicum.filmorate.query.SqlQuery.*;

@Slf4j
@Repository
public class ReviewDbStorage implements ReviewStorage {
    private final JdbcTemplate jdbcTemplate;

    @Autowired
    public ReviewDbStorage(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    /*------Основные методы------*/
    @Override
    public Review findReviewById(Long reviewId) {
        if (checkIfReviewExists(reviewId)) {
            Review review = jdbcTemplate.queryForObject(SQL_QUERY_FIND_REVIEW_BY_ID, getReviewMapper(), reviewId);
            return review;
        } else {
            log.error("Отзыв с id {} еще не добавлен.", reviewId);
            throw new IllegalIdException(ILLEGAL_REVIEW_ID_MESSAGE + reviewId, ILLEGAL_REVIEW_ID_ADVICE);
        }
    }

    @Override
    public Review addReview(Review review) {
        checkParameters(review);
        if (!checkIfReviewExists(review.getReviewId()) && checkIfUserExists(review.getUserId())
                && checkIfFilmExists(review.getFilmId())) {
            SimpleJdbcInsert insertReview = new SimpleJdbcInsert(jdbcTemplate)
                    .withTableName("reviews")
                    .usingGeneratedKeyColumns("review_id");
            Long reviewId = insertReview.executeAndReturnKey(reviewToMap(review)).longValue();
            try {
                review.setReviewId(reviewId);
                // добавляем Event в БД:
                Events.addEvent(jdbcTemplate, EventType.REVIEW, EventOperation.ADD, review.getUserId(), reviewId);
            } catch (NullPointerException e) {
                throw new RuntimeException("null");
            }
            return review;
        } else {
            throw new IllegalIdException(ILLEGAL_NEW_REVIEW_ID_MESSAGE, ILLEGAL_NEW_REVIEW_ID_ADVICE);
        }
    }

    @Override
    public Review updateReviewById(Review review) {
        checkParameters(review);
        if (checkIfReviewExists(review.getReviewId()) && checkIfUserExists(review.getUserId())
                && checkIfFilmExists(review.getFilmId())) {
            jdbcTemplate.update(SQL_QUERY_UPDATE_REVIEW_BY_ID, review.getContent(),
                    review.getIsPositive(), review.getReviewId());
            // добавляем Event в БД:
            Events.addEvent(jdbcTemplate, EventType.REVIEW, EventOperation.UPDATE,
                    findReviewById(review.getReviewId()).getUserId(), review.getReviewId());

        } else if (!checkIfReviewExists(review.getReviewId()) && checkIfUserExists(review.getUserId())
                && checkIfFilmExists(review.getFilmId())) {
            addReview(review);
            // добавляем Event в БД:
            Events.addEvent(jdbcTemplate, EventType.REVIEW, EventOperation.ADD,
                    review.getUserId(), review.getReviewId());

        } else if (!checkIfUserExists(review.getUserId()) || !checkIfFilmExists(review.getFilmId())) {
            log.debug("{}: " + ILLEGAL_USER_ID_MESSAGE + review.getUserId() +
                            " / " + ILLEGAL_FILM_ID_MESSAGE + review.getFilmId(),
                    IllegalIdException.class.getSimpleName());
            throw new IllegalIdException(ILLEGAL_USER_ID_MESSAGE + review.getUserId() +
                    " / " + ILLEGAL_FILM_ID_MESSAGE + review.getFilmId(), ILLEGAL_OBJECTS_ID_ADVICE);
        }

        return jdbcTemplate.queryForObject("SELECT * " +
                                           "FROM REVIEWS " +
                                           "WHERE review_id = ?;", getReviewMapper(), review.getReviewId());
    }

    @Override
    public String deleteReviewById(Long reviewId) {
        // получаем объект Review перед удалением, чтобы перенести его данные в объект Event:
        Review review = findReviewById(reviewId);
        jdbcTemplate.update("DELETE FROM REVIEWS " +
                            "WHERE review_id = ?;", reviewId);
        // добавляем Event в БД:
        Events.addEvent(jdbcTemplate, EventType.REVIEW, EventOperation.REMOVE, review.getUserId(), reviewId);
        return "Review deleted";
    }

    public List<Review> findAllReviews(Long filmId, Integer count) {
        if (filmId != null) {
            if (checkIfFilmExists(filmId)) {
                List<Review> reviews = jdbcTemplate.query(SQL_QUERY_FIND_ALL_REVIEWS_BY_ID, getReviewMapper(), filmId, count);
                return reviews;
            } else {
                log.debug("{}: " + ILLEGAL_FILM_ID_MESSAGE + filmId, IllegalIdException.class.getSimpleName());
                throw new IllegalIdException(ILLEGAL_FILM_ID_MESSAGE + filmId, ILLEGAL_FILM_ID_ADVICE);
            }
        }

        List<Review> reviews = jdbcTemplate.query(SQL_QUERY_FIND_ALL_REVIEWS, getReviewMapper(), count);
        return reviews;
    }

    public String likeReview(Long reviewId, Long userId) {
        if (!checkIfLikeOrDislikeAdded(reviewId, userId, true)) {
            jdbcTemplate.update(SQL_QUERY_LIKE_OR_DISLIKE_REVIEW, reviewId, userId, true);
            updateUsefulProperty(reviewId, 1);
            return "Лайк добавлен";
        }

        log.debug("{}: " + "Лайк уже добавлен", AlreadyExistException.class.getSimpleName());
        throw new AlreadyExistException("Лайк уже добавлен", "Проверьте введенные данные");
    }

    @Override
    public String dislikeReview(Long reviewId, Long userId) {
        if (!checkIfLikeOrDislikeAdded(reviewId, userId, false)) {
            jdbcTemplate.update(SQL_QUERY_LIKE_OR_DISLIKE_REVIEW, reviewId, userId, false);
            updateUsefulProperty(reviewId, -1);
            return "Дизлайк добавлен";
        }

        log.debug("{}: " + "Дизлайк уже добавлен", AlreadyExistException.class.getSimpleName());
        throw new AlreadyExistException("Дизлайк уже добавлен", "Проверьте введенные данные");
    }

    @Override
    public String removeLikeFromReview(Long reviewId, Long userId) {
        if (checkIfLikeOrDislikeAdded(reviewId, userId, true)) {
            jdbcTemplate.update(SQL_QUERY_REMOVE_LIKE_OR_DISLIKE_FROM_REVIEW, reviewId, userId, true);
            updateUsefulProperty(reviewId, -1);
            return "Лайк удален";
        }

        log.debug("{}: " + "Лайк не был добавлен", IllegalIdException.class.getSimpleName());
        throw new IllegalIdException("Лайк не был добавлен", "Проверьте введенные данные");
    }

    @Override
    public String removeDislikeFromReview(Long reviewId, Long userId) {
        if (checkIfLikeOrDislikeAdded(reviewId, userId, false)) {
            jdbcTemplate.update(SQL_QUERY_REMOVE_LIKE_OR_DISLIKE_FROM_REVIEW, reviewId, userId, false);
            updateUsefulProperty(reviewId, 1);
            return "Дизлайк удален";
        }

        log.debug("{}: " + "Дизлайк не был добавлен", IllegalIdException.class.getSimpleName());
        throw new IllegalIdException("Дизлайк не был добавлен", "Проверьте введенные данные");
    }

    /*------Вспомогательные методы------*/
    private void checkParameters(Review review) {
        if (review.getUserId() == null || review.getFilmId() == null || review.getContent() == null
                || review.getIsPositive() == null) {
            log.debug("{}: " + "Некорректные данные отзыва.", IncorrectRequestParameterException.class.getSimpleName());
            throw new IncorrectRequestParameterException("Некорректные данные отзыва.", "Проверьте данные отзыва.");
        }
    }

    private void updateUsefulProperty(Long reviewId, Integer count) {
        Review review = findReviewById(reviewId);
        jdbcTemplate.update("UPDATE reviews SET useful = ? " +
                            "WHERE review_id = ?;", review.getUseful() + count, reviewId);
    }

    private boolean checkIfLikeOrDislikeAdded(Long reviewId, Long userId, Boolean isLike) {
        Integer count = jdbcTemplate.queryForObject("SELECT COUNT(*) " +
                                                    "FROM review_likes " +
                                                    "WHERE review_id = ? " +
                                                      "AND user_id = ? " +
                                                      "AND is_like = ?;", Integer.class, reviewId, userId, isLike);
        if (count == 0) {
            return false;
        }
        return true;
    }

    private boolean checkIfFilmExists(Long id) {
        Integer result = jdbcTemplate.queryForObject("SELECT COUNT(*) " +
                                                     "FROM FILMS " +
                                                     "WHERE film_id = ?;", Integer.class, id);
        if (result == 0) {
            log.error("фильм с id {} еще не добавлен.", id);
            return false;
        }
        return true;
    }

    private boolean checkIfUserExists(Long id) {
        Integer result = jdbcTemplate.queryForObject("SELECT COUNT(*) " +
                                                     "FROM USERS " +
                                                     "WHERE user_id = ?", Integer.class, id);
        if (result == 0) {
            log.error("Пользователя с таким id {} нет", id);
            return false;
        }
        return true;
    }

    private Map<String, Object> reviewToMap(Review review) {
        return Map.of(
                "user_id", review.getUserId(),
                "film_id", review.getFilmId(),
                "content", review.getContent(),
                "is_positive", review.getIsPositive(),
                "useful", 0
        );
    }

    private boolean checkIfReviewExists(Long reviewId) {
        Integer result = jdbcTemplate.queryForObject("SELECT COUNT(*) " +
                                                     "FROM REVIEWS " +
                                                     "WHERE review_id = ?;", Integer.class, reviewId);
        if (result == 0) {
            log.debug("Ревью с таким id {} нет", reviewId);
            return false;
        }
        log.debug("Ревью с таким id {} есть", reviewId);
        return true;
    }

    public RowMapper<Review> getReviewMapper() {
        return (rs, rowNum) -> Review.builder()
                .reviewId(rs.getLong("review_id"))
                .content(rs.getString("content"))
                .isPositive(rs.getBoolean("is_positive"))
                .userId(rs.getLong("user_id"))
                .filmId(rs.getLong("film_id"))
                .useful(rs.getLong("useful"))
                .build();
    }
}