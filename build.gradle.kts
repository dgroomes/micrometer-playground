plugins {
    java
    application
}

repositories {
    mavenCentral()
}

dependencies {
    implementation(libs.slf4j.api)
    implementation(libs.slf4j.simple)
    implementation(libs.micrometer.influx)
}

application {
    mainClass.set("dgroomes.Runner")
}
