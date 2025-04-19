package edu.uth.nurseborn.services;

import edu.uth.nurseborn.dtos.WebIncomeDTO;
import edu.uth.nurseborn.models.Booking;
import edu.uth.nurseborn.models.User;
import edu.uth.nurseborn.models.enums.BookingStatus;
import edu.uth.nurseborn.models.enums.Role;
import edu.uth.nurseborn.repositories.BookingRepository;
import edu.uth.nurseborn.repositories.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class WebIncomeService {

    @Autowired
    private BookingRepository bookingRepository;

    @Autowired
    private UserRepository userRepository;

    public WebIncomeDTO getWebIncomeStats(String filterType, String filterValue) {
        WebIncomeDTO dto = new WebIncomeDTO();

        // Lấy tất cả booking có trạng thái COMPLETED
        List<Booking> completedBookings = bookingRepository.findAll().stream()
                .filter(booking -> booking.getStatus() == BookingStatus.COMPLETED)
                .collect(Collectors.toList());

        // Lọc theo thời gian
        if (filterValue != null && !filterValue.isEmpty()) {
            if ("daily".equalsIgnoreCase(filterType)) {
                LocalDate date = LocalDate.parse(filterValue);
                completedBookings = completedBookings.stream()
                        .filter(booking -> booking.getBookingDate().equals(date))
                        .collect(Collectors.toList());
            } else if ("monthly".equalsIgnoreCase(filterType)) {
                YearMonth yearMonth = YearMonth.parse(filterValue);
                LocalDate start = yearMonth.atDay(1);
                LocalDate end = yearMonth.atEndOfMonth();
                completedBookings = completedBookings.stream()
                        .filter(booking -> !booking.getBookingDate().isBefore(start) && !booking.getBookingDate().isAfter(end))
                        .collect(Collectors.toList());
            } else if ("yearly".equalsIgnoreCase(filterType)) {
                int year = Integer.parseInt(filterValue);
                completedBookings = completedBookings.stream()
                        .filter(booking -> booking.getBookingDate().getYear() == year)
                        .collect(Collectors.toList());
            }
        }

        // Tính toán thống kê
        long bookingCount = completedBookings.size();
        double nurseIncome = completedBookings.stream().mapToDouble(Booking::getPrice).sum() * 1000; // Nhân 1000
        double webIncome = nurseIncome * 0.1; // 10% tổng thu nhập y tá
        double nurseAfterDiscount = nurseIncome - webIncome;

        // Đếm số lượng gia đình và y tá
        List<User> users = userRepository.findAll();
        long familyCount = users.stream().filter(user -> user.getRole() == Role.FAMILY).count();
        long nurseCount = users.stream().filter(user -> user.getRole() == Role.NURSE).count();

        // Chuẩn bị dữ liệu cho biểu đồ
        List<String> chartLabels = new ArrayList<>();
        List<Double> chartData = new ArrayList<>();

        if ("daily".equalsIgnoreCase(filterType)) {
            // Thống kê theo giờ trong ngày
            for (int hour = 0; hour < 24; hour++) {
                final int h = hour;
                double hourlyIncome = completedBookings.stream()
                        .filter(booking -> booking.getStartTime() != null && booking.getStartTime().getHour() == h)
                        .mapToDouble(Booking::getPrice)
                        .sum() * 1000; // Nhân 1000
                chartLabels.add(String.format("%02d:00", hour));
                chartData.add(hourlyIncome);
            }
        } else if ("monthly".equalsIgnoreCase(filterType)) {
            // Thống kê theo ngày trong tháng
            YearMonth yearMonth = filterValue != null ? YearMonth.parse(filterValue) : YearMonth.now();
            int daysInMonth = yearMonth.lengthOfMonth();
            for (int day = 1; day <= daysInMonth; day++) {
                LocalDate date = yearMonth.atDay(day);
                double dailyIncome = completedBookings.stream()
                        .filter(booking -> booking.getBookingDate().equals(date))
                        .mapToDouble(Booking::getPrice)
                        .sum() * 1000; // Nhân 1000
                chartLabels.add(String.format("%02d", day));
                chartData.add(dailyIncome);
            }
        } else {
            // Thống kê theo tháng trong năm
            for (int month = 1; month <= 12; month++) {
                final int m = month;
                double monthlyIncome = completedBookings.stream()
                        .filter(booking -> booking.getBookingDate().getMonthValue() == m)
                        .mapToDouble(Booking::getPrice)
                        .sum() * 1000; // Nhân 1000
                chartLabels.add(String.format("Tháng %02d", month));
                chartData.add(monthlyIncome);
            }
        }

        // Gán giá trị vào DTO
        dto.setBookingCount(bookingCount);
        dto.setNurseIncome(nurseIncome);
        dto.setWebIncome(webIncome);
        dto.setNurseAfterDiscount(nurseAfterDiscount);
        dto.setFamilyCount(familyCount);
        dto.setNurseCount(nurseCount);
        dto.setChartLabels(chartLabels);
        dto.setChartData(chartData);

        return dto;
    }
}