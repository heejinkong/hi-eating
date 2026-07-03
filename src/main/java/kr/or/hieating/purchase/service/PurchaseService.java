package kr.or.hieating.purchase.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import kr.or.hieating.purchase.dto.ProductPurchaseTargetDto;
import kr.or.hieating.purchase.dto.PurchaseCreateCommand;
import kr.or.hieating.purchase.dto.PurchaseOptionAllocationDto;
import kr.or.hieating.purchase.dto.PurchaseProductOptionStockDto;
import kr.or.hieating.purchase.dto.RecentPurchaseProductDto;
import kr.or.hieating.purchase.exception.PurchaseException;
import kr.or.hieating.purchase.mapper.PurchaseMapper;
import kr.or.hieating.utils.ImageUrlResolver;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class PurchaseService {

  private final PurchaseMapper purchaseMapper;
  private final ImageUrlResolver imageUrlResolver;

  public int countPurchases(Long userId) {
    return purchaseMapper.countPurchasesByUserId(userId);
  }

  public Optional<RecentPurchaseProductDto> findLatestPurchaseProduct(Long userId) {
    return purchaseMapper
        .findLatestPurchaseProductByUserId(userId)
        .map(this::resolveProductImageUrl);
  }

  private RecentPurchaseProductDto resolveProductImageUrl(RecentPurchaseProductDto product) {
    product.setPictureLocation(imageUrlResolver.resolve(product.getPictureLocation()));
    return product;
  }

  @Transactional
  public Long purchase(Long userId, Long productId, int quantity) {
    if (quantity < 1) {
      throw new PurchaseException("구매 수량은 1개 이상이어야 합니다.");
    }

    ProductPurchaseTargetDto product =
        purchaseMapper
            .findProductForPurchase(productId)
            .orElseThrow(() -> new PurchaseException("존재하지 않는 상품입니다."));
    if (!"ON_SALE".equals(product.status())) {
      throw new PurchaseException("현재 구매할 수 없는 상품입니다.");
    }

    List<PurchaseProductOptionStockDto> options =
        purchaseMapper.findProductOptionsForPurchase(productId);
    List<PurchaseOptionAllocationDto> allocations = allocateOptions(options, quantity);

    PurchaseCreateCommand command =
        new PurchaseCreateCommand(null, userId, productId, quantity, product.price());
    purchaseMapper.insertPurchase(command);

    Long purchaseId = command.getId();
    if (purchaseId == null) {
      throw new PurchaseException("구매 이력을 생성하지 못했습니다.");
    }

    for (PurchaseOptionAllocationDto allocation : allocations) {
      int updated =
          purchaseMapper.decreaseProductOptionStock(
              allocation.productOptionId(), allocation.quantity());
      if (updated != 1) {
        throw new PurchaseException("상품 재고가 부족합니다.");
      }

      purchaseMapper.insertPurchaseProductOption(
          purchaseId, allocation.productOptionId(), allocation.quantity());
    }

    return purchaseId;
  }

  private List<PurchaseOptionAllocationDto> allocateOptions(
      List<PurchaseProductOptionStockDto> options, int quantity) {
    int remainingQuantity = quantity;
    List<PurchaseOptionAllocationDto> allocations = new ArrayList<>();

    for (PurchaseProductOptionStockDto option : options) {
      if (remainingQuantity == 0) {
        break;
      }

      int allocatedQuantity = Math.min(option.stock(), remainingQuantity);
      allocations.add(new PurchaseOptionAllocationDto(option.productOptionId(), allocatedQuantity));
      remainingQuantity -= allocatedQuantity;
    }

    if (remainingQuantity > 0) {
      throw new PurchaseException("상품 재고가 부족합니다.");
    }

    return allocations;
  }
}
