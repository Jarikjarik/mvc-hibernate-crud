package web.controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.servlet.view.InternalResourceViewResolver;
import web.model.User;
import web.service.UserService;

import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.flash;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

@ExtendWith(MockitoExtension.class)
class UserControllerTest {

    @Mock
    private UserService userService;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        UserController controller = new UserController(userService);
        InternalResourceViewResolver viewResolver = new InternalResourceViewResolver();
        viewResolver.setPrefix("/WEB-INF/pages/");
        viewResolver.setSuffix(".html");
        mockMvc = MockMvcBuilders.standaloneSetup(controller, new GlobalExceptionHandler())
                .setViewResolvers(viewResolver)
                .build();
    }

    @Test
    void getAllUsersShouldRenderUsersPage() throws Exception {
        when(userService.countUsers(null)).thenReturn(1L);
        when(userService.getUsers(null, "id", "asc", 0, 5))
                .thenReturn(List.of(buildUser(1L, "Ivan", "Petrov", "ivan@example.com")));

        mockMvc.perform(get("/users"))
                .andExpect(status().isOk())
                .andExpect(view().name("users"))
                .andExpect(model().attributeExists("users"))
                .andExpect(model().attribute("currentPage", 1))
                .andExpect(model().attribute("totalPages", 1));
    }

    @Test
    void getAllUsersShouldApplySearchAndPaginationParameters() throws Exception {
        when(userService.countUsers("anna")).thenReturn(6L);
        when(userService.getUsers("anna", "surname", "desc", 1, 5))
                .thenReturn(List.of(buildUser(2L, "Anna", "Smirnova", "anna@example.com")));

        mockMvc.perform(get("/users")
                        .param("q", "anna")
                        .param("sort", "surname")
                        .param("dir", "desc")
                        .param("page", "2"))
                .andExpect(status().isOk())
                .andExpect(view().name("users"))
                .andExpect(model().attribute("searchTerm", "anna"))
                .andExpect(model().attribute("sortBy", "surname"))
                .andExpect(model().attribute("sortDirection", "desc"))
                .andExpect(model().attribute("currentPage", 2))
                .andExpect(model().attribute("totalPages", 2));
    }

    @Test
    void createUserShouldRejectDuplicateEmail() throws Exception {
        when(userService.findByEmail("ivan@example.com"))
                .thenReturn(Optional.of(buildUser(1L, "Ivan", "Petrov", "ivan@example.com")));

        mockMvc.perform(post("/users")
                        .characterEncoding("UTF-8")
                        .param("name", "Иван")
                        .param("surname", "Петров")
                        .param("email", "ivan@example.com"))
                .andExpect(status().isOk())
                .andExpect(view().name("adduser"))
                .andExpect(model().attributeHasFieldErrors("user", "email"));

        verify(userService, never()).addUser(any());
    }

    @Test
    void createUserShouldRedirectAfterSuccessfulSave() throws Exception {
        when(userService.findByEmail("ivan.petrov@example.com")).thenReturn(Optional.empty());

        mockMvc.perform(post("/users")
                        .characterEncoding("UTF-8")
                        .param("name", "Иван")
                        .param("surname", "Петров")
                        .param("email", "ivan.petrov@example.com"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/users"))
                .andExpect(flash().attribute("successMessage", "User has been created successfully."));

        verify(userService).addUser(any(User.class));
    }

    @Test
    void editFormShouldShowErrorPageWhenUserIsMissing() throws Exception {
        when(userService.findById(42L)).thenReturn(Optional.empty());

        mockMvc.perform(get("/users/edit").param("id", "42"))
                .andExpect(status().isOk())
                .andExpect(view().name("error"))
                .andExpect(model().attribute("errorMessage", "User with id 42 was not found."));
    }

    @Test
    void deleteShouldRedirectWithFlashMessage() throws Exception {
        mockMvc.perform(post("/users/delete").param("id", "5"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/users"))
                .andExpect(flash().attribute("successMessage", "User has been deleted successfully."));

        verify(userService).deleteUser(5L);
    }

    private User buildUser(Long id, String name, String surname, String email) {
        User user = new User();
        user.setId(id);
        user.setName(name);
        user.setSurname(surname);
        user.setEmail(email);
        return user;
    }
}
