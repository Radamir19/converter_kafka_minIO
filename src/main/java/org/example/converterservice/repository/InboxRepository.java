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
    @Query(value = "INSERT INTO inbox (event_id, processed_at) VALUES (:id, now()) ON CONFLICT DO NOTHING", nativeQuery = true)
    int tryInsert(@Param("id") UUID eventId);
}
