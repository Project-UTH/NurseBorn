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
}


