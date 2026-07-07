package kr.or.hieating.recommendation.service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import kr.or.hieating.product.domain.Product;
import kr.or.hieating.product.mapper.ProductMapper;
import kr.or.hieating.recommendation.domain.ProductEmbedding;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Order(2)
public class ProductEmbeddingService implements CommandLineRunner {

  private static final Logger log = LoggerFactory.getLogger(ProductEmbeddingService.class);

  private final ProductMapper productMapper;
  private final org.springframework.ai.embedding.EmbeddingModel embeddingModel;

  private final Map<Long, ProductEmbedding> productEmbeddings = new ConcurrentHashMap<>();
  private final Map<Long, String> productNames = new ConcurrentHashMap<>();

  @Override
  public void run(String... args) {
    log.info("상품 임베딩 자동 초기화 시작...");
    generateAllProductEmbeddings();
  }

  public void generateAllProductEmbeddings() {
    log.info("모든 ON_SALE 상품의 임베딩 생성 시작");

    List<Product> products = productMapper.findAllActiveProducts();
    log.info("활성 상품 수: {}", products.size());

    for (Product product : products) {
      try {
        String text = generateEmbeddingText(product);
        float[] raw = embeddingModel.embed(text);
        List<Float> embedding = toFloatList(raw);

        ProductEmbedding productEmbedding =
            new ProductEmbedding(product.id(), embedding, embedding.size());
        productEmbeddings.put(product.id(), productEmbedding);
        productNames.put(product.id(), product.name());

        log.debug("상품 {} 임베딩 생성 완료. 차원: {}", product.id(), embedding.size());
      } catch (Exception e) {
        log.error("상품 {} 임베딩 생성 실패: {}", product.id(), e.getMessage(), e);
      }
    }

    log.info("모든 ON_SALE 상품의 임베딩 생성 완료. 총 상품 수: {}", productEmbeddings.size());
  }

  public ProductEmbedding getProductEmbedding(Long productId) {
    ProductEmbedding embedding = productEmbeddings.get(productId);
    if (embedding == null) {
      throw new IllegalArgumentException("상품 " + productId + "의 임베딩이 존재하지 않습니다.");
    }
    return embedding;
  }

  public List<ProductEmbedding> getAllProductEmbeddings() {
    return new ArrayList<>(productEmbeddings.values());
  }

  private String generateEmbeddingText(Product product) {
    return product.name();
  }

  public void updateProductEmbedding(Long productId) {
    try {
      Product product = findProductByIdIgnoreStatus(productId);
      String text = generateEmbeddingText(product);
      float[] raw = embeddingModel.embed(text);
      List<Float> newEmbedding = toFloatList(raw);

      ProductEmbedding newEmbeddingEntity =
          new ProductEmbedding(product.id(), newEmbedding, newEmbedding.size());
      productEmbeddings.put(productId, newEmbeddingEntity);
      productNames.put(productId, product.name());

      log.debug("상품 {} 임베딩 갱신 완료", productId);
    } catch (Exception e) {
      log.error("상품 {} 임베딩 갱신 실패: {}", productId, e.getMessage(), e);
      throw new IllegalStateException("상품 " + productId + " 임베딩 갱신 실패", e);
    }
  }

  private Product findProductByIdIgnoreStatus(Long productId) {
    return productMapper
        .findByIdIgnoreStatus(productId)
        .orElseThrow(() -> new IllegalArgumentException("상품 " + productId + "을(를) 찾을 수 없습니다."));
  }

  public Product findProductById(Long productId) {
    return productMapper
        .findById(productId)
        .orElseThrow(() -> new IllegalArgumentException("상품 " + productId + "을(를) 찾을 수 없습니다."));
  }

  public Map<Long, List<Float>> getEmbeddingsForProductIds(List<Long> productIds) {
    Map<Long, List<Float>> result = new HashMap<>();
    for (Long productId : productIds) {
      ProductEmbedding embedding = getProductEmbedding(productId);
      result.put(productId, embedding.embedding());
    }
    return result;
  }

  public List<List<Float>> getEmbeddingsForMultipleTexts(List<String> texts) {
    try {
      List<float[]> rawList = embeddingModel.embed(texts);
      return rawList.stream().map(this::toFloatList).collect(java.util.stream.Collectors.toList());
    } catch (Exception e) {
      log.error("여러 텍스트 임베딩 생성 실패: {}", e.getMessage(), e);
      throw new RuntimeException("임베딩 생성 실패", e);
    }
  }

  public String getProductName(Long productId) {
    String name = productNames.get(productId);
    if (name == null) {
      Product product = findProductById(productId);
      name = product.name();
      productNames.put(productId, name);
    }
    return name;
  }

  private List<Float> toFloatList(float[] array) {
    List<Float> list = new ArrayList<>(array.length);
    for (float v : array) {
      list.add(v);
    }
    return list;
  }
}
