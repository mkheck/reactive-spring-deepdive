package com.example.coffeeservicemongo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;

import javax.annotation.PostConstruct;
import java.util.UUID;

@SpringBootApplication
public class CoffeeServiceMongoApplication {

    public static void main(String[] args) {
        SpringApplication.run(CoffeeServiceMongoApplication.class, args);
    }
}

@Component
class DataLoader {
    private final CoffeeRepository repo;

    DataLoader(CoffeeRepository repo) {
        this.repo = repo;
    }

    @PostConstruct
    private void init() {
        repo.deleteAll().thenMany(
                Flux.just("Jet Black Mongo", "Darth Mongo", "Pit of Despair Mongo", "Black Alert Mongo")
                        .map(name -> new Coffee(UUID.randomUUID().toString(), name))
                        .flatMap(repo::save))
                .thenMany(repo.findAll())
                .subscribe(System.out::println);
    }
}

@RestController
class CoffeeController {
    private final CoffeeRepository repo;

    CoffeeController(CoffeeRepository repo) {
        this.repo = repo;
    }

    @GetMapping("/coffees")
    public Flux<Coffee> getAllCoffees() {
        return repo.findAll();
    }
}

interface CoffeeRepository extends ReactiveCrudRepository<Coffee, String> {
}

@Document
@Data
@NoArgsConstructor
@AllArgsConstructor
class Coffee {
    @Id
    private String id;
    private String name;
}