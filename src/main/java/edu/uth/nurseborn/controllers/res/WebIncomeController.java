package edu.uth.nurseborn.controllers.res;

import edu.uth.nurseborn.dtos.WebIncomeDTO;
import edu.uth.nurseborn.services.WebIncomeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@RequestMapping("/admin")
public class WebIncomeController {

    @Autowired
    private WebIncomeService webIncomeService;

    @GetMapping("/web-income")
    public String getWebIncomeStats(
            @RequestParam(value = "filterType", defaultValue = "yearly") String filterType,
            @RequestParam(value = "filterValue", required = false) String filterValue,
            Model model) {

        WebIncomeDTO webIncomeDTO = webIncomeService.getWebIncomeStats(filterType, filterValue);
        model.addAttribute("webIncomeDTO", webIncomeDTO);
        return "admin/web_income";
    }
}