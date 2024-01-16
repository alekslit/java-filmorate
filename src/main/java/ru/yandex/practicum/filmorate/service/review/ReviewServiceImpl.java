package ru.yandex.practicum.filmorate.service.review;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.dao.review.ReviewStorage;
import ru.yandex.practicum.filmorate.model.Review;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ReviewServiceImpl implements ReviewService {
    private final ReviewStorage storage;

    @Override
    public Review findReviewById(Long id) {
        return storage.findReviewById(id);
    }

    @Override
    public Review addReview(Review review) {
        return storage.addReview(review);
    }

    @Override
    public Review updateReviewById(Review review) {
        return storage.updateReviewById(review);
    }

    @Override
    public String deleteReviewById(Long id) {
        return storage.deleteReviewById(id);
    }

    @Override
    public List<Review> findAllReviews(Long filmId, Integer count) {
        return storage.findAllReviews(filmId, count);
    }

    @Override
    public String likeReview(Long reviewId, Long userId) {
        return storage.likeReview(reviewId, userId);
    }

    @Override
    public String dislikeReview(Long reviewId, Long userId) {
        return storage.dislikeReview(reviewId, userId);
    }

    @Override
    public String removeLikeFromReview(Long reviewId, Long userId) {
        return storage.removeLikeFromReview(reviewId, userId);
    }

    @Override
    public String removeDislikeFromReview(Long reviewId, Long userId) {
        return storage.removeDislikeFromReview(reviewId, userId);
    }
}