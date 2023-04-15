package com.example.springsocial.controller;

import com.example.springsocial.model.ERole;
import com.example.springsocial.model.Role;
import com.example.springsocial.exception.BadRequestException;
import com.example.springsocial.model.AuthProvider;
import com.example.springsocial.model.User;
import com.example.springsocial.payload.ApiResponse;
import com.example.springsocial.payload.AuthResponse;
import com.example.springsocial.payload.LoginRequest;
import com.example.springsocial.payload.SignUpRequest;
import com.example.springsocial.repository.RoleRepository;
import com.example.springsocial.repository.UserRepository;
import com.example.springsocial.security.TokenProvider;
import net.bytebuddy.utility.RandomString;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.repository.query.Param;
import org.springframework.http.ResponseEntity;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;
import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.util.HashSet;
import java.util.Set;

@RestController
@RequestMapping("/auth")
public class AuthController {

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;
    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private TokenProvider tokenProvider;
    @Autowired
    private JavaMailSender mailSender;

    @PostMapping("/login")
    public ResponseEntity<?> authenticateUser(@Valid @RequestBody LoginRequest loginRequest) {

        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        loginRequest.getEmail(),
                        loginRequest.getPassword()
                )
        );

        SecurityContextHolder.getContext().setAuthentication(authentication);

        String token = tokenProvider.createToken(authentication);
        return ResponseEntity.ok(new AuthResponse(token));
    }

    @PostMapping("/signup")
    public ResponseEntity<?> registerUser(@Valid @RequestBody SignUpRequest signUpRequest ,String siteURL) throws UnsupportedEncodingException, MessagingException {
        {
//            if (userRepository.existsByEmail(signUpRequest.getEmail())) {
//                throw new BadRequestException("Email address already in use.");
//            }
//            Set<Role> roles = new HashSet<>();
//            if (signUpRequest.getLevel().equals("2")) {
//                Role userRole = roleRepository.findByName(ERole.LEVEL_2_USER)
//                        .orElseThrow(() -> new RuntimeException("Error: Role is not found."));
//                roles.add(userRole);  }
//            if (signUpRequest.getLevel().equals("1")) {
//                Role userRole = roleRepository.findByName(ERole.LEVEL_1_USER)
//                        .orElseThrow(() -> new RuntimeException("Error: Role is not found."));
//                roles.add(userRole);  }if (signUpRequest.getLevel().equals("3")) {
//            Role userRole = roleRepository.findByName(ERole.LEVEL_3_USER)
//                    .orElseThrow(() -> new RuntimeException("Error: Role is not found."));
//            roles.add(userRole);  }if (signUpRequest.getLevel().equals("vip")) {
//            Role userRole = roleRepository.findByName(ERole.VIP)
//                    .orElseThrow(() -> new RuntimeException("Error: Role is not found."));
//            roles.add(userRole);  }if (signUpRequest.getLevel().equals("admin")) {
//            Role userRole = roleRepository.findByName(ERole.ADMIN)
//                    .orElseThrow(() -> new RuntimeException("Error: Role is not found."));
//            roles.add(userRole);  }
            Set<Role> roles = new HashSet<>();
            Role role = roleRepository.findByName(ERole.ADMIN);
            roles.add(role);
            // Creating user's account
            User user = new User();
            String randomCode = RandomString.make(64);
            user.setRoles(roles);
            user.setVerificationCode(randomCode);
            user.setName(signUpRequest.getName());
            user.setEmail(signUpRequest.getEmail());
            user.setPassword(signUpRequest.getPassword());
            user.setProvider(AuthProvider.local);
            user.setEmailVerified(false);

            user.setPassword(passwordEncoder.encode(user.getPassword()));

            User result = userRepository.save(user);
            sendVerificationEmail(user, siteURL);
            URI location = ServletUriComponentsBuilder
                    .fromCurrentContextPath().path("/user/me")
                    .buildAndExpand(result.getId()).toUri();

            return ResponseEntity.created(location)
                    .body(new ApiResponse(true, "User registered successfully@"));
        }
    }
    @PostMapping("/process_register")
    public String processRegister(User user, HttpServletRequest request) throws UnsupportedEncodingException, MessagingException {
        sendVerificationEmail(user, getSiteURL(request));
        return "register_success";
    }

    private String getSiteURL(HttpServletRequest request) {
        String siteURL = request.getRequestURL().toString();
        return siteURL.replace(request.getServletPath(), "");
    }
    public boolean verify(String verificationCode) {
        User user = userRepository.findByVerificationCode(verificationCode);

        if (user == null || user.isEmailVerified()) {
            return false;
        } else {
            user.setVerificationCode(null);
            user.setEmailVerified(true);
            userRepository.save(user);

            return true;
        }
    }
    private void sendVerificationEmail(User user, String siteURL) throws MessagingException, UnsupportedEncodingException {
        String toAddress = user.getEmail();
        String fromAddress = "damir.ps04@gmail.com";
        String senderName = "damirCO";
        String subject = "Please verify your registration";
        String content = "Dear [[name]],<br>" + "Please click the link below to verify your registration:<br>" +"<h3><a href=\"[[URL]]\" target=\"_self\">[[URL]]</a></h3>" + "Thank you,<br>"
                + "Your company name.";
        MimeMessage message = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message);
        helper.setFrom(fromAddress, senderName);
        helper.setTo(toAddress);
        helper.setSubject(subject);

        content = content.replace("[[name]]", user.getName());
        String verifyURL = "localhost:8080/auth" + "/verify?code=" + user.getVerificationCode();
        content = content.replace("[[URL]]", verifyURL);
        helper.setText(content, true);
        mailSender.send(message);
    }
    @GetMapping("/verify")
    public String verifyUser(@Param("code") String code) {
        if (verify(code)) {
            return "verify_success";
        } else {
            return "verify_fail";
        }
    }

}
