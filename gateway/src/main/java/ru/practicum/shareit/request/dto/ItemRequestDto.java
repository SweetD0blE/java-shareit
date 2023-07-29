package ru.practicum.shareit.request.dto;

import lombok.*;
import lombok.experimental.FieldDefaults;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.validation.GroupValidation.Create;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;
import java.time.LocalDateTime;
import java.util.List;

/**
 * TODO Sprint add-item-requests.
 */
@Getter
@Setter
@Builder
@EqualsAndHashCode
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ItemRequestDto {

    Long id;

    @NotBlank(groups = {Create.class})
    @Size(max = 200, groups = {Create.class})
    String description;

    LocalDateTime created;

    List<ItemDto> items;
}
