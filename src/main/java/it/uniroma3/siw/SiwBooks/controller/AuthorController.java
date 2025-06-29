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

    @GetMapping("/admin/authors/edit/{id}")
    public String showEditAuthorForm(@PathVariable("id") Long id, Model model) {
        Optional<Author> authorOpt = authorService.findById(id);
        if (authorOpt.isEmpty()) {
            return "redirect:/authors";
        }
        model.addAttribute("author", authorOpt.get());
        return "admin/authorForm";
    }

    @PostMapping("/admin/authors")
    public String saveAuthor(@ModelAttribute("author") Author author,
                             @RequestParam(value = "photoFile", required = false) MultipartFile photoFile) throws IOException {
        Author authorToSave;
        if (author.getId() != null) {
            Optional<Author> existingAuthorOpt = authorService.findById(author.getId());
            if (existingAuthorOpt.isPresent()) {
                authorToSave = existingAuthorOpt.get();
                authorToSave.setFirstName(author.getFirstName());
                authorToSave.setLastName(author.getLastName());
                authorToSave.setBirthDate(author.getBirthDate());
                authorToSave.setDeathDate(author.getDeathDate());
                authorToSave.setNationality(author.getNationality());
            } else {
                return "redirect:/authors";
            }
        } else {
            authorToSave = author;
        }

        if (photoFile != null && !photoFile.isEmpty()) {
            Files.createDirectories(Paths.get(UPLOAD_DIR));
            String originalFilename = Paths.get(photoFile.getOriginalFilename()).getFileName().toString();
            String sanitizedFilename = originalFilename.replaceAll("[^a-zA-Z0-9\\.\\-_]", "_");
            String newFileName = UUID.randomUUID() + "_" + sanitizedFilename;
            Path path = Paths.get(UPLOAD_DIR, newFileName);

            if (authorToSave.getPhotoPath() != null && !authorToSave.getPhotoPath().isEmpty()) {
                String existingFileName = Paths.get(authorToSave.getPhotoPath()).getFileName().toString();
                Path oldFile = Paths.get(UPLOAD_DIR, existingFileName);
                try {
                    if (Files.exists(oldFile)) {
                        Files.delete(oldFile);
                    }
                } catch (IOException e) {
                    System.err.println("Errore durante l'eliminazione della vecchia immagine autore: " + e.getMessage());
                }
            }

            Files.copy(photoFile.getInputStream(), path, StandardCopyOption.REPLACE_EXISTING);
            authorToSave.setPhotoPath("/" + UPLOAD_DIR + "/" + newFileName);
        }

        authorService.save(authorToSave);
        return "redirect:/authors/" + authorToSave.getId();
    }

    @GetMapping("/admin/authors/delete/{id}")
    public String deleteAuthor(@PathVariable("id") Long id) {
        Optional<Author> authorOpt = authorService.findById(id);
        if (authorOpt.isPresent()) {
            Author authorToDelete = authorOpt.get();
            String photoPath = authorToDelete.getPhotoPath();

            if (photoPath != null && !photoPath.isEmpty()) {
                try {
                    String fileName = Paths.get(photoPath).getFileName().toString();
                    Path filePath = Paths.get(UPLOAD_DIR, fileName);
                    if (Files.exists(filePath)) {
                        Files.delete(filePath);
                    }
                } catch (IOException e) {
                    System.err.println("Errore durante l'eliminazione della foto dell'autore " + id + ": " + e.getMessage());
                }
            }
            authorService.deleteById(id);
        }
        return "redirect:/authors";
    }
}