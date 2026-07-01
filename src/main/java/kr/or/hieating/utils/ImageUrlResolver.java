package kr.or.hieating.utils;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class ImageUrlResolver {

  private final String serverIp;

  public ImageUrlResolver(@Value("${SERVER_IP:}") String serverIp) {
    this.serverIp = serverIp;
  }

  public String resolve(String imagePath) {
    if (imagePath == null || imagePath.isBlank() || isAbsoluteUrl(imagePath)) {
      return imagePath;
    }

    String baseUrl = normalizeBaseUrl(serverIp);
    if (baseUrl.isBlank()) {
      return imagePath;
    }

    return baseUrl + normalizePath(imagePath);
  }

  private boolean isAbsoluteUrl(String imagePath) {
    return imagePath.startsWith("http://") || imagePath.startsWith("https://");
  }

  private String normalizeBaseUrl(String serverIp) {
    if (serverIp == null || serverIp.isBlank()) {
      return "";
    }

    String trimmedServerIp = serverIp.trim();
    String baseUrl = isAbsoluteUrl(trimmedServerIp) ? trimmedServerIp : "http://" + trimmedServerIp;

    while (baseUrl.endsWith("/")) {
      baseUrl = baseUrl.substring(0, baseUrl.length() - 1);
    }

    return baseUrl;
  }

  private String normalizePath(String imagePath) {
    String trimmedPath = imagePath.trim();
    return trimmedPath.startsWith("/") ? trimmedPath : "/" + trimmedPath;
  }
}
