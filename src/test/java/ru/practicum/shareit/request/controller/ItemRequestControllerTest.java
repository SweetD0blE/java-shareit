package ru.practicum.shareit.request.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.shareit.exception.ObjectNotFoundException;
import ru.practicum.shareit.request.dto.ItemRequestDto;
import ru.practicum.shareit.request.service.ItemRequestService;

import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Random;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ItemRequestController.class)
@AutoConfigureMockMvc
public class ItemRequestControllerTest {

    @Autowired
    private MockMvc mockMvc;
    @MockBean
    private ItemRequestService itemRequestService;

    String url = "/requests";

    ItemRequestDto.ItemRequestDtoBuilder itemRequestDtoBuilder;

    JavaTimeModule module = new JavaTimeModule();
    ObjectMapper mapper = new ObjectMapper().registerModule(module)
            .setDateFormat(new SimpleDateFormat("yyyy-MM-dd HH:mm"));

    @BeforeEach
    void setUp() {
        itemRequestDtoBuilder = ItemRequestDto.builder()
                .description("описание")
                .created(LocalDateTime.now());
    }

    @Test
    void getAllRequestsWithOffers_ReturnListTest() throws Exception {
        when(itemRequestService.getAllRequestsWithOffers(1L))
                .thenReturn(List.of(itemRequestDtoBuilder.id(1L).build()));
        mockMvc.perform(get(url)
                        .header("X-Sharer-User-Id", 1))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id", is(1)))
                .andExpect(jsonPath("$.size()", is(1)));
    }

    @Test
    void getAllRequestsWithOffers_ReturnEmptyListTest() throws Exception {
        when(itemRequestService.getAllRequestsWithOffers(1L)).thenReturn(Collections.emptyList());
        mockMvc.perform(get(url)
                        .header("X-Sharer-User-Id", 2))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(0)));
    }

    @Test
    void getAllRequestsWithOffersWithNotFoundUser_ReturnStatus404Test() throws Exception {
        when(itemRequestService.getAllRequestsWithOffers(999L))
                .thenThrow(new ObjectNotFoundException(String.format("Пользователь с id = %d не был найден", 999L)));
        mockMvc.perform(get(url)
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("X-Sharer-User-Id", 999))
                .andDo(print())
                .andExpect(status().isNotFound())
                .andExpect(content().json("{\"error\":\"Пользователь с id = 999 не был найден\"}"));
    }

    @Test
    void getRequestWithOffersById_ReturnStatus200AndCorrectJsonTest() throws Exception {
        ItemRequestDto itemRequestDto = ItemRequestDto.builder().id(1L).build();
        String json = mapper.writeValueAsString(itemRequestDto);

        when(itemRequestService.getRequestWithOffersById(1L, 1L)).thenReturn(itemRequestDto);
        mockMvc.perform(get(url + "/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("X-Sharer-User-Id", 1)
                        .content(json))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().json(json));
    }

    @Test
    void getRequestWithOffersByIdWhenNotExistingUserId_ReturnStatus404Test() throws Exception {
        when(itemRequestService.getRequestWithOffersById(999L, 1L))
                .thenThrow(new ObjectNotFoundException(String.format("Пользователь с id = %d не был найден", 999L)));
        mockMvc.perform(get(url + "/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("X-Sharer-User-Id", 999))
                .andDo(print())
                .andExpect(status().isNotFound())
                .andExpect(content().json("{\"error\":\"Пользователь с id = 999 не был найден\"}"));
    }

    @Test
    void getRequestWithOffersByIdWhenNotExistingRequestId_ReturnStatus404Test() throws Exception {
        when(itemRequestService.getRequestWithOffersById(1L, 999L))
                .thenThrow(new ObjectNotFoundException(String.format("Запрос с id = %d не был найден", 999L)));
        mockMvc.perform(get(url + "/999")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("X-Sharer-User-Id", 1))
                .andDo(print())
                .andExpect(status().isNotFound())
                .andExpect(content().json("{\"error\":\"Запрос с id = 999 не был найден\"}"));
    }

    @Test
    void getRequests_ReturnStatus200AndCorrectJsonTest() throws Exception {
        when(itemRequestService.getRequests(1L, 0, 1))
                .thenReturn(List.of(itemRequestDtoBuilder.id(1L).build()));
        mockMvc.perform(get(url + "/all")
                        .header("X-Sharer-User-Id", 1)
                        .param("from", "0")
                        .param("size", "1"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id", is(1)))
                .andExpect(jsonPath("$.size()", is(1)));
    }

    @Test
    void getRequestsIfFromNegative_ReturnStatus400Test() throws Exception {
        mockMvc.perform(get(url + "/all")
                        .header("X-Sharer-User-Id", 1)
                        .param("from", "-1"))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].code", is(400)))
                .andExpect(jsonPath("$[0].error", is("must be greater than or equal to 0")));
    }

    @Test
    void getRequestsIfSizeZero_ReturnStatus400Test() throws Exception {
        mockMvc.perform(get(url + "/all")
                        .header("X-Sharer-User-Id", 1)
                        .param("size", "0"))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].code", is(400)))
                .andExpect(jsonPath("$[0].error", is("must be greater than 0")));
    }

    @Test
    void getRequestsIfSizeNegative_ReturnStatus400Test() throws Exception {
        mockMvc.perform(get(url + "/all")
                        .header("X-Sharer-User-Id", 1)
                        .param("size", "-1"))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].code", is(400)))
                .andExpect(jsonPath("$[0].error", is("must be greater than 0")));
    }

    @Test
    void createRequest_ReturnStatus200AndCorrectJsonTest() throws Exception {
        ItemRequestDto itemRequestDto = itemRequestDtoBuilder.build();
        ItemRequestDto outItemRequestDto = itemRequestDtoBuilder.id(1L).build();
        String json = mapper.writeValueAsString(itemRequestDto);
        String jsonAdded = mapper.writeValueAsString(outItemRequestDto);

        when(itemRequestService.saveRequest(1L, itemRequestDto)).thenReturn(outItemRequestDto);
        mockMvc.perform(post(url)
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("X-Sharer-User-Id", 1)
                        .content(json))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().json(jsonAdded));
    }

    @Test
    void createRequestWithNotFoundUser_ReturnStatus404Test() throws Exception {
        ItemRequestDto itemRequestDto = itemRequestDtoBuilder.build();
        String json = mapper.writeValueAsString(itemRequestDto);
        when(itemRequestService.saveRequest(999L, itemRequestDto))
                .thenThrow(new ObjectNotFoundException(String.format("Пользователь с id = %d не был найден", 999L)));
        mockMvc.perform(post(url)
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("X-Sharer-User-Id", 999)
                        .content(json))
                .andDo(print())
                .andExpect(status().isNotFound())
                .andExpect(content().json("{\"error\":\"Пользователь с id = 999 не был найден\"}"));
    }

    @Test
    void createRequestIfRequestEmptyDescription_ReturnStatus400Test() throws Exception {
        ItemRequestDto itemRequestDto = itemRequestDtoBuilder.description("").build();
        String json = mapper.writeValueAsString(itemRequestDto);
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

}