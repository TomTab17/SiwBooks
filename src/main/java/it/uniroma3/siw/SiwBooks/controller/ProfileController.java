package it.uniroma3.siw.SiwBooks.controller;

import it.uniroma3.siw.SiwBooks.model.User;
import it.uniroma3.siw.SiwBooks.service.UserService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.security.Principal;

@Controller
@RequestMapping("/profile")
public class ProfileController {

    @Autowired
    private UserService userService;

    @GetMapping("/myProfile")
    public String getProfile(Model model, Authentication authentication) {
        String username = authentication.getName();
        User loggedUser = userService.getUserByUsername(username);
        model.addAttribute("loggedUser", loggedUser);
        return "/profile/profile";
    }


    // Mostra il form per modificare il profilo
    @GetMapping("/editProfile")
    public String showEditProfile(Model model, Principal principal) {
        String username = principal.getName();
        User user = userService.getUserByUsername(username);
        model.addAttribute("loggedUser", user);
        return "/profile/editProfile"; 
    }

    @PostMapping("/editProfile")
    public String updateProfile(@ModelAttribute("loggedUser") User updatedUser, Principal principal, RedirectAttributes redirectAttributes) {
        String username = principal.getName();

        User existingUser = userService.getUserByUsername(username);
        if (existingUser == null) {
            // Utente non trovato
            return "redirect:/login";
        }

        // Aggiorna i campi modificabili
        existingUser.setFirstName(updatedUser.getFirstName());
        existingUser.setLastName(updatedUser.getLastName());

        if (updatedUser.getPassword() != null && !updatedUser.getPassword().isEmpty()) {
            existingUser.setPassword(updatedUser.getPassword());
        }


        existingUser.setAvatar(updatedUser.getAvatar());

        userService.save(existingUser);

        redirectAttributes.addFlashAttribute("successMessage", "Profilo aggiornato con successo!");
        return "redirect:/profile/myProfile";
    }
}
