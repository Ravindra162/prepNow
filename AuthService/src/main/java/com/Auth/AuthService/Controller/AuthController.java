package com.Auth.AuthService.Controller;

import com.Auth.AuthService.Model.User;
import com.Auth.AuthService.Repo.UserRepo;
import com.Auth.AuthService.Service.EmailService;
import com.Auth.AuthService.Service.JwtService;
import com.Auth.AuthService.Service.OtpService;
import jakarta.mail.MessagingException;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final UserRepo userRepository;
    private final PasswordEncoder passwordEncoder;
    private final EmailService emailService;
    private final OtpService otpService;
    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;

    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody User user) {
        // Check if user already exists
        if (userRepository.findByEmail(user.getEmail()).isPresent()) {
            return ResponseEntity.badRequest().body("Email already registered");
        }

        // Generate and send OTP
        try {
            String otp = otpService.generateOTP(user.getEmail());
            emailService.sendOtpEmail(user.getEmail(), otp);

            // Save user with unverified email
            user.setEmailVerified(false);
            user.setPassword(passwordEncoder.encode(user.getPassword()));
            User savedUser = userRepository.save(user);

            Map<String, Object> response = new HashMap<>();
            response.put("message", "Please verify your email with the OTP sent. OTP will expire in 2 minutes.");
            response.put("userId", savedUser.getId().toString());
            response.put("expiresIn", "2 minutes");

            return ResponseEntity.ok(response);
        } catch (MessagingException e) {
            return ResponseEntity.internalServerError().body("Failed to send verification email");
        }
    }

    @PostMapping("/verify-email")
    public ResponseEntity<?> verifyEmail(@RequestParam String email, @RequestParam String otp) {
        try {
            if (otpService.isOtpExpired(email)) {
                return ResponseEntity.badRequest().body("OTP has expired. Please request a new one.");
            }

            if (otpService.validateOTP(email, otp)) {
                User user = userRepository.findByEmail(email)
                        .orElseThrow(() -> new RuntimeException("User not found"));
                user.setEmailVerified(true);
                userRepository.save(user);
                return ResponseEntity.ok("Email verified successfully");
            }
            return ResponseEntity.badRequest().body("Invalid OTP");
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PostMapping("/resend-otp")
    public ResponseEntity<?> resendOtp(@RequestParam String email) {
        if (!userRepository.findByEmail(email).isPresent()) {
            return ResponseEntity.badRequest().body("Email not registered");
        }

        // Clean up any existing OTP for this email
        otpService.clearOTP(email);

        try {
            String otp = otpService.generateOTP(email);
            emailService.sendOtpEmail(email, otp);

            Map<String, String> response = new HashMap<>();
            response.put("message", "New OTP sent successfully. Valid for 2 minutes.");
            response.put("expiresIn", "2 minutes");

            return ResponseEntity.ok(response);
        } catch (MessagingException e) {
            return ResponseEntity.internalServerError().body("Failed to send OTP");
        }
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest request, HttpServletResponse response) {
        try {
            Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.email(), request.password())
            );

            if (authentication.isAuthenticated()) {
                User user = userRepository.findByEmail(request.email())
                    .orElseThrow(() -> new RuntimeException("User not found"));

                if (!user.isEmailVerified()) {
                    return ResponseEntity.badRequest().body("Please verify your email first");
                }

                String token = jwtService.generateToken(request.email());
                jwtService.createCookie(token, response);

                Map<String, Object> responseBody = new HashMap<>();
                responseBody.put("message", "Login successful");
                responseBody.put("username", user.getUsername());

                return ResponseEntity.ok(responseBody);
            }
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Invalid email or password");
        }
        return ResponseEntity.badRequest().body("Authentication failed");
    }

    @PostMapping("/logout")
    public ResponseEntity<?> logout(HttpServletResponse response) {
        jwtService.clearCookie(response);
        return ResponseEntity.ok("Logged out successfully");
    }

    // Add LoginRequest record
    private record LoginRequest(String email, String password) {}
}
