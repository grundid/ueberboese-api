package com.github.juliusd.ueberboeseapi.recent;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jdbc.repository.query.Modifying;
import org.springframework.data.jdbc.repository.query.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface RecentRepository extends CrudRepository<Recent, Long> {

  @Query(
      "SELECT * FROM RECENT WHERE ACCOUNT_ID = :accountId AND LOCATION = :location AND SOURCE_ID ="
          + " :sourceId")
  Optional<Recent> findByAccountIdAndLocationAndSourceId(
      String accountId, String location, String sourceId);

  @Modifying
  @Query("DELETE FROM RECENT WHERE ACCOUNT_ID = :accountId AND ID = :id")
  void deleteByAccountIdAndId(String accountId, Long id);

  @Query("SELECT * FROM RECENT WHERE ACCOUNT_ID = :accountId ORDER BY LAST_PLAYED_AT DESC")
  List<Recent> findAllByAccountId(String accountId);
}
