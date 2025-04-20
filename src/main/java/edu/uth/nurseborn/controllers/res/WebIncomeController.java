package edu.uth.nurseborn.controllers.res;

import edu.uth.nurseborn.dtos.WebIncomeDTO;
import edu.uth.nurseborn.services.WebIncomeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.time.temporal.WeekFields;
import java.util.Locale;

@Controller
@RequestMapping("/admin")
public class WebIncomeController {

    @Autowired
    private WebIncomeService webIncomeService;

    @GetMapping("/web-income")
    public String getWebIncomeStats(
            @RequestParam(value = "filterType", defaultValue = "weekly") String filterType,
            @RequestParam(value = "filterValue", required = false) String filterValue,
            Model model) {

        // Đặt giá trị mặc định cho filterValue nếu không có giá trị được truyền
        if (filterValue == null || filterValue.isEmpty()) {
            LocalDate now = LocalDate.now();
            if ("weekly".equals(filterType)) {
                WeekFields weekFields = WeekFields.of(Locale.getDefault());
                int week = now.get(weekFields.weekOfYear());
                int year = now.getYear();
                filterValue = String.format("%d-W%d", year, week); // Ví dụ: "2025-W16"
            } else if ("monthly".equals(filterType)) {
                filterValue = YearMonth.now().format(DateTimeFormatter.ofPattern("yyyy-MM")); // Ví dụ: "2025-04"
            } else if ("yearly".equals(filterType)) {
                filterValue = String.valueOf(now.getYear()); // Ví dụ: "2025"
            }
        }

        WebIncomeDTO webIncomeDTO = webIncomeService.getWebIncomeStats(filterType, filterValue);
        // Gán filterType và filterValue vào DTO để sử dụng trong giao diện
        webIncomeDTO.setFilterType(filterType);
        webIncomeDTO.setFilterValue(filterValue);
        model.addAttribute("webIncomeDTO", webIncomeDTO);
        return "admin/web_income";
    }
}