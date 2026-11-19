package ar.com.cocos.challenge.domain.model;

import java.util.Set;

/**
 * Estado de la orden: NEW, FILLED, REJECTED, CANCELLED.
 */
public enum OrderStatus {
    NEW {
        @Override
        public Set<OrderStatus> nextStatus() {
            return Set.of(FILLED, REJECTED, CANCELLED);
        }
    },
    FILLED {
        @Override
        public Set<OrderStatus> nextStatus() {
            return Set.of();
        }
    },
    REJECTED {
        @Override
        public Set<OrderStatus> nextStatus() {
            return Set.of();
        }
    },
    CANCELLED {
        @Override
        public Set<OrderStatus> nextStatus() {
            return Set.of();
        }
    };

    public abstract Set<OrderStatus> nextStatus();

    public Result<Void> validateTransition(OrderStatus nextStatus) {
        if (!nextStatus().contains(nextStatus)) {
            return Result.failure("Next status not allowed " + nextStatus);
        }
        return Result.success(null);
    }
}
