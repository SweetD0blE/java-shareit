package ru.practicum.shareit.item.repository;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import ru.practicum.shareit.item.comment.model.Comment;
import ru.practicum.shareit.item.comment.repository.CommentRepository;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.request.model.ItemRequest;
import ru.practicum.shareit.request.repository.ItemRequestRepository;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.repository.UserRepository;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
public class CommentRepositoryTest {

    @Autowired
    private CommentRepository commentRepository;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private ItemRepository itemRepository;
    @Autowired
    private ItemRequestRepository itemRequestRepository;

    private Item item;
    private User itemOwner;
    private User requestor;
    private Comment comment;
    private ItemRequest itemRequest;

    @BeforeEach
    public void beforeEach() {
        LocalDateTime date = LocalDateTime.now();
        itemOwner = userRepository.save(new User(null, "itemOwner", "itemOwner@mail.ru"));
        requestor = userRepository.save(new User(null, "requestor", "requestor@mail.ru"));
        itemRequest = itemRequestRepository.save(new ItemRequest(null, "description", requestor, date));

        item = itemRepository.save(
                new Item(null,
                        "item",
                        "description",
                        true,
                        itemOwner,
                        null));

        comment = commentRepository.save(new Comment(null, "comment", item, itemOwner, LocalDateTime.now()));
    }

    @Test
    public void findByItemIdTest() {
        List<Comment> result = commentRepository.findCommentsByItem_Id(item.getId());

        assertNotNull(result);
        assertFalse(result.isEmpty());
        assertEquals(comment.getId(), result.get(0).getId());
        assertEquals(comment.getText(), result.get(0).getText());
        assertEquals(comment.getAuthor(), itemOwner);
        assertEquals(comment.getItem(), item);
    }

    @AfterEach
    public void afterEach() {
        commentRepository.deleteAll();
        userRepository.deleteAll();
        itemRepository.deleteAll();
        itemRequestRepository.deleteAll();
    }
}
