package com.app.neverest.domain;

import java.util.UUID;

public class Reward {

    private final UUID id;
    private final String title;
    private final String partnerName;
    private final String description;
    private final int pointsCost;
    private final String address;
    private final String imageB64;
    private Integer stock;
    private boolean active;

    public Reward(
            UUID id,
            String title,
            String partnerName,
            String description,
            int pointsCost,
            Integer stock
    ) {
        this(id, title, partnerName, description, pointsCost, stock, true, null, null);
    }

    public Reward(
            UUID id,
            String title,
            String partnerName,
            String description,
            int pointsCost,
            Integer stock,
            boolean active
    ) {
        this(id, title, partnerName, description, pointsCost, stock, active, null, null);
    }

    public Reward(
            UUID id,
            String title,
            String partnerName,
            String description,
            int pointsCost,
            Integer stock,
            boolean active,
            String address
    ) {
        this(id, title, partnerName, description, pointsCost, stock, active, address, null);
    }

    public Reward(
            UUID id,
            String title,
            String partnerName,
            String description,
            int pointsCost,
            Integer stock,
            boolean active,
            String address,
            String imageB64
    ) {
        this.id = id;
        this.title = title;
        this.partnerName = partnerName;
        this.description = description;
        this.pointsCost = pointsCost;
        this.stock = stock;
        this.active = active;
        this.address = address;
        this.imageB64 = imageB64;
    }

    public UUID id() {
        return id;
    }

    public String title() {
        return title;
    }

    public String partnerName() {
        return partnerName;
    }

    public String description() {
        return description;
    }

    public int pointsCost() {
        return pointsCost;
    }

    public synchronized Integer stock() {
        return stock;
    }

    public synchronized boolean active() {
        return active;
    }

    public String address() {
        return address;
    }

    public String imageB64() {
        return imageB64;
    }

    public synchronized boolean consumeOneStock() {
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

    public synchronized void restoreOneStock() {
        if (stock != null) {
            stock += 1;
        }
    }

    public synchronized void deactivate() {
        this.active = false;
    }
}
