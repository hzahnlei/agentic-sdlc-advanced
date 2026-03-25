.PHONY: build test coverage clean run db db-stop

REPORTS_DIR := test_reports

build:
	mvn -q compile

test:
	mkdir -p $(REPORTS_DIR)/tests
	mvn -q test
	cp -r target/surefire-reports/. $(REPORTS_DIR)/tests/

coverage:
	mkdir -p $(REPORTS_DIR)/coverage
	mvn -q verify
	python3 scripts/jacoco_to_cobertura.py \
	    target/site/jacoco/jacoco.xml \
	    $(REPORTS_DIR)/coverage/coverage.xml

clean:
	mvn -q clean
	rm -rf $(REPORTS_DIR)

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
