package org.example.converterservice.repository;

import org.example.converterservice.model.InboxEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface InboxRepository extends JpaRepository<InboxEntity, UUID> {
    @Modifying
    @Query(value = "INSERT INTO inbox (event_id, status) VALUES (:id,'RECEIVED') ON CONFLICT DO NOTHING", nativeQuery = true)
    int tryInsert(@Param("id") UUID eventId);

    @Modifying
    @Query(value = "UPDATE inbox SET processed_at = CASE WHEN :to = 'PROCESSED' THEN now() ELSE processed_at END, status = :to WHERE event_id = :id AND status IN(:from)",
            nativeQuery = true)
    int updateStatus(@Param("id") UUID eventId, @Param("from")List<String> from, @Param("to")String to);
}
