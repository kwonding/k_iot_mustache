package org.example.demo_ssr_v0;

import org.springframework.web.bind.annotation.GetMapping;

public class Home {
    @GetMapping("/")
    public String home() {
        return "index";
    }
}
