build:
	./gradlew clean build
	
report:
	./gradlew jacocoTestReport

start:
	./gradlew run

test:
	./gradlew test

.PHONY: build
