package org.example.converterservice.repository;

import org.example.converterservice.model.OutboxEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Repository
public interface OutboxRepository extends JpaRepository<OutboxEntity, UUID> {
    List<OutboxEntity> findTop10ByStatusOrderByCreatedAtAsc(String status);

    @Modifying
    @Query(value = "UPDATE outbox SET status = 'SENDING' WHERE id = :id AND status = 'NEw'", nativeQuery = true)
    int trySend(@Param("id") UUID eventId);

    @Modifying
    @Query(value = "UPDATE outbox SET status = 'SENT', published_at = now() WHERE id = :id AND status  = 'SENDING'", nativeQuery = true)
    int tryUpdate(@Param("id") UUID eventId);

    @Modifying
    @Query(value = "UPDATE outbox SET max_attempts = max_attempts + 1, status = CASE WHEN max_attempts + 1 >= :max THEN 'ERROR' ELSE 'NEW' END WHERE id = :id AND status = 'SENDING'", nativeQuery = true)
    int markFailed(@Param("id") UUID id, @Param("max") Short maxAttempts);
}
