package ru.practicum.gateway.user;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import javax.validation.constraints.Email;

@Getter
@Setter
@AllArgsConstructor
@Builder
public class UserDto {

    private long id;
    private String name;
    @Email(message = "Email не соответствует формату")
    private String email;
}
