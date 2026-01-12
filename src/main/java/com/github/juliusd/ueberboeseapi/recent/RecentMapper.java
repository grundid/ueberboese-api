package com.github.juliusd.ueberboeseapi.recent;

import com.github.juliusd.ueberboeseapi.generated.dtos.CredentialApiDto;
import com.github.juliusd.ueberboeseapi.generated.dtos.RecentItemApiDto;
import com.github.juliusd.ueberboeseapi.generated.dtos.SourceApiDto;
import java.time.OffsetDateTime;
import java.util.List;
import org.jspecify.annotations.NonNull;
import org.springframework.stereotype.Component;

@Component
public class RecentMapper {
  public List<RecentItemApiDto> convertToApiDtos(List<Recent> recents, List<SourceApiDto> sources) {
    return recents.stream().map(recent -> convertToApiDto(recent, sources)).toList();
  }

  private RecentItemApiDto convertToApiDto(Recent recent, List<SourceApiDto> sources) {

    SourceApiDto source =
        sources.stream()
            .filter(it -> it.getId().equals(recent.sourceId()))
            .findFirst()
            .orElseGet(() -> createMockSource(recent));

    // Create recent item
    RecentItemApiDto recentItem = new RecentItemApiDto();
    recentItem.setId(String.valueOf(recent.id()));
    recentItem.setContentItemType(recent.contentItemType());
    recentItem.setCreatedOn(recent.createdOn());
    recentItem.setUpdatedOn(recent.updatedOn());
    recentItem.setLastplayedat(recent.lastPlayedAt());
    recentItem.setLocation(recent.location());
    recentItem.setName(recent.name());
    recentItem.setSourceid(recent.sourceId());
    recentItem.setSource(source);

    return recentItem;
  }

  private static @NonNull SourceApiDto createMockSource(Recent recent) {
    CredentialApiDto credential = new CredentialApiDto();
    SourceApiDto source = new SourceApiDto();
    source.setId(recent.sourceId());
    source.setType("Audio");
    source.setCreatedOn(OffsetDateTime.parse("2018-08-11T08:55:28.000+00:00"));
    source.setUpdatedOn(OffsetDateTime.parse("2019-07-20T17:48:31.000+00:00"));

    // Set source-specific mock data based on sourceId
    if ("19989643".equals(recent.sourceId())) {
      // Spotify source (user1namespot)
      credential.setType("token_version_3");
      credential.setValue("mockTokenUser2");
      source.setCredential(credential);
      source.setName("user1namespot");
      source.setSourceproviderid("15");
      source.setSourcename("user1@example.org");
      source.setUsername("user1namespot");
    } else if ("19989342".equals(recent.sourceId())) {
      // TuneIn source
      credential.setType("token");
      credential.setValue("eyJduTune=");
      source.setCredential(credential);
      source.setName("");
      source.setSourceproviderid("25");
      source.setSourcename("");
      source.setUsername("");
    } else {
      // Default source
      credential.setType("token");
      credential.setValue("eyDu=");
      source.setCredential(credential);
      source.setName("");
      source.setSourceproviderid("25");
      source.setSourcename("");
      source.setUsername("");
    }
    return source;
  }
}
