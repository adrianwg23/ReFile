build-jar:
	./gradlew build -x check

build-image:
	docker build -t adrianwg23/refile .

build: build-jar build-image

run-db:
	docker run -d -p 5432:5432 --name db -e POSTGRES_PASSWORD=password -e POSTGRES_DB=refile postgres

.PHONY: build-jar build-image build