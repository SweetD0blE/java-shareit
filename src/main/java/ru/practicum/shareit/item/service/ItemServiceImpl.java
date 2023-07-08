package ru.practicum.shareit.item.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.booking.dto.BookingItemDto;
import ru.practicum.shareit.booking.enums.BookingStatus;
import ru.practicum.shareit.booking.mapper.BookingMapper;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.repository.BookingRepository;
import ru.practicum.shareit.exception.ObjectNotFoundException;
import ru.practicum.shareit.exception.ValidationException;
import ru.practicum.shareit.item.comment.dto.CommentCreateDto;
import ru.practicum.shareit.item.comment.dto.CommentDto;
import ru.practicum.shareit.item.comment.mapper.CommentMapper;
import ru.practicum.shareit.item.comment.module.Comment;
import ru.practicum.shareit.item.comment.repository.CommentRepository;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.dto.ItemOwnerDto;
import ru.practicum.shareit.item.mapper.ItemMapper;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.repository.ItemRepository;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.repository.UserRepository;
import ru.practicum.shareit.user.service.UserService;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ItemServiceImpl implements ItemService {

    private final BookingRepository bookingRepository;
    private final CommentRepository commentRepository;
    private final UserService userService;
    private final ItemRepository itemRepository;
    private final UserRepository userRepository;

    @Override
    public List<ItemOwnerDto> getAllItemsByUserId(Long userId, Integer from, Integer size) {
        userService.validateUserById(userId);
        PageRequest pageRequest = PageRequest.of(from / size, size, Sort.by("id"));
        List<Item> items = itemRepository.findAllByOwnerId(userId, pageRequest);
        List<ItemOwnerDto> itemsOwnerDto = items.stream()
                .map(ItemMapper::toItemOwnerDto)
                .collect(Collectors.toList());
        List<Booking> bookings = bookingRepository.findBookingsByItemIn(items);
        if (bookings != null && !bookings.isEmpty()) {
            for (ItemOwnerDto i : itemsOwnerDto) {
                List<Booking> bookingsOwnerItems = bookings.stream()
                        .filter(f -> Objects.equals(f.getItem().getId(), i.getId()))
                        .collect(Collectors.toList());
                addLastAndNextBookings(i, bookingsOwnerItems);
            }
        }
        return itemsOwnerDto;
    }

    @Override
    public ItemOwnerDto getItemById(Long userId, Long itemId) {
        userService.validateUserById(userId);
        Item item = itemRepository.findById(itemId).orElseThrow(() ->
                new ObjectNotFoundException(String.format("Вещь с id = %d не найдена", itemId)));
        ItemOwnerDto itemOwnerDto = ItemMapper.toItemOwnerDto(item);
        Set<Comment> comments = commentRepository.findCommentsByItem_Id(itemId);
        itemOwnerDto.setComments(comments.stream()
                .map(CommentMapper::toCommentDto)
                .collect(Collectors.toSet()));
        List<Booking> bookings = bookingRepository.findBookingsByItem_Id(itemOwnerDto.getId());
        if (bookings != null && !bookings.isEmpty() && Objects.equals(item.getOwner().getId(), userId)) {
            addLastAndNextBookings(itemOwnerDto, bookings);
        }
        return itemOwnerDto;
    }

    @Override
    public List<ItemDto> getSearchItem(String text, Integer from, Integer size) {
        if (text.isBlank()) {
            return Collections.emptyList();
        }
        Pageable pageable = PageRequest.of(from / size, size);
        List<Item> items = itemRepository.search(text, pageable);
        return items.stream()
                .map(ItemMapper::toItemDto)
                .collect(Collectors.toList());
    }

    @Transactional
    @Override
    public ItemDto createItem(Long userId, ItemDto itemDto) {
       User user = userRepository.findById(userId).orElseThrow(() ->
               new ObjectNotFoundException(String.format("Пользователь с id = %d не найден", userId)));
       return ItemMapper.toItemDto(itemRepository.save(ItemMapper.toCreateItem(itemDto, user)));
    }

    @Transactional
    @Override
    public ItemDto updateItem(Long userId, Long itemId, ItemDto itemDto) {
        User user = userRepository.findById(userId).orElseThrow(null);
        Item item = itemRepository.findById(itemId).orElseThrow(() ->
                new ObjectNotFoundException(String.format("Вещь с id = %d не найдена", itemId)));
        if (!Objects.equals(item.getOwner(), user)) {
            throw new ObjectNotFoundException(String.format("Пользователь с id = %d не найден", userId));
        }
        if (itemDto.getName() != null) {
            item.setName(itemDto.getName());
        }
        if (itemDto.getDescription() != null) {
            item.setDescription(itemDto.getDescription());
        }
        if (itemDto.getAvailable() != null) {
            item.setAvailable(itemDto.getAvailable());
        }
        return ItemMapper.toItemDto(itemRepository.save(item));
    }

    @Transactional
    @Override
    public CommentDto createComment(Long userId, Long itemId, CommentCreateDto commentCreateDto) {
        User user = userRepository.findById(userId).orElseThrow(() ->
                new ObjectNotFoundException(String.format("Пользователь с id = %d не найден", userId)));
        Item item = itemRepository.findById(itemId).orElseThrow(() ->
                new ObjectNotFoundException(String.format("Вещь с id = %d не найдена", itemId)));
        List<Booking> bookings = bookingRepository.findBookingByItem_IdAndStatusOrderByEndAsc(itemId,
                BookingStatus.APPROVED);
        Comment comment = Comment.builder()
                .text(commentCreateDto.getText())
                .item(item)
                .author(user)
                .created(LocalDateTime.now())
                .build();
        if (bookings.stream().noneMatch(s -> s.getEnd().isBefore(LocalDateTime.now()))) {
            throw new ValidationException("Вы можете добавить комментарий только после завершения бронирования.");
        }
        if (bookings.stream().noneMatch(s -> Objects.equals(s.getBooker(), user))) {
            throw new ValidationException("Вы можете добавить комментарий только после оформления бронирования");
        }
        return CommentMapper.toCommentDto(commentRepository.save(comment));
    }

    private void addLastAndNextBookings(ItemOwnerDto itemOwnerDto, List<Booking> bookings) {
        itemOwnerDto.setLastBooking(bookings.stream()
                .filter(s -> s.getStart().isBefore(LocalDateTime.now()))
                .filter(s -> Objects.equals(s.getStatus(), BookingStatus.APPROVED))
                .map(BookingMapper::toBookingItemDto)
                .max(Comparator.comparing(BookingItemDto::getEnd))
                .orElse(null)
        );
        itemOwnerDto.setNextBooking(bookings.stream()
                .filter(s -> s.getStart().isAfter(LocalDateTime.now()))
                .filter(s -> Objects.equals(s.getStatus(), BookingStatus.APPROVED))
                .map(BookingMapper::toBookingItemDto)
                .min(Comparator.comparing(BookingItemDto::getStart))
                .orElse(null)
        );
    }

}
