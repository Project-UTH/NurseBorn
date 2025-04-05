package edu.uth.nurseborn.controllers.auth;

import edu.uth.nurseborn.dtos.LoginRequestDTO;
import edu.uth.nurseborn.dtos.LoginResponseDTO;
import edu.uth.nurseborn.dtos.RegisterRequestDTO;
import edu.uth.nurseborn.dtos.UserDTO;
import edu.uth.nurseborn.services.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @Autowired
    private UserService userService;

    @PostMapping("/register")
    public ResponseEntity<String> register(@RequestBody RegisterRequestDTO requestDTO) {
        UserDTO userDTO = userService.registerUser(requestDTO);
        return ResponseEntity.ok("Đăng ký thành công");
    }

    @PostMapping("/login")
    public ResponseEntity<LoginResponseDTO> login(@RequestBody LoginRequestDTO loginRequest) {
        LoginResponseDTO response = userService.login(loginRequest);
        return ResponseEntity.ok(response);
    }
}