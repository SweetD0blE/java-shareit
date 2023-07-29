package ru.practicum.shareit.request.mapper;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.springframework.stereotype.Component;
import ru.practicum.shareit.request.dto.ItemRequestDto;
import ru.practicum.shareit.request.model.ItemRequest;
import ru.practicum.shareit.user.model.User;

@Component
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class ItemRequestMapper {

    public static ItemRequest toItemRequest(User user, ItemRequestDto itemRequestDto) {
        return ItemRequest.builder()
                .description(itemRequestDto.getDescription())
                .requestor(user)
                .build();
    }

    public static ItemRequestDto toItemRequestDto(ItemRequest itemRequest) {
        return ItemRequestDto.builder()
                .id(itemRequest.getId())
                .description(itemRequest.getDescription())
                .created(itemRequest.getCreated())
                .build();
    }
}
