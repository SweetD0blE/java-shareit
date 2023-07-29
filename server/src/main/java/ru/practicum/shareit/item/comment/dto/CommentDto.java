package ru.practicum.shareit.item.comment.dto;

import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.LocalDateTime;

@Getter
@Setter
@Builder
@EqualsAndHashCode
@FieldDefaults(level = AccessLevel.PRIVATE)
public class CommentDto {

    Long id;

    String text;

    String authorName;

    LocalDateTime created;

}
