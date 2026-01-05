package com.yourname.spotifytodeezer.model;

import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * Response object containing the selected tracks info
 * This is what gets returned as JSON when user submits
 */
@Data
@AllArgsConstructor
public class TransferResponse {
    private String isrc;
    private String title;
    private String artist;
}