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
    public String registerUser(@ModelAttribute User user, Model model) {
        UserService.RegisterResult result = userService.registerWithVerification(user);

        switch (result) {
            case SUCCESS:
                return "redirect:/verify?username=" + user.getUsername();
            case USERNAME_ALREADY_IN_USE:
                model.addAttribute("error", "Username già in uso. Scegli un altro username.");
                break;
            case EMAIL_ALREADY_IN_USE:
                model.addAttribute("error", "Email già in uso. Scegli un'altra email.");
                break;
            case USERNAME_ALREADY_IN_USE_PENDING_VERIFICATION:
                model.addAttribute("error", "Username già registrato e in attesa di verifica. Controlla la tua email.");
                model.addAttribute("username", user.getUsername());
                return "auth/verify";
            case EMAIL_ALREADY_IN_USE_PENDING_VERIFICATION:
                model.addAttribute("error", "Email già registrata e in attesa di verifica. Controlla la tua email.");
                model.addAttribute("username", user.getUsername());
                return "auth/verify";
        }

        model.addAttribute("user", user);
        return "auth/register";
    }

    @GetMapping("/login")
    public String showLoginForm(@RequestParam(value = "error", required = false) String error,
                                @RequestParam(value = "logout", required = false) String logout,
                                @RequestParam(value = "verified", required = false) String verified,
                                Model model) {
        if (error != null) {
            model.addAttribute("loginError", "Username o password non validi. Riprova.");
        }
        if (logout != null) {
            model.addAttribute("logoutMessage", "Logout effettuato con successo.");
        }
        if (verified != null) {
            model.addAttribute("verifiedMessage", "Account verificato con successo! Ora puoi accedere.");
        }
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