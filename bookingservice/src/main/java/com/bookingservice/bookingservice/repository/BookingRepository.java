package com.bookingservice.bookingservice.repository;

import com.bookingservice.bookingservice.model.Booking;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface BookingRepository extends JpaRepository<Booking, UUID> {
    List<Booking> findByUserId(UUID userId);

    List<Booking> findByProviderId(UUID providerId);

    List<Booking> findBySkillId(UUID skillId);

    void deleteByUserId(UUID userId);

    void deleteByProviderId(UUID providerId);
}
