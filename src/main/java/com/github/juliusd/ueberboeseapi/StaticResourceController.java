package com.github.juliusd.ueberboeseapi;

import jakarta.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.io.InputStream;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * Controller for serving static resources.
 *
 * <p>This controller handles requests for static resources (like icons) and serves them locally
 * instead of proxying to external hosts. It has higher priority than the ProxyController's
 * catch-all mapping because it uses a more specific path pattern.
 */
@Controller
@RequestMapping("/icons")
public class StaticResourceController {

  /**
   * Serves static icon files from classpath:/static/icons/.
   *
   * @param request the HTTP request
   * @return ResponseEntity with the file contents or 404 if not found
   * @throws IOException if reading the file fails
   */
  @GetMapping("/**")
  public ResponseEntity<byte[]> serveIcon(HttpServletRequest request) throws IOException {
    // Extract the path after /icons/
    String path = request.getRequestURI().substring("/icons/".length());
    Resource resource = new ClassPathResource("static/icons/" + path);

    if (!resource.exists() || !resource.isReadable()) {
      return ResponseEntity.notFound().build();
    }

    // Read the file content
    byte[] content;
    try (InputStream inputStream = resource.getInputStream()) {
      content = inputStream.readAllBytes();
    }

    // Determine content type from file extension
    String contentType = determineContentType(path);

    HttpHeaders headers = new HttpHeaders();
    headers.setContentType(MediaType.parseMediaType(contentType));
    headers.setContentLength(content.length);

    return new ResponseEntity<>(content, headers, HttpStatus.OK);
  }

  private String determineContentType(String filename) {
    if (filename.endsWith(".png")) {
      return "image/png";
    } else if (filename.endsWith(".jpg") || filename.endsWith(".jpeg")) {
      return "image/jpeg";
    } else if (filename.endsWith(".gif")) {
      return "image/gif";
    } else if (filename.endsWith(".svg")) {
      return "image/svg+xml";
    } else if (filename.endsWith(".ico")) {
      return "image/x-icon";
    }
    return "application/octet-stream";
  }
}
