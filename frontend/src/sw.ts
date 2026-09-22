/// <reference lib="webworker" />
import { clientsClaim } from 'workbox-core';
import { precacheAndRoute, cleanupOutdatedCaches } from 'workbox-precaching';

declare let self: ServiceWorkerGlobalScope;

// Cleanup old caches
cleanupOutdatedCaches();

// Precache static assets injected by Vite
precacheAndRoute(self.__WB_MANIFEST || []);

// Claim clients
clientsClaim();
self.skipWaiting();

// We can add background sync logic here or handle it in the application layer
self.addEventListener('fetch', (event) => {
    // If it's an API request, we can attempt a network first, fallback to offline UI cache,
    // but our app will handle offline checks using `idb-store`.
    
    // For non-API requests, use default workbox routing.
});
