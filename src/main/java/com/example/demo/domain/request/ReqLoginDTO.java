package com.example.demo.domain.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ReqLoginDTO {
    @NotBlank(message = "username không được để trống")
    private String email;

    @NotBlank(message = "password không được để trống")
    private String password;


}
