package kr.or.hieating.promotion.admin.service;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import kr.or.hieating.global.apiPayload.code.status.ErrorStatus;
import kr.or.hieating.global.apiPayload.exception.GeneralException;
import kr.or.hieating.promotion.admin.mapper.AdminPromotionMapper;
import kr.or.hieating.promotion.domain.Promotions;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AdminPromotionService {

  private final AdminPromotionMapper adminPromotionMapper;

  public List<Promotions> getAllPromotions() {
    List<Promotions> promotions = adminPromotionMapper.selectAllPromotions();
    return promotions != null ? promotions : List.of();
  }

  @Transactional
  public Promotions registerPromotion(
      MultipartFile file, String title, String link, LocalDate startsAt, LocalDate endsAt) {
    // 파일 업로드 수행 (중복 코드 제거, 확장자/Content-Type 화이트리스트 검증 포함)
    String imgSrc = uploadImage(file);

    Promotions promotion =
        Promotions.builder()
            .title(title)
            .imgSrc(imgSrc)
            .link(link)
            // display_order는 insert SQL 내 서브쿼리 SELECT COALESCE(MAX(display_order), 0) + 1000 를 통해
            // 원자적으로 계산
            .startsAt(startsAt.atStartOfDay())
            .endsAt(endsAt.atTime(23, 59, 59))
            .build();

    adminPromotionMapper.insertPromotion(promotion);
    return promotion;
  }

  @Transactional
  public void updatePromotionDetails(
      int id, MultipartFile file, String title, String link, LocalDate startsAt, LocalDate endsAt) {
    String newImgSrc = null;
    String oldImgSrcToDelete = null;

    // 새로운 이미지 파일이 전달된 경우 업로드 처리
    if (file != null && !file.isEmpty()) {
      newImgSrc = uploadImage(file);

      // 삭제할 기존 이미지 경로 기억 (DB 업데이트 성공 후에 삭제하기 위함)
      Promotions oldPromotion = adminPromotionMapper.selectPromotionById(id);
      if (oldPromotion != null && oldPromotion.getImgSrc() != null) {
        oldImgSrcToDelete = oldPromotion.getImgSrc();
      }
    }

    Promotions promotion =
        Promotions.builder()
            .id(id)
            .title(title)
            .link(link)
            .imgSrc(newImgSrc)
            .startsAt(startsAt.atStartOfDay())
            .endsAt(endsAt.atTime(23, 59, 59))
            .build();

    adminPromotionMapper.updatePromotion(promotion);

    // DB 업데이트가 성공한 후에 물리적으로 기존 이미지 파일 삭제
    if (oldImgSrcToDelete != null) {
      deletePhysicalFile(oldImgSrcToDelete);
    }
  }

  @Transactional
  public void deletePromotion(int id) {
    // 이미지 파일 삭제를 위해 먼저 DB에서 배너 정보를 조회
    Promotions promotion = adminPromotionMapper.selectPromotionById(id);
    if (promotion != null) {
      deletePhysicalFile(promotion.getImgSrc());
    }

    // 데이터베이스에서 최종 배너 레코드 삭제
    adminPromotionMapper.deletePromotion(id);
  }

  /**
   * 이미지 업로드를 공통 처리하고, 확장자 및 Content-Type을 화이트리스트로 검증합니다. 경로 조작(Path Traversal) 방지를 위해 고유 UUID 파일명만
   * 사용합니다.
   */
  private String uploadImage(MultipartFile file) {
    if (file == null || file.isEmpty()) {
      throw new GeneralException(ErrorStatus.EMPTY_FILE);
    }

    // Content-Type 검증 (이미지 형식만 허용)
    String contentType = file.getContentType();
    if (contentType == null || !contentType.startsWith("image/")) {
      throw new GeneralException(ErrorStatus.INVALID_FILE_TYPE);
    }

    // 확장자 검증
    String originalName = file.getOriginalFilename();
    String ext = "";
    if (originalName != null && originalName.contains(".")) {
      ext = originalName.substring(originalName.lastIndexOf(".")).toLowerCase();
    }

    List<String> allowedExtensions = List.of(".jpg", ".jpeg", ".png", ".gif", ".webp");
    if (!allowedExtensions.contains(ext)) {
      throw new GeneralException(ErrorStatus.INVALID_FILE_TYPE);
    }

    // 고유 파일명 생성
    String fileName = UUID.randomUUID().toString() + ext;

    try {
      String projectPath = System.getProperty("user.dir");

      // 정적 리소스(static) 폴더 경로에 저장
      File uploadDirSrc = new File(projectPath, "src/main/resources/static/images/promotions");
      if (!uploadDirSrc.exists()) {
        uploadDirSrc.mkdirs();
      }
      File destFileSrc = new File(uploadDirSrc, fileName);
      file.transferTo(destFileSrc);

      // 빌드 결과(build/) 디렉토리에 동기화 복사 진행
      File uploadDirBuild = new File(projectPath, "build/resources/main/static/images/promotions");
      if (uploadDirBuild.exists()) {
        File destFileBuild = new File(uploadDirBuild, fileName);
        Files.copy(
            destFileSrc.toPath(), destFileBuild.toPath(), StandardCopyOption.REPLACE_EXISTING);
      }

      return "/images/promotions/" + fileName;
    } catch (Exception e) {
      throw new RuntimeException("프로모션 이미지 저장 중 오류가 발생했습니다.", e);
    }
  }

  /** 로컬 및 빌드 디렉토리의 물리 이미지 파일을 삭제하는 공통 헬퍼 메서드입니다. */
  private void deletePhysicalFile(String imgSrc) {
    if (imgSrc == null || imgSrc.isBlank()) {
      return;
    }
    try {
      String projectPath = System.getProperty("user.dir");

      // 프로젝트 static 소스 디렉토리 내 실제 파일 삭제
      File srcFile = new File(projectPath, "src/main/resources/static" + imgSrc);
      if (srcFile.exists()) {
        srcFile.delete();
      }

      // 빌드 출력(build) 디렉토리 내 복제된 파일도 삭제
      File buildFile = new File(projectPath, "build/resources/main/static" + imgSrc);
      if (buildFile.exists()) {
        buildFile.delete();
      }
    } catch (Exception e) {
      System.err.println("물리적 파일 삭제 실패 (" + imgSrc + "): " + e.getMessage());
    }
  }
}
