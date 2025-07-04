package it.uniroma3.siw.SiwBooks.controller;

import it.uniroma3.siw.SiwBooks.model.Book;
import it.uniroma3.siw.SiwBooks.model.Review;
import it.uniroma3.siw.SiwBooks.model.User;
import it.uniroma3.siw.SiwBooks.service.BookService;
import it.uniroma3.siw.SiwBooks.service.ReviewService;
import it.uniroma3.siw.SiwBooks.service.UserService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.user.OAuth2User;
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

    private User getLoggedInUser(Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            return null;
        }

        Object principal = authentication.getPrincipal();

        if (principal instanceof OAuth2User) {
            OAuth2User oauth2User = (OAuth2User) principal;
            String email = oauth2User.getAttribute("email");
            if (email != null) {
                return userService.getUserByEmail(email);
            }
        } else if (principal instanceof org.springframework.security.core.userdetails.UserDetails) {
            String username = ((org.springframework.security.core.userdetails.UserDetails) principal).getUsername();
            return userService.getUserByUsername(username);
        }
        return null;
    }

    @GetMapping("/books/{bookId}/reviews/new")
    public String newReviewForm(@PathVariable Long bookId,
                                Authentication authentication,
                                Model model) {
        Optional<Book> bookOpt = bookService.findById(bookId);
        if (bookOpt.isEmpty()) {
            return "redirect:/books";
        }

        Book book = bookOpt.get();
        User user = getLoggedInUser(authentication);

        if (user == null) {
            return "redirect:/login?error=notLoggedInToReview";
        }

        if (reviewService.findByBookAndUser(book, user).isPresent()) {
            model.addAttribute("alreadyReviewed", true);
            return "redirect:/books/" + bookId + "?error=alreadyReviewed";
        }

        model.addAttribute("book", book);
        model.addAttribute("review", new Review());
        model.addAttribute("isEditing", false);
        return "reviews/reviewForm";
    }

    @GetMapping("/books/{bookId}/reviews/edit")
    public String editReviewForm(@PathVariable Long bookId, Authentication authentication, Model model, RedirectAttributes redirectAttributes) {
        Optional<Book> bookOpt = bookService.findById(bookId);
        if (bookOpt.isEmpty()) {
            redirectAttributes.addFlashAttribute("errorMessage", "Libro non trovato.");
            return "redirect:/books";
        }

        Book book = bookOpt.get();
        User user = getLoggedInUser(authentication);

        if (user == null) {
            redirectAttributes.addFlashAttribute("errorMessage", "Devi essere loggato per modificare una recensione.");
            return "redirect:/login";
        }

        Optional<Review> existingReviewOpt = reviewService.findByBookAndUser(book, user);
        if (existingReviewOpt.isEmpty()) {
            redirectAttributes.addFlashAttribute("errorMessage", "Nessuna recensione trovata da modificare per questo libro.");
            return "redirect:/books/" + bookId;
        }

        model.addAttribute("book", book);
        model.addAttribute("review", existingReviewOpt.get());
        model.addAttribute("isEditing", true);
        return "reviews/reviewForm";
    }

    @PostMapping("/books/{bookId}/reviews")
    public String upsertReview(@PathVariable Long bookId,
                             @Valid @ModelAttribute("review") Review review,
                             BindingResult bindingResult,
                             Authentication authentication,
                             RedirectAttributes redirectAttributes,
                             Model model) {

        Optional<Book> bookOpt = bookService.findById(bookId);
        if (bookOpt.isEmpty()) {
            redirectAttributes.addFlashAttribute("errorMessage", "Libro non trovato.");
            return "redirect:/books";
        }

        Book book = bookOpt.get();
        User user = getLoggedInUser(authentication);

        if (user == null) {
            redirectAttributes.addFlashAttribute("errorMessage", "Devi essere loggato per inviare una recensione.");
            return "redirect:/login";
        }
        
        Optional<Review> existingReviewOpt = reviewService.findByBookAndUser(book, user);

        if (bindingResult.hasErrors()) {
            model.addAttribute("book", book);
            model.addAttribute("isEditing", existingReviewOpt.isPresent()); 
            return "reviews/reviewForm";
        }

        Review reviewToSave = existingReviewOpt.orElse(new Review()); 
        reviewToSave.setTitle(review.getTitle());
        reviewToSave.setText(review.getText());
        reviewToSave.setRating(review.getRating());
        reviewToSave.setBook(book);
        reviewToSave.setUser(user);
        
        reviewService.save(reviewToSave);

        if (existingReviewOpt.isPresent()) {
            redirectAttributes.addFlashAttribute("successMessage", "Recensione aggiornata con successo!");
        } else {
            redirectAttributes.addFlashAttribute("successMessage", "Recensione inviata con successo!");
        }
        return "redirect:/books/" + bookId;
    }

    @GetMapping("/books/{bookId}/reviews/delete/{reviewId}")
    public String deleteUserReview(@PathVariable Long bookId, @PathVariable Long reviewId, 
                                 Authentication authentication, RedirectAttributes redirectAttributes) {
        User currentUser = getLoggedInUser(authentication);
        if (currentUser == null) {
            redirectAttributes.addFlashAttribute("errorMessage", "Devi essere loggato per eliminare una recensione.");
            return "redirect:/login";
        }

        Optional<Review> reviewOpt = reviewService.findById(reviewId);
        if (reviewOpt.isEmpty()) {
            redirectAttributes.addFlashAttribute("errorMessage", "Recensione non trovata.");
            return "redirect:/books/" + bookId;
        }

        Review reviewToDelete = reviewOpt.get();

        if (!reviewToDelete.getUser().getId().equals(currentUser.getId())) {
            redirectAttributes.addFlashAttribute("errorMessage", "Non sei autorizzato a eliminare questa recensione.");
            return "redirect:/books/" + bookId;
        }
        
        if (!reviewToDelete.getBook().getId().equals(bookId)) {
            redirectAttributes.addFlashAttribute("errorMessage", "La recensione non appartiene a questo libro.");
            return "redirect:/books/" + bookId;
        }

        reviewService.deleteById(reviewId);
        redirectAttributes.addFlashAttribute("successMessage", "Recensione eliminata con successo.");
        return "redirect:/books/" + bookId;
    }

    @GetMapping("/admin/reviews/delete/{id}")
    public String deleteReview(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        Optional<Review> reviewOpt = reviewService.findById(id);
        if (reviewOpt.isPresent()) {
            Long bookId = reviewOpt.get().getBook().getId();
            reviewService.deleteById(id);
            redirectAttributes.addFlashAttribute("successMessage", "Recensione eliminata con successo.");
            return "redirect:/books/" + bookId;
        }
        redirectAttributes.addFlashAttribute("errorMessage", "Recensione non trovata.");
        return "redirect:/books";
    }
}