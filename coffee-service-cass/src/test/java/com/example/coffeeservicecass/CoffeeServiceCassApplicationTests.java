package com.example.coffeeservicecass;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit4.SpringRunner;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.time.Duration;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = CoffeeService.class)
public class CoffeeServiceCassApplicationTests {
	@Autowired
    private CoffeeService svc;

    @MockBean
    private CoffeeRepository repo;

	private Coffee coffee;

    @Before
    public void setup() {
        coffee = new Coffee("000-TEST-999", "Tester's Choice");
        Mockito.when(repo.findAll()).thenReturn(Flux.just(coffee));
        Mockito.when(repo.findById(coffee.getId())).thenReturn(Mono.just(coffee));
        //coffee = repo.findAll().blockFirst();
    }

    @Test
    public void getOrdersForCoffeeTake10() {
        StepVerifier.withVirtualTime(() -> svc.getCoffeeOrders(coffee.getId()).take(10))
                .thenAwait(Duration.ofHours(10))
                .expectNextCount(10)
                .verifyComplete();
    }

    @Test
    public void getCoffeeById() {
        StepVerifier.create(svc.getCoffeeById(coffee.getId()))
                .expectNext(coffee)
                .verifyComplete();
    }

    @Test
    public void getAllCoffees() {
	    StepVerifier.withVirtualTime(() -> svc.getAllCoffees())
                .thenAwait(Duration.ofHours(10))
                .expectNext(coffee)
                .verifyComplete();
    }

    @Test
    public void getAllCoffeesThenError() {
	    StepVerifier.create(svc.getAllCoffees().concatWith(Mono.error(new Throwable("I've got a bad feeling about this..."))))
                //.thenAwait(Duration.ofHours(1))
                .expectNext(coffee)
                //.expectNext(new Coffee("111-ERROR-888", "That's some bad coffee :("))
                .verifyError();
    }
}
