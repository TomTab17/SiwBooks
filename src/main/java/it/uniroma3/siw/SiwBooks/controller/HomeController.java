package it.uniroma3.siw.SiwBooks.controller;

import it.uniroma3.siw.SiwBooks.model.Book;
import it.uniroma3.siw.SiwBooks.service.BookService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.List;

@Controller
public class HomeController {

    @Autowired
    private BookService bookService;

    @GetMapping("/")
    public String home(Authentication authentication, Model model) {
        if (authentication != null && authentication.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"))) {
            model.addAttribute("isAdmin", true);
        }
        // Prendo ultimi 10 libri e li aggiungo al modello
        List<Book> latestBooks = bookService.findLatestBooks(10);
        model.addAttribute("books", latestBooks);

        return "home";
    }

    @GetMapping("/homeAdmin")
    public String homeAdmin(Authentication authentication, Model model) {
        // Se vuoi mostrare dati o info personalizzate, puoi usarli qui
        model.addAttribute("username", authentication.getName());
        return "admin/homeAdmin"; // Assicurati che esista il file homeAdmin.html in templates
    }
}
