import { ed25519 } from '@noble/curves/ed25519';
import { offlineSyncService } from './OfflineSyncService';

export interface QrPayload {
    jti: string;
    bkg: number;
    knd: string;
    vF: number;
    vU: number;
    kId: string;
    hmac?: string; // Phase 14 added HMAC but we only rely on signature for offline
}

export interface ValidationResult {
    isValid: boolean;
    reasonCode?: string;
    payload?: QrPayload;
    offlineMode: boolean;
}

export class QrValidationService {
    
    public static async validateToken(token: string): Promise<ValidationResult> {
        // format: SS1.base64payload.base64sig
        if (!token.startsWith('SS1.')) {
            return { isValid: false, reasonCode: 'INVALID_FORMAT', offlineMode: navigator.onLine === false };
        }

        const parts = token.substring(4).split('.');
        if (parts.length !== 2) {
            return { isValid: false, reasonCode: 'INVALID_FORMAT', offlineMode: navigator.onLine === false };
        }

        const payloadB64 = parts[0];
        const sigB64 = parts[1];

        let payloadStr: string;
        let payload: QrPayload;
        
        try {
            // Buffer is not strictly available in browser without polyfill, using atob safely for base64url
            payloadStr = atob(payloadB64.replace(/-/g, '+').replace(/_/g, '/'));
            payload = JSON.parse(payloadStr);
        } catch (e) {
            return { isValid: false, reasonCode: 'MALFORMED_PAYLOAD', offlineMode: navigator.onLine === false };
        }

        const isOnline = navigator.onLine;

        // If online, we could just return and let backend handle it, but we can also pre-validate
        if (isOnline) {
             return { isValid: true, payload, offlineMode: false }; // Let backend do the real validation
        }

        // Offline mode: perform cryptographic signature verification
        const publicKeyHex = offlineSyncService.getPublicKey();
        if (!publicKeyHex) {
            return { isValid: false, reasonCode: 'NO_PUBLIC_KEY', offlineMode: true };
        }

        try {
            // Convert sig from base64url to Uint8Array
            const sigBytes = Uint8Array.from(atob(sigB64.replace(/-/g, '+').replace(/_/g, '/')), c => c.charCodeAt(0));
            // Convert payload string to Uint8Array for signing check
            const msgBytes = new TextEncoder().encode(payloadB64);

            const isValidSig = ed25519.verify(sigBytes, msgBytes, publicKeyHex);
            if (!isValidSig) {
                return { isValid: false, reasonCode: 'INVALID_SIGNATURE', offlineMode: true };
            }

            // Check time bounds
            const nowSeconds = Math.floor(Date.now() / 1000);
            if (nowSeconds < payload.vF) {
                return { isValid: false, reasonCode: 'NOT_YET_VALID', offlineMode: true };
            }
            if (nowSeconds > payload.vU) {
                return { isValid: false, reasonCode: 'EXPIRED', offlineMode: true };
            }

            // Check revocation
            if (offlineSyncService.isJtiRevoked(payload.jti)) {
                return { isValid: false, reasonCode: 'REVOKED', offlineMode: true };
            }

            return { isValid: true, payload, offlineMode: true };

        } catch (e) {
            console.error("Crypto verification failed", e);
            return { isValid: false, reasonCode: 'CRYPTO_ERROR', offlineMode: true };
        }
    }
}
