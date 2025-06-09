package it.uniroma3.siw.SiwBooks.controller;

import it.uniroma3.siw.SiwBooks.model.Author;
import it.uniroma3.siw.SiwBooks.model.Book;
import it.uniroma3.siw.SiwBooks.service.AuthorService;
import it.uniroma3.siw.SiwBooks.service.BookService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@Controller
public class BookController {

    @Autowired
    private BookService bookService;

    @Autowired
    private AuthorService authorService;

    // Lista dei libri
    @GetMapping("/books")
    public String listBooks(Model model) {
        model.addAttribute("books", bookService.findAll());
        return "books/list";
    }

    // Dettaglio libro
    @GetMapping("/books/{id}")
    public String bookDetails(@PathVariable("id") Long id, Model model) {
        Optional<Book> bookOpt = bookService.findById(id);
        if (bookOpt.isPresent()) {
            model.addAttribute("book", bookOpt.get());
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
    public String addBook(@ModelAttribute("book") Book book, @RequestParam("authorIds") List<Long> authorIds) {
        List<Author> selectedAuthors = authorService.findAll().stream()
                .filter(a -> authorIds.contains(a.getId()))
                .toList();
        book.setAuthors(selectedAuthors);
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

