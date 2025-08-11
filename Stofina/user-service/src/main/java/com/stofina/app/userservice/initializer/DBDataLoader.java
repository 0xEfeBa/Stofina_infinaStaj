package com.stofina.app.userservice.initializer;

import com.stofina.app.commondata.model.enums.RoleType;
import com.stofina.app.commondata.model.enums.UserStatus;
import com.stofina.app.userservice.model.Role;
import com.stofina.app.userservice.model.User;
import com.stofina.app.userservice.repository.RoleRepository;
import com.stofina.app.userservice.repository.UserRepository;
import jakarta.annotation.PostConstruct;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.Set;

@Slf4j
@Component
@RequiredArgsConstructor
public class DBDataLoader {

    private final RoleRepository roleRepository;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @PostConstruct
    @Transactional
    public void initData() {
        log.info("Initializing default roles and users...");

        for (RoleType roleType : RoleType.values()) {
            if (!roleRepository.existsByRoleType(roleType)) {
                Role role = Role.builder()
                        .roleType(roleType)
                        .build();
                roleRepository.save(role);
                log.info("Role created: {}", roleType);
            } else {
                log.info("Role already exists: {}", roleType);
            }
        }

        createUserIfNotExists(
                "cihandilsizdev@gmail.com",
                "Cihan",
                "Dilsiz",
                "5462462787",
                "System Administrator",
                "Cihan@3333",
                RoleType.CUSTOMER_SUPER_ADMIN
        );
        createUserIfNotExists(
                "ebruebg@hotmail.com",
                "Ebru",
                "Uslu",
                "5462462780",
                "System Administrator",
                "Ebru@6327",
                RoleType.CUSTOMER_SUPER_ADMIN
        );
        createUserIfNotExists(
                "efefromearth@gmail.com",
                "Efe",
                "Altop",
                "5462462781",
                "System Administrator",
                "Efe@0606",
                RoleType.CUSTOMER_SUPER_ADMIN
        );
        createUserIfNotExists(
                "maxirem9@gmail.com",
                "Irem",
                "Turen",
                "5462462789",
                "System Administrator",
                "irem1470T@",
                RoleType.CUSTOMER_SUPER_ADMIN
        );

    }

    private void createUserIfNotExists(String email, String firstName, String lastName,
                                       String phoneNumber, String title, String rawPassword,
                                       RoleType roleType) {
        if (!userRepository.existsByEmail(email)) {
            Role role = roleRepository.findByRoleType(roleType)
                    .orElseThrow(() -> new RuntimeException("Role not found: " + roleType));

            User user = User.builder()
                    .email(email)
                    .firstName(firstName)
                    .lastName(lastName)
                    .phoneNumber(phoneNumber)
                    .title(title)
                    .username(firstName.toLowerCase() + "." + lastName.toLowerCase())
                    .status(UserStatus.ACTIVE)
                    .password(passwordEncoder.encode(rawPassword))
                    .roles(Set.of(role))
                    .build();

            userRepository.save(user);
            log.info("Default {} user created: {} {}", roleType, firstName, lastName);
        } else {
            log.info("User already exists: {}", email);
        }
    }
}

