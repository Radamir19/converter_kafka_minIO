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
@Table(name = "outbox")
@NoArgsConstructor
@Getter
@Setter
public class OutboxEntity {

    @Id
    private UUID id;

    @Column(nullable = false)
    private String aggregate;

    @Column(nullable = false)
    private String payload;

    @CreationTimestamp
    @Column(nullable = false)
    private OffsetDateTime createdAt;

    private OffsetDateTime publishedAt;
}
