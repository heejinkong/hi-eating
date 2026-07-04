package kr.or.hieating.table.service;

import java.util.List;
import kr.or.hieating.review.config.ReviewImageUploadClient;
import kr.or.hieating.review.utils.ReviewImageUrlResolver;
import kr.or.hieating.table.dto.TableBuilderProductDto;
import kr.or.hieating.table.dto.TableCaptureCreateCommand;
import kr.or.hieating.table.dto.TableCaptureResponseDto;
import kr.or.hieating.table.mapper.TableBuilderMapper;
import kr.or.hieating.utils.ImageUrlResolver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionTemplate;
import org.springframework.web.multipart.MultipartFile;

@Service
public class TableBuilderService {

  private static final Logger log = LoggerFactory.getLogger(TableBuilderService.class);

  private final TableBuilderMapper tableBuilderMapper;
  private final ImageUrlResolver imageUrlResolver;
  private final ReviewImageUrlResolver reviewImageUrlResolver;
  private final ReviewImageUploadClient reviewImageUploadClient;
  private final TransactionTemplate transactionTemplate;

  public TableBuilderService(
      TableBuilderMapper tableBuilderMapper,
      ImageUrlResolver imageUrlResolver,
      ReviewImageUrlResolver reviewImageUrlResolver,
      ReviewImageUploadClient reviewImageUploadClient,
      TransactionTemplate transactionTemplate) {
    this.tableBuilderMapper = tableBuilderMapper;
    this.imageUrlResolver = imageUrlResolver;
    this.reviewImageUrlResolver = reviewImageUrlResolver;
    this.reviewImageUploadClient = reviewImageUploadClient;
    this.transactionTemplate = transactionTemplate;
  }

  public List<TableBuilderProductDto> findProducts(Long userId) {
    List<TableBuilderProductDto> products = tableBuilderMapper.findTableBuilderProducts(userId);
    products.forEach(
        product -> {
          product.setPictureLocation(imageUrlResolver.resolve(product.getPictureLocation()));
          product.setGlbSrc(imageUrlResolver.resolve(product.getGlbSrc()));
        });
    return products;
  }

  public TableCaptureResponseDto createCapturePost(Long userId, MultipartFile captureImage) {
    String imgSrc = reviewImageUploadClient.upload(captureImage);

    try {
      return transactionTemplate.execute(status -> insertPost(userId, imgSrc));
    } catch (RuntimeException e) {
      log.warn("Table capture post insert failed after image upload. orphanImgSrc={}", imgSrc, e);
      throw e;
    }
  }

  private TableCaptureResponseDto insertPost(Long userId, String imgSrc) {
    TableCaptureCreateCommand command = new TableCaptureCreateCommand(null, userId, imgSrc);
    tableBuilderMapper.insertPost(command);
    return new TableCaptureResponseDto(command.getId(), reviewImageUrlResolver.resolve(imgSrc));
  }
}
