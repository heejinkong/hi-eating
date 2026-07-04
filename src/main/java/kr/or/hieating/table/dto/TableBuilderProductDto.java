package kr.or.hieating.table.dto;

public class TableBuilderProductDto {

  private Long productId;
  private String productName;
  private int price;
  private Integer salePrice;
  private int discountRate;
  private String pictureLocation;
  private String glbSrc;
  private boolean favorite;

  public boolean hasModel() {
    return glbSrc != null && !glbSrc.isBlank();
  }

  public Long getProductId() {
    return productId;
  }

  public void setProductId(Long productId) {
    this.productId = productId;
  }

  public String getProductName() {
    return productName;
  }

  public void setProductName(String productName) {
    this.productName = productName;
  }

  public int getPrice() {
    return price;
  }

  public void setPrice(int price) {
    this.price = price;
  }

  public Integer getSalePrice() {
    return salePrice;
  }

  public void setSalePrice(Integer salePrice) {
    this.salePrice = salePrice;
  }

  public int getDiscountRate() {
    return discountRate;
  }

  public void setDiscountRate(int discountRate) {
    this.discountRate = discountRate;
  }

  public String getPictureLocation() {
    return pictureLocation;
  }

  public void setPictureLocation(String pictureLocation) {
    this.pictureLocation = pictureLocation;
  }

  public String getGlbSrc() {
    return glbSrc;
  }

  public void setGlbSrc(String glbSrc) {
    this.glbSrc = glbSrc;
  }

  public boolean isFavorite() {
    return favorite;
  }

  public void setFavorite(boolean favorite) {
    this.favorite = favorite;
  }
}
