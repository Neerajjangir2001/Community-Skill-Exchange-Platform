package com.bookingservice.bookingservice.mapper;

import com.bookingservice.bookingservice.DTO.BookingResponse;
import com.bookingservice.bookingservice.model.Booking;

public class
Mapper {
    public static BookingResponse toResponse(Booking booking) {
        if (booking == null) {
            return null;
        }

        return BookingResponse.builder()
                .id(booking.getId())
                .userId(booking.getUserId())
                .providerId(booking.getProviderId())
                .skillId(booking.getSkillId())
                .startTime(booking.getStartTime())
                .endTime(booking.getEndTime())
                .totalHours(booking.getTotalHours())
                .pricePerHour(booking.getPricePerHour())
                .totalPrice(booking.getTotalPrice())
                .status(booking.getStatus())
                .createdAt(booking.getCreatedAt())
                .updatedAt(booking.getUpdatedAt())
                .build();
    }
}
