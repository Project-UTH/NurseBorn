package edu.uth.nurseborn.services;

import edu.uth.nurseborn.dtos.NurseIncomeDTO;
import edu.uth.nurseborn.models.NurseIncome;
import edu.uth.nurseborn.models.User;
import edu.uth.nurseborn.repositories.NurseIncomeRepository;
import edu.uth.nurseborn.repositories.UserRepository;
import org.modelmapper.ModelMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.temporal.IsoFields;
import java.time.temporal.WeekFields;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Service xử lý logic nghiệp vụ liên quan đến thu nhập của y tá.
 */
@Service
public class NurseIncomeService {

    private static final Logger logger = LoggerFactory.getLogger(NurseIncomeService.class);

    @Autowired
    private NurseIncomeRepository nurseIncomeRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ModelMapper modelMapper;

    /**
     * Lấy danh sách thu nhập của y tá theo khoảng thời gian và kỳ.
     */
    public List<NurseIncomeDTO> getIncomeByPeriod(Long nurseUserId, String period, LocalDate startDate, LocalDate endDate) {
        logger.debug("Lấy thu nhập cho nurseUserId: {}, kỳ: {}, ngày bắt đầu: {}, ngày kết thúc: {}", nurseUserId, period, startDate, endDate);

        User nurseUser = userRepository.findById(nurseUserId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy người dùng với ID: " + nurseUserId));

        List<NurseIncome> incomes = nurseIncomeRepository.findByNurseUserAndBookingDateBetween(nurseUser, startDate, endDate);
        logger.info("Số lượng bản ghi thu nhập tìm thấy: {}", incomes.size());

        return incomes.stream()
                .map(income -> modelMapper.map(income, NurseIncomeDTO.class))
                .collect(Collectors.toList());
    }

    /**
     * Tính tổng thu nhập của y tá trong khoảng thời gian.
     */
    public Double getTotalIncome(Long nurseUserId, LocalDate startDate, LocalDate endDate) {
        User nurseUser = userRepository.findById(nurseUserId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy người dùng với ID: " + nurseUserId));
        Double total = nurseIncomeRepository.sumIncomeByNurseUserAndDateRange(nurseUser, startDate, endDate);
        return total != null ? total : 0.0;
    }

    /**
     * Đếm số lượng đặt lịch của y tá trong khoảng thời gian.
     */
    public Long getBookingCount(Long nurseUserId, LocalDate startDate, LocalDate endDate) {
        User nurseUser = userRepository.findById(nurseUserId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy người dùng với ID: " + nurseUserId));
        Long count = nurseIncomeRepository.countBookingsByNurseUserAndDateRange(nurseUser, startDate, endDate);
        return count != null ? count : 0L;
    }

    /**
     * Tạo nhãn cho biểu đồ dựa trên kỳ thống kê.
     */
    public List<String> getChartLabels(List<NurseIncomeDTO> incomes, String period, LocalDate startDate, LocalDate endDate) {
        List<String> labels = new ArrayList<>();
        switch (period.toUpperCase()) {
            case "DAY":
                // Chỉ có 1 ngày, nhãn là ngày đó
                labels.add(startDate.toString());
                break;

            case "WEEK":
                // Nhãn là từng ngày trong tuần (7 ngày)
                LocalDate currentDay = startDate;
                while (!currentDay.isAfter(endDate)) {
                    labels.add("Ngày " + currentDay.toString());
                    currentDay = currentDay.plusDays(1);
                }
                break;

            case "MONTH":
                // Nhãn là từng tuần trong tháng (tuần 1, tuần 2, v.v.)
                LocalDate currentWeekStart = startDate;
                while (!currentWeekStart.isAfter(endDate)) {
                    int weekNumber = currentWeekStart.get(IsoFields.WEEK_OF_WEEK_BASED_YEAR);
                    labels.add("Tuần " + weekNumber);
                    currentWeekStart = currentWeekStart.plusWeeks(1);
                }
                break;

            default:
                throw new IllegalArgumentException("Kỳ không hợp lệ: " + period);
        }
        return labels;
    }

    /**
     * Tạo dữ liệu cho biểu đồ dựa trên kỳ thống kê.
     */
    public List<Double> getChartData(List<NurseIncomeDTO> incomes, String period, LocalDate startDate, LocalDate endDate) {
        // Tính tổng thu nhập theo nhãn
        Map<String, Double> incomeByLabel = incomes.stream()
                .collect(Collectors.groupingBy(
                        income -> {
                            LocalDate date = income.getBookingDate();
                            return switch (period.toUpperCase()) {
                                case "DAY" -> date.toString();
                                case "WEEK" -> "Ngày " + date.toString();
                                case "MONTH" -> "Tuần " + date.get(IsoFields.WEEK_OF_WEEK_BASED_YEAR);
                                default -> throw new IllegalArgumentException("Kỳ không hợp lệ: " + period);
                            };
                        },
                        Collectors.summingDouble(income -> income.getStatus() == edu.uth.nurseborn.models.enums.BookingStatus.ACCEPTED ? income.getPrice() : 0.0)
                ));

        // Tạo danh sách dữ liệu theo nhãn
        List<Double> data = new ArrayList<>();
        switch (period.toUpperCase()) {
            case "DAY":
                // Chỉ có 1 ngày
                data.add(incomeByLabel.getOrDefault(startDate.toString(), 0.0));
                break;

            case "WEEK":
                // Dữ liệu cho từng ngày trong tuần
                LocalDate currentDay = startDate;
                while (!currentDay.isAfter(endDate)) {
                    String label = "Ngày " + currentDay.toString();
                    data.add(incomeByLabel.getOrDefault(label, 0.0));
                    currentDay = currentDay.plusDays(1);
                }
                break;

            case "MONTH":
                // Dữ liệu cho từng tuần trong tháng
                LocalDate currentWeekStart = startDate;
                while (!currentWeekStart.isAfter(endDate)) {
                    String label = "Tuần " + currentWeekStart.get(IsoFields.WEEK_OF_WEEK_BASED_YEAR);
                    data.add(incomeByLabel.getOrDefault(label, 0.0));
                    currentWeekStart = currentWeekStart.plusWeeks(1);
                }
                break;

            default:
                throw new IllegalArgumentException("Kỳ không hợp lệ: " + period);
        }
        return data;
    }
}