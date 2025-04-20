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
import java.time.temporal.WeekFields;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
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

        // Tính toán thu nhập ngày hiện tại
        LocalDate today = LocalDate.now();
        List<Booking> todayBookings = completedBookings.stream()
                .filter(booking -> booking.getBookingDate().equals(today))
                .collect(Collectors.toList());
        long todayBookingCount = todayBookings.size();
        double todayNurseIncome = todayBookings.stream().mapToDouble(Booking::getPrice).sum() ;
        double todayWebIncome = todayNurseIncome * 0.1;
        double todayNurseAfterDiscount = todayNurseIncome - todayWebIncome;

        // Lọc theo thời gian
        if (filterValue != null && !filterValue.isEmpty()) {
            if ("weekly".equalsIgnoreCase(filterType)) {
                // Giả sử filterValue có định dạng "2023-W45"
                String[] parts = filterValue.split("-W");
                int year = Integer.parseInt(parts[0]);
                int week = Integer.parseInt(parts[1]);
                WeekFields weekFields = WeekFields.of(Locale.getDefault());
                LocalDate weekStart = LocalDate.of(year, 1, 1)
                        .with(weekFields.weekOfYear(), week)
                        .with(weekFields.dayOfWeek(), 1);
                LocalDate weekEnd = weekStart.plusDays(6);
                completedBookings = completedBookings.stream()
                        .filter(booking -> !booking.getBookingDate().isBefore(weekStart) && !booking.getBookingDate().isAfter(weekEnd))
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
        double nurseIncome = completedBookings.stream().mapToDouble(Booking::getPrice).sum() ;
        double webIncome = nurseIncome * 0.1;
        double nurseAfterDiscount = nurseIncome - webIncome;

        // Đếm số lượng gia đình và y tá
        List<User> users = userRepository.findAll();
        long familyCount = users.stream().filter(user -> user.getRole() == Role.FAMILY).count();
        long nurseCount = users.stream().filter(user -> user.getRole() == Role.NURSE).count();

        // Chuẩn bị dữ liệu cho biểu đồ
        List<String> chartLabels = new ArrayList<>();
        List<Double> chartData = new ArrayList<>();

        if ("weekly".equalsIgnoreCase(filterType)) {
            // Thống kê theo ngày trong tuần
            String[] parts = filterValue != null ? filterValue.split("-W") : new String[]{String.valueOf(today.getYear()), String.valueOf(today.get(WeekFields.of(Locale.getDefault()).weekOfYear()))};
            int year = Integer.parseInt(parts[0]);
            int week = Integer.parseInt(parts[1]);
            WeekFields weekFields = WeekFields.of(Locale.getDefault());
            LocalDate weekStart = LocalDate.of(year, 1, 1)
                    .with(weekFields.weekOfYear(), week)
                    .with(weekFields.dayOfWeek(), 1);
            for (int day = 0; day < 7; day++) {
                LocalDate date = weekStart.plusDays(day);
                double dailyIncome = completedBookings.stream()
                        .filter(booking -> booking.getBookingDate().equals(date))
                        .mapToDouble(Booking::getPrice)
                        .sum();
                chartLabels.add("Thứ " + (day + 2));
                chartData.add(dailyIncome);
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
                        .sum();
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
                        .sum() ;
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
        dto.setTodayBookingCount(todayBookingCount);
        dto.setTodayWebIncome(todayWebIncome);
        dto.setTodayNurseIncome(todayNurseIncome);
        dto.setTodayNurseAfterDiscount(todayNurseAfterDiscount);

        return dto;
    }
}