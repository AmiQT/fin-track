package com.amiqt.fintrackpro.repository;

import com.amiqt.fintrackpro.model.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByEmail(String email);
    List<User> findByRoleIn(List<com.amiqt.fintrackpro.enums.Role> roles);
}
