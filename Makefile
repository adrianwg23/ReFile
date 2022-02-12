build-jar:
	./gradlew build -x check

build-image:
	docker build -t adrianwg23/refile .

build: build-jar build-image

.PHONY: build-jar build-image build