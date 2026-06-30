package kr.or.hieating.product.service;

import java.util.List;
import kr.or.hieating.product.dto.MostPurchasedProductResponseDto;
import kr.or.hieating.product.mapper.ProductMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ProductService {

  private final ProductMapper productMapper;

  public List<MostPurchasedProductResponseDto> findMostPurchasedProducts() {
    return productMapper.findMostPurchasedProducts();
  }
}
