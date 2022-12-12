package com.bennerg.cosmos;

import javax.annotation.PostConstruct;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.annotation.Id;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;

import com.azure.spring.data.cosmos.core.mapping.Container;
import com.azure.spring.data.cosmos.core.mapping.GeneratedValue;
import com.azure.spring.data.cosmos.core.mapping.PartitionKey;
import com.azure.spring.data.cosmos.repository.ReactiveCosmosRepository;

import io.micrometer.core.lang.NonNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@SpringBootApplication
public class ConnectorApplication {
	public static void main(String[] args) {
		SpringApplication.run(ConnectorApplication.class, args);
	}
}

@Slf4j
@Component
@AllArgsConstructor
class DataLoader {
	private final UserRepository repo;

	@PostConstruct
	void loadData() {
		repo.deleteAll()
			.thenMany(Flux.just(
				new User("Bob", "Barker", "221b Baker St."),
				new User("Willy", "Wonka", "123 Candy Ln.")))
			.flatMap(repo::save)
			.thenMany(repo.findAll())
			.subscribe(user -> log.info(user.toString()));
	}
}

@RestController
@AllArgsConstructor
class ConnectorController {
	private final UserRepository repo;

	@GetMapping
	Flux<User> getAllUsers() {
		return repo.findAll();
	}
}

interface UserRepository extends ReactiveCosmosRepository<User, String> {}

@Container(containerName = "data")
@Data
@NoArgsConstructor
@RequiredArgsConstructor
class User {
	@Id
	@GeneratedValue
	private String id;
	@NonNull
	@PartitionKey
	private String firstName;
	@NonNull
	private String lastName;
	@NonNull
	private String address;
}
