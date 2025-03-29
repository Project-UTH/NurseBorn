package edu.uth.nurseborn.controllers;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/")
public class HomeController {

    @GetMapping
    public String home() {
        return "home";  // Trả về file home.html
    }
    //Trang Login
    @GetMapping("/login-h")
    public String login() {
        return "master/auth-login-basic";
    }
    //Trang RoloSelection
    @GetMapping("/role-select")
    public String roleSelect() {
        return "master/role-selection";
    }
    //Trang Đăng ký Family
    @GetMapping("/register-family")
    public String registerFamily() {
        return "master/auth-register-basic-family";
    }
    //Trang Đăng ký Nurse
    @GetMapping("/register-nurse")
    public String registerNurse() {
        return "master/auth-register-basic-nurse";
    }

    @GetMapping("/job-re")
    public String jobRe() {return "nurse/job-requests";}

    //Phần tính năng dành cho y tá
    @GetMapping("/job-requests")
    public String jobrequests() {
        return "nurse/job-requests";
    }
    @GetMapping("/manage-services")
    public String manageservices() {
        return "nurse/manage-services";
    }
    @GetMapping("/create-profile")
    public String createprofile() {return "nurse/create-profile";
    }
    @GetMapping("/nursepage")
    public String nursepage() {
        return "master/nursepage";
    }@GetMapping("/nurse_review")
    public String nurse_review() {
        return "master/nurse_review";
    }
}



