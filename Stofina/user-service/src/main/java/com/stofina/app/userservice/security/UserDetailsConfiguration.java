package com.stofina.app.userservice.security;


import com.stofina.app.userservice.exception.UserNotFoundException;
import com.stofina.app.userservice.model.User;
import com.stofina.app.userservice.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.transaction.annotation.Transactional;

import static com.stofina.app.userservice.constants.UserConstants.USER_NOT_FOUND;


@RequiredArgsConstructor
@Slf4j
public class UserDetailsConfiguration implements UserDetailsService {
    private final UserRepository userRepository;

    @Transactional(readOnly = true)
    public UserDetails loadUserByUsername(String email) {
        User user = userRepository.findByEmail(email).orElseThrow(()->new UserNotFoundException(USER_NOT_FOUND));
        log.info("Retrieved  user {}", email);
        if (user!=null) {
            return UserDetailsImpl.build(user);
        }
        log.error("Retrieved  user not found {}", email);
        throw new UserNotFoundException(USER_NOT_FOUND);
    }
}