package com.yourname.spotifytodeezer.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Model class representing a music track
 *
 * @Data - Lombok annotation that generates:
 *   - Getters for all fields
 *   - Setters for all fields
 *   - toString() method
 *   - equals() and hashCode() methods
 *
 * @NoArgsConstructor - Generates a no-argument constructor
 * @AllArgsConstructor - Generates a constructor with all fields
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Track {
    private String id;           // Spotify track ID
    private String name;         // Song title
    private List<String> artists; // List of artist names
    private String album;        // Album name
    private String isrc;         // International Standard Recording Code
    private String uri;          // Spotify URI (spotify:track:xxx)
}