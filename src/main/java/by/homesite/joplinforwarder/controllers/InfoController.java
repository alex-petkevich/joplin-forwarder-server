package by.homesite.joplinforwarder.controllers;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/info")
public class InfoController {

    @GetMapping("/")
    public ResponseEntity<?> getApplicationInfo() {

        StringBuilder result = new StringBuilder();
        result.append("Application: ").append(System.currentTimeMillis());

        return ResponseEntity.ok(result);
    }
}
