package org.example.converterservice.repository;

import org.example.converterservice.model.OutboxEntity;
import org.springframework.data.domain.Limit;
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

    @Query(value = "UPDATE outbox SET status = 'SENDING' WHERE id IN(SELECT id FROM outbox WHERE status = 'NEW' ORDER BY created_at LIMIT :limit FOR UPDATE SKIP LOCKED) RETURNING *",
            nativeQuery = true)
    List<OutboxEntity> claimBatch(@Param("limit") int limit);

    @Modifying
    @Query(value = "UPDATE outbox SET attempts = attempts + 1, status = CASE WHEN attempts + 1 >= :max THEN 'ERROR' ELSE 'NEW' END WHERE id IN (:ids) AND status = 'SENDING'", nativeQuery = true)
    int markFailed(@Param("ids") List<UUID> ids, @Param("max") Short maxAttempts);

    @Modifying
    @Query(value = "UPDATE outbox SET published_at = now(), status = 'SENT' WHERE id IN (:ids) AND status = 'SENDING'",
            nativeQuery = true)
    int markSent(@Param("ids") List<UUID> ids);
}
