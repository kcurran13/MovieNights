import React, {useEffect, useState, useContext} from 'react';
import {ListGroup} from 'react-bootstrap'
import {StoreContext} from '../context/Store';

export default function GuestSelector(props) {
  const [users, setUsers] = useState(null)
  const store = useContext(StoreContext);

  useEffect(() => {
    getUsers();
  }, []);

  async function getUsers() {
    const jwt = store.getJwt();
    let response = await fetch('/api/users/connectedToGoogle', {
      method: 'GET',
      headers: {
        'Content-Type': 'application/json',
        'Authorization': 'Bearer ' + jwt
      }
    });

    if (response.status === 200) {
      let users = await response.json();
      let myId = store.currentUser.id;
      for (let i = 0; i < users.length; i++) {
        if (users[i].id === myId) {
          props.callback(users[i])
          users.splice(i, 1);
        }
      }
      setUsers(users);
    }
  }

  return (
    <div>
      <ListGroup>
        {users &&
        users.map(user => <ListGroup.Item key={user.id}>{user.username}
            <input onClick={() => props.callback(user)} type="checkbox" className="checkbox" id="exampleCheck1"/>
          </ListGroup.Item>
        )
        }
      </ListGroup>
    </div>
  )
}

