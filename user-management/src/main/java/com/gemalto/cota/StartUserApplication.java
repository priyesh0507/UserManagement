package com.gemalto.cota;

import com.gemalto.cota.entities.User;
import com.gemalto.cota.repo.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Profile;

import java.math.BigDecimal;

@SpringBootApplication
public class StartUserApplication {

    // start everything
    public static void main(String[] args) {
        SpringApplication.run(StartUserApplication.class, args);
    }

    // run this only on profile 'demo', avoid run this in test
/*    @Profile("demo")
    @Bean
    CommandLineRunner initDatabase(UserRepository repository) {
        return args -> {
            repository.save(new User("A Guide to the Bodhisattva Way of Life", "Santideva", new BigDecimal("15.41")));
            repository.save(new User("The Life-Changing Magic of Tidying Up", "Marie Kondo", new BigDecimal("9.69")));
            repository.save(new User("Refactoring: Improving the Design of Existing Code", "Martin Fowler", new BigDecimal("47.99")));
        };
    }*/
}