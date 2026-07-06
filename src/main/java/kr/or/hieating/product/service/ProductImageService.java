package kr.or.hieating.product.service;

import java.util.List;
import java.util.Map;
import kr.or.hieating.product.mapper.ProductMapper;
import kr.or.hieating.utils.ImageUrlResolver;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ProductImageService {

  private final ProductMapper productMapper;
  private final ImageUrlResolver imageUrlResolver;

  public Map<Long, String> getImageUrls(List<Long> productIds) {
    return productIds.stream()
        .collect(
            java.util.stream.Collectors.toMap(
                productId -> productId, this::getProductImage, (a, b) -> a));
  }

  public String getProductImage(Long productId) {
    return imageUrlResolver.resolve(getFirstImageLocation(productId));
  }

  private String getFirstImageLocation(Long productId) {
    return productMapper.findProductImageUrls(productId).stream()
        .findFirst()
        .orElse("/images/logo-hi-eating.png");
  }
}
