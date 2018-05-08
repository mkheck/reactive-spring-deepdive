package com.example.coffeeservicecouch;

import com.couchbase.client.java.repository.annotation.Field;
import com.couchbase.client.java.repository.annotation.Id;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.couchbase.core.mapping.Document;
import org.springframework.data.couchbase.core.query.View;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;

import javax.annotation.PostConstruct;
import java.util.UUID;

@SpringBootApplication
public class CoffeeServiceCouchApplication {

    public static void main(String[] args) {
        SpringApplication.run(CoffeeServiceCouchApplication.class, args);
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
        repo.findAllById().flatMap(repo::delete).thenMany(
                Flux.just("Jet Black Couch", "Darth Couch", "Pit of Despair Couch", "Black Alert Couch")
                        .map(name -> new Coffee(UUID.randomUUID().toString(), name))
                        .flatMap(repo::save))
                .thenMany(repo.findAllById())
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
        return repo.findAllById();
    }
}

interface CoffeeRepository extends ReactiveCrudRepository<Coffee, String> {
    @View(viewName = "all")
    Flux<Coffee> findAllById();
}

@Document
@Data
@NoArgsConstructor
@AllArgsConstructor
class Coffee {
    @Id
    private String id;
    @Field
    private String name;
}