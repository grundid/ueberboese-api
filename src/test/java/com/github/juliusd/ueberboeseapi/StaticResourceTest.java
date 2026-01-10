package com.github.juliusd.ueberboeseapi;

import static com.github.tomakehurst.wiremock.client.WireMock.getRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlMatching;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.options;
import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.github.tomakehurst.wiremock.WireMockServer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.web.servlet.MockMvc;

class StaticResourceTest extends TestBase {

  @Autowired private MockMvc mockMvc;

  private WireMockServer wireMockServer;

  @BeforeEach
  void setUpWireMock() {
    // Set up main target host mock server to verify static resources are NOT proxied
    wireMockServer = new WireMockServer(options().port(8089));
    wireMockServer.start();
  }

  @AfterEach
  void tearDown() {
    if (wireMockServer != null) {
      wireMockServer.stop();
    }
  }

  @Test
  void shouldServeIconFromLocalStaticResources() throws Exception {
    // When & Then
    byte[] response =
        mockMvc
            .perform(get("/icons/radio-logo-monochrome-small.png"))
            .andExpect(status().isOk())
            .andExpect(content().contentType("image/png"))
            .andReturn()
            .getResponse()
            .getContentAsByteArray();

    // Verify the response contains PNG magic bytes
    assertThat(response).isNotEmpty();
    assertThat(response.length).isGreaterThan(8);
    // PNG files start with magic bytes: 89 50 4E 47 0D 0A 1A 0A
    assertThat(response[0]).isEqualTo((byte) 0x89);
    assertThat(response[1]).isEqualTo((byte) 0x50); // 'P'
    assertThat(response[2]).isEqualTo((byte) 0x4E); // 'N'
    assertThat(response[3]).isEqualTo((byte) 0x47); // 'G'

    // Verify the request was NOT proxied to WireMock
    wireMockServer.verify(0, getRequestedFor(urlMatching("/icons/.*")));
  }

  @Test
  void shouldReturn404ForNonExistentStaticResource() throws Exception {
    // When & Then
    mockMvc.perform(get("/icons/non-existent.png")).andExpect(status().isNotFound());

    // Verify the request was NOT proxied to WireMock
    wireMockServer.verify(0, getRequestedFor(urlMatching("/icons/.*")));
  }
}
