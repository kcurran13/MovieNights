import React, {useContext} from 'react';
import {StoreContext} from '../context/Store';
import {useHistory, Link} from "react-router-dom";
import {Navbar, Nav} from 'react-bootstrap';

export default function MyNavbar() {
  const store = useContext(StoreContext);
  const history = useHistory();

  function signOut() {
    if (store.logout()) {
      history.push("/");
    }
  }

  return (
    <div>
    {store.currentUser ?
        <Navbar>
          <Link to="/"><Navbar.Brand>Movie Nights</Navbar.Brand></Link>
          <Nav className="mr-auto">
          </Nav>
          <Navbar.Collapse className="justify-content-end">
            <Nav.Link onClick={signOut}>Sign out</Nav.Link>
            <Navbar.Text>
              Welcome {store.currentUser.username}
            </Navbar.Text>
          </Navbar.Collapse>
        </Navbar>
     : null }
    </div>
  )
}