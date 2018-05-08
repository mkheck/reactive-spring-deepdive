package com.example.coffeeservicecass;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

@RunWith(SpringRunner.class)
@WebFluxTest(controllers = {CoffeeController.class, CoffeeService.class})
public class CoffeeServiceCassWebTests {
    @Autowired
    private WebTestClient client;

    @MockBean
    private CoffeeRepository repo;

    private Coffee coffee;

    @Before
    public void setup() {
        coffee = new Coffee("000-TEST-999", "Tester's Choice");
        Mockito.when(repo.findAll()).thenReturn(Flux.just(coffee));
        Mockito.when(repo.findById(coffee.getId())).thenReturn(Mono.just(coffee));
        coffee = repo.findAll().blockFirst();
    }

    @Test
    public void webAllCoffees() {
        client.get()
                .uri("/coffees")
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .consumeWith(System.out::println);
//                .consumeWith(response -> Assertions.assertThat(response.getResponseBody()).isNotEmpty());
    }

    @Test
    public void webGetCoffeeById() {
        client.get()
                .uri("/coffees/{id}", coffee.getId())
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                //.consumeWith(System.out::println);
                .consumeWith(response -> Assert.assertTrue(response.getResponseBody().length > 0));
    }

    @Test
    public void webGetCoffeeOrdersTake1() {
        StepVerifier.create(client.get()
                .uri("/coffees/{id}/orders", coffee.getId())
                .exchange()
                .expectStatus().isOk()
                .returnResult(CoffeeOrder.class)
                .getResponseBody()
                .take(1))
                .consumeNextWith(order -> System.out.println("\n>>> Coffee order: " + order + "\n"))
                .verifyComplete();
                //.expectNextCount(1)
                //.verifyComplete();
    }

/*
    @Test
    public void webGetCoffeeOrdersTake10() {
        // Note that using withVirtualTime() times out waiting for response via SSE.  ?:/
        StepVerifier.create(client.get()
                .uri("/coffees/{id}/orders", coffee.getId())
                .exchange()
                .expectStatus().isOk()
                .returnResult(CoffeeOrder.class)
                .getResponseBody()
                .take(10))
                .thenAwait(Duration.ofSeconds(30))
                .expectNextCount(10)
                .verifyComplete();
    }
*/
}