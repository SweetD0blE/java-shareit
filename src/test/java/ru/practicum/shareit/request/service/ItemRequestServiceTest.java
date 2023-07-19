package ru.practicum.shareit.request.service;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.request.dto.ItemRequestDto;
import ru.practicum.shareit.request.mapper.ItemRequestMapper;
import ru.practicum.shareit.request.model.ItemRequest;
import ru.practicum.shareit.user.model.User;

import javax.persistence.EntityManager;
import java.util.ArrayList;
import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

@Transactional
@SpringBootTest
@RequiredArgsConstructor(onConstructor_ = @Autowired)
class ItemRequestServiceTest {

    private final EntityManager entityManager;
    private final ItemRequestService itemRequestService;

    @Test
    void findAllTest() {
        User requester = makeUser("name", "mail@mail.ru");
        entityManager.persist(requester);
        System.out.println(requester.getId());
        List<ItemRequestDto> requests = List.of(
                makeRequestDto("description1"),
                makeRequestDto("description2"),
                makeRequestDto("description3")
        );
        List<ItemRequest> saveRequests = new ArrayList<>();
        requests.stream()
                .map(r -> ItemRequestMapper.toItemRequest(requester, r))
                .forEach(request -> {
                    request.setRequestor(requester);
                    entityManager.persist(request);
                    saveRequests.add(request);
                });
        User owner = makeUser("name2", "mail2@mail.ru");
        entityManager.persist(owner);
        ItemRequest request = saveRequests.get(0);
        Item item = makeAvailableItem(owner, request);
        entityManager.persist(item);

        entityManager.flush();

        List<ItemRequestDto> targetRequests = itemRequestService.getRequests(owner.getId(), 0, 10);

        assertThat(targetRequests, hasSize(requests.size()));
        assertThat(targetRequests, hasItem(allOf(
                hasProperty("id", notNullValue()),
                hasProperty("description", equalTo(requests.get(0).getDescription()))
        )));
        assertThat(targetRequests, hasItem(allOf(
                hasProperty("id", notNullValue()),
                hasProperty("description", equalTo(requests.get(1).getDescription()))
        )));
        assertThat(targetRequests, hasItem(allOf(
                hasProperty("id", notNullValue()),
                hasProperty("description", equalTo(requests.get(2).getDescription()))
        )));
        assertThat(targetRequests, hasItem(
                hasProperty("items", notNullValue())
        ));
    }

    private ItemRequestDto makeRequestDto(String description) {
        return ItemRequestDto.builder()
                .description(description)
                .build();
    }

    private User makeUser(String name, String email) {
        return User.builder()
                .name(name)
                .email(email)
                .build();
    }

    private Item makeAvailableItem(User owner, ItemRequest request) {
        return Item.builder()
                .name("name")
                .description("description")
                .available(true)
                .owner(owner)
                .request(request)
                .build();
    }
}