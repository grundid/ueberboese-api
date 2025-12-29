package com.github.juliusd.ueberboeseapi.spotify;

import java.util.List;
import org.springframework.data.jdbc.repository.query.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SpotifyAccountRepository extends CrudRepository<SpotifyAccount, String> {

  @Query("SELECT * FROM SPOTIFY_ACCOUNT ORDER BY CREATED_AT DESC")
  List<SpotifyAccount> findAllByOrderByCreatedAtDesc();

  boolean existsBySpotifyUserId(String spotifyUserId);
}
