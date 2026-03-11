package web.service;

import web.model.User;
import java.util.List;
import java.util.Optional;

public interface UserService {
    List<User> getUsers();
    void addUser(User user);
    void deleteUser(Long id);
    void editUser(User user);
    Optional<User> findById(Long id);
    Optional<User> findByEmail(String email);

}
