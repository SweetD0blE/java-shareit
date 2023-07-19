package ru.practicum.shareit.item.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.shareit.exception.ObjectNotFoundException;
import ru.practicum.shareit.item.comment.dto.CommentDto;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.dto.ItemOwnerDto;
import ru.practicum.shareit.item.service.ItemService;

import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Random;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;

@WebMvcTest(ItemController.class)
@RequiredArgsConstructor(onConstructor_ = @Autowired)
class ItemControllerTest {

    @Autowired
    private MockMvc mockMvc;
    @MockBean
    private final ItemService itemService;

    String url = "/items";

    ItemDto.ItemDtoBuilder itemDtoBuilder;
    ItemOwnerDto.ItemOwnerDtoBuilder itemOwnerDtoBuilder;
    CommentDto.CommentDtoBuilder commentDtoBuilder;

    JavaTimeModule module = new JavaTimeModule();
    ObjectMapper mapper = new ObjectMapper().registerModule(module)
            .setDateFormat(new SimpleDateFormat("yyyy-MM-dd HH:mm"));

    @BeforeEach
    void setUp() {
        itemDtoBuilder = ItemDto.builder()
                .name("item")
                .description("описание")
                .available(true)
                .requestId(null);
        itemOwnerDtoBuilder = ItemOwnerDto.builder()
                .name("itemOwner")
                .description("описание для владельца")
                .available(true);
        commentDtoBuilder = CommentDto.builder()
                .text("проверьте отзыв об использовании товара");
    }

    @Test
    void getAllItems_ReturnListItemsTest() throws Exception {
        when(itemService.getAllItemsByUserId(1L, 0, 10))
                .thenReturn(List.of(itemOwnerDtoBuilder.id(1L).build()));
        mockMvc.perform(get(url)
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("X-Sharer-User-Id", 1))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id", is(1)))
                .andExpect(jsonPath("$.size()", is(1)));
    }

    @Test
    void getAllItemsIfNoUsers_ReturnEmptyListTest() throws Exception {
        when(itemService.getAllItemsByUserId(1L, 0, 10)).thenReturn(Collections.emptyList());
        mockMvc.perform(get(url)
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("X-Sharer-User-Id", 1))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.size()", is(0)));
    }

    @Test
    void getItemById_ReturnStatus200AndCorrectJsonTest() throws Exception {
        ItemOwnerDto itemOwnerDto = ItemOwnerDto.builder().id(1L).build();
        String json = mapper.writeValueAsString(itemOwnerDto);

        when(itemService.getItemById(1L, 1L)).thenReturn(itemOwnerDto);
        mockMvc.perform(get(url + "/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("X-Sharer-User-Id", 1)
                        .content(json))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().json(json));
    }

    @Test
    void getItemByIdWhenNotExistingUserId_ReturnStatus404Test() throws Exception {
        when(itemService.getItemById(999L, 1L))
                .thenThrow(new ObjectNotFoundException(String.format("User not found: id=%d", 999L)));
        mockMvc.perform(get(url + "/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("X-Sharer-User-Id", 999))
                .andDo(print())
                .andExpect(status().isNotFound())
                .andExpect(content().json("{\"error\":\"User not found: id=999\"}"));
    }

    @Test
    void getItemByIdWhenNotExistingItemId_ReturnStatus404Test() throws Exception {
        when(itemService.getItemById(1L, 999L))
                .thenThrow(new ObjectNotFoundException(String.format("Item not found: id=%d", 999L)));
        mockMvc.perform(get(url + "/999")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("X-Sharer-User-Id", 1))
                .andDo(print())
                .andExpect(status().isNotFound())
                .andExpect(content().json("{\"error\":\"Item not found: id=999\"}"));
    }

    @Test
    void searchItem_ReturnEmptyListTest() throws Exception {
        when(itemService.getSearchItem("", 0, 10)).thenReturn(Collections.emptyList());
        mockMvc.perform(get(url + "/search").param("text", ""))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(0)));
    }

