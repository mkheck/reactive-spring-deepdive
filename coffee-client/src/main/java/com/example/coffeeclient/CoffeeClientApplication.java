package com.example.coffeeclient;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.ExchangeFilterFunctions;
import org.springframework.web.reactive.function.client.WebClient;

import javax.annotation.PostConstruct;
import java.util.Date;

@SpringBootApplication
public class CoffeeClientApplication {
    @Bean
    WebClient client() {
        return WebClient.builder()
                .baseUrl("http://localhost:8083/coffees")
                .filter(ExchangeFilterFunctions.basicAuthentication("rob", "BetterPasswordForTheWin"))
                .build();
    }

    public static void main(String[] args) {
        SpringApplication.run(CoffeeClientApplication.class, args);
    }
}

@Component
class Demo {
    private final WebClient client;

    Demo(WebClient client) {
        this.client = client;
    }

    @PostConstruct
    private void run() {
        client.get()
                .retrieve()
                .bodyToFlux(Coffee.class)
                .filter(coffee -> coffee.getName().equalsIgnoreCase("BLACK ALERT cAssAndrA"))
                .flatMap(coffee -> client.get()
                    .uri("/{id}/orders", coffee.getId())
                    .retrieve()
                    .bodyToFlux(CoffeeOrder.class))
                .subscribe(System.out::println);
    }
}

@Data
@NoArgsConstructor
@AllArgsConstructor
class CoffeeOrder {
    private String coffeeId;
    private Date dateOrdered;
}

@Data
@NoArgsConstructor
@AllArgsConstructor
class Coffee {
    private String id;
    private String name;
}