build-jar:
	./gradlew bootJar

build-image:
	docker build -t adrianwg23/refile .

.PHONY: build-jar build-image