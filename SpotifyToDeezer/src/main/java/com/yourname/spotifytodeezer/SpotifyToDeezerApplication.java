package com.yourname.spotifytodeezer;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Main Spring Boot Application
 *
 * @SpringBootApplication is a convenience annotation that combines:
 * - @Configuration: Marks this as a source of bean definitions
 * - @EnableAutoConfiguration: Tells Spring Boot to auto-configure based on dependencies
 * - @ComponentScan: Tells Spring to scan this package for components
 */
@SpringBootApplication
public class SpotifyToDeezerApplication {

    public static void main(String[] args) {
        // This starts the entire Spring Boot application
        SpringApplication.run(SpotifyToDeezerApplication.class, args);
        System.out.println("🎵 Spotify to Deezer app is running!");
        System.out.println("📍 Open http://localhost:8080 in your browser");
    }
}