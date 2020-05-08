import React, {useContext} from 'react';
import {StoreContext} from '../context/Store';
import {BrowserRouter as Router, Route} from 'react-router-dom';
import Navbar from '../components/Navbar';
import EventFormPage from './EventFormPage';
import LoginPage from './LoginPage';

const LandingPage = () => {
  const store = useContext(StoreContext);

  return (
    <Router>
      <Navbar/>
      {store.currentUser ?
        <div>
          <Route path="/" exact component={EventFormPage}/>
        </div> :
        <LoginPage/>
      }
    </Router>
  )
};

export default LandingPage;