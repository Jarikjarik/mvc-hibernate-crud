package web.config;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.flywaydb.core.Flyway;
import web.model.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.DependsOn;
import org.springframework.context.annotation.PropertySources;
import org.springframework.context.annotation.PropertySource;
import org.springframework.core.env.Environment;
import org.springframework.orm.hibernate5.HibernateTransactionManager;
import org.springframework.orm.hibernate5.LocalSessionFactoryBean;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import javax.sql.DataSource;
import java.util.Properties;

@Configuration
@PropertySources({
        @PropertySource("classpath:db.properties"),
        @PropertySource(value = "classpath:db.local.properties", ignoreResourceNotFound = true)
})
@EnableTransactionManagement
@ComponentScan(basePackages = {"web.service", "web.dao"})
public class DatabaseConfig {

    private final Environment env;

    @Autowired
    public DatabaseConfig(Environment env) {
        this.env = env;
    }

    @Bean
    public DataSource getDataSource() {
        HikariConfig hikariConfig = new HikariConfig();
        hikariConfig.setDriverClassName(getRequiredProperty("DB_DRIVER", "db.driver"));
        hikariConfig.setJdbcUrl(getRequiredProperty("DB_URL", "db.url"));
        hikariConfig.setUsername(getRequiredProperty("DB_USERNAME", "db.username"));
        hikariConfig.setPassword(getRequiredProperty("DB_PASSWORD", "db.password"));
        hikariConfig.setPoolName("mvc-hibernate-crud-pool");
        hikariConfig.setMaximumPoolSize(getIntProperty("DB_MAX_POOL_SIZE", "db.maxPoolSize", 10));
        hikariConfig.setMinimumIdle(getIntProperty("DB_MIN_IDLE", "db.minIdle", 2));
        hikariConfig.setConnectionTimeout(getLongProperty("DB_CONNECTION_TIMEOUT_MS", "db.connectionTimeoutMs", 30000L));

        return new HikariDataSource(hikariConfig);
    }

    @Bean(initMethod = "migrate")
    public Flyway flyway() {
        return Flyway.configure()
                .dataSource(getDataSource())
                .locations("classpath:db/migration")
                .baselineOnMigrate(true)
                .load();
    }

    @Bean
    @DependsOn("flyway")
    public LocalSessionFactoryBean getSessionFactory() {
        LocalSessionFactoryBean factoryBean = new LocalSessionFactoryBean();
        factoryBean.setDataSource(getDataSource());

        Properties props = new Properties();
        props.put("hibernate.show_sql", getRequiredProperty("HIBERNATE_SHOW_SQL", "hibernate.show_sql"));
        props.put("hibernate.hbm2ddl.auto", getRequiredProperty("HIBERNATE_HBM2DDL_AUTO", "hibernate.hbm2ddl.auto"));
        props.put("hibernate.format_sql", getRequiredProperty("HIBERNATE_FORMAT_SQL", "hibernate.format_sql"));
        props.put("hibernate.dialect", getRequiredProperty("HIBERNATE_DIALECT", "hibernate.dialect"));

        factoryBean.setHibernateProperties(props);
        factoryBean.setAnnotatedClasses(User.class);

        return factoryBean;
    }

    @Bean
    public HibernateTransactionManager getTransactionManager() {
        HibernateTransactionManager transactionManager = new HibernateTransactionManager();
        transactionManager.setSessionFactory(getSessionFactory().getObject());
        return transactionManager;
    }

    private String getRequiredProperty(String envKey, String propertyKey) {
        String value = env.getProperty(envKey);
        if (value == null || value.isBlank()) {
            value = env.getProperty(propertyKey);
        }
        if (value == null || value.isBlank()) {
            throw new IllegalStateException("Missing required configuration property: " + propertyKey);
        }
        return value;
    }

    private int getIntProperty(String envKey, String propertyKey, int defaultValue) {
        String value = env.getProperty(envKey);
        if (value == null || value.isBlank()) {
            value = env.getProperty(propertyKey);
        }
        if (value == null || value.isBlank()) {
            return defaultValue;
        }
        return Integer.parseInt(value);
    }

    private long getLongProperty(String envKey, String propertyKey, long defaultValue) {
        String value = env.getProperty(envKey);
        if (value == null || value.isBlank()) {
            value = env.getProperty(propertyKey);
        }
        if (value == null || value.isBlank()) {
            return defaultValue;
        }
        return Long.parseLong(value);
    }
}