    @Test
    void searchItemIfFromNegative_ReturnStatus400Test() throws Exception {
        mockMvc.perform(get(url + "/search").param("from", "-1"))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].code", is(400)))
                .andExpect(jsonPath("$[0].error", is("must be greater than or equal to 0")));
    }

    @Test
    void searchItemIfSizeZero_ReturnStatus400Test() throws Exception {
        mockMvc.perform(get(url + "/search").param("size", "0"))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].code", is(400)))
                .andExpect(jsonPath("$[0].error", is("must be greater than 0")));
    }

    @Test
    void searchItemIfSizeNegative_ReturnStatus400Test() throws Exception {
        mockMvc.perform(get(url + "/search").param("size", "-1"))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].code", is(400)))
                .andExpect(jsonPath("$[0].error", is("must be greater than 0")));
    }

    @Test
    void createItem_ReturnStatus200AndCorrectJsonTest() throws Exception {
        ItemDto itemDto = itemDtoBuilder.build();
        ItemDto itemDtoResponse = itemDtoBuilder.id(1L).build();
        String json = mapper.writeValueAsString(itemDto);
        String jsonAdded = mapper.writeValueAsString(itemDtoResponse);

        when(itemService.createItem(1L, itemDto)).thenReturn(itemDtoResponse);
        mockMvc.perform(post(url)
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("X-Sharer-User-Id", 1)
                        .content(json))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().json(jsonAdded));
    }

    @Test
    void createItemWithNotFoundUser_ReturnStatus404Test() throws Exception {
        ItemDto itemDto = itemDtoBuilder.build();
        String json = mapper.writeValueAsString(itemDto);
        when(itemService.createItem(999L, itemDto))
                .thenThrow(new ObjectNotFoundException(String.format("User not found: id=%d", 999L)));
        mockMvc.perform(post(url)
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("X-Sharer-User-Id", 999)
                        .content(json))
                .andDo(print())
                .andExpect(status().isNotFound())
                .andExpect(content().json("{\"error\":\"User not found: id=999\"}"));
    }

    @Test
    void createItemWithoutUserId_ReturnStatus500Test() throws Exception {
        ItemDto itemDto = itemDtoBuilder.build();
        String json = mapper.writeValueAsString(itemDto);
        mockMvc.perform(post(url)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andDo(print())
                .andExpect(status().is5xxServerError())
                .andExpect(content().json("{\"error\":\"Required request header 'X-Sharer-User-Id' " +
                        "for method parameter type Long is not present\"}"));
    }

    @Test
    void createItemIfItemWithoutAvailable_ReturnStatus400Test() throws Exception {
        ItemDto itemDto = itemDtoBuilder.available(null).build();
        String json = mapper.writeValueAsString(itemDto);
        mockMvc.perform(post(url)
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("X-Sharer-User-Id", 1)
                        .content(json))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].code", is(400)))
                .andExpect(jsonPath("$[0].fieldName", is("available")))
                .andExpect(jsonPath("$[0].error", is("must not be null")));
    }

    @Test
    void createItemIfItemEmptyName_ReturnStatus400Test() throws Exception {
        ItemDto itemDto = itemDtoBuilder.name("").build();
        String json = mapper.writeValueAsString(itemDto);
        mockMvc.perform(post(url)
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("X-Sharer-User-Id", 1)
                        .content(json))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].code", is(400)))
                .andExpect(jsonPath("$[0].fieldName", is("name")))
                .andExpect(jsonPath("$[0].error", is("must not be blank")));
    }

    @Test
    void createItemIfItemEmptyDescription_ReturnStatus400Test() throws Exception {
        ItemDto itemDto = itemDtoBuilder.description("").build();
        String json = mapper.writeValueAsString(itemDto);
        mockMvc.perform(post(url)
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("X-Sharer-User-Id", 1)
                        .content(json))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].code", is(400)))
                .andExpect(jsonPath("$[0].fieldName", is("description")))
                .andExpect(jsonPath("$[0].error", is("must not be blank")));
    }

    @Test
    void createCommentItem_ReturnStatus200AndCorrectJsonTest() throws Exception {
        CommentDto commentDto = commentDtoBuilder.build();
        CommentDto outCommentDto = commentDtoBuilder.id(1L).authorName("name").created(LocalDateTime.now()).build();
        String json = mapper.writeValueAsString(commentDto);
        String jsonAdded = mapper.writeValueAsString(outCommentDto);

        when(itemService.createComment(1L, 1L, commentDto)).thenReturn(outCommentDto);
        mockMvc.perform(post(url + "/1/comment")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("X-Sharer-User-Id", 1)
                        .content(json))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().json(jsonAdded));
    }

    @Test
    void createCommentItemWithNotFoundUser_ReturnStatus404Test() throws Exception {
        CommentDto commentDto = commentDtoBuilder.build();
        String json1 = mapper.writeValueAsString(commentDto);
        when(itemService.createComment(999L, 1L, commentDto))
                .thenThrow(new ObjectNotFoundException(String.format("User not found: id=%d", 999L)));
        mockMvc.perform(post(url + "/1/comment")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("X-Sharer-User-Id", 999)
                        .content(json1))
                .andDo(print())
                .andExpect(status().isNotFound())
                .andExpect(content().json("{\"error\":\"User not found: id=999\"}"));
    }

    @Test
    void createCommentItemWithNotFoundItem_ReturnStatus404Test() throws Exception {
        CommentDto commentDto = commentDtoBuilder.build();
        String json = mapper.writeValueAsString(commentDto);
        when(itemService.createComment(1L, 999L, commentDto))
                .thenThrow(new ObjectNotFoundException(String.format("Item not found: id=%d", 999L)));
        mockMvc.perform(post(url + "/999/comment")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("X-Sharer-User-Id", 1)
                        .content(json))
                .andDo(print())
                .andExpect(status().isNotFound())
                .andExpect(content().json("{\"error\":\"Item not found: id=999\"}"));
    }

    @Test
    void createCommentItemIfCommentEmptyText_ReturnStatus400Test() throws Exception {
        CommentDto commentDto = commentDtoBuilder.text("").build();
        String json = mapper.writeValueAsString(commentDto);
        mockMvc.perform(post(url + "/1/comment")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("X-Sharer-User-Id", 1)
                        .content(json))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].code", is(400)))
                .andExpect(jsonPath("$[0].fieldName", is("text")))
                .andExpect(jsonPath("$[0].error", is("must not be blank")));
    }

    @Test
    void createCommentItemIfCommentTextWrongSize_ReturnStatus400Test() throws Exception {
        byte[] array = new byte[550];
        new Random().nextBytes(array);
        String generatedString = new String(array, StandardCharsets.UTF_8);
        CommentDto commentDto = commentDtoBuilder.text(generatedString).build();
        String json = mapper.writeValueAsString(commentDto);
        mockMvc.perform(post(url + "/1/comment")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("X-Sharer-User-Id", 1)
                        .content(json))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].code", is(400)))
                .andExpect(jsonPath("$[0].fieldName", is("text")))
                .andExpect(jsonPath("$[0].error", is("size must be between 0 and 400")));
    }

    @Test
    void patchItem_ReturnStatus200AndCorrectJsonTest() throws Exception {
        ItemDto itemDto = itemDtoBuilder.id(1L).name("Update").description("Update description")
                .available(true).build();
        ItemDto itemDtoResponse = itemDtoBuilder.id(1L).name("Update").description("Update description")
                .available(true).build();
        String json = mapper.writeValueAsString(itemDto);
        String jsonAdded = mapper.writeValueAsString(itemDtoResponse);

        when(itemService.updateItem(1L, 1L, itemDto)).thenReturn(itemDtoResponse);
        mockMvc.perform(patch(url + "/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("X-Sharer-User-Id", 1)
                        .content(json))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().json(jsonAdded));
    }

    @Test
    void patchItemWithoutUserId_ReturnStatus500Test() throws Exception {
        ItemDto itemDto = itemDtoBuilder.build();
        String json = mapper.writeValueAsString(itemDto);
        mockMvc.perform(patch(url + "/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andDo(print())
                .andExpect(status().is5xxServerError())
                .andExpect(content().json("{\"error\":\"Required request header 'X-Sharer-User-Id' " +
                        "for method parameter type Long is not present\"}"));
    }

    @Test
    void patchItemWithNotFoundItem_ReturnStatus404Test() throws Exception {
        ItemDto itemDto = itemDtoBuilder.build();
        String json = mapper.writeValueAsString(itemDto);
        when(itemService.updateItem(1L, 999L, itemDto))
                .thenThrow(new ObjectNotFoundException(String.format("Item not found: id=%d", 999L)));
        mockMvc.perform(patch(url + "/999")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("X-Sharer-User-Id", 1)
                        .content(json))
                .andDo(print())
                .andExpect(status().isNotFound())
                .andExpect(content().json("{\"error\":\"Item not found: id=999\"}"));
    }

    @Test
    void patchItemWithNotFoundUser_ReturnStatus404Test() throws Exception {
        ItemDto itemDto = itemDtoBuilder.build();
        String json = mapper.writeValueAsString(itemDto);
        when(itemService.updateItem(999L, 1L, itemDto))
                .thenThrow(new ObjectNotFoundException(String.format("User not found: id=%d", 999L)));
        mockMvc.perform(patch(url + "/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("X-Sharer-User-Id", 999)
                        .content(json))
                .andDo(print())
                .andExpect(status().isNotFound())
                .andExpect(content().json("{\"error\":\"User not found: id=999\"}"));
    }

    @Test
    void patchItemIfItemName_ReturnStatus200AndCorrectJsonTest() throws Exception {
        ItemDto itemDto = itemDtoBuilder.id(1L).name("Update").build();
        ItemDto itemDtoResponse = itemDtoBuilder.id(1L).name("Update").build();
        String json = mapper.writeValueAsString(itemDto);
        String jsonAdded = mapper.writeValueAsString(itemDtoResponse);

        when(itemService.updateItem(1L, 1L, itemDto)).thenReturn(itemDtoResponse);
        this.mockMvc.perform(patch(url + "/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("X-Sharer-User-Id", 1)
                        .content(json))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().json(jsonAdded));
    }

    @Test
    void patchItemIfItemDescription_ReturnStatus200AndCorrectJsonTest() throws Exception {
        ItemDto itemDto = itemDtoBuilder.id(1L).description("Обновление описания").build();
        ItemDto itemDtoResponse = itemDtoBuilder.id(1L).description("Обновление описания").build();
        String json = mapper.writeValueAsString(itemDto);
        String jsonAdded = mapper.writeValueAsString(itemDtoResponse);

        when(itemService.updateItem(1L, 1L, itemDto)).thenReturn(itemDtoResponse);
        this.mockMvc.perform(patch(url + "/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("X-Sharer-User-Id", 1)
                        .content(json))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().json(jsonAdded));
    }

    @Test
    void patchItemIfItemAvailable_ReturnStatus200AndCorrectJsonTest() throws Exception {
        ItemDto itemDto = itemDtoBuilder.id(1L).available(true).build();
        ItemDto itemDtoResponse = itemDtoBuilder.id(1L).available(true).build();
        String json = mapper.writeValueAsString(itemDto);
        String jsonAdded = mapper.writeValueAsString(itemDtoResponse);

        when(itemService.updateItem(1L, 1L, itemDto)).thenReturn(itemDtoResponse);
        this.mockMvc.perform(patch(url + "/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("X-Sharer-User-Id", 1)
                        .content(json))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().json(jsonAdded));
    }
}