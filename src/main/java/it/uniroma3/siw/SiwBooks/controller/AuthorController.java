package it.uniroma3.siw.SiwBooks.controller;

import it.uniroma3.siw.SiwBooks.model.Author;
import it.uniroma3.siw.SiwBooks.service.AuthorService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Optional;
import java.util.UUID;

@Controller
public class AuthorController {

    private static final String UPLOAD_DIR = "uploads/authors-covers";

    @Autowired
    private AuthorService authorService;

    @GetMapping("/authors")
    public String listAuthors(Model model) {
        model.addAttribute("authors", authorService.findAll());
        return "authors/list";
    }

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

    @GetMapping("/admin/authors/new")
    public String showAddAuthorForm(Model model) {
        model.addAttribute("author", new Author());
        return "admin/authorForm";
    }

    @PostMapping("/admin/authors")
    public String addAuthor(@ModelAttribute("author") Author author,
                            @RequestParam("photoFile") MultipartFile photoFile) throws IOException {

        if (!photoFile.isEmpty()) {
            Files.createDirectories(Paths.get(UPLOAD_DIR));

            String originalFilename = Paths.get(photoFile.getOriginalFilename()).getFileName().toString();
            String sanitizedFilename = originalFilename.replaceAll("[^a-zA-Z0-9\\.\\-_]", "_");
            String newFileName = UUID.randomUUID() + "_" + sanitizedFilename;
            Path path = Paths.get(UPLOAD_DIR, newFileName);

            Files.copy(photoFile.getInputStream(), path, StandardCopyOption.REPLACE_EXISTING);
            author.setPhotoPath("/" + UPLOAD_DIR + "/" + newFileName);
        }

        authorService.save(author);
        return "redirect:/authors/" + author.getId();
    }

    @GetMapping("/admin/authors/delete/{id}")
    public String deleteAuthor(@PathVariable("id") Long id) {
        authorService.deleteById(id);
        return "redirect:/authors";
    }
}