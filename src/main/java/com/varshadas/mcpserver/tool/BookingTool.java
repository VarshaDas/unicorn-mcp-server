package com.varshadas.mcpserver.tool;

import com.varshadas.mcpserver.model.UnicornBooking;
import com.varshadas.mcpserver.repository.BookingRepository;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.stereotype.Component;

import java.time.LocalDate;

@Component
public class BookingTool {

    private final BookingRepository bookingRepository;

    public BookingTool(BookingRepository bookingRepository) {
        this.bookingRepository = bookingRepository;
    }

    @Tool(description = "Book a unicorn rental. Stores the booking in the database and returns a confirmation with booking ID.")
    public String createBooking(
            @ToolParam(description = "Customer name") String customerName,
            @ToolParam(description = "Package: SPARKLE, RAINBOW, MAGICAL_KINGDOM, or CORPORATE") String packageName,
            @ToolParam(description = "Booking date in YYYY-MM-DD format") String bookingDate,
            @ToolParam(description = "Duration in hours (or days for CORPORATE)") int duration,
            @ToolParam(description = "Whether to include optional insurance") boolean includeInsurance) {

        var booking = bookingRepository.save(
                new UnicornBooking(customerName, packageName.toUpperCase(), LocalDate.parse(bookingDate), duration, includeInsurance));

        return "Booking confirmed! ID: %d — %s booked the %s package on %s for %d %s (insurance: %s)".formatted(
                booking.getId(), customerName, packageName, bookingDate, duration,
                packageName.equalsIgnoreCase("CORPORATE") ? "day(s)" : "hour(s)",
                includeInsurance ? "yes" : "no");
    }

    @Tool(description = "Retrieve all bookings for a given customer name.")
    public String getBookings(@ToolParam(description = "Customer name to look up") String customerName) {
        var bookings = bookingRepository.findByCustomerNameIgnoreCaseOrderByCreatedAtDesc(customerName);

        if (bookings.isEmpty()) return "No bookings found for %s.".formatted(customerName);

        var sb = new StringBuilder("Bookings for %s:\n".formatted(customerName));
        for (var b : bookings) {
            String unit = b.getPackageName().equalsIgnoreCase("CORPORATE") ? "day(s)" : "hrs";
            sb.append("- ID %d: %s package on %s, %d %s, insurance: %s\n".formatted(
                    b.getId(), b.getPackageName(), b.getBookingDate(), b.getDuration(),
                    unit, b.isIncludeInsurance() ? "yes" : "no"));
        }
        return sb.toString();
    }
}
