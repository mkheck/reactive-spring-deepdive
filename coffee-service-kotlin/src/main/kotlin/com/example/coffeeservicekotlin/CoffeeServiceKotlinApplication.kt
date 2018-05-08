package com.example.coffeeservicekotlin

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.mapping.Document
import org.springframework.data.repository.reactive.ReactiveCrudRepository
import org.springframework.stereotype.Component
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RestController
import reactor.core.publisher.Flux
import java.util.*
import javax.annotation.PostConstruct

@SpringBootApplication
class CoffeeServiceKotlinApplication

fun main(args: Array<String>) {
    runApplication<CoffeeServiceKotlinApplication>(*args)
}

@Component
class DataLoader(private val repo: CoffeeRepository) {
    @PostConstruct
    fun load() {
        repo.deleteAll().thenMany(
                Flux.just("Jet Black KMongo", "Darth KMongo", "Black as my Soul KMongo",
                        "Midnight KMongo", "Black Alert KMongo")
                        .map { Coffee(UUID.randomUUID().toString(), it) }
                        .flatMap { coffee -> repo.save(coffee) })
                .thenMany(repo.findAll())
                .subscribe { println(it) }
    }
}

@RestController
class CoffeeController(private val repo: CoffeeRepository) {
    @GetMapping("/coffees")
    fun all() = repo.findAll()
}

interface CoffeeRepository : ReactiveCrudRepository<Coffee, String>

@Document
data class Coffee(@Id val id: String, val name: String)