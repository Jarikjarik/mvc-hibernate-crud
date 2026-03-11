package web.dao;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import web.config.DatabaseConfig;
import web.model.User;

import javax.sql.DataSource;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@ExtendWith(SpringExtension.class)
@Testcontainers
@ContextConfiguration(classes = DatabaseConfig.class, initializers = UserDaoImplIT.Initializer.class)
class UserDaoImplIT {

    @Container
    @SuppressWarnings("resource")
    private static final PostgreSQLContainer<?> POSTGRESQL_CONTAINER = new PostgreSQLContainer<>("postgres:17-alpine")
            .withDatabaseName("mvc-hibernate-crud-test")
            .withUsername("app_user")
            .withPassword("12345");

    @Autowired
    private UserDao userDao;

    @Autowired
    private DataSource dataSource;

    private JdbcTemplate jdbcTemplate;

    @BeforeEach
    void setUp() {
        jdbcTemplate = new JdbcTemplate(dataSource);
        jdbcTemplate.execute("TRUNCATE TABLE users RESTART IDENTITY");
    }

    @Test
    void addUserShouldPersistAndPopulateAuditFields() {
        User user = buildUser("Ivan", "Petrov", "ivan.petrov@example.com");

        userDao.addUser(user);

        assertNotNull(user.getId());
        Optional<User> persistedUser = userDao.findById(user.getId());
        assertTrue(persistedUser.isPresent());
        assertNotNull(persistedUser.get().getCreatedAt());
        assertNotNull(persistedUser.get().getUpdatedAt());
    }

    @Test
    void findByEmailShouldBeCaseInsensitive() {
        User user = buildUser("Anna", "Smirnova", "anna.smirnova@example.com");
        userDao.addUser(user);

        Optional<User> foundUser = userDao.findByEmail("ANNA.SMIRNOVA@EXAMPLE.COM");

        assertTrue(foundUser.isPresent());
        assertEquals("Anna", foundUser.get().getName());
    }

    @Test
    void editUserShouldUpdatePersistedState() {
        User user = buildUser("Maksim", "Sokolov", "maksim.sokolov@example.com");
        userDao.addUser(user);

        user.setSurname("Volkov");
        user.setEmail("maksim.volkov@example.com");
        userDao.editUser(user);

        User updatedUser = userDao.findById(user.getId()).orElseThrow();
        assertEquals("Volkov", updatedUser.getSurname());
        assertEquals("maksim.volkov@example.com", updatedUser.getEmail());
    }

    @Test
    void deleteUserShouldRemoveEntity() {
        User user = buildUser("Olga", "Romanova", "olga.romanova@example.com");
        userDao.addUser(user);

        userDao.deleteUser(user.getId());

        assertTrue(userDao.findById(user.getId()).isEmpty());
    }

    @Test
    void getUsersShouldApplySearchSortingAndPagination() {
        userDao.addUser(buildUser("Anna", "Smirnova", "anna.smirnova@example.com"));
        userDao.addUser(buildUser("Anastasia", "Sidorova", "anastasia.sidorova@example.com"));
        userDao.addUser(buildUser("Boris", "Petrov", "boris.petrov@example.com"));

        assertEquals(2L, userDao.countUsers("an"));

        var users = userDao.getUsers("an", "name", "asc", 0, 1);

        assertEquals(1, users.size());
        assertEquals("Anastasia", users.get(0).getName());
    }

    private User buildUser(String name, String surname, String email) {
        User user = new User();
        user.setName(name);
        user.setSurname(surname);
        user.setEmail(email);
        return user;
    }

    static class Initializer implements ApplicationContextInitializer<ConfigurableApplicationContext> {
        @Override
        public void initialize(ConfigurableApplicationContext applicationContext) {
            POSTGRESQL_CONTAINER.start();
            System.setProperty("DB_DRIVER", "org.postgresql.Driver");
            System.setProperty("DB_URL", POSTGRESQL_CONTAINER.getJdbcUrl());
            System.setProperty("DB_USERNAME", POSTGRESQL_CONTAINER.getUsername());
            System.setProperty("DB_PASSWORD", POSTGRESQL_CONTAINER.getPassword());
            System.setProperty("HIBERNATE_SHOW_SQL", "false");
            System.setProperty("HIBERNATE_HBM2DDL_AUTO", "validate");
            System.setProperty("HIBERNATE_FORMAT_SQL", "false");
            System.setProperty("HIBERNATE_DIALECT", "org.hibernate.dialect.PostgreSQL10Dialect");
        }
    }
}
