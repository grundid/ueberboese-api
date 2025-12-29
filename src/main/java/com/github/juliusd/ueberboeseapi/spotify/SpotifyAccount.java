package com.github.juliusd.ueberboeseapi.spotify;

import java.time.OffsetDateTime;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Version;
import org.springframework.data.relational.core.mapping.Table;

@Table("SPOTIFY_ACCOUNT")
public record SpotifyAccount(
    @Id String spotifyUserId,
    String displayName,
    String refreshToken,
    OffsetDateTime createdAt,
    OffsetDateTime updatedAt,
    @Version Long version) {}
