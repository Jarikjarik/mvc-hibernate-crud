package web.config;

import org.flywaydb.core.Flyway;
import web.model.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.DependsOn;
import org.springframework.context.annotation.PropertySource;
import org.springframework.core.env.Environment;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import org.springframework.orm.hibernate5.HibernateTransactionManager;
import org.springframework.orm.hibernate5.LocalSessionFactoryBean;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import javax.sql.DataSource;
import java.util.Properties;

@Configuration
@PropertySource("classpath:db.properties")
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
        DriverManagerDataSource dataSource = new DriverManagerDataSource();
        dataSource.setDriverClassName(getRequiredProperty("DB_DRIVER", "db.driver"));
        dataSource.setUrl(getRequiredProperty("DB_URL", "db.url"));
        dataSource.setUsername(getRequiredProperty("DB_USERNAME", "db.username"));
        dataSource.setPassword(getRequiredProperty("DB_PASSWORD", "db.password"));

        return dataSource;
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
}
