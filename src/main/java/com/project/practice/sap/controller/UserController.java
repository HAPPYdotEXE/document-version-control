// package com.project.practice.sap.controller;

// import org.springframework.http.HttpStatus;
// import org.springframework.http.ResponseEntity;
// import org.springframework.web.bind.annotation.GetMapping;
// import org.springframework.web.bind.annotation.PathVariable;
// import org.springframework.web.bind.annotation.PostMapping;
// import org.springframework.web.bind.annotation.RequestBody;
// import org.springframework.web.bind.annotation.RestController;
// import com.project.practice.sap.dto.CreateUserRequestDTO;
// import com.project.practice.sap.dto.UserResponseDTO;
// import com.project.practice.sap.service.UserService;

// @RestController
// public class UserController {
//     private final UserService service;

//     public UserController(UserService service) {
//         this.service = service;
//     }

//     @PostMapping
//     public ResponseEntity<UserResponseDTO> createUser(@RequestBody CreateUserRequestDTO request) {
//         UserResponseDTO response = service.createUser(request);
//         return ResponseEntity.status(HttpStatus.CREATED).body(response);
//     }

//     @GetMapping("/{id}")
//     public ResponseEntity<UserResponseDTO> getUserById(@PathVariable Long id) {
//         return ResponseEntity.ok(service.getUserById(id));
//     }
// }

package com.project.practice.sap.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.project.practice.sap.dto.CreateUserRequestDTO;
import com.project.practice.sap.dto.UserResponseDTO;
import com.project.practice.sap.service.UserService;

@RestController
@RequestMapping("/users")
public class UserController {

    private final UserService service;

    public UserController(UserService service) {
        this.service = service;
    }

    @PostMapping
    public ResponseEntity<UserResponseDTO> createUser(@RequestBody CreateUserRequestDTO request) {
        UserResponseDTO response = service.createUser(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<UserResponseDTO> getUserById(@PathVariable Long id) {
        return ResponseEntity.ok(service.getUserById(id));
    }
}
