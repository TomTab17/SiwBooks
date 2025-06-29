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
import java.util.stream.Collectors;

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

    private static final String UPLOAD_DIR = "uploads/book-covers";

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

    @GetMapping("/admin/books/edit/{id}")
    public String showEditBookForm(@PathVariable("id") Long id, Model model) {
        Optional<Book> bookOpt = bookService.findById(id);
        if (bookOpt.isEmpty()) {
            return "redirect:/books";
        }
        Book book = bookOpt.get();
        model.addAttribute("book", book);
        model.addAttribute("allAuthors", authorService.findAll());
        List<Long> selectedAuthorIds = book.getAuthors().stream()
                                            .map(Author::getId)
                                            .collect(Collectors.toList());
        model.addAttribute("selectedAuthorIds", selectedAuthorIds);
        return "admin/bookForm";
    }

    @PostMapping("/admin/books")
public String saveBook(@ModelAttribute("book") Book book,
                       @RequestParam("authorIds") List<Long> authorIds,
                       @RequestParam(value = "coverImage", required = false) MultipartFile coverImage) throws IOException {

    Book bookToSave;
    if (book.getId() != null) {
        // Caso di aggiornamento: carica il libro esistente
        Optional<Book> existingBookOpt = bookService.findById(book.getId());
        if (existingBookOpt.isPresent()) {
            bookToSave = existingBookOpt.get();
            // Aggiorna i campi del libro esistente
            bookToSave.setTitle(book.getTitle());
            bookToSave.setPublicationYear(book.getPublicationYear());
        } else {
            // Se il libro non esiste, reindirizza con errore
            return "redirect:/books";
        }
    } else {
        // Caso di creazione: crea un nuovo libro
        bookToSave = new Book();
        bookToSave.setTitle(book.getTitle());
        bookToSave.setPublicationYear(book.getPublicationYear());
    }

    // Gestione degli autori
    List<Author> selectedAuthors = authorService.findAll().stream()
            .filter(a -> authorIds.contains(a.getId()))
            .collect(Collectors.toList());
    bookToSave.setAuthors(selectedAuthors);

    // Gestione dell'immagine di copertina
    if (coverImage != null && !coverImage.isEmpty()) {
        Files.createDirectories(Paths.get(UPLOAD_DIR));

        String originalFilename = Paths.get(coverImage.getOriginalFilename()).getFileName().toString();
        String sanitizedFilename = originalFilename.replaceAll("[^a-zA-Z0-9\\.\\-_]", "_");
        String newFileName = UUID.randomUUID() + "_" + sanitizedFilename;
        Path path = Paths.get(UPLOAD_DIR, newFileName);

        // Elimina l'immagine esistente, se presente
        if (bookToSave.getImagePath() != null && !bookToSave.getImagePath().isEmpty()) {
            Path oldFile = Paths.get(UPLOAD_DIR, Paths.get(bookToSave.getImagePath()).getFileName().toString());
            try {
                if (Files.exists(oldFile)) {
                    Files.delete(oldFile);
                }
            } catch (IOException e) {
                System.err.println("Errore durante l'eliminazione della vecchia immagine: " + e.getMessage());
            }
        }

        Files.copy(coverImage.getInputStream(), path, StandardCopyOption.REPLACE_EXISTING);
        bookToSave.setImagePath("/" + UPLOAD_DIR + "/" + newFileName);
    }

    bookService.save(bookToSave);
    return "redirect:/books/" + bookToSave.getId();
}

    @GetMapping("/admin/books/delete/{id}")
    public String deleteBook(@PathVariable("id") Long id) {
        Optional<Book> bookOpt = bookService.findById(id);
        if (bookOpt.isPresent()) {
            Book bookToDelete = bookOpt.get();
            String imagePath = bookToDelete.getImagePath();

            if (imagePath != null && !imagePath.isEmpty()) {
                try {
                    String fileName = Paths.get(imagePath).getFileName().toString();
                    Path filePath = Paths.get(UPLOAD_DIR, fileName);

                    if (Files.exists(filePath)) {
                        Files.delete(filePath);
                    }
                } catch (IOException e) {
                    System.err.println("Errore durante l'eliminazione dell'immagine del libro " + id + ": " + e.getMessage());
                }
            }
            bookService.deleteById(id);
        }
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