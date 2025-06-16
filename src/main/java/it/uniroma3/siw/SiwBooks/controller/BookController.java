package it.uniroma3.siw.SiwBooks.controller;

import it.uniroma3.siw.SiwBooks.model.Author;
import it.uniroma3.siw.SiwBooks.model.Book;
import it.uniroma3.siw.SiwBooks.model.User;
import it.uniroma3.siw.SiwBooks.service.AuthorService;
import it.uniroma3.siw.SiwBooks.service.BookService;
import it.uniroma3.siw.SiwBooks.service.ReviewService;
import it.uniroma3.siw.SiwBooks.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Controller
public class BookController {

    @Autowired
    private BookService bookService;

    @Autowired
    private AuthorService authorService;

    @Autowired
    private ReviewService reviewService;

    @Autowired
    private UserService userService;

    // Lista dei libri
    @GetMapping("/books")
    public String listBooks(Model model) {
        model.addAttribute("books", bookService.findAll());
        return "books/list";
    }

    // Dettaglio libro
    @GetMapping("/books/{id}")
    public String bookDetails(@PathVariable("id") Long id, Model model, Authentication authentication) {
        Optional<Book> bookOpt = bookService.findById(id);
        if (bookOpt.isPresent()) {
            Book book = bookOpt.get();
            model.addAttribute("book", book);

            boolean userHasReviewed = false;
            if (authentication != null && authentication.isAuthenticated()) {
                String username = authentication.getName();
                User currentUser = userService.getUserByUsername(username);
                if (currentUser != null) {
                    userHasReviewed = reviewService.findByBookAndUser(book, currentUser).isPresent();
                }
            }
            model.addAttribute("userHasReviewed", userHasReviewed);

            return "books/details";
        } else {
            return "redirect:/books";
        }
    }

    // Form per admin - nuovo libro 
    @GetMapping("/admin/books/new")
    public String showAddBookForm(Model model) {
        model.addAttribute("book", new Book());
        model.addAttribute("allAuthors", authorService.findAll());
        return "admin/bookForm";
    }

    // Salvataggio libro (admin)
    @PostMapping("/admin/books")
    public String addBook(@ModelAttribute("book") Book book,
                          @RequestParam("authorIds") List<Long> authorIds,
                          @RequestParam("coverImage") MultipartFile coverImage) throws IOException {

        List<Author> selectedAuthors = authorService.findAll().stream()
                .filter(a -> authorIds.contains(a.getId()))
                .toList();
        book.setAuthors(selectedAuthors);

        if (!coverImage.isEmpty()) {
            String uploadDir = "uploads/book-covers";
            Files.createDirectories(Paths.get(uploadDir));

            String originalFilename = Paths.get(coverImage.getOriginalFilename()).getFileName().toString();
            String sanitizedFilename = originalFilename.replaceAll("[^a-zA-Z0-9\\.\\-_]", "_");
            String newFileName = UUID.randomUUID() + "_" + sanitizedFilename;
            Path path = Paths.get(uploadDir, newFileName);

            Files.copy(coverImage.getInputStream(), path, StandardCopyOption.REPLACE_EXISTING);
            book.setImagePath("/" + uploadDir + "/" + newFileName);
        }

        bookService.save(book);
        return "redirect:/books";
    }


    // Cancellazione libro (admin)
    @GetMapping("/admin/books/delete/{id}")
    public String deleteBook(@PathVariable("id") Long id) {
        bookService.deleteById(id);
        return "redirect:/books";
    }
}