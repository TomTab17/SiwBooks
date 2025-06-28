package it.uniroma3.siw.SiwBooks.service;

import it.uniroma3.siw.SiwBooks.model.User;
import it.uniroma3.siw.SiwBooks.repository.UserRepository;
import org.springframework.context.annotation.Lazy;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class CustomOAuth2UserService extends DefaultOAuth2UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public CustomOAuth2UserService(UserRepository userRepository, @Lazy PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        OAuth2User oauth2User = super.loadUser(userRequest);

        String email = oauth2User.getAttribute("email");
        String firstName = oauth2User.getAttribute("given_name");
        String lastName = oauth2User.getAttribute("family_name");
        String avatarUrl = oauth2User.getAttribute("picture");

        User user;
        Optional<User> userOpt = userRepository.findByEmail(email);

        if (userOpt.isEmpty()) {
            user = new User();
            user.setEmail(email);
            user.setFirstName(firstName);
            user.setLastName(lastName);
            user.setUsername(email);
            user.setPassword(passwordEncoder.encode(""));
            user.setRoles(Set.of("ROLE_USER"));
            user.setEnabled(true);
            user.setAvatar(avatarUrl);
            user.setGoogleUser(true);
            userRepository.save(user);
        } else {
            user = userOpt.get();
            if (user.getAvatar() == null || (avatarUrl != null && !user.getAvatar().equals(avatarUrl))) {
                 user.setAvatar(avatarUrl);
                 userRepository.save(user);
            }
            if (!user.isGoogleUser()) {
                user.setGoogleUser(true);
                userRepository.save(user);
            }
        }

        Set<GrantedAuthority> authorities = new HashSet<>();
        authorities.addAll(oauth2User.getAuthorities());

        if (user.getRoles() != null && !user.getRoles().isEmpty()) {
            authorities.addAll(user.getRoles().stream()
                               .map(SimpleGrantedAuthority::new)
                               .collect(Collectors.toSet()));
        }

        return new DefaultOAuth2User(
                authorities,
                oauth2User.getAttributes(),
                userRequest.getClientRegistration().getProviderDetails().getUserInfoEndpoint().getUserNameAttributeName()
        );
    }
}