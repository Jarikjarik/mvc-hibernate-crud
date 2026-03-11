package web.dao;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import web.model.User;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.util.List;
import java.util.Optional;

@Repository
public class UserDaoImpl implements UserDao {

    private static final Logger logger = LoggerFactory.getLogger(UserDaoImpl.class);

    @PersistenceContext
    private EntityManager entityManager;

    @Override
    @Transactional(readOnly = true)
    public List<User> getUsers() {
        logger.debug("Fetching all users");
        return entityManager.createQuery("from User u order by u.id", User.class).getResultList();
    }

    @Override
    @Transactional
    public void addUser(User user) {
        logger.info("Adding new user: {}", user);
        entityManager.persist(user);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<User> findById(Long id) {
        logger.debug("Finding user by id: {}", id);
        return Optional.ofNullable(entityManager.find(User.class, id));
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<User> findByEmail(String email) {
        logger.debug("Finding user by email: {}", email);
        return entityManager.createQuery(
                        "select u from User u where lower(u.email) = lower(:email)",
                        User.class
                )
                .setParameter("email", email)
                .getResultStream()
                .findFirst();
    }

    @Override
    @Transactional
    public void deleteUser(Long id) {
        logger.info("Deleting user with id: {}", id);
        User user = findById(id).orElse(null);
        if (user != null) {
            entityManager.remove(user);
        }
    }

    @Override
    @Transactional
    public void editUser(User user) {
        logger.info("Editing user: {}", user);
        entityManager.merge(user);
    }
}
