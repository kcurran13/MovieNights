export default class Rest {
  async getActiveUser(jwt) {
    const res = await fetch(`/api/auth/active_user`, {
      method: 'GET',
      headers:
      {
        'Content-Type': 'application/json',
        'Authorization': 'Bearer ' + jwt
      }
    });
    if (res.status === 200) {
      return await res.json();
    } else if (res.status === 204) {
      return null;
    }
  };

  async login(credentials) {
    const res = await fetch(`/api/auth/login`, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify(credentials)
    });
    if (res.status === 401) {
      return false
    } else if (res.status === 200) {
      return await res.json();
    }
  };

  async refreshToken() {
    const res = await fetch(`/api/auth/refresh_token`, {
      method: 'POST',
    });
    if (res.status === 200) {
      return await res.json();
    } else if (res.status === 504){
      return "try again"
    }
    else {
      return null;
    }
  };

  async logout() {
    const res = await fetch(`/api/auth/logout`, {
      method: 'DELETE',
    });
    if (res.status === 200) {
      return true;
    } else {
      return false;
    }
  };

  async search(query, jwt) {
    if (query.length > 0) {
      const res = await fetch(`/api/movies/search/${query}`, {
        method: 'GET',
        headers:
        {
          'Authorization': 'Bearer ' + jwt
        }
      });
      return await res.json();
    } else {
      return "The query is empty"
    }
  };
}