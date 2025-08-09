package com.stofina.app.userservice.repository;


import com.stofina.app.commondata.model.enums.RoleType;
import com.stofina.app.userservice.model.Role;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
@Repository
public interface RoleRepository extends JpaRepository<Role,Long> {

    Optional<Role> findByRoleType(RoleType name);
    boolean existsByRoleType(RoleType roleType);

}
