package com.github.juliusd.ueberboeseapi;

import jakarta.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.util.StreamUtils;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * Controller that handles all unknown/unmapped requests and forwards them to the configured target
 * host via the ProxyService.
 */
@Controller
@RequiredArgsConstructor
@Slf4j
public class ProxyController {

  private final ProxyService proxyService;

  /**
   * Catches all unmapped requests and forwards them to the target host. This mapping has the lowest
   * priority due to the /** pattern. Actuator endpoints are excluded by using a path condition.
   *
   * @param request the HTTP request
   * @return ResponseEntity with the proxied response
   * @throws IOException if reading the request body fails
   */
  @RequestMapping("/**")
  public ResponseEntity<byte[]> proxyRequest(HttpServletRequest request) throws IOException {
    log.info("Proxying request: {} {}", request.getMethod(), request.getRequestURI());
    Charset charset = getCharset(request);
    String body = StreamUtils.copyToString(request.getInputStream(), charset);
    return proxyService.forwardRequest(request, body);
  }

  private static Charset getCharset(HttpServletRequest request) {
    Charset charset = StandardCharsets.UTF_8;
    if (request.getCharacterEncoding() != null) {
      try {
        charset = Charset.forName(request.getCharacterEncoding());
      } catch (Exception e) {
        // Fallback to UTF-8 if charset is invalid
        charset = StandardCharsets.UTF_8;
      }
    }
    return charset;
  }
}
