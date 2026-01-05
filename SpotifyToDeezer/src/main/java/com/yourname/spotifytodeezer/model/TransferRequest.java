package com.yourname.spotifytodeezer.model;

import lombok.Data;

import java.util.List;

/**
 * Request object for the transfer endpoint
 * This represents the data sent from the frontend when user clicks "Submit"
 */
@Data
public class TransferRequest {
    private String playlistName;           // Name for the new Deezer playlist
    private List<String> selectedTrackIds; // List of Spotify track IDs selected by user
}