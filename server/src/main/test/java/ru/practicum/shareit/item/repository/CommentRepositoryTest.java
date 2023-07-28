package ru.practicum.shareit.item.repository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import ru.practicum.shareit.item.comment.model.Comment;
import ru.practicum.shareit.item.comment.repository.CommentRepository;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.repository.UserRepository;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@DataJpaTest
class CommentRepositoryTest {

    @Autowired
    private TestEntityManager testEntityManager;
    @Autowired
    private CommentRepository commentRepository;
    @Autowired
    private ItemRepository itemRepository;
    @Autowired
    private UserRepository userRepository;

    private User author;
    private User owner;
    private Item item;
    private Item item1;
    private Comment comment;

    @BeforeEach
    void setUp() {
        author = User.builder()
                .name("name")
                .email("mail@mail.ru")
                .build();
        author = userRepository.save(author);

        owner = User.builder()
                .name("name2")
                .email("mail2@mail.ru")
                .build();
        owner = userRepository.save(owner);

        item = Item.builder()
                .name("name")
                .description("description")
                .available(true)
                .owner(owner)
                .build();
        itemRepository.save(item);

        item1 = Item.builder()
                .name("name1")
                .description("description1")
                .owner(owner)
                .available(true)
                .build();
        itemRepository.save(item1);

        comment = Comment.builder()
                .text("comment")
                .item(item)
                .author(author)
                .build();
        commentRepository.save(comment);
    }

    @Test
    void findAllByItemId_ReturnEmptyListTest() {
        //EmptyList
        List<Comment> comments = commentRepository.findCommentsByItem_Id(99L);
        assertNotNull(comments);
        assertEquals(0, comments.size());
    }

    @Test
    void findAllByItemId_ReturnListCommentsTest() {
        //Single List
        List<Comment> comments = commentRepository.findCommentsByItem_Id(comment.getItem().getId());
        assertNotNull(comments);
        assertEquals(1, comments.size());
        assertEquals(comments.get(0).getId(), comment.getId());
    }

    @Test
    void findAllByItemId_ReturnRollbackTest() {
        List<Comment> comments = commentRepository.findAll();
        assertEquals(1, comments.size());
    }

    @Test
    void findAllByItemIdIn_ReturnEmptyListTest() {
        List<Comment> comments = commentRepository.findCommentsByItemIn(List.of(item1));
        assertNotNull(comments);
        assertEquals(0, comments.size());
    }

    @Test
    void findAllByItemIdIn_ReturnListCommentsTest() {
        List<Comment> comments = commentRepository.findCommentsByItemIn(List.of(item));
        assertNotNull(comments);
        assertEquals(1, comments.size());
    }

    @Test
    void findAllByItemIdInWhenSort_ReturnListCommentsTest() {
        Comment comment1 = new Comment();
        comment1.setText("new comment");
        comment1.setItem(item1);
        comment1.setAuthor(author);
        commentRepository.save(comment1);

        List<Comment> comments = commentRepository.findCommentsByItemIn(List.of(item, item1));
        assertNotNull(comments);
        assertEquals(2, comments.size());
        assertEquals(comments.get(1).getId(), comment1.getId());
    }

}
