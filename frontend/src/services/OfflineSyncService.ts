import { type IDBPDatabase, openDB } from 'idb';
import { api } from './api';

const DB_NAME = 'smartspace-offline-db';
const STORE_NAME = 'outbox';
const MANIFEST_STORE = 'manifest';

interface OfflineLog {
    clientEventId: string;
    jti?: string;
    eventType: string;
    verdict: string;
    reasonCode?: string;
    identityMethod: string;
    deviceId: string;
    headcount?: number;
    occurredAtEpochMs: number;
}

export class OfflineSyncService {
    private db: Promise<IDBPDatabase>;
    private publicKey: string | null = null;
    private revokedJtis: Set<string> = new Set();
    private isOnline = navigator.onLine;

    constructor() {
        this.db = openDB(DB_NAME, 1, {
            upgrade(db) {
                db.createObjectStore(STORE_NAME, { keyPath: 'clientEventId' });
                db.createObjectStore(MANIFEST_STORE);
            },
        });

        window.addEventListener('online', () => {
            this.isOnline = true;
            this.syncOutbox();
        });
        
        window.addEventListener('offline', () => {
            this.isOnline = false;
        });

        // Initialize manifest
        this.loadManifestFromDb();
        if (this.isOnline) {
            this.fetchManifest();
        }
    }

    private async loadManifestFromDb() {
        const db = await this.db;
        const pk = await db.get(MANIFEST_STORE, 'publicKey');
        const jtis = await db.get(MANIFEST_STORE, 'revokedJtis');
        if (pk) this.publicKey = pk;
        if (jtis) this.revokedJtis = new Set(jtis);
    }

    public async fetchManifest() {
        try {
            const res = await api.get('/entry/manifest');
            const data = res.data;
            this.publicKey = data.publicKey;
            this.revokedJtis = new Set(data.revokedJtis);

            const db = await this.db;
            const tx = db.transaction(MANIFEST_STORE, 'readwrite');
            await tx.store.put(this.publicKey, 'publicKey');
            await tx.store.put(data.revokedJtis, 'revokedJtis');
            await tx.done;
        } catch (err) {
            console.error('Failed to fetch manifest', err);
        }
    }

    public getPublicKey(): string | null {
        return this.publicKey;
    }

    public isJtiRevoked(jti: string): boolean {
        return this.revokedJtis.has(jti);
    }

    public async logEvent(log: OfflineLog) {
        if (this.isOnline) {
            // Try to sync immediately
            try {
                await api.post('/entry/sync', { logs: [log] });
                return;
            } catch (e) {
                console.error("Immediate sync failed, falling back to idb", e);
            }
        }
        
        // Save to outbox
        const db = await this.db;
        await db.put(STORE_NAME, log);
    }

    public async syncOutbox() {
        if (!this.isOnline) return;

        const db = await this.db;
        const allLogs = await db.getAll(STORE_NAME);
        
        if (allLogs.length === 0) return;

        try {
            await api.post('/entry/sync', { logs: allLogs });
            // Clear successfully synced logs
            const tx = db.transaction(STORE_NAME, 'readwrite');
            for (const log of allLogs) {
                await tx.store.delete(log.clientEventId);
            }
            await tx.done;
        } catch (err) {
            console.error('Failed to sync outbox', err);
        }
    }
}

export const offlineSyncService = new OfflineSyncService();
