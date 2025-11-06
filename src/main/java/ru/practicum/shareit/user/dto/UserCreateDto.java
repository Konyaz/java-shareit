package ru.practicum.shareit.user.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserCreateDto {
    private Long id;

    @NotBlank(message = "field is empty")
    private String name;

    @Email(message = "not valid email")
    @NotBlank(message = "field is empty")
    private String email;
}