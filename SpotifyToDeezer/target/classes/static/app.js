// State management
let spotifyTracks = [];
let selectedTracks = new Set();

// DOM Elements
const spotifyClientId = document.getElementById('spotify-client-id');
const spotifyClientSecret = document.getElementById('spotify-client-secret');
const spotifyPlaylistUrl = document.getElementById('spotify-playlist-url');
const fetchPlaylistBtn = document.getElementById('fetch-playlist-btn');
const fetchError = document.getElementById('fetch-error');

//hidden
const trackSelection = document.getElementById('track-selection');
const trackList = document.getElementById('track-list');
const trackCount = document.getElementById('track-count');
const selectAllBtn = document.getElementById('select-all-btn');
const deselectAllBtn = document.getElementById('deselect-all-btn');

const transferSection = document.getElementById('transfer-section');
const deezerPlaylistName = document.getElementById('deezer-playlist-name');
const transferBtn = document.getElementById('transfer-btn');

const resultsSection = document.getElementById('results-section');
const resultsJson = document.getElementById('results-json');

// Fetch Spotify Playlist
fetchPlaylistBtn.addEventListener('click', async () => {
    const clientId = spotifyClientId.value.trim();
    const clientSecret = spotifyClientSecret.value.trim();
    const playlistUrl = spotifyPlaylistUrl.value.trim();

    // Validation
    if (!clientId || !clientSecret || !playlistUrl) {
        showError(fetchError, 'Please fill in all fields');
        return;
    }

    // Show loading state
    fetchPlaylistBtn.disabled = true;
    fetchPlaylistBtn.textContent = 'Fetching...';
    fetchError.classList.remove('show'); //if refreshed after no result fetching

    try {
        // Call our Spring Boot backend
        const response = await fetch('/api/spotify/playlist', {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({
                playlistUrl: playlistUrl,
                clientId: clientId,
                clientSecret: clientSecret
            })
        });

        const data = await response.json();

        if (response.ok) {
            // Success! Store tracks and display them
            spotifyTracks = data.tracks;
            displayTracks();
            trackSelection.classList.remove('hidden');
            transferSection.classList.remove('hidden');

            console.log('Fetched', spotifyTracks.length, 'tracks, and removed hidden');
        } else {
            // Error from backend
            showError(fetchError, data.error || 'Failed to fetch playlist');
        }
    } catch (error) {
        // Network error
        showError(fetchError, 'Connection error: ' + error.message);
        console.error('Error:', error);
    } finally {
        // Reset button
        fetchPlaylistBtn.disabled = false;
        fetchPlaylistBtn.textContent = 'Fetch Playlist';
    }
});

// Display Tracks
function displayTracks() {
    trackList.innerHTML = '';
    selectedTracks.clear();

    spotifyTracks.forEach((track, index) => {
        const trackItem = document.createElement('div');
        trackItem.className = 'track-item';

        // Checkbox
        const checkbox = document.createElement('input');
        checkbox.type = 'checkbox';
        checkbox.id = `track-${index}`;
        checkbox.checked = true; // Select all by default
        selectedTracks.add(track.id);

        checkbox.addEventListener('change', (e) => { //checkbox can only change by cheked or unchecked
            if (e.target.checked) {
                selectedTracks.add(track.id);
            } else {
                selectedTracks.delete(track.id);
            }
            updateTrackCount();
        });

        // Track info
        const trackInfo = document.createElement('div');
        trackInfo.className = 'track-info';

        const isrcDisplay = track.isrc ? `ISRC: ${track.isrc}` : 'No ISRC';

        trackInfo.innerHTML = `
            <div class="track-name">${escapeHtml(track.name)}</div>
            <div class="track-meta">
                ${escapeHtml(track.artists.join(', '))} • ${escapeHtml(track.album)}
                <br>${isrcDisplay}
            </div>
        `;

        trackItem.appendChild(checkbox);
        trackItem.appendChild(trackInfo);
        trackList.appendChild(trackItem);
    });

    updateTrackCount();
}

// Update track counter
function updateTrackCount() {
    trackCount.textContent = `${selectedTracks.size} / ${spotifyTracks.length} selected`;
}

// Select/Deselect All
selectAllBtn.addEventListener('click', () => {
    selectedTracks.clear();
    spotifyTracks.forEach((track, index) => {
        selectedTracks.add(track.id);
        document.getElementById(`track-${index}`).checked = true;
    });
    updateTrackCount();
});

deselectAllBtn.addEventListener('click', () => {
    selectedTracks.clear();
    spotifyTracks.forEach((_, index) => {
        document.getElementById(`track-${index}`).checked = false;
    });
    updateTrackCount();
});

// Transfer/Submit Button
transferBtn.addEventListener('click', async () => {
    const playlistName = deezerPlaylistName.value.trim();

    if (!playlistName) {
        alert('Please enter a playlist name');
        return;
    }

    if (selectedTracks.size === 0) {
        alert('Please select at least one track');
        return;
    }

    // Show loading
    transferBtn.disabled = true;
    transferBtn.textContent = 'Processing...';
    resultsSection.classList.add('hidden');

    try {
        // Call transfer endpoint
        const response = await fetch('/api/spotify/transfer', {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({
                playlistName: playlistName,
                selectedTrackIds: Array.from(selectedTracks)
            })
        });

        const data = await response.json();

        if (response.ok) {
            // Display JSON result
            resultsJson.textContent = JSON.stringify(data, null, 2);
            resultsSection.classList.remove('hidden');

            // Scroll to results
            resultsSection.scrollIntoView({ behavior: 'smooth' });

            console.log('Transfer response:', data);
        } else {
            alert('Error: ' + (data.error || 'Transfer failed'));
        }
    } catch (error) {
        alert('Connection error: ' + error.message);
        console.error('Error:', error);
    } finally {
        transferBtn.disabled = false;
        transferBtn.textContent = 'Submit & Show JSON';
    }
});

// Helper Functions
function showError(element, message) {
    element.textContent = message;
    element.classList.add('show');
}

function escapeHtml(text) {
    const div = document.createElement('div');
    div.textContent = text;
    return div.innerHTML;
}
