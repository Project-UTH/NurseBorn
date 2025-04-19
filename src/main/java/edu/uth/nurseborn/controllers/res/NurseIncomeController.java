package edu.uth.nurseborn.controllers.res;

import edu.uth.nurseborn.dtos.NurseIncomeDTO;
import edu.uth.nurseborn.models.User;
import edu.uth.nurseborn.repositories.UserRepository;
import edu.uth.nurseborn.services.NurseIncomeService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.time.LocalDate;
import java.time.YearMonth;
import java.time.temporal.WeekFields;
import java.util.List;
import java.util.Locale;

/**
 * Controller xử lý yêu cầu liên quan đến thống kê thu nhập của y tá.
 */
@Controller
@RequestMapping("/nurse")
public class NurseIncomeController {

    private static final Logger logger = LoggerFactory.getLogger(NurseIncomeController.class);

    @Autowired
    private NurseIncomeService nurseIncomeService;

    @Autowired
    private UserRepository userRepository;

    /**
     * Xử lý yêu cầu GET để hiển thị thống kê thu nhập của y tá.
     */
    @GetMapping("/income")
    public String getNurseIncome(
            @RequestParam(value = "period", defaultValue = "MONTH") String period,
            @RequestParam(value = "specificDate", required = false) String specificDate,
            Model model) {

        logger.debug("Nhận yêu cầu thống kê thu nhập với kỳ: {}, ngày cụ thể: {}", period, specificDate);

        // Lấy thông tin y tá đang đăng nhập
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        User nurseUser = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy y tá: " + username));

        if (!"NURSE".equalsIgnoreCase(nurseUser.getRole().name())) {
            logger.warn("Người dùng {} không phải y tá", username);
            return "redirect:/";
        }

        // Xác định khoảng thời gian dựa trên period và specificDate
        LocalDate startDate;
        LocalDate endDate;
        String displayDate;

        if (specificDate != null && !specificDate.isEmpty()) {
            switch (period.toUpperCase()) {
                case "DAY":
                    // specificDate là một ngày cụ thể (YYYY-MM-DD)
                    LocalDate selectedDay = LocalDate.parse(specificDate);
                    startDate = selectedDay;
                    endDate = selectedDay;
                    displayDate = selectedDay.toString();
                    break;

                case "WEEK":
                    // specificDate là một tuần (YYYY-WW)
                    String[] weekParts = specificDate.split("-W");
                    int year = Integer.parseInt(weekParts[0]);
                    int week = Integer.parseInt(weekParts[1]);
                    LocalDate firstDayOfWeek = LocalDate.of(year, 1, 1)
                            .with(WeekFields.of(Locale.getDefault()).weekOfYear(), week)
                            .with(WeekFields.of(Locale.getDefault()).dayOfWeek(), 1); // Ngày đầu tuần (thứ 2)
                    startDate = firstDayOfWeek;
                    endDate = firstDayOfWeek.plusDays(6); // Ngày cuối tuần (chủ nhật)
                    displayDate = startDate.toString();
                    break;

                case "MONTH":
                    // specificDate là một tháng (YYYY-MM)
                    YearMonth yearMonth = YearMonth.parse(specificDate);
                    startDate = yearMonth.atDay(1);
                    endDate = yearMonth.atEndOfMonth();
                    displayDate = startDate.toString();
                    break;

                default:
                    throw new IllegalArgumentException("Kỳ không hợp lệ: " + period);
            }
        } else {
            // Mặc định nếu không có specificDate
            endDate = LocalDate.now().plusMonths(1);
            startDate = switch (period.toUpperCase()) {
                case "DAY" -> endDate.minusDays(6);
                case "WEEK" -> endDate.minusWeeks(3);
                default -> endDate.minusMonths(12);
            };
            displayDate = startDate.toString();
        }

        // Fetch income data
        List<NurseIncomeDTO> incomes = nurseIncomeService.getIncomeByPeriod(nurseUser.getUserId(), period, startDate, endDate);
        Double totalIncome = nurseIncomeService.getTotalIncome(nurseUser.getUserId(), startDate, endDate);
        Long bookingCount = nurseIncomeService.getBookingCount(nurseUser.getUserId(), startDate, endDate);
        List<String> chartLabels = nurseIncomeService.getChartLabels(incomes, period, startDate, endDate);
        List<Double> chartData = nurseIncomeService.getChartData(incomes, period, startDate, endDate);

        // Log để kiểm tra dữ liệu
        logger.info("Dữ liệu thu nhập trả về: {}", incomes);
        logger.info("Nhãn biểu đồ: {}", chartLabels);
        logger.info("Dữ liệu biểu đồ: {}", chartData);

        // Add attributes to model
        model.addAttribute("incomes", incomes);
        model.addAttribute("totalIncome", totalIncome);
        model.addAttribute("bookingCount", bookingCount);
        model.addAttribute("period", period.toUpperCase());
        model.addAttribute("specificDate", specificDate != null ? specificDate : displayDate);
        model.addAttribute("chartLabels", chartLabels);
        model.addAttribute("chartData", chartData);

        logger.info("Hiển thị thống kê thu nhập cho y tá: {}", username);
        return "nurse/nurse_income";
    }
}