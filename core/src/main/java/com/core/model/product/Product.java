package com.core.model.product;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import javax.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "products")
public class Product {
    @Id
    @Column(name = "id")
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "products_s")
    @SequenceGenerator(name = "products_s", sequenceName = "products_id_seq", allocationSize = 1)
    private Long id;

    @Column(name = "price", nullable = false)
    private BigDecimal price;

    @OneToMany(fetch = FetchType.LAZY, cascade = CascadeType.ALL, mappedBy = "product", orphanRemoval = true)
    private List<ProductDescription> descriptions = new ArrayList<>();

    @OneToMany(fetch = FetchType.LAZY, cascade = CascadeType.ALL, mappedBy = "product", orphanRemoval = true)
    private List<ProductFeature> features = new ArrayList<>();

    @CreationTimestamp
    @Column(name = "creation_date", nullable = false, updatable = false)
    private LocalDateTime creationDate;

    @UpdateTimestamp
    @Column(name = "last_update_date", nullable = false)
    private LocalDateTime lastUpdateDate;

    @Version
    @Column(name = "version", nullable = false)
    private int version;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public void setPrice(BigDecimal price) {
        this.price = price;
    }

    public List<ProductDescription> getDescriptions() {
        return descriptions;
    }

    public void setDescriptions(List<ProductDescription> descriptions) {
        this.descriptions = descriptions;
    }

    public void addDescription(ProductDescription description) {
        this.descriptions.add(description);
        description.setProduct(this);
    }

    public void removeDescription(ProductDescription description) {
        this.descriptions.remove(description);
        description.setProduct(null);
    }

    public List<ProductFeature> getFeatures() {
        return features;
    }

    public void setFeatures(List<ProductFeature> features) {
        this.features = features;
    }

    public void addFeature(ProductFeature feature) {
        this.features.add(feature);
        feature.setProduct(this);
    }

    public void removeFeature(ProductFeature feature) {
        this.features.remove(feature);
        feature.setProduct(null);
    }

    public LocalDateTime getCreationDate() {
        return creationDate;
    }

    public void setCreationDate(LocalDateTime creationDate) {
        this.creationDate = creationDate;
    }

    public LocalDateTime getLastUpdateDate() {
        return lastUpdateDate;
    }

    public void setLastUpdateDate(LocalDateTime lastUpdateDate) {
        this.lastUpdateDate = lastUpdateDate;
    }

    public int getVersion() {
        return version;
    }

    public void setVersion(int version) {
        this.version = version;
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + "(" +
                "id = " + id + ", " +
                "price = " + price + ", " +
                "creationDate = " + creationDate + ", " +
                "lastUpdateDate = " + lastUpdateDate + ", " +
                "version = " + version + ")";
    }
}
