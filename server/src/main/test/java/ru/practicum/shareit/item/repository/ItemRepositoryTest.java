package ru.practicum.shareit.item.repository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.data.domain.PageRequest;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.request.model.ItemRequest;
import ru.practicum.shareit.request.repository.ItemRequestRepository;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.repository.UserRepository;

import javax.persistence.Query;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@DataJpaTest
public class ItemRepositoryTest {

    @Autowired
    private TestEntityManager testEntityManager;
    @Autowired
    private ItemRepository itemRepository;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private ItemRequestRepository itemRequestRepository;

    private static final PageRequest PAGE = PageRequest.of(0, 1);

    private User owner;
    private Item item;
    private User requester;
    private ItemRequest itemRequest;

    @BeforeEach
    void setUp() {
        owner = User.builder()
                .name("name")
                .email("mail@mail.ru")
                .build();
        owner = userRepository.save(owner);

        requester = User.builder()
                .name("name2")
                .email("mail2@mail.ru")
                .build();
        requester = userRepository.save(requester);

        itemRequest = ItemRequest.builder()
                .description("description")
                .requestor(requester)
                .build();
        itemRequest = itemRequestRepository.save(itemRequest);

        item = Item.builder()
                .name("nameItem")
                .description("descriptionItem")
                .available(true)
                .owner(owner)
                .request(itemRequest)
                .build();
        item = itemRepository.save(item);
    }

    @Test
    void findByOwnerId_ReturnEmptyListTest() {
        List<Item> items = itemRepository.findAllByOwnerId(0L, PAGE);
        assertNotNull(items);
        assertEquals(0, items.size());
    }

    @Test
    void findAllByOwnerId_ReturnListItemsTest() {
        List<Item> items = itemRepository.findAllByOwnerId(owner.getId(), PAGE);
        assertNotNull(items);
        assertEquals(1, items.size());
        assertEquals(item.getId(), items.get(0).getId());
    }

    @Test
    void findAllByOwnerIdWithPaging_ReturnListItemsTest() {
        Item item1 = Item.builder()
                .name("test")
                .description("description test item")
                .owner(owner)
                .available(true)
                .build();
        itemRepository.save(item1);

        List<Item> items = itemRepository.findAllByOwnerId(owner.getId(), PAGE);
        assertNotNull(items);
        assertEquals(1, items.size());
        assertEquals(item.getId(), items.get(0).getId());

        PageRequest page = PageRequest.of(1, 1);
        items = itemRepository.findAllByOwnerId(owner.getId(), page);
        assertNotNull(items);
        assertEquals(1, items.size());
        assertEquals(item1.getId(), items.get(0).getId());
    }

    @Test
    void searchTest() {
        // Empty
        String text = "text";
        Query query = testEntityManager.getEntityManager()
                .createQuery("select i from Item i where (upper(i.name) like upper(concat('%', :text, '%')) " +
                        "or upper(i.description) like upper(concat('%', :text, '%'))) " +
                        "and i.available = true order by i.id ");
        List<Item> items = query.setParameter("text", text).getResultList();
        assertEquals(0, items.size());
        List<Item> itemsSearch = itemRepository.search(text, PAGE);
        assertNotNull(itemsSearch);
        assertEquals(0, itemsSearch.size());

        // Found
        text = "Item";
        items = query.setParameter("text", text).getResultList();
        assertEquals(1, items.size());
        itemsSearch = itemRepository.search(text, PAGE);
        assertNotNull(itemsSearch);
        assertEquals(1, itemsSearch.size());
        assertEquals(items.get(0).getId(), itemsSearch.get(0).getId());

        // Paging
        Item item1 = Item.builder()
                .name("testingItem")
                .description("descriptionTesting")
                .available(true)
                .owner(owner)
                .build();
        itemRepository.save(item1);

        itemsSearch = itemRepository.search(text, PAGE);
        assertNotNull(itemsSearch);
        assertEquals(1, itemsSearch.size());

        PageRequest page = PageRequest.of(0, 2);
        itemsSearch = itemRepository.search(text, page);
        assertNotNull(itemsSearch);
        assertEquals(2, itemsSearch.size());
    }

    @Test
    void findByRequestId_ReturnEmptyListTest() {
        List<Item> items = itemRepository.findAllByRequest_IdOrderByRequestDesc(0L);
        assertNotNull(items);
        assertEquals(0, items.size());
    }

    @Test
    void findByRequestId_ReturnListItemsTest() {
        List<Item> items = itemRepository.findAllByRequest_IdOrderByRequestDesc(itemRequest.getId());
        assertNotNull(items);
        assertEquals(1, items.size());
        assertEquals(item.getId(), items.get(0).getId());
    }

    @Test
    void findByRequestIdIn_ReturnEmptyListTest() {
        List<Item> items = itemRepository.findByRequestIdIn(List.of(0L));
        assertNotNull(items);
        assertEquals(0, items.size());
    }

    @Test
    void findByRequestIdIn_ReturnListItemsTest() {
        List<Item> items = itemRepository.findByRequestIdIn(List.of(itemRequest.getId()));
        assertNotNull(items);
        assertEquals(1, items.size());
        assertEquals(item.getId(), items.get(0).getId());
    }
}