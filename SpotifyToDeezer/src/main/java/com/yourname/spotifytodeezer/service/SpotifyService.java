package com.yourname.spotifytodeezer.service;

import com.yourname.spotifytodeezer.model.Track;
import org.apache.hc.core5.http.ParseException;
import org.springframework.stereotype.Service;
import se.michaelthelin.spotify.SpotifyApi;
import se.michaelthelin.spotify.exceptions.SpotifyWebApiException;
import se.michaelthelin.spotify.model_objects.credentials.ClientCredentials;
import se.michaelthelin.spotify.model_objects.specification.ArtistSimplified;
import se.michaelthelin.spotify.model_objects.specification.Paging;
import se.michaelthelin.spotify.model_objects.specification.PlaylistTrack;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Service class for Spotify API interactions
 *
 * @Service - Marks this as a Spring service component
 * Spring will automatically create an instance of this class (singleton by default)
 * and manage its lifecycle
 */
@Service
public class SpotifyService {

    /**
     * Fetches all tracks from a Spotify playlist
     *
     * @param playlistUrl - The full Spotify playlist URL
     * @param clientId - Spotify API client ID
     * @param clientSecret - Spotify API client secret
     * @return List of Track objects
     */
    public List<Track> getPlaylistTracks(String playlistUrl, String clientId, String clientSecret)
            throws IOException, ParseException, SpotifyWebApiException {

        // Step 1: Extract playlist ID from URL
        // URL format: https://open.spotify.com/playlist/37i9dQZF1DXcBWIGoYBM5M
        String playlistId = extractPlaylistId(playlistUrl);

        // Step 2: Build Spotify API client with credentials
        SpotifyApi spotifyApi = new SpotifyApi.Builder()
                .setClientId(clientId)
                .setClientSecret(clientSecret)
                .build();

        // Step 3: Get access token using Client Credentials flow
        // This is a "app-only" authentication - no user login needed
        ClientCredentials clientCredentials = spotifyApi.clientCredentials().build().execute();
        spotifyApi.setAccessToken(clientCredentials.getAccessToken());

        // Step 4: Fetch all tracks from the playlist
        List<Track> tracks = new ArrayList<>();
        int offset = 0;
        int limit = 100; // Spotify API max limit per request

        // Loop through all pages of results (playlists can have 100+ songs)
        while (true) {
            // Make API call to get playlist tracks
            Paging<PlaylistTrack> playlistTracks = spotifyApi
                    .getPlaylistsItems(playlistId)
                    .offset(offset)
                    .limit(limit)
                    .build()
                    .execute();

            // Convert Spotify API objects to our Track model
            for (PlaylistTrack playlistTrack : playlistTracks.getItems()) {
                if (playlistTrack.getTrack() instanceof se.michaelthelin.spotify.model_objects.specification.Track) {
                    se.michaelthelin.spotify.model_objects.specification.Track spotifyTrack =
                            (se.michaelthelin.spotify.model_objects.specification.Track) playlistTrack.getTrack();

                    // Extract artist names
                    List<String> artists = Arrays.stream(spotifyTrack.getArtists())
                            .map(ArtistSimplified::getName)
                            .collect(Collectors.toList());

                    // Get ISRC (might be null)
                    String isrc = spotifyTrack.getExternalIds() != null
                            ? spotifyTrack.getExternalIds().getExternalIds().get("isrc")
                            : null;

                    // Create our Track object
                    Track track = new Track(
                            spotifyTrack.getId(),
                            spotifyTrack.getName(),
                            artists,
                            spotifyTrack.getAlbum().getName(),
                            isrc,
                            spotifyTrack.getUri()
                    );

                    tracks.add(track);
                }
            }

            // Check if we've fetched all tracks
            if (playlistTracks.getItems().length < limit) {
                break; // No more tracks to fetch
            }

            offset += limit; // Move to next page
        }

        return tracks;
    }

    /**
     * Extracts playlist ID from Spotify URL
     * Supports formats:
     * - https://open.spotify.com/playlist/ID
     * - https://open.spotify.com/playlist/ID?si=xxx
     */
    private String extractPlaylistId(String url) {
        // Remove query parameters if present
        String cleanUrl = url.split("\\?")[0];

        // Extract ID (last part of URL)
        String[] parts = cleanUrl.split("/");
        return parts[parts.length - 1];
    }

    /**
     * Get a specific track by ID
     * Useful for the transfer phase to get full track info
     */
    public Track getTrackById(String trackId, String clientId, String clientSecret)
            throws IOException, ParseException, SpotifyWebApiException {

        SpotifyApi spotifyApi = new SpotifyApi.Builder()
                .setClientId(clientId)
                .setClientSecret(clientSecret)
                .build();

        ClientCredentials clientCredentials = spotifyApi.clientCredentials().build().execute();
        spotifyApi.setAccessToken(clientCredentials.getAccessToken());

        se.michaelthelin.spotify.model_objects.specification.Track spotifyTrack =
                spotifyApi.getTrack(trackId).build().execute();

        List<String> artists = Arrays.stream(spotifyTrack.getArtists())
                .map(ArtistSimplified::getName)
                .collect(Collectors.toList());

        String isrc = spotifyTrack.getExternalIds() != null
                ? spotifyTrack.getExternalIds().getExternalIds().get("isrc")
                : null;

        return new Track(
                spotifyTrack.getId(),
                spotifyTrack.getName(),
                artists,
                spotifyTrack.getAlbum().getName(),
                isrc,
                spotifyTrack.getUri()
        );
    }
}