package com.dsmpear.main.controller;

import com.dsmpear.main.service.user.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.constraints.Email;

@RestController
@RequestMapping("/email")
@RequiredArgsConstructor
public class EmailController {
    private final UserService userService;

    @GetMapping("/auth")
    public void verifyAccount(int number, @Email String email) {
        userService.verify(number, email);
    }
}
