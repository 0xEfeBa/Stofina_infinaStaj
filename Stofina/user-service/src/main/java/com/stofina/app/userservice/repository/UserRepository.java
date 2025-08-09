package com.stofina.app.userservice.repository;

import com.stofina.app.commondata.model.enums.UserStatus;
import com.stofina.app.userservice.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByEmail(String email);

    List<User> findAllByStatus(UserStatus status);

    boolean existsByEmail(String email);

 }
