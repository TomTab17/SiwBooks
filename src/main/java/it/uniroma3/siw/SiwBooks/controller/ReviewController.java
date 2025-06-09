package it.uniroma3.siw.SiwBooks.controller;

import it.uniroma3.siw.SiwBooks.model.Book;
import it.uniroma3.siw.SiwBooks.model.Review;
import it.uniroma3.siw.SiwBooks.model.User;
import it.uniroma3.siw.SiwBooks.service.BookService;
import it.uniroma3.siw.SiwBooks.service.ReviewService;
import it.uniroma3.siw.SiwBooks.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

@Controller
public class ReviewController {

    @Autowired
    private ReviewService reviewService;

    @Autowired
    private BookService bookService;

    @Autowired
    private UserService userService;

    // Form nuova recensione
    @GetMapping("/books/{bookId}/reviews/new")
    public String newReviewForm(@PathVariable Long bookId,
                                @AuthenticationPrincipal org.springframework.security.core.userdetails.User currentUser,
                                Model model) {
        Optional<Book> bookOpt = bookService.findById(bookId);
        if (bookOpt.isEmpty()) return "redirect:/books";

        Book book = bookOpt.get();
        User user = userService.getUserByUsername(currentUser.getUsername());

        if (reviewService.findByBookAndUser(book, user).isPresent())
            return "redirect:/books/" + bookId;

        model.addAttribute("book", book);
        model.addAttribute("review", new Review());
        return "reviews/reviewForm";
    }

    // Salvataggio recensione
    @PostMapping("/books/{bookId}/reviews")
    public String saveReview(@PathVariable Long bookId,
                             @ModelAttribute Review review,
                             @AuthenticationPrincipal org.springframework.security.core.userdetails.User currentUser) {
        Optional<Book> bookOpt = bookService.findById(bookId);
        if (bookOpt.isEmpty()) return "redirect:/books";

        Book book = bookOpt.get();
        User user = userService.getUserByUsername(currentUser.getUsername());

        if (reviewService.findByBookAndUser(book, user).isPresent())
            return "redirect:/books/" + bookId;

        review.setBook(book);
        review.setUser(user);
        reviewService.save(review);
        return "redirect:/books/" + bookId;
    }

    // Cancellazione (solo admin)
    @GetMapping("/admin/reviews/delete/{id}")
    public String deleteReview(@PathVariable Long id) {
        reviewService.deleteById(id);
        return "redirect:/books";
    }
}

