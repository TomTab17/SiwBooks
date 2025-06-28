package it.uniroma3.siw.SiwBooks.controller;

import it.uniroma3.siw.SiwBooks.model.User;
import it.uniroma3.siw.SiwBooks.service.UserService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/profile")
public class ProfileController {

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
        System.err.println("Tipo di principal non riconosciuto: " + principal.getClass().getName());
        return null;
    }

    @GetMapping("/myProfile")
    public String getProfile(Model model, Authentication authentication) {
        User loggedUser = getLoggedInUser(authentication);

        if (loggedUser != null) {
            model.addAttribute("loggedUser", loggedUser);
            System.out.println("Utente loggato in /myProfile trovato: " + loggedUser.getUsername() + ", Avatar: " + loggedUser.getAvatar());
        } else {
            System.err.println("Utente autenticato (" + authentication.getName() + ") non trovato nel database in /profile/myProfile!");
            return "redirect:/login?error=userNotFound";
        }

        return "/profile/profile";
    }

    @GetMapping("/editProfile")
    public String showEditProfile(Model model, Authentication authentication) {
        User user = getLoggedInUser(authentication);

        if (user != null) {
            model.addAttribute("loggedUser", user);
            return "/profile/editProfile";
        } else {
            System.err.println("Utente autenticato (" + authentication.getName() + ") non trovato nel database in /profile/editProfile!");
            return "redirect:/login";
        }
    }

    @PostMapping("/editProfile")
    public String updateProfile(@ModelAttribute("loggedUser") User updatedUser, Authentication authentication, RedirectAttributes redirectAttributes) {
        User existingUser = getLoggedInUser(authentication);

        if (existingUser == null) {
            redirectAttributes.addFlashAttribute("errorMessage", "Errore: utente non trovato o tipo di autenticazione non supportato.");
            return "redirect:/login";
        }

        existingUser.setFirstName(updatedUser.getFirstName());
        existingUser.setLastName(updatedUser.getLastName());

        if (authentication.getPrincipal() instanceof org.springframework.security.core.userdetails.UserDetails) {
            if (updatedUser.getPassword() != null && !updatedUser.getPassword().isEmpty()) {
                existingUser.setPassword(userService.encodePassword(updatedUser.getPassword()));
            }
        }

        existingUser.setAvatar(updatedUser.getAvatar());

        userService.save(existingUser);

        redirectAttributes.addFlashAttribute("successMessage", "Profilo aggiornato con successo!");
        return "redirect:/profile/myProfile";
    }
}