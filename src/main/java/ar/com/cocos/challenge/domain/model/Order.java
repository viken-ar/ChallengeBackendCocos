package ar.com.cocos.challenge.domain.model;

import java.math.BigDecimal;
import java.time.Instant;

public class Order {

    private Integer id;
    private Integer instrumentId;
    private String instrumentName;
    private Integer userId;
    private Integer size;
    private BigDecimal price;
    private OrderType type;
    private OrderSide side;
    private OrderStatus status;
    private Instant datetime;
    private BigDecimal amount;
    private String rejectReason;

    public Order() {}

    public Order(Integer id, Integer instrumentId, String instrumentName, Integer userId, Integer size, BigDecimal price,
                 OrderType type, OrderSide side, OrderStatus status, Instant datetime, String rejectReason, BigDecimal amount) {
        this.id = id;
        this.instrumentId = instrumentId;
        this.instrumentName = instrumentName;
        this.userId = userId;
        this.size = size;
        this.price = price;
        this.type = type;
        this.side = side;
        this.status = status;
        this.datetime = datetime;
        this.rejectReason = rejectReason;
        this.amount = amount;
    }

    private Order(Builder builder) {
        this.id = builder.id;
        this.instrumentId = builder.instrumentId;
        this.instrumentName = builder.instrumentName;
        this.userId = builder.userId;
        this.size = builder.size;
        this.price = builder.price;
        this.type = builder.type;
        this.side = builder.side;
        this.status = builder.status;
        this.datetime = builder.datetime;
        this.rejectReason = builder.rejectReason;
        this.amount = builder.amount;
    }

    public void fill(BigDecimal executionPrice) {
        if (this.price == null) {
            this.price = executionPrice;
        }
        Result<Void> result = transition(OrderStatus.FILLED);
        if (result.isSuccess()) {
            this.status = OrderStatus.FILLED;
        }
    }

    public void reject(String rejectReason) {
        Result<Void> result = transition(OrderStatus.REJECTED);
        if (result.isSuccess()) {
            this.rejectReason = rejectReason;
            this.status = OrderStatus.REJECTED;
        }
    }

    public Result<Void> cancel() {
        Result<Void> result = transition(OrderStatus.CANCELLED);
        if (result.isSuccess()) {
            this.status = OrderStatus.CANCELLED;
        }
        return result;
    }

    private Result<Void> transition(OrderStatus nextStatus) {
        Result<Void> result = status.validateTransition(nextStatus);
        if (result.isFailure()) {
            return Result.failure("Transition not allowed to " + nextStatus);
        }
        return result;
    }

    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }
    public Integer getInstrumentId() { return instrumentId; }
    public void setInstrumentId(Integer instrumentId) { this.instrumentId = instrumentId; }
    public String getInstrumentName() { return instrumentName; }
    public void setInstrumentName(String instrumentName) { this.instrumentName = instrumentName; }
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

    public BigDecimal getAmount() { return amount; }
    public void setAmount(BigDecimal amount) { this.amount = amount; }

    public Instant getDatetime() { return datetime; }
    public void setDatetime(Instant datetime) { this.datetime = datetime; }
    public String getRejectReason() { return rejectReason; }
    public void setRejectReason(String rejectReason) { this.rejectReason = rejectReason; }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private Integer id;
        private Integer instrumentId;
        private String instrumentName;
        private Integer userId;
        private Integer size;
        private BigDecimal price;
        private OrderType type;
        private OrderSide side;
        private OrderStatus status;
        private Instant datetime;
        private BigDecimal amount;
        private String rejectReason;

        public Builder id(Integer id) { this.id = id; return this; }
        public Builder instrumentId(Integer instrumentId) { this.instrumentId = instrumentId; return this; }
        public Builder instrumentName(String instrumentName) { this.instrumentName = instrumentName; return this; }
        public Builder userId(Integer userId) { this.userId = userId; return this; }
        public Builder size(Integer size) { this.size = size; return this; }
        public Builder price(BigDecimal price) { this.price = price; return this; }
        public Builder type(OrderType type) { this.type = type; return this; }
        public Builder side(OrderSide side) { this.side = side; return this; }
        public Builder status(OrderStatus status) { this.status = status; return this; }
        public Builder datetime(Instant datetime) { this.datetime = datetime; return this; }
        public Builder rejectReason(String rejectReason) { this.rejectReason = rejectReason; return this; }
        public Builder amount(BigDecimal amount) { this.amount = amount; return this; }

        public Order build() {
            return new Order(this);
        }
    }
}
