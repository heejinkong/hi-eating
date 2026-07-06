package kr.or.hieating.purchase.mapper;

import java.util.List;
import java.util.Optional;
import kr.or.hieating.purchase.dto.ProductPurchaseTargetDto;
import kr.or.hieating.purchase.dto.PurchaseCreateCommand;
import kr.or.hieating.purchase.dto.PurchaseProductListItemResponseDto;
import kr.or.hieating.purchase.dto.PurchaseProductListSearchCondition;
import kr.or.hieating.purchase.dto.PurchaseProductOptionStockDto;
import kr.or.hieating.purchase.dto.RecentPurchaseProductDto;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface PurchaseMapper {

  Optional<ProductPurchaseTargetDto> findProductForPurchase(@Param("productId") Long productId);

  int countPurchasesByUserId(@Param("userId") Long userId);

  Optional<RecentPurchaseProductDto> findLatestPurchaseProductByUserId(
      @Param("userId") Long userId);

  List<PurchaseProductListItemResponseDto> findPurchaseProducts(
      PurchaseProductListSearchCondition condition);

  List<PurchaseProductOptionStockDto> findProductOptionsForPurchase(
      @Param("productId") Long productId);

  int insertPurchase(PurchaseCreateCommand command);

  int insertPurchaseProductOption(
      @Param("purchaseId") Long purchaseId,
      @Param("productOptionId") Long productOptionId,
      @Param("quantity") int quantity);

  int decreaseProductOptionStock(
      @Param("productOptionId") Long productOptionId, @Param("quantity") int quantity);
}
