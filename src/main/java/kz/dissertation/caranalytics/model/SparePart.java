package kz.dissertation.caranalytics.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.math.BigDecimal;

@Entity
@Table(name = "spare_parts")
public class SparePart {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String partNumber;

    @Column(nullable = false)
    private String partName;

    @Column(nullable = false)
    private String manufacturer;

    @Column(nullable = false)
    private String subsystem;

    @Column
    private String compatibleBrand;

    @Column
    private String compatibleModel;

    @Column
    private String faultCode;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal price;

    @Column(nullable = false)
    private String currency;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private SparePartStockStatus stockStatus;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getPartNumber() {
        return partNumber;
    }

    public void setPartNumber(String partNumber) {
        this.partNumber = partNumber;
    }

    public String getPartName() {
        return partName;
    }

    public void setPartName(String partName) {
        this.partName = partName;
    }

    public String getManufacturer() {
        return manufacturer;
    }

    public void setManufacturer(String manufacturer) {
        this.manufacturer = manufacturer;
    }

    public String getSubsystem() {
        return subsystem;
    }

    public void setSubsystem(String subsystem) {
        this.subsystem = subsystem;
    }

    public String getCompatibleBrand() {
        return compatibleBrand;
    }

    public void setCompatibleBrand(String compatibleBrand) {
        this.compatibleBrand = compatibleBrand;
    }

    public String getCompatibleModel() {
        return compatibleModel;
    }

    public void setCompatibleModel(String compatibleModel) {
        this.compatibleModel = compatibleModel;
    }

    public String getFaultCode() {
        return faultCode;
    }

    public void setFaultCode(String faultCode) {
        this.faultCode = faultCode;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public void setPrice(BigDecimal price) {
        this.price = price;
    }

    public String getCurrency() {
        return currency;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }

    public SparePartStockStatus getStockStatus() {
        return stockStatus;
    }

    public void setStockStatus(SparePartStockStatus stockStatus) {
        this.stockStatus = stockStatus;
    }
}
