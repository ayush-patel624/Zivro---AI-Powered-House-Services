package com.zivro.repository;

import com.zivro.domain.Dispute;
import com.zivro.domain.DisputeStatus;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DisputeRepository extends JpaRepository<Dispute, Long> {

    List<Dispute> findAllByOrderByCreatedAtDesc();

    long countByStatus(DisputeStatus status);

    boolean existsByBooking_Id(Long bookingId);
}
