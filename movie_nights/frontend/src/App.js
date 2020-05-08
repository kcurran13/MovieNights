import React from 'react';
import Store from './context/Store';
import './App.css';
import LandingPage from './pages/LandingPage';

function App() {

  return (
      <Store>
        <LandingPage />
      </Store>
  );
}

export default App;
