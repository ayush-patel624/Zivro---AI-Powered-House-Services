package com.zivro.repository;

import com.zivro.domain.User;
import com.zivro.domain.Worker;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

public interface WorkerRepository extends JpaRepository<Worker, Long> {

    @EntityGraph(attributePaths = "user")
    List<Worker> findAllByOrderByIdAsc();

    @EntityGraph(attributePaths = "user")
    List<Worker> findByVerifiedTrueOrderByRatingDesc();

    Optional<Worker> findByUser(User user);

    boolean existsByEmployeeId(String employeeId);

    long countByVerified(boolean verified);
}
