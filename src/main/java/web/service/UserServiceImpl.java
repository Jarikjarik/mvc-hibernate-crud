package web.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import web.dao.UserDao;
import web.model.User;
import java.util.List;
import java.util.Optional;

@Service
public class UserServiceImpl implements UserService {

    private static final Logger logger = LoggerFactory.getLogger(UserServiceImpl.class);

    private final UserDao userDao;

    public UserServiceImpl(UserDao userDao) {
        this.userDao = userDao;
    }

    @Override
    public Optional<User> findById(Long id) {
        logger.debug("Finding user by id: {}", id);
        return userDao.findById(id);
    }

    @Override
    public Optional<User> findByEmail(String email) {
        logger.debug("Finding user by email: {}", email);
        return userDao.findByEmail(email);
    }

    @Override
    public List<User> getUsers() {
        logger.debug("Getting all users");
        return userDao.getUsers();
    }

    @Override
    public List<User> getUsers(String searchTerm, String sortBy, String sortDirection, int page, int size) {
        logger.debug(
                "Getting users with searchTerm={}, sortBy={}, sortDirection={}, page={}, size={}",
                searchTerm,
                sortBy,
                sortDirection,
                page,
                size
        );
        return userDao.getUsers(searchTerm, sortBy, sortDirection, page, size);
    }

    @Override
    public long countUsers(String searchTerm) {
        logger.debug("Counting users with searchTerm={}", searchTerm);
        return userDao.countUsers(searchTerm);
    }

    @Override
    public void addUser(User user) {
        if (user == null) {
            throw new IllegalArgumentException("User cannot be null");
        }
        logger.info("Adding new user: {}", user);
        userDao.addUser(user);
    }

    @Override
    public void deleteUser(Long id) {
        if (id == null) {
            throw new IllegalArgumentException("User id cannot be null.");
        }
        if (findById(id).isEmpty()) {
            throw new IllegalArgumentException("User with id " + id + " was not found.");
        }
        logger.info("Deleting user with id: {}", id);
        userDao.deleteUser(id);
    }

    @Override
    public void editUser(User user) {
        if (user == null) {
            throw new IllegalArgumentException("User cannot be null.");
        }
        if (user.getId() == null) {
            throw new IllegalArgumentException("User id cannot be null.");
        }
        User existingUser = userDao.findById(user.getId())
                .orElseThrow(() -> new IllegalArgumentException("User with id " + user.getId() + " was not found."));

        existingUser.setName(user.getName());
        existingUser.setSurname(user.getSurname());
        existingUser.setEmail(user.getEmail());

        logger.info("Editing user: {}", existingUser);
        userDao.editUser(existingUser);
    }
}
