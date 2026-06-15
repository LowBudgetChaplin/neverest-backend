package com.app.neverest.persistence.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "nev_reward_categories")
public class RewardCategoryEntity {

    @Id
    @Column(name = "code", length = 40, nullable = false)
    private String code;

    @Column(name = "label_en", length = 80, nullable = false)
    private String labelEn;

    @Column(name = "label_ro", length = 80, nullable = false)
    private String labelRo;

    @Column(name = "sort_order", nullable = false)
    private int sortOrder;

    protected RewardCategoryEntity() {
    }

    public String getCode() { return code; }
    public String getLabelEn() { return labelEn; }
    public String getLabelRo() { return labelRo; }
    public int getSortOrder() { return sortOrder; }
}
