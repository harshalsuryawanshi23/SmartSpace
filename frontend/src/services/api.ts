export const api = {
  get: async (url: string, options?: RequestInit & { params?: any }) => {
    let finalUrl = url;
    // VERY simple query params implementation for the mock
    if (options && (options as any).params) {
       const p = new URLSearchParams((options as any).params);
       finalUrl += '?' + p.toString();
    }
    const res = await fetch(finalUrl, { headers: { Authorization: `Bearer ${localStorage.getItem('token')}` }, ...options });
    if (!res.ok) throw new Error('API Error');
    return res.json();
  },
  post: async (url: string, body?: any, options?: RequestInit) => {
    const res = await fetch(url, { 
      method: 'POST', 
      headers: { 'Content-Type': 'application/json', Authorization: `Bearer ${localStorage.getItem('token')}` },
      body: body ? JSON.stringify(body) : undefined,
      ...options
    });
    if (!res.ok) throw new Error('API Error');
    return res.json();
  },
  put: async (url: string, body?: any, options?: RequestInit) => {
    const res = await fetch(url, { 
      method: 'PUT', 
      headers: { 'Content-Type': 'application/json', Authorization: `Bearer ${localStorage.getItem('token')}` },
      body: body ? JSON.stringify(body) : undefined,
      ...options
    });
    if (!res.ok) throw new Error('API Error');
    return res.json();
  }
};
