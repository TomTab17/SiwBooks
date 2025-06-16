package it.uniroma3.siw.SiwBooks.controller;

import it.uniroma3.siw.SiwBooks.model.Book;
import it.uniroma3.siw.SiwBooks.model.Review;
import it.uniroma3.siw.SiwBooks.model.User;
import it.uniroma3.siw.SiwBooks.service.BookService;
import it.uniroma3.siw.SiwBooks.service.ReviewService;
import it.uniroma3.siw.SiwBooks.service.UserService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

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
        if (bookOpt.isEmpty()) {
            return "redirect:/books";
        }

        Book book = bookOpt.get();
        User user = userService.getUserByUsername(currentUser.getUsername());

        if (reviewService.findByBookAndUser(book, user).isPresent()) {
            model.addAttribute("alreadyReviewed", true);
            return "redirect:/books/" + bookId + "?error=alreadyReviewed";
        }

        model.addAttribute("book", book);
        model.addAttribute("review", new Review());
        return "reviews/reviewForm";
    }

    // Salvataggio recensione
    @PostMapping("/books/{bookId}/reviews")
    public String saveReview(@PathVariable Long bookId,
                             @Valid @ModelAttribute("review") Review review,
                             BindingResult bindingResult,
                             @AuthenticationPrincipal org.springframework.security.core.userdetails.User currentUser,
                             RedirectAttributes redirectAttributes,
                             Model model) {

        Optional<Book> bookOpt = bookService.findById(bookId);
        if (bookOpt.isEmpty()) {
            return "redirect:/books";
        }

        Book book = bookOpt.get();
        User user = userService.getUserByUsername(currentUser.getUsername());

        if (reviewService.findByBookAndUser(book, user).isPresent()) {
            redirectAttributes.addFlashAttribute("errorMessage", "Hai già recensito questo libro.");
            return "redirect:/books/" + bookId;
        }

        if (bindingResult.hasErrors()) {
            model.addAttribute("book", book);
            return "reviews/reviewForm";
        }

        review.setBook(book);
        review.setUser(user);
        reviewService.save(review);
        redirectAttributes.addFlashAttribute("successMessage", "Recensione inviata con successo!");
        return "redirect:/books/" + bookId;
    }

    // Cancellazione (solo admin)
    @GetMapping("/admin/reviews/delete/{id}")
    public String deleteReview(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        Optional<Review> reviewOpt = reviewService.findById(id);
        if (reviewOpt.isPresent()) {
            Long bookId = reviewOpt.get().getBook().getId();
            reviewService.deleteById(id);
            redirectAttributes.addFlashAttribute("successMessage", "Recensione eliminata con successo.");
            return "redirect:/books/" + bookId;
        }
        return "redirect:/books";
    }
}