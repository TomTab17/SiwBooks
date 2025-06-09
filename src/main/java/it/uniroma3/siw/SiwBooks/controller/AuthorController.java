package it.uniroma3.siw.SiwBooks.controller;

import it.uniroma3.siw.SiwBooks.model.Author;
import it.uniroma3.siw.SiwBooks.service.AuthorService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

@Controller
public class AuthorController {

    @Autowired
    private AuthorService authorService;

    // Lista di tutti gli autori
    @GetMapping("/authors")
    public String listAuthors(Model model) {
        model.addAttribute("authors", authorService.findAll());
        return "authors/list"; // thymeleaf template
    }

    // Visualizza dettaglio autore
    @GetMapping("/authors/{id}")
    public String authorDetails(@PathVariable("id") Long id, Model model) {
        Optional<Author> authorOpt = authorService.findById(id);
        if (authorOpt.isPresent()) {
            model.addAttribute("author", authorOpt.get());
            return "authors/details";
        } else {
            return "redirect:/authors";
        }
    }

    // (per admin) mostra form per aggiungere autore
    @GetMapping("/admin/authors/new")
    public String showAddAuthorForm(Model model) {
        model.addAttribute("author", new Author());
        return "admin/authorForm";
    }

    // (per admin) salva autore
    @PostMapping("/admin/authors")
    public String addAuthor(@ModelAttribute("author") Author author) {
        authorService.save(author);
        return "redirect:/authors";
    }

    // (per admin) cancella autore
    @GetMapping("/admin/authors/delete/{id}")
    public String deleteAuthor(@PathVariable("id") Long id) {
        authorService.deleteById(id);
        return "redirect:/authors";
    }
}

