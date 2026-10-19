package org.example.converterservice.repository;

import org.example.converterservice.model.InboxEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface InboxRepository extends JpaRepository<InboxEntity, UUID> {
    @Modifying
    @Query(value = "INSERT INTO inbox (event_id, status) VALUES (:id,'RECEIVED') ON CONFLICT DO NOTHING", nativeQuery = true)
    int tryInsert(@Param("id") UUID eventId);

    @Modifying
    @Query(value = "UPDATE inbox SET status = 'IN_PROCESS' WHERE event_id = :id AND status = 'RECEIVED'", nativeQuery = true)
    int tryProcess(@Param("id") UUID eventId);

    @Modifying
    @Query(value = "UPDATE inbox SET status = 'PROCESSED', processed_at = now() WHERE event_id = :id AND status = 'IN_PROCESS'", nativeQuery = true)
    int tryUpdate(@Param("id") UUID eventId);

    @Modifying
    @Query(value = "UPDATE inbox SET status = 'ERROR', processed_at = now() WHERE event_id = :id AND status <> 'SENT'", nativeQuery = true)
    void setErrorStatus(@Param("id") UUID eventId);

    @Modifying
    @Query(value = "UPDATE inbox SET status = 'RECEIVED' WHERE event_id = :id AND status = 'IN_PROCESS'", nativeQuery = true)
    void release(@Param("id") UUID eventId);
}
