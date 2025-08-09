package com.stofina.app.userservice.config;

import com.stofina.app.commondata.model.enums.RoleType;
import com.stofina.app.userservice.model.Role;
import com.stofina.app.userservice.repository.RoleRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Configuration;

@Slf4j
@Configuration
@RequiredArgsConstructor
public class RoleDataLoader implements CommandLineRunner {

    private final RoleRepository roleRepository;

    @Override
    @Transactional
    public void run(String... args) {
        log.info(" Loading default roles...");
        for (RoleType roleType : RoleType.values()) {
            if (!roleRepository.existsByRoleType(roleType)) {
                Role role = Role.builder()
                        .roleType(roleType)
                        .build();
                roleRepository.save(role);
                log.info(" Role created: {}", roleType);
            } else {
                log.info("ℹ Role already exists: {}", roleType);
            }
        }
    }
}
