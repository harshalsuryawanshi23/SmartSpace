import { useState, useEffect } from 'react';

// Using a mock public key for the MVP since we don't have real VAPID keys setup.
const PUBLIC_VAPID_KEY = 'BCz3G19XbSZb57qS9T-s3t_bJj7G5W5c-v-s11T12Z_g8WqP-o3k4n-O1M9K-210oZ12O0P2S-9K5g5p0D7i6w0';

function urlBase64ToUint8Array(base64String: string) {
  const padding = '='.repeat((4 - base64String.length % 4) % 4);
  const base64 = (base64String + padding).replace(/-/g, '+').replace(/_/g, '/');
  const rawData = window.atob(base64);
  const outputArray = new Uint8Array(rawData.length);
  for (let i = 0; i < rawData.length; ++i) {
    outputArray[i] = rawData.charCodeAt(i);
  }
  return outputArray;
}

export function useWebPush(userId: string | null) {
  const [isSupported, setIsSupported] = useState(false);
  const [isSubscribed, setIsSubscribed] = useState(false);
  const [loading, setLoading] = useState(false);

  useEffect(() => {
    if ('serviceWorker' in navigator && 'PushManager' in window) {
      setIsSupported(true);
      checkSubscription();
    }
  }, []);

  const checkSubscription = async () => {
    try {
      const registration = await navigator.serviceWorker.ready;
      const subscription = await registration.pushManager.getSubscription();
      setIsSubscribed(!!subscription);
    } catch (e) {
      console.error('Error checking subscription', e);
    }
  };

  const subscribe = async () => {
    if (!isSupported || !userId) return;
    setLoading(true);
    try {
      const registration = await navigator.serviceWorker.ready;
      
      const subscription = await registration.pushManager.subscribe({
        userVisibleOnly: true,
        applicationServerKey: urlBase64ToUint8Array(PUBLIC_VAPID_KEY)
      });
      
      const subJson = subscription.toJSON();
      
      await fetch('/api/v1/push/subscribe', {
        method: 'POST',
        headers: { 
          'Content-Type': 'application/json',
          'X-User-Id': userId // MVP auth
        },
        body: JSON.stringify({
          endpoint: subJson.endpoint,
          keys: {
            p256dh: subJson.keys?.p256dh,
            auth: subJson.keys?.auth
          },
          userAgent: navigator.userAgent
        })
      });
      
      setIsSubscribed(true);
    } catch (e) {
      console.error('Failed to subscribe to web push', e);
    } finally {
      setLoading(false);
    }
  };

  const unsubscribe = async () => {
    if (!isSupported) return;
    setLoading(true);
    try {
      const registration = await navigator.serviceWorker.ready;
      const subscription = await registration.pushManager.getSubscription();
      if (subscription) {
        await subscription.unsubscribe();
        await fetch('/api/v1/push/unsubscribe', {
          method: 'DELETE',
          headers: { 'Content-Type': 'application/json' },
          body: JSON.stringify({ endpoint: subscription.endpoint })
        });
        setIsSubscribed(false);
      }
    } catch (e) {
      console.error('Failed to unsubscribe', e);
    } finally {
      setLoading(false);
    }
  };

  return { isSupported, isSubscribed, loading, subscribe, unsubscribe };
}
