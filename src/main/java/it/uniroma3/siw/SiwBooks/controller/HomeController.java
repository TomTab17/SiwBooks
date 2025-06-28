package it.uniroma3.siw.SiwBooks.controller;

import it.uniroma3.siw.SiwBooks.model.User;
import it.uniroma3.siw.SiwBooks.model.Book;
import it.uniroma3.siw.SiwBooks.service.BookService;
import it.uniroma3.siw.SiwBooks.service.UserService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.List;
import java.util.Optional;

@Controller
public class HomeController {

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
            String email = ((OAuth2User) principal).getAttribute("email");
            if (email != null) {
                return userService.getUserByEmail(email);
            }
        } else if (principal instanceof org.springframework.security.core.userdetails.UserDetails) {
            String username = ((org.springframework.security.core.userdetails.UserDetails) principal).getUsername();
            return userService.getUserByUsername(username);
        }
        return null;
    }

    @GetMapping("/")
    public String home(Authentication authentication, Model model) {
        User loggedUser = getLoggedInUser(authentication);

        if (loggedUser != null) {
            model.addAttribute("loggedUser", loggedUser);
            if (loggedUser.getRoles() != null && loggedUser.getRoles().contains("ROLE_ADMIN")) {
                model.addAttribute("isAdmin", true);
            }
        }

        List<Book> latestBooks = bookService.findLatestBooks(10);
        model.addAttribute("books", latestBooks);

        return "home";
    }
}