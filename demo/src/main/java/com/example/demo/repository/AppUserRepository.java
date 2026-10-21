package com.example.demo.repository;

import com.example.demo.entity.AppUser;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import jakarta.persistence.LockModeType;

import java.util.Optional;

public interface AppUserRepository extends JpaRepository<AppUser, Long> {

    Optional<AppUser> findByUsernameIgnoreCase(String username);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select u from AppUser u where lower(u.username) = lower(:username)")
    Optional<AppUser> findForUpdateByUsername(@Param("username") String username);

    boolean existsByUsernameIgnoreCase(String username);

    boolean existsByEmailIgnoreCase(String email);

    long countByRoleAndEnabledTrue(com.example.demo.entity.UserRole role);

    default java.util.List<AppUser> findAllByIdAscending() {
        return findAll(Sort.by(Sort.Direction.ASC, "id"));
    }
}
