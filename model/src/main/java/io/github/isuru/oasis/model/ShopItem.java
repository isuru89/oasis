package io.github.isuru.oasis.model;

public class ShopItem {

    private Long id;
    private String title;
    private String description;

    private int forHero;

    private float price;
    private String imageRef;

    private String scope;
    private Integer level;
    private Long expirationAt;
    private Integer maxAvailableItems;

    public int getForHero() {
        return forHero;
    }

    public void setForHero(int forHero) {
        this.forHero = forHero;
    }

    public Integer getMaxAvailableItems() {
        return maxAvailableItems;
    }

    public void setMaxAvailableItems(Integer maxAvailableItems) {
        this.maxAvailableItems = maxAvailableItems;
    }

    public Long getExpirationAt() {
        return expirationAt;
    }

    public void setExpirationAt(Long expirationAt) {
        this.expirationAt = expirationAt;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public float getPrice() {
        return price;
    }

    public void setPrice(float price) {
        this.price = price;
    }

    public String getImageRef() {
        return imageRef;
    }

    public void setImageRef(String imageRef) {
        this.imageRef = imageRef;
    }

    public String getScope() {
        return scope;
    }

    public void setScope(String scope) {
        this.scope = scope;
    }

    public Integer getLevel() {
        return level;
    }

    public void setLevel(Integer level) {
        this.level = level;
    }
}
