package com.group.xlibris.security.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class RolePageController {

    @GetMapping("/user")
    public String userPage() {
        return "user";
    }

    @GetMapping("/moderator")
    public String moderatorPage() {
        return "moderator";
    }

    @GetMapping("/admin")
    public String adminPage() {
        return "admin";
    }
}
