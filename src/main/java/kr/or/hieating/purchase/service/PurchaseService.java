package kr.or.hieating.purchase.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import kr.or.hieating.global.apiPayload.code.status.ErrorStatus;
import kr.or.hieating.global.apiPayload.exception.GeneralException;
import kr.or.hieating.purchase.dto.ProductPurchaseTargetDto;
import kr.or.hieating.purchase.dto.PurchaseCreateCommand;
import kr.or.hieating.purchase.dto.PurchaseOptionAllocationDto;
import kr.or.hieating.purchase.dto.PurchaseProductListPageResponseDto;
import kr.or.hieating.purchase.dto.PurchaseProductListSearchCondition;
import kr.or.hieating.purchase.dto.PurchaseProductOptionStockDto;
import kr.or.hieating.purchase.dto.RecentPurchaseProductDto;
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

  public PurchaseProductListPageResponseDto findPurchaseProducts(
      PurchaseProductListSearchCondition condition) {
    int totalCount = purchaseMapper.countPurchasesByUserId(condition.getUserId());
    int totalPages = Math.max((int) Math.ceil((double) totalCount / condition.getSize()), 1);
    var products = purchaseMapper.findPurchaseProducts(condition);

    products.forEach(
        product ->
            product.setPictureLocation(imageUrlResolver.resolve(product.getPictureLocation())));

    return new PurchaseProductListPageResponseDto(
        products, condition.getPage(), condition.getSize(), totalCount, totalPages);
  }

  private RecentPurchaseProductDto resolveProductImageUrl(RecentPurchaseProductDto product) {
    product.setPictureLocation(imageUrlResolver.resolve(product.getPictureLocation()));
    return product;
  }

  @Transactional
  public Long purchase(Long userId, Long productId, int quantity) {
    if (quantity < 1) {
      throw new GeneralException(ErrorStatus.PURCHASE_INVALID_QUANTITY);
    }

    ProductPurchaseTargetDto product =
        purchaseMapper
            .findProductForPurchase(productId)
            .orElseThrow(() -> new GeneralException(ErrorStatus.PURCHASE_PRODUCT_NOT_FOUND));
    if (!"ON_SALE".equals(product.status())) {
      throw new GeneralException(ErrorStatus.PURCHASE_PRODUCT_NOT_ON_SALE);
    }

    List<PurchaseProductOptionStockDto> options =
        purchaseMapper.findProductOptionsForPurchase(productId);
    List<PurchaseOptionAllocationDto> allocations = allocateOptions(options, quantity);

    PurchaseCreateCommand command =
        new PurchaseCreateCommand(null, userId, productId, quantity, product.price());
    purchaseMapper.insertPurchase(command);

    Long purchaseId = command.getId();
    if (purchaseId == null) {
      throw new GeneralException(ErrorStatus.PURCHASE_CREATION_FAILED);
    }

    for (PurchaseOptionAllocationDto allocation : allocations) {
      int updated =
          purchaseMapper.decreaseProductOptionStock(
              allocation.productOptionId(), allocation.quantity());
      if (updated != 1) {
        throw new GeneralException(ErrorStatus.PURCHASE_OUT_OF_STOCK);
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
      throw new GeneralException(ErrorStatus.PURCHASE_OUT_OF_STOCK);
    }

    return allocations;
  }
}
