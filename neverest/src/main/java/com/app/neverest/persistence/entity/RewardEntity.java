package com.app.neverest.persistence.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import jakarta.persistence.Version;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "nev_rewards")
public class RewardEntity {

    @Id
    @Column(name = "id", nullable = false, updatable = false)
    private UUID id;

    @Column(name = "title", nullable = false, length = 180)
    private String title;

    @Column(name = "partner_name", nullable = false, length = 180)
    private String partnerName;

    @Column(name = "description", nullable = false, length = 700)
    private String description;

    @Column(name = "points_cost", nullable = false)
    private int pointsCost;

    @Column(name = "stock")
    private Integer stock;

    @Column(name = "active", nullable = false)
    private boolean active;

    @Version
    @Column(name = "version", nullable = false)
    private long version;

    @Column(name = "address", length = 300)
    private String address;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    protected RewardEntity() {
    }

    public RewardEntity(
            UUID id,
            String title,
            String partnerName,
            String description,
            int pointsCost,
            Integer stock,
            String address
    ) {
        this.id = id;
        this.title = title;
        this.partnerName = partnerName;
        this.description = description;
        this.pointsCost = pointsCost;
        this.stock = stock;
        this.address = address;
        this.active = true;
    }

    public RewardEntity(
            UUID id,
            String title,
            String partnerName,
            String description,
            int pointsCost,
            Integer stock
    ) {
        this(id, title, partnerName, description, pointsCost, stock, null);
    }

    @PrePersist
    void onCreate() {
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
    }

    public UUID getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public String getPartnerName() {
        return partnerName;
    }

    public String getDescription() {
        return description;
    }

    public int getPointsCost() {
        return pointsCost;
    }

    public Integer getStock() {
        return stock;
    }

    public boolean isActive() {
        return active;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public boolean consumeOneStock() {
        if (!active) {
            return false;
        }

        if (stock == null) {
            return true;
        }

        if (stock <= 0) {
            return false;
        }

        stock -= 1;
        return true;
    }

    public void restoreOneStock() {
        if (stock != null) {
            stock += 1;
        }
    }

    public void deactivate() {
        this.active = false;
    }
}
