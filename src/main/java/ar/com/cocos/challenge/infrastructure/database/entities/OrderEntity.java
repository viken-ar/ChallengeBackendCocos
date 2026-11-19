package ar.com.cocos.challenge.infrastructure.database.entities;

import ar.com.cocos.challenge.domain.model.OrderSide;
import ar.com.cocos.challenge.domain.model.OrderStatus;
import ar.com.cocos.challenge.domain.model.OrderType;
import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;

import java.math.BigDecimal;
import java.time.Instant;

@Entity
@Table(name = "orders")
public class OrderEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @OneToOne
    @JoinColumn(name = "instrumentid", referencedColumnName = "id")
    private InstrumentEntity instrument;

    @Column(name = "userid")
    private Integer userId;

    @Column(name = "size")
    private Integer size;

    @Column(name = "price", precision = 10, scale = 2)
    private BigDecimal price;

    @Enumerated(EnumType.STRING)
    @Column(name = "type")
    private OrderType type;

    @Enumerated(EnumType.STRING)
    @Column(name = "side")
    private OrderSide side;

    @Enumerated(EnumType.STRING)
    @Column(name = "status")
    private OrderStatus status;

    @Column(name = "datetime")
    @CreationTimestamp
    private Instant datetime;

    @Column(name = "reject_reason")
    private String rejectReason;

    public OrderEntity() {}

    public OrderEntity(Integer id, InstrumentEntity instrument, Integer userId, Integer size, BigDecimal price,
                       OrderType type, OrderSide side, OrderStatus status, Instant datetime, String rejectReason) {
        this.id = id;
        this.instrument = instrument;
        this.userId = userId;
        this.size = size;
        this.price = price;
        this.type = type;
        this.side = side;
        this.status = status;
        this.datetime = datetime;
        this.rejectReason = rejectReason;
    }

    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }

    public InstrumentEntity getInstrument() { return instrument; }
    public void setInstrument(InstrumentEntity instrument) { this.instrument = instrument; }

    public Integer getUserId() { return userId; }
    public void setUserId(Integer userId) { this.userId = userId; }

    public Integer getSize() { return size; }
    public void setSize(Integer size) { this.size = size; }

    public BigDecimal getPrice() { return price; }
    public void setPrice(BigDecimal price) { this.price = price; }

    public OrderType getType() { return type; }
    public void setType(OrderType type) { this.type = type; }

    public OrderSide getSide() { return side; }
    public void setSide(OrderSide side) { this.side = side; }

    public OrderStatus getStatus() { return status; }
    public void setStatus(OrderStatus status) { this.status = status; }

    public Instant getDatetime() { return datetime; }
    public void setDatetime(Instant datetime) { this.datetime = datetime; }

    public String getRejectReason() { return rejectReason; }
    public void setRejectReason(String rejectReason) { this.rejectReason = rejectReason; }
}
