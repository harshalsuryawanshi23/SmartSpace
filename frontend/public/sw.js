self.addEventListener('push', function (event) {
  if (event.data) {
    const payload = event.data.json();
    const options = {
      body: payload.body,
      icon: '/icon.png',
      badge: '/badge.png',
      data: payload.url || '/'
    };

    event.waitUntil(
      self.registration.showNotification(payload.title || 'SmartSpace Notification', options)
    );
  }
});

self.addEventListener('notificationclick', function (event) {
  event.notification.close();
  const urlToOpen = event.notification.data;
  event.waitUntil(
    clients.matchAll({ type: 'window', includeUncontrolled: true }).then(function (clientList) {
      for (let i = 0; i < clientList.length; i++) {
        let client = clientList[i];
        if (client.url === urlToOpen && 'focus' in client) {
          return client.focus();
        }
      }
      if (clients.openWindow) {
        return clients.openWindow(urlToOpen);
      }
    })
  );
});
