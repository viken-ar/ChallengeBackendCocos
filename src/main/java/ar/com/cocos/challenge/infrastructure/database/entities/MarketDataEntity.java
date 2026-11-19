package ar.com.cocos.challenge.infrastructure.database.entities;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "marketdata")
public class MarketDataEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "instrumentid")
    private Integer instrumentId;

    @Column(name = "high", precision = 10, scale = 2)
    private BigDecimal high;

    @Column(name = "low", precision = 10, scale = 2)
    private BigDecimal low;

    @Column(name = "open", precision = 10, scale = 2)
    private BigDecimal open;

    @Column(name = "close", precision = 10, scale = 2)
    private BigDecimal close;

    @Column(name = "previousclose", precision = 10, scale = 2)
    private BigDecimal previousClose;

    @Column(name = "date")
    private LocalDate date;

    public MarketDataEntity() {}

    public MarketDataEntity(Integer id, Integer instrumentId, BigDecimal high, BigDecimal low,
                            BigDecimal open, BigDecimal close, BigDecimal previousClose, LocalDate date) {
        this.id = id;
        this.instrumentId = instrumentId;
        this.high = high;
        this.low = low;
        this.open = open;
        this.close = close;
        this.previousClose = previousClose;
        this.date = date;
    }

    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }

    public Integer getInstrumentId() { return instrumentId; }
    public void setInstrumentId(Integer instrumentId) { this.instrumentId = instrumentId; }

    public BigDecimal getHigh() { return high; }
    public void setHigh(BigDecimal high) { this.high = high; }

    public BigDecimal getLow() { return low; }
    public void setLow(BigDecimal low) { this.low = low; }

    public BigDecimal getOpen() { return open; }
    public void setOpen(BigDecimal open) { this.open = open; }

    public BigDecimal getClose() { return close; }
    public void setClose(BigDecimal close) { this.close = close; }

    public BigDecimal getPreviousClose() { return previousClose; }
    public void setPreviousClose(BigDecimal previousClose) { this.previousClose = previousClose; }

    public LocalDate getDate() { return date; }
    public void setDate(LocalDate date) { this.date = date; }
}
