package ru.yandex.practicum.filmorate.dao.review;

import ru.yandex.practicum.filmorate.model.Review;

import java.util.List;

public interface ReviewStorage {
    Review findReviewById(Long id);

    Review addReview(Review review);

    Review updateReviewById(Review review);

    String deleteReviewById(Long id);

    List<Review> findAllReviews(Long filmId, Integer count);

    String likeReview(Long reviewId, Long userId);

    String dislikeReview(Long reviewId, Long userId);

    String removeLikeFromReview(Long reviewId, Long userId);

    String removeDislikeFromReview(Long reviewId, Long userId);
}