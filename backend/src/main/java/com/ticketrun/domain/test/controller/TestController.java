package com.ticketrun.domain.test.controller;

import com.ticketrun.domain.test.service.TestService;
import com.ticketrun.global.response.Empty;
import com.ticketrun.global.response.RsData;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/test")
public class TestController {

    private final TestService testService;

    @GetMapping
    public RsData<Empty> helloWorld(){
        return new RsData<>("200", "성공");
    }

}
