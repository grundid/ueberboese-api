package com.github.juliusd.ueberboeseapi.recent;

import java.time.OffsetDateTime;
import lombok.Builder;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Version;
import org.springframework.data.relational.core.mapping.Table;

@Builder
@Table("RECENT")
public record Recent(
    @Id Long id,
    String accountId,
    String name,
    String location,
    String sourceId,
    String contentItemType,
    String deviceId,
    OffsetDateTime lastPlayedAt,
    OffsetDateTime createdOn,
    OffsetDateTime updatedOn,
    @Version Long version) {}
