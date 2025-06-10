package it.uniroma3.siw.SiwBooks.service;

import it.uniroma3.siw.SiwBooks.model.User;
import it.uniroma3.siw.SiwBooks.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;

import java.util.Set;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;

import java.util.Random;

import java.util.Optional;

@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JavaMailSender mailSender;

    public User save(User user) {
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        return userRepository.save(user);
    }

    public Optional<User> findByUsername(String username) {
        return userRepository.findByUsername(username);
    }

    public User getUserByUsername(String username) {
        return this.findByUsername(username).orElse(null);
    }

    public User registerWithVerification(User user) {
        String encodedPassword = passwordEncoder.encode(user.getPassword());
        user.setPassword(encodedPassword);
        user.setRoles(Set.of("ROLE_USER"));

        // Genera codice di verifica
        String code = String.valueOf(new Random().nextInt(900000) + 100000); // 6 cifre
        user.setVerificationCode(code);
        user.setEnabled(false);

        userRepository.save(user);
        sendVerificationEmail(user);

        return user;
    }

    private void sendVerificationEmail(User user) {
        String toAddress = user.getEmail();
        String subject = "Codice di verifica SIWBooks";
        String content = "Ciao " + user.getFirstName() + ",<br>"
                + "Il tuo codice di verifica è: <b>" + user.getVerificationCode() + "</b><br>"
                + "Inseriscilo per attivare il tuo account.";

        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message);
            helper.setTo(toAddress);
            helper.setSubject(subject);
            helper.setText(content, true);
            mailSender.send(message);
        } catch (MessagingException e) {
            throw new IllegalStateException("Errore nell'invio della mail");
        }
    }

    public boolean verifyCode(String username, String code) {
        Optional<User> optionalUser = userRepository.findByUsername(username);
        if (optionalUser.isPresent()) {
            User user = optionalUser.get();
            if (user.getVerificationCode().equals(code)) {
                user.setEnabled(true);
                user.setVerificationCode(null); // pulizia
                userRepository.save(user);
                return true;
            }
        }
        return false;
    }
}

