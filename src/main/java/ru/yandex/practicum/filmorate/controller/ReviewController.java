package ru.yandex.practicum.filmorate.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.model.Review;
import ru.yandex.practicum.filmorate.service.review.ReviewServiceImpl;

import javax.validation.Valid;
import java.util.List;

@RestController
@RequestMapping("/reviews")
public class ReviewController {
    private final ReviewServiceImpl reviewServiceImpl;

    @Autowired
    public ReviewController(ReviewServiceImpl reviewServiceImpl) {
        this.reviewServiceImpl = reviewServiceImpl;
    }

    @PostMapping
    public Review addReview(@Valid @RequestBody Review review) {
        return reviewServiceImpl.addReview(review);
    }

    @GetMapping("/{id}")
    public Review getReviewById(@PathVariable Long id) {
        return reviewServiceImpl.findReviewById(id);
    }

    @DeleteMapping("/{id}")
    public String deleteReview(@PathVariable Long id) {
        return reviewServiceImpl.deleteReviewById(id);
    }

    @PutMapping
    public Review updateReview(@Valid @RequestBody Review review) {
        return reviewServiceImpl.updateReviewById(review);
    }

    @GetMapping
    public List<Review> getReviewForFilmOrAll(@RequestParam(required = false) Long filmId,
                                              @RequestParam(defaultValue = "10") Integer count) {
        return reviewServiceImpl.findAllReviews(filmId, count);
    }

    //PUT /reviews/{id}/like/{userId} — пользователь ставит лайк отзыву.
    @PutMapping("/{id}/like/{userId}")
    public String addLikeToReview(@PathVariable("id") Long id, @PathVariable("userId") Long userId) {
        return reviewServiceImpl.likeReview(id, userId);
    }

    //PUT /reviews/{id}/dislike/{userId} — пользователь ставит дизлайк отзыву.
    @PutMapping("/{id}/dislike/{userId}")
    public String addDislikeToReview(@PathVariable("id") Long id, @PathVariable("userId") Long userId) {
        return reviewServiceImpl.dislikeReview(id, userId);
    }

    //DELETE /reviews/{id}/like/{userId} — пользователь удаляет лайк/дизлайк отзыву.
    @DeleteMapping("/{id}/like/{userId}")
    public String removeLikeFromReview(@PathVariable("id") Long id, @PathVariable("userId") Long userId) {
        return reviewServiceImpl.removeLikeFromReview(id, userId);
    }

    //DELETE /reviews/{id}/dislike/{userId} — пользователь удаляет дизлайк отзыву.
    @DeleteMapping("/{id}/dislike/{userId}")
    public String removeDislikeFromReview(@PathVariable("id") Long id, @PathVariable("userId") Long userId) {
        return reviewServiceImpl.removeDislikeFromReview(id, userId);
    }
}