package com.learning.order_management.repo;

import com.learning.order_management.model.OrderEvent;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

@Repository
public interface OrderEventRepo extends CrudRepository<OrderEvent, Long> {
    List<OrderEvent> findByStatusOrderByCreatedAtAsc(String status);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select event from OrderEvent event where event.status = :status order by event.createdAt asc")
    List<OrderEvent> findByStatusForUpdateOrderByCreatedAtAsc(@Param("status") String status);
}
