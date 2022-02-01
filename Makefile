build-jar:
	./gradlew bootJar

build-image:
	docker build -t refile-java .

.PHONY: build-jar build-image