package com.zivro.repository;

import com.zivro.domain.Role;
import com.zivro.domain.User;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByEmailIgnoreCase(String email);

    //Finding user by email and fetching worker profile
    @Query(
            "SELECT DISTINCT u FROM User u LEFT JOIN FETCH u.workerProfile WHERE LOWER(u.email) = LOWER(:email)")
    Optional<User> findByEmailIgnoreCaseWithWorker(@Param("email") String email);

    boolean existsByEmailIgnoreCase(String email);

    long countByRole(Role role);
}
