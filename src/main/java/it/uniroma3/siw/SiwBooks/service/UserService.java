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

    public String encodePassword(String rawPassword) {
        return passwordEncoder.encode(rawPassword);
    }

    public Optional<User> findByUsername(String username) {
        return userRepository.findByUsername(username);
    }

    public Optional<User> findByEmail(String email) {
        return userRepository.findByEmail(email);
    }

    public User getUserByUsername(String username) {
        return this.findByUsername(username).orElse(null);
    }

    public RegisterResult registerWithVerification(User user) {
        Optional<User> existingUserByUsername = userRepository.findByUsername(user.getUsername());
        if (existingUserByUsername.isPresent()) {
            User foundUser = existingUserByUsername.get();
            if (!foundUser.isEnabled()) {
                return RegisterResult.USERNAME_ALREADY_IN_USE_PENDING_VERIFICATION;
            } else {
                return RegisterResult.USERNAME_ALREADY_IN_USE;
            }
        }

        Optional<User> existingUserByEmail = userRepository.findByEmail(user.getEmail());
        if (existingUserByEmail.isPresent()) {
            User foundUser = existingUserByEmail.get();
            if (!foundUser.isEnabled()) {
                return RegisterResult.EMAIL_ALREADY_IN_USE_PENDING_VERIFICATION;
            } else {
                return RegisterResult.EMAIL_ALREADY_IN_USE;
            }
        }

        String encodedPassword = passwordEncoder.encode(user.getPassword());
        user.setPassword(encodedPassword);
        user.setRoles(Set.of("ROLE_USER"));

        String code = String.valueOf(new Random().nextInt(900000) + 100000); // 6 cifre
        user.setVerificationCode(code);
        user.setEnabled(false);

        userRepository.save(user);
        sendVerificationEmail(user);

        return RegisterResult.SUCCESS;
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
            throw new IllegalStateException("Errore nell'invio della mail", e);
        }
    }

    // Email di conferma registrazione
    private void sendRegistrationConfirmationEmail(User user) {
        String toAddress = user.getEmail();
        String subject = "Registrazione completata su SIWBooks!";
        String content = "Ciao " + user.getFirstName() + ",<br>"
                + "La tua registrazione a SIWBooks è stata completata con successo! Di seguito i dettagli del tuo account:<br><br>"
                + "<b>Nome:</b> " + user.getFirstName() + "<br>"
                + "<b>Cognome:</b> " + user.getLastName() + "<br>"
                + "<b>Email:</b> " + user.getEmail() + "<br>"
                + "<b>Username:</b> " + user.getUsername() + "<br><br>"
                + "Ora puoi accedere al tuo account e iniziare a esplorare i nostri libri!<br><br>"
                + "Il team di SIWBooks";

        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message);
            helper.setTo(toAddress);
            helper.setSubject(subject);
            helper.setText(content, true);
            mailSender.send(message);
        } catch (MessagingException e) {
            throw new IllegalStateException("Errore nell'invio della mail di conferma registrazione", e);
        }
    }

    public boolean verifyCode(String username, String code) {
        Optional<User> optionalUser = userRepository.findByUsername(username);
        if (optionalUser.isPresent()) {
            User user = optionalUser.get();
            if (user.getVerificationCode() != null && user.getVerificationCode().equals(code)) {
                user.setEnabled(true);
                user.setVerificationCode(null); // pulizia
                userRepository.save(user);
                // Invia l'email di conferma
                sendRegistrationConfirmationEmail(user);
                return true;
            }
        }
        return false;
    }

    public enum RegisterResult {
        SUCCESS,
        USERNAME_ALREADY_IN_USE,
        EMAIL_ALREADY_IN_USE,
        USERNAME_ALREADY_IN_USE_PENDING_VERIFICATION,
        EMAIL_ALREADY_IN_USE_PENDING_VERIFICATION
    }
}