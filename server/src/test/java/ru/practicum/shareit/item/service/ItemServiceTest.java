package ru.practicum.shareit.item.service;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.booking.enums.BookingStatus;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.item.comment.model.Comment;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.dto.ItemOwnerDto;
import ru.practicum.shareit.item.mapper.ItemMapper;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.model.User;

import javax.persistence.EntityManager;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

@Transactional
@SpringBootTest
@RequiredArgsConstructor(onConstructor_ = @Autowired)
class ItemServiceTest {

    private final EntityManager entityManager;
    private final ItemService itemService;

    @Test
    void findAllByUserId_ReturnListItemsTest() {
        User owner = makeUser("name1", "mail1@mail.ru");
        entityManager.persist(owner);

        User booker = makeUser("name2", "mail2@mail.ru");
        entityManager.persist(booker);

        List<ItemDto> itemDtos = List.of(
                makeItemDto("name1", "description1"),
                makeItemDto("name2", "description2"),
                makeItemDto("name3", "description3")
        );
        List<Item> savedItems = new ArrayList<>();
        itemDtos.stream()
                .map(itemDto -> ItemMapper.toItem(itemDto, owner, null))
                .forEach(item -> {
                    entityManager.persist(item);
                    savedItems.add(item);
                });
        Booking booking = makeBooking(LocalDateTime.now().minusSeconds(60), LocalDateTime.now().plusSeconds(60),
                savedItems.get(0), booker);
        entityManager.persist(booking);
        Comment comment = makeComment("text", booker, savedItems.get(0));
        entityManager.persist(comment);
        entityManager.flush();

        List<ItemOwnerDto> items = itemService.getAllItemsByUserId(owner.getId(), 0, 10);

        assertThat(items, hasSize(itemDtos.size()));
        for (ItemDto request : itemDtos) {
            assertThat(items, hasItem(allOf(
                    hasProperty("id", notNullValue()),
                    hasProperty("description", equalTo(request.getDescription()))
            )));
        }
        assertThat(items, hasItem(
                hasProperty("comments", notNullValue())
        ));

        assertThat(items, hasItem(
                hasProperty("lastBooking", notNullValue())
        ));
    }

    private User makeUser(String name, String email) {
        return User.builder()
                .name(name)
                .email(email)
                .build();
    }

    private ItemDto makeItemDto(String name, String description) {
        return ItemDto.builder()
                .name(name)
                .description(description)
                .available(true)
                .build();
    }

    private Comment makeComment(String text, User author, Item item) {
        return Comment.builder()
                .text(text)
                .author(author)
                .item(item)
                .build();
    }

    private Booking makeBooking(LocalDateTime start, LocalDateTime end, Item item, User booker) {
        return Booking.builder()
                .item(item)
                .booker(booker)
                .status(BookingStatus.APPROVED)
                .start(start)
                .end(end)
                .build();
    }
}