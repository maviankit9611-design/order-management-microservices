package com.learning.order_management.repo;

import com.learning.order_management.model.OrderEvent;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface OrderEventRepo extends CrudRepository<OrderEvent, Long> {
    List<OrderEvent> findByStatusOrderByCreatedAtAsc(String status);
}
