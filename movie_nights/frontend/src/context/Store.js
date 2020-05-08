import React, {Component, createContext} from 'react';
import Rest from '../utilities/Rest';

export const StoreContext = createContext();

export default class Store extends Component {
  db = new Rest();

  state = {
    currentUser: null,
    db: this.db,
    tokenValidationTimer: null,
    MOVIENIGHTSJWTCOOKIENAME: 'MOVIENIGHTSJWT',
    currentSearchResult: [],
    setCurrentSearchResult: (currentSearchResult) => {
      this.setState({currentSearchResult})
    },
    login: async (user) => {
      let loginResult = await this.db.login(user);
      if (loginResult) {
        this.state.setJwt(loginResult.jwt);
        this.state.startTokenValidationTimer();
        this.setState({ currentUser: loginResult.dbUser })
        return true;
      }
      return false;
    },
    logout: async () => {
      let result = await this.db.logout();
      if (result) {
        this.state.deleteJwt();
        clearInterval(this.state.tokenValidationTimer);
        this.setState({currentUser: null})
        return true;
      }
      return false;
    },
    getActiveUser: async () => {
      let jwt = this.state.getJwt();
      if (jwt) {
        return await this.db.getActiveUser(jwt);
      }
      return null;
    },
    getMoviesFromApi: async (query, jwt) => {
      return await this.db.search(query, jwt)
    },
    getJwt: () => {
      let decodedCookie = decodeURIComponent(document.cookie);
      let ca = decodedCookie.split(';');
      for (let i = 0; i < ca.length; i++) {
        let c = ca[i];
        while (c.charAt(0) === ' ') {
          c = c.substring(1);
        }
        if (c.indexOf(this.state.MOVIENIGHTSJWTCOOKIENAME) === 0) {
          return c.substring(this.state.MOVIENIGHTSJWTCOOKIENAME.length + 1, c.length);
        }
      }
      return null;
    },
    setJwt: (token) => {
      let date = new Date();
      date.setTime(date.getTime + (10 * 365 * 24 * 60 * 60))
      let expires = "expires=" + date.toUTCString();
      document.cookie = `${this.state.MOVIENIGHTSJWTCOOKIENAME}=${token}; ${expires};path=/;`;
    },
    getJwtContent: () => {
      let jwt = this.state.getJwt();
      if (jwt) {
        let header = decodeURIComponent(atob(jwt.split(".")[0]));
        let payload = decodeURIComponent(atob(jwt.split(".")[1]));
        header = JSON.parse(header);
        payload = JSON.parse(payload);
        return {header, payload}
      }
      return null;
    },
    isJwtValid: () => {
      if (this.state.getJwt()) {
        let currTime = Date.now();
        let expirationTime = this.state.getJwtContent().payload.exp * 1000;
        return (currTime + 60000) < expirationTime;
      }
    },
    refreshToken: async () => {
      let result = await this.db.refreshToken();
      if (result && result !== "try again") {
        this.state.setJwt(result.jwt);
        this.setState({currentUser: result.user});
        return true;
      } else if (result && result === "try again") {
        return "try again";
      }
      clearInterval(this.state.tokenValidationTimer);
      this.setState({currentUser: null});
      return false;
    },
    startTokenValidationTimer: () => {
      this.setState({
        tokenValidationTimer: setInterval(() => {
          if (!this.state.isJwtValid()) {
            this.state.refreshToken();
          }
        }, 45000)
      })
    },
    deleteJwt: () => {
      document.cookie = `${this.state.MOVIENIGHTSJWTCOOKIENAME}=; expires=Thu, 01 Jan 1970 00:00:00 UTC; path=/;`;
    },
    getAndSetUserIfLoggedIn: async () => {
      this.state.isJwtValid();
      if (!this.state.currentUser && this.state.getJwt()) {
        if (this.state.isJwtValid()) {
          let activeUser = await this.state.getActiveUser();
          if (activeUser) {
            this.state.startTokenValidationTimer();
            this.setState({currentUser: activeUser})
          }
        } else {
          await this.state.refreshToken();
        }
      }
    }
  };

  async componentDidMount() {
    this.state.getAndSetUserIfLoggedIn();
  };

  componentWillUnmount() {
    clearInterval(this.state.tokenValidationTimer);
  }

  render() {
    return <StoreContext.Provider value={{...this.state}}>
      {this.props.children}
    </StoreContext.Provider>
  }
}