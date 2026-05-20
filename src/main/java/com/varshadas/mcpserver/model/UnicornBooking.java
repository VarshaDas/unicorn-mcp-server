package com.varshadas.mcpserver.model;

import jakarta.persistence.*;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "UNICORN_BOOKINGS")
public class UnicornBooking {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String customerName;

    @Column(nullable = false)
    private String packageName;

    @Column(nullable = false)
    private LocalDate bookingDate;

    @Column(nullable = false)
    private int duration;

    private boolean includeInsurance;

    @Column(updatable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    public UnicornBooking() {}

    public UnicornBooking(String customerName, String packageName, LocalDate bookingDate, int duration, boolean includeInsurance) {
        this.customerName = customerName;
        this.packageName = packageName;
        this.bookingDate = bookingDate;
        this.duration = duration;
        this.includeInsurance = includeInsurance;
    }

    public Long getId() { return id; }
    public String getCustomerName() { return customerName; }
    public String getPackageName() { return packageName; }
    public LocalDate getBookingDate() { return bookingDate; }
    public int getDuration() { return duration; }
    public boolean isIncludeInsurance() { return includeInsurance; }
    public LocalDateTime getCreatedAt() { return createdAt; }
}
