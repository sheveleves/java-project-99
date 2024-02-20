plugins {
	application
	checkstyle
	id("jacoco")
	id("org.springframework.boot") version "3.2.2"
	id("io.spring.dependency-management") version "1.1.4"
}

group = "hexlet.code"
version = "0.0.1-SNAPSHOT"

java {
	sourceCompatibility = JavaVersion.VERSION_20
}

repositories {
	mavenCentral()
}
application { mainClass.set("hexlet.code.AppApplication") }

dependencies {
	implementation("org.springframework.boot:spring-boot-starter")
	implementation("org.springframework.boot:spring-boot-starter-web")
	testImplementation("org.springframework.boot:spring-boot-starter-test")

}

tasks.test {
	useJUnitPlatform()
	finalizedBy(tasks.jacocoTestReport)
}

tasks.jacocoTestReport {
	dependsOn (tasks.test)
	reports {
		xml.required = true
		csv.required = false
	}
}
