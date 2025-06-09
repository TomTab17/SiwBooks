package it.uniroma3.siw.SiwBooks.service;

import it.uniroma3.siw.SiwBooks.model.Book;
import it.uniroma3.siw.SiwBooks.model.Review;
import it.uniroma3.siw.SiwBooks.model.User;
import it.uniroma3.siw.SiwBooks.repository.ReviewRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class ReviewService {

    @Autowired
    private ReviewRepository reviewRepository;

    public Optional<Review> findById(Long id) {
        return reviewRepository.findById(id);
    }

    public Optional<Review> findByBookAndUser(Book book, User user) {
        return reviewRepository.findByBookAndUser(book, user);
    }

    public Review save(Review review) {
        return reviewRepository.save(review);
    }

    public void deleteById(Long id) {
        reviewRepository.deleteById(id);
    }
}
