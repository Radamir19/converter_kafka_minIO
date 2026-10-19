package org.example.converterservice.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;

import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(name = "inbox")
@NoArgsConstructor
@Getter
@Setter
public class InboxEntity {
    @Id
    private UUID eventId;

    @CreationTimestamp
    @Column(nullable = false)
    private OffsetDateTime processedAt;

    @Column(nullable = false)
    private String status;

    public InboxEntity(UUID eventId) {
        this.eventId = eventId;
    }
}
