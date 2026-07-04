package kr.or.hieating.promotion.admin.config;

import java.io.IOException;
import java.net.http.HttpClient;
import java.time.Duration;
import java.util.List;
import kr.or.hieating.global.apiPayload.code.status.ErrorStatus;
import kr.or.hieating.global.apiPayload.exception.GeneralException;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.MediaType;
import org.springframework.http.client.ClientHttpRequestFactory;
import org.springframework.http.client.JdkClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestClient;
import org.springframework.web.multipart.MultipartFile;

@Component
@Slf4j
public class PromotionImageUploadClient {

  private static final List<String> ALLOWED_EXTENSIONS =
      List.of("jpg", "jpeg", "png", "gif", "webp");

  private final RestClient restClient;
  private final String uploadUrl;
  private final String deleteUrl;
  private final String publicBaseUrl;
  private final String publicPathPrefix;

  public PromotionImageUploadClient(
      RestClient.Builder restClientBuilder,
      @Value("${greenfood.promotion-image.upload-url}") String uploadUrl,
      @Value(
              "${greenfood.promotion-image.delete-url:${greenfood.promotion-image.upload-url}/{filename}}")
          String deleteUrl,
      @Value("${greenfood.promotion-image.public-base-url}") String publicBaseUrl,
      @Value("${greenfood.promotion-image.public-path-prefix:/uploads/images}")
          String publicPathPrefix,
      @Value("${greenfood.promotion-image.connect-timeout-ms:3000}") long connectTimeoutMillis,
      @Value("${greenfood.promotion-image.read-timeout-ms:10000}") long readTimeoutMillis) {
    this.restClient =
        restClientBuilder
            .requestFactory(createRequestFactory(connectTimeoutMillis, readTimeoutMillis))
            .build();
    this.uploadUrl = uploadUrl;
    this.deleteUrl = deleteUrl;
    this.publicBaseUrl = normalizeBaseUrl(publicBaseUrl);
    this.publicPathPrefix = normalizePublicPathPrefix(publicPathPrefix);
  }

  public String upload(MultipartFile file) {
    validateImage(file);

    try {
      UploadResponse response =
          restClient
              .post()
              .uri(uploadUrl)
              .contentType(MediaType.MULTIPART_FORM_DATA)
              .body(createRequestBody(file))
              .retrieve()
              .body(UploadResponse.class);

      if (response == null || !StringUtils.hasText(response.getSavedFileName())) {
        throw new GeneralException(ErrorStatus._INTERNAL_SERVER_ERROR);
      }

      String savedFileName = stripLeadingSlashes(response.getSavedFileName().trim());
      return publicBaseUrl + publicPathPrefix + "/" + savedFileName;
    } catch (GeneralException e) {
      throw e;
    } catch (Exception e) {
      log.warn("Promotion image upload failed. uploadUrl={}", uploadUrl, e);
      throw new GeneralException(ErrorStatus._INTERNAL_SERVER_ERROR);
    }
  }

  public void delete(String imageUrl) {
    String savedFileName = extractSavedFileName(imageUrl);
    if (!StringUtils.hasText(savedFileName)) {
      return;
    }

    try {
      restClient.delete().uri(deleteUrl, savedFileName).retrieve().toBodilessEntity();
    } catch (Exception e) {
      log.warn(
          "Promotion image delete failed. deleteUrl={}, savedFileName={}",
          deleteUrl,
          savedFileName,
          e);
    }
  }

  private ClientHttpRequestFactory createRequestFactory(
      long connectTimeoutMillis, long readTimeoutMillis) {
    HttpClient httpClient =
        HttpClient.newBuilder().connectTimeout(Duration.ofMillis(connectTimeoutMillis)).build();
    JdkClientHttpRequestFactory requestFactory = new JdkClientHttpRequestFactory(httpClient);
    requestFactory.setReadTimeout(Duration.ofMillis(readTimeoutMillis));
    return requestFactory;
  }

  private MultiValueMap<String, Object> createRequestBody(MultipartFile file) throws IOException {
    MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
    body.add("image", createFileResource(file));
    return body;
  }

  private ByteArrayResource createFileResource(MultipartFile file) throws IOException {
    String filename = StringUtils.cleanPath(String.valueOf(file.getOriginalFilename()));
    return new ByteArrayResource(file.getBytes()) {
      @Override
      public String getFilename() {
        return filename;
      }
    };
  }

  private void validateImage(MultipartFile file) {
    if (file == null || file.isEmpty()) {
      throw new GeneralException(ErrorStatus.EMPTY_FILE);
    }

    String contentType = file.getContentType();
    String extension = StringUtils.getFilenameExtension(file.getOriginalFilename());
    if (contentType == null
        || !contentType.startsWith("image/")
        || extension == null
        || !ALLOWED_EXTENSIONS.contains(extension.toLowerCase())) {
      throw new GeneralException(ErrorStatus.INVALID_FILE_TYPE);
    }
  }

  private String normalizePublicPathPrefix(String pathPrefix) {
    String normalized = StringUtils.hasText(pathPrefix) ? pathPrefix.trim() : "/uploads/images";
    if (!normalized.startsWith("/")) {
      normalized = "/" + normalized;
    }
    while (normalized.endsWith("/")) {
      normalized = normalized.substring(0, normalized.length() - 1);
    }
    return normalized;
  }

  private String normalizeBaseUrl(String baseUrl) {
    String normalized = StringUtils.hasText(baseUrl) ? baseUrl.trim() : "";
    while (normalized.endsWith("/")) {
      normalized = normalized.substring(0, normalized.length() - 1);
    }
    return normalized;
  }

  private String extractSavedFileName(String imageUrl) {
    if (!StringUtils.hasText(imageUrl)) {
      return null;
    }

    String trimmedUrl = imageUrl.trim();
    String publicUrlPrefix = publicBaseUrl + publicPathPrefix + "/";
    if (trimmedUrl.startsWith(publicUrlPrefix)) {
      return cleanSavedFileName(trimmedUrl.substring(publicUrlPrefix.length()));
    }

    String relativePathPrefix = publicPathPrefix + "/";
    if (trimmedUrl.startsWith(relativePathPrefix)) {
      return cleanSavedFileName(trimmedUrl.substring(relativePathPrefix.length()));
    }

    return null;
  }

  private String cleanSavedFileName(String savedFileName) {
    String cleaned = stripLeadingSlashes(savedFileName);
    int queryStart = cleaned.indexOf('?');
    if (queryStart >= 0) {
      cleaned = cleaned.substring(0, queryStart);
    }
    int fragmentStart = cleaned.indexOf('#');
    if (fragmentStart >= 0) {
      cleaned = cleaned.substring(0, fragmentStart);
    }
    return cleaned;
  }

  private String stripLeadingSlashes(String value) {
    String stripped = value;
    while (stripped.startsWith("/")) {
      stripped = stripped.substring(1);
    }
    return stripped;
  }

  @Getter
  @Setter
  private static class UploadResponse {

    private String savedFileName;
  }
}
