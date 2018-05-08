package com.example.coffeeservicecass;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.cassandra.core.mapping.PrimaryKey;
import org.springframework.data.cassandra.core.mapping.Table;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.http.MediaType;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.core.userdetails.MapReactiveUserDetailsService;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.server.SecurityWebFilterChain;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.reactive.function.server.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Hooks;
import reactor.core.publisher.Mono;

import javax.annotation.PostConstruct;
import java.time.Duration;
import java.util.Date;
import java.util.UUID;

import static org.springframework.web.reactive.function.server.RequestPredicates.*;
import static org.springframework.web.reactive.function.server.RouterFunctions.*;

@SpringBootApplication
public class CoffeeServiceCassApplication {
//    @Bean
//    RouterFunction<ServerResponse> routes(CoffeeHandler handler) {
//        return route(GET("/coffees"), handler::all)
//                .andRoute(GET("/coffees/debug"), handler::debug)
//                .andRoute(GET("/coffees/{id}"), handler::byId)
//                .andRoute(GET("/coffees/{id}/orders"), handler::orders);
//    }

    public static void main(String[] args) {
        SpringApplication.run(CoffeeServiceCassApplication.class, args);
    }
}

//@Component
//class CoffeeHandler {
//    private final CoffeeService svc;
//
//    CoffeeHandler(CoffeeService svc) {
//        this.svc = svc;
//    }
//
//    Mono<ServerResponse> all(ServerRequest req) {
//        return ServerResponse.ok()
//                .body(svc.getAllCoffees(), Coffee.class);
//    }
//
//    Mono<ServerResponse> debug(ServerRequest req) {
//        return ServerResponse.ok()
//                .body(svc.getAllCoffees()
//                        .concatWith(Mono.error(new Throwable("Functional errors ftw!")))
//                        .checkpoint("Check!"), Coffee.class);
//    }
//
//    Mono<ServerResponse> byId(ServerRequest req) {
//        return ServerResponse.ok()
//                .body(svc.getCoffeeById(req.pathVariable("id")), Coffee.class);
//    }
//
//    Mono<ServerResponse> orders(ServerRequest req) {
//        return ServerResponse.ok()
//                .contentType(MediaType.TEXT_EVENT_STREAM)
//                .body(svc.getCoffeeOrders(req.pathVariable("id")), CoffeeOrder.class);
//    }
//}

@Component
class DataLoader {
    private final CoffeeRepository repo;

    DataLoader(CoffeeRepository repo) {
        this.repo = repo;
    }

    @PostConstruct
    private void init() {
        repo.deleteAll().thenMany(
                Flux.just("Jet Black Cassandra", "Darth Cassandra", "Pit of Despair Cassandra", "Black Alert Cassandra")
                        .map(name -> new Coffee(UUID.randomUUID().toString(), name))
                        .flatMap(repo::save))
                .thenMany(repo.findAll())
                .subscribe(System.out::println);
    }
}

@EnableWebFluxSecurity
@Configuration
class SecurityConfig {
    @Bean
    MapReactiveUserDetailsService authentication() {
        UserDetails mark = User.withDefaultPasswordEncoder()
                .username("mark")
                .password("badpassword")
                .roles("USER")
                .build();

        UserDetails rob = User.withDefaultPasswordEncoder()
                .username("rob")
                .password("BetterPasswordForTheWin")
                .roles("USER", "ADMIN")
                .build();

        return new MapReactiveUserDetailsService(mark, rob);
    }

    @Bean
    SecurityWebFilterChain authorization(ServerHttpSecurity http) {
        return http.httpBasic().and()
                .authorizeExchange().anyExchange()
                .hasRole("ADMIN").and()
                .build();
    }
}

@RestController
@RequestMapping("/coffees")
class CoffeeController {
    private final CoffeeService svc;

    CoffeeController(CoffeeService svc) {
        this.svc = svc;
    }

    @GetMapping
    public Flux<Coffee> getAllCoffees() {
        return svc.getAllCoffees();
    }

    @GetMapping(value = "/debug")
    public Flux<Coffee> getCoffeesAndError() {
        return svc.getCoffeesAndError();
    }

    @GetMapping("/{id}")
    public Mono<Coffee> getCoffeeById(@PathVariable String id) {
        return svc.getCoffeeById(id);
    }

    @GetMapping(value = "/{id}/orders", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<CoffeeOrder> getOrdersForCoffee(@PathVariable String id) {
        return svc.getCoffeeOrders(id);
    }
}

@Service
class CoffeeService {
    private final CoffeeRepository repo;

    CoffeeService(CoffeeRepository repo) {
        this.repo = repo;
    }

    public Flux<Coffee> getAllCoffees() {
        return repo.findAll();
    }

    public Flux<Coffee> getCoffeesAndError() {
        Hooks.resetOnOperatorDebug();
        //Hooks.onOperatorDebug();
        return repo.findAll()
                .concatWith(Mono.error(new Throwable("Oops, I did it again...")))
                .checkpoint("Checkmate checkpoint", true);
    }

    public Mono<Coffee> getCoffeeById(String id) {
        return repo.findById(id);
    }

    public Flux<CoffeeOrder> getCoffeeOrders(String coffeeId) {
        return Flux.<CoffeeOrder>generate(sink -> sink.next(new CoffeeOrder(coffeeId, new Date())))
                .delayElements(Duration.ofSeconds(1));
    }
}

interface CoffeeRepository extends ReactiveCrudRepository<Coffee, String> {
}

@Data
@NoArgsConstructor
@AllArgsConstructor
class CoffeeOrder {
    private String coffeeId;
    private Date dateOrdered;
}

@Table
@Data
@NoArgsConstructor
@AllArgsConstructor
class Coffee {
    @PrimaryKey
    private String id;
    private String name;
}