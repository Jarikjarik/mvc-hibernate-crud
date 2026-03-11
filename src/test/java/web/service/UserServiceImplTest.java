package web.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import web.dao.UserDao;
import web.model.User;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserServiceImplTest {

    @Mock
    private UserDao userDao;

    @InjectMocks
    private UserServiceImpl userService;

    @Test
    void addUserShouldRejectNullInput() {
        assertThrows(IllegalArgumentException.class, () -> userService.addUser(null));
        verify(userDao, never()).addUser(any());
    }

    @Test
    void editUserShouldUpdateExistingEntityInsteadOfMergingDetachedObject() {
        User existingUser = buildUser(1L, "Ivan", "Petrov", "ivan@example.com");
        User updateRequest = buildUser(1L, "Иван", "Петров", "ivan.petrov@example.com");

        when(userDao.findById(1L)).thenReturn(Optional.of(existingUser));

        userService.editUser(updateRequest);

        assertEquals("Иван", existingUser.getName());
        assertEquals("Петров", existingUser.getSurname());
        assertEquals("ivan.petrov@example.com", existingUser.getEmail());
        verify(userDao).editUser(existingUser);
    }

    @Test
    void deleteUserShouldFailWhenUserDoesNotExist() {
        when(userDao.findById(99L)).thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class, () -> userService.deleteUser(99L));
        verify(userDao, never()).deleteUser(99L);
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
