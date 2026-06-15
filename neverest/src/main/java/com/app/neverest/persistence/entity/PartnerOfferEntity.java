package com.app.neverest.persistence.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "nev_partner_offers")
public class PartnerOfferEntity {

    @Id
    @Column(name = "id", nullable = false, updatable = false)
    private UUID id;

    @Column(name = "owner_user_id", nullable = false)
    private UUID ownerUserId;

    @Column(name = "brand", nullable = false, length = 120)
    private String brand;

    @Column(name = "title", nullable = false, length = 180)
    private String title;

    @Column(name = "description", length = 700)
    private String description;

    @Column(name = "discount_label", length = 80)
    private String discountLabel;

    @Column(name = "image_b64", columnDefinition = "MEDIUMTEXT")
    private String imageB64;

    @Column(name = "link_url", length = 500)
    private String linkUrl;

    @Column(name = "active", nullable = false)
    private boolean active = true;

    @Column(name = "valid_from")
    private LocalDateTime validFrom;

    @Column(name = "valid_until")
    private LocalDateTime validUntil;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    protected PartnerOfferEntity() {
    }

    public PartnerOfferEntity(UUID id, UUID ownerUserId, String brand, String title) {
        this.id = id;
        this.ownerUserId = ownerUserId;
        this.brand = brand;
        this.title = title;
        this.active = true;
    }

    @PrePersist
    void onCreate() {
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
    }

    public UUID getId() { return id; }
    public UUID getOwnerUserId() { return ownerUserId; }
    public String getBrand() { return brand; }
    public void setBrand(String brand) { this.brand = brand; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public String getDiscountLabel() { return discountLabel; }
    public void setDiscountLabel(String discountLabel) { this.discountLabel = discountLabel; }
    public String getImageB64() { return imageB64; }
    public void setImageB64(String imageB64) { this.imageB64 = imageB64; }
    public String getLinkUrl() { return linkUrl; }
    public void setLinkUrl(String linkUrl) { this.linkUrl = linkUrl; }
    public boolean isActive() { return active; }
    public void setActive(boolean active) { this.active = active; }
    public LocalDateTime getValidFrom() { return validFrom; }
    public void setValidFrom(LocalDateTime validFrom) { this.validFrom = validFrom; }
    public LocalDateTime getValidUntil() { return validUntil; }
    public void setValidUntil(LocalDateTime validUntil) { this.validUntil = validUntil; }
    public LocalDateTime getCreatedAt() { return createdAt; }
}
