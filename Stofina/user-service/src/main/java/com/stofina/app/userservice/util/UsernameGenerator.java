package com.stofina.app.userservice.util;

import com.stofina.app.userservice.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Collection;
@RequiredArgsConstructor

    @Component
    public class UsernameGenerator {
    private final UserRepository userRepository;


    public String generateUsername(String firstName, String lastName) {
            String baseUsername = firstName + "." + lastName;
            int counter = 1;

            String username = baseUsername;
            while (usernameExists(username, userRepository.findAll())) {
                username = baseUsername + counter;
                counter++;
            }

            return username;
        }

        private boolean usernameExists(String username, Collection<?> entities) {
            return entities.stream().anyMatch(e -> {
                try {
                    var method = e.getClass().getMethod("getUsername");
                    String existingUsername = (String) method.invoke(e);
                    return username.equalsIgnoreCase(existingUsername);
                } catch (Exception ex) {
                    return false;
                }
            });
        }

    }
