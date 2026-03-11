package web.dao;

import web.model.User;
import java.util.List;
import java.util.Optional;

public interface UserDao {
    List<User> getUsers();
    void addUser(User user);
    void deleteUser(Long id);
    void editUser(User user);
    Optional<User> findById(Long id);
    Optional<User> findByEmail(String email);

}
