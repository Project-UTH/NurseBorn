package edu.uth.nurseborn.dtos;

import lombok.Data;

import java.security.Timestamp;

@Data
public class UserDTO {
    private Long userId;
    private String username;
    private String password; // Chỉ dùng khi đăng ký/đăng nhập
    private String email;
    private String role;
    private String fullName;
    private String phoneNumber;
    private String address;
    private boolean isVerified;
    private Timestamp createdAt;
}