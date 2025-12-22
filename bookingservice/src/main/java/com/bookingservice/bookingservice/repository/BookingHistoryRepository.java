package com.bookingservice.bookingservice.repository;

import com.bookingservice.bookingservice.model.BookingHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;
@Repository
public interface BookingHistoryRepository extends JpaRepository<BookingHistory, UUID> {
}
