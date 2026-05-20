package com.varshadas.mcpserver.repository;

import com.varshadas.mcpserver.model.UnicornBooking;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface BookingRepository extends JpaRepository<UnicornBooking, Long> {
    List<UnicornBooking> findByCustomerNameIgnoreCaseOrderByCreatedAtDesc(String customerName);
}
