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
import org.springframework.security.oauth2.core.user.OAuth2User;
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

    @GetMapping("/books")
    public String listBooks(Model model) {
        model.addAttribute("books", bookService.findAll());
        return "books/list";
    }

    @GetMapping("/books/{id}")
    public String bookDetails(@PathVariable("id") Long id, Model model, Authentication authentication) {
        Optional<Book> bookOpt = bookService.findById(id);
        if (bookOpt.isEmpty()) {
            return "redirect:/books";
        }

        Book book = bookOpt.get();
        model.addAttribute("book", book);

        boolean userHasReviewed = false;
        User currentUser = getLoggedInUser(authentication);
        if (currentUser != null) {
            userHasReviewed = reviewService.findByBookAndUser(book, currentUser).isPresent();
        }
        model.addAttribute("userHasReviewed", userHasReviewed);

        return "books/details";
    }

    @GetMapping("/admin/books/new")
    public String showAddBookForm(Model model) {
        model.addAttribute("book", new Book());
        model.addAttribute("allAuthors", authorService.findAll());
        return "admin/bookForm";
    }

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


    @GetMapping("/admin/books/delete/{id}")
    public String deleteBook(@PathVariable("id") Long id) {
        bookService.deleteById(id);
        return "redirect:/books";
    }

    @GetMapping("/books/search")
    public String searchBooks(@RequestParam(value = "q", required = false) String query,
                              @RequestParam(value = "type", defaultValue = "title") String searchType,
                              Model model) {
        List<Book> searchResults;
        if (query == null || query.isBlank()) {
            searchResults = bookService.findAll();
        } else {
            if ("author".equalsIgnoreCase(searchType)) {
                searchResults = bookService.searchBooksByAuthor(query);
            } else {
                searchResults = bookService.searchBooksByTitle(query);
            }
        }
        model.addAttribute("query", query);
        model.addAttribute("searchType", searchType);
        model.addAttribute("books", searchResults);
        return "books/searchResults";
    }
}