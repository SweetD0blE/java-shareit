package ru.practicum.shareit.user.dto;

import lombok.*;
import lombok.experimental.FieldDefaults;
import ru.practicum.shareit.validation.GroupValidation.Create;
import ru.practicum.shareit.validation.GroupValidation.Update;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;

@Getter
@Builder
@EqualsAndHashCode
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class UserDto {

    Long id;

    @NotBlank(groups = {Create.class, Update.class})
    String name;

    @NotBlank(groups = {Create.class})
    @Email(groups = {Create.class, Update.class})
    String email;
}
