package web.dao;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import web.model.User;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.TypedQuery;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.Set;

@Repository
public class UserDaoImpl implements UserDao {

    private static final Logger logger = LoggerFactory.getLogger(UserDaoImpl.class);
    private static final Set<String> ALLOWED_SORT_FIELDS = Set.of("id", "name", "surname", "email", "createdAt", "updatedAt");

    @PersistenceContext
    private EntityManager entityManager;

    @Override
    @Transactional(readOnly = true)
    public List<User> getUsers() {
        logger.debug("Fetching all users");
        return entityManager.createQuery("from User u order by u.id", User.class).getResultList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<User> getUsers(String searchTerm, String sortBy, String sortDirection, int page, int size) {
        logger.debug(
                "Fetching paginated users with searchTerm={}, sortBy={}, sortDirection={}, page={}, size={}",
                searchTerm,
                sortBy,
                sortDirection,
                page,
                size
        );

        String normalizedSortBy = normalizeSortBy(sortBy);
        String normalizedSortDirection = normalizeSortDirection(sortDirection);
        String queryString = "from User u"
                + buildSearchClause(searchTerm)
                + " order by u." + normalizedSortBy + " " + normalizedSortDirection + ", u.id asc";

        TypedQuery<User> query = entityManager.createQuery(queryString, User.class);
        applySearchParameter(query, searchTerm);
        query.setFirstResult(page * size);
        query.setMaxResults(size);
        return query.getResultList();
    }

    @Override
    @Transactional(readOnly = true)
    public long countUsers(String searchTerm) {
        logger.debug("Counting users with searchTerm={}", searchTerm);
        String queryString = "select count(u) from User u" + buildSearchClause(searchTerm);
        TypedQuery<Long> query = entityManager.createQuery(queryString, Long.class);
        applySearchParameter(query, searchTerm);
        return query.getSingleResult();
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

    private String buildSearchClause(String searchTerm) {
        if (searchTerm == null || searchTerm.isBlank()) {
            return "";
        }
        return " where lower(u.name) like :searchTerm"
                + " or lower(u.surname) like :searchTerm"
                + " or lower(u.email) like :searchTerm";
    }

    private void applySearchParameter(TypedQuery<?> query, String searchTerm) {
        if (searchTerm == null || searchTerm.isBlank()) {
            return;
        }
        query.setParameter("searchTerm", "%" + searchTerm.trim().toLowerCase(Locale.ROOT) + "%");
    }

    private String normalizeSortBy(String sortBy) {
        if (sortBy == null || sortBy.isBlank()) {
            return "id";
        }
        return ALLOWED_SORT_FIELDS.contains(sortBy) ? sortBy : "id";
    }

    private String normalizeSortDirection(String sortDirection) {
        return "desc".equalsIgnoreCase(sortDirection) ? "desc" : "asc";
    }
}
