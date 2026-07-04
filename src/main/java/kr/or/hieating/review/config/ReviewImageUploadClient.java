package kr.or.hieating.review.config;

import java.io.IOException;
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
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestClient;
import org.springframework.web.multipart.MultipartFile;

@Component
@Slf4j
public class ReviewImageUploadClient {

  private static final List<String> ALLOWED_EXTENSIONS =
      List.of("jpg", "jpeg", "png", "gif", "webp");

  private final RestClient restClient;
  private final String uploadUrl;
  private final String publicPathPrefix;

  public ReviewImageUploadClient(
      RestClient.Builder restClientBuilder,
      @Value("${greenfood.review-image.upload-url}") String uploadUrl,
      @Value("${greenfood.review-image.public-path-prefix:/uploads/images}")
          String publicPathPrefix,
      @Value("${greenfood.review-image.connect-timeout-ms:3000}") long connectTimeoutMillis,
      @Value("${greenfood.review-image.read-timeout-ms:10000}") long readTimeoutMillis) {
    this.restClient =
        restClientBuilder
            .requestFactory(createRequestFactory(connectTimeoutMillis, readTimeoutMillis))
            .build();
    this.uploadUrl = uploadUrl;
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
        throw new GeneralException(ErrorStatus.REVIEW_IMAGE_UPLOAD_FAILED);
      }

      return publicPathPrefix + "/" + response.getSavedFileName();
    } catch (GeneralException e) {
      throw e;
    } catch (Exception e) {
      log.warn("Review image upload failed. uploadUrl={}", uploadUrl, e);
      throw new GeneralException(ErrorStatus.REVIEW_IMAGE_UPLOAD_FAILED);
    }
  }

  private SimpleClientHttpRequestFactory createRequestFactory(
      long connectTimeoutMillis, long readTimeoutMillis) {
    SimpleClientHttpRequestFactory requestFactory = new SimpleClientHttpRequestFactory();
    requestFactory.setConnectTimeout(Duration.ofMillis(connectTimeoutMillis));
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
      throw new GeneralException(ErrorStatus.EMPTY_REVIEW_IMAGE_FILE);
    }

    String contentType = file.getContentType();
    if (contentType == null || !contentType.startsWith("image/")) {
      throw new GeneralException(ErrorStatus.INVALID_REVIEW_IMAGE_FILE_TYPE);
    }

    String extension = StringUtils.getFilenameExtension(file.getOriginalFilename());
    if (extension == null || !ALLOWED_EXTENSIONS.contains(extension.toLowerCase())) {
      throw new GeneralException(ErrorStatus.INVALID_REVIEW_IMAGE_FILE_TYPE);
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

  @Getter
  @Setter
  private static class UploadResponse {

    private String message;
    private String originalName;
    private String savedFileName;
    private String savedPath;
    private Long size;
  }
}
