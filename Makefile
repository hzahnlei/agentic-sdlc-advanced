.PHONY: build test run db db-stop

build:
	mvn compile

test:
	mvn test

run:
	mvn spring-boot:run

db:
	@if [ "$$(docker ps -q -f name=^postgres$$)" ]; then \
		echo "PostgreSQL container already running."; \
	else \
		docker run --rm -d \
			--name postgres \
			-e POSTGRES_USER=postgres \
			-e POSTGRES_PASSWORD=postgres \
			-e POSTGRES_DB=mcpdb \
			-p 5432:5432 \
			postgres:alpine; \
		echo "PostgreSQL started on port 5432 (db: mcpdb, user: postgres, password: postgres)"; \
	fi

db-stop:
	docker stop postgres
