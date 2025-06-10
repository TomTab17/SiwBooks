package it.uniroma3.siw.SiwBooks.controller;

import it.uniroma3.siw.SiwBooks.model.User;
import it.uniroma3.siw.SiwBooks.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;


@Controller
public class AuthController {

    @Autowired
    private UserService userService;

    @GetMapping("/register")
    public String showRegistrationForm(Model model) {
        model.addAttribute("user", new User());
        return "auth/register";
    }

    @PostMapping("/register")
    public String registerUser(@ModelAttribute User user) {
        userService.registerWithVerification(user);
        return "redirect:/verify?username=" + user.getUsername();
    }

    @GetMapping("/login")
    public String showLoginForm() {
        return "auth/login";
    }

    @GetMapping("/verify")
    public String showVerificationForm(@RequestParam String username, Model model) {
        model.addAttribute("username", username);
        return "auth/verify";
    }

    @PostMapping("/verify")
    public String verifyUser(@RequestParam String username, @RequestParam String code, Model model) {
        if (userService.verifyCode(username, code)) {
            return "redirect:/login?verified=true";
        } else {
            model.addAttribute("error", "Codice non valido");
            model.addAttribute("username", username);
            return "auth/verify";
        }
    }
}

