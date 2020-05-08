import React, {useEffect, useState, useContext, useRef} from 'react';
import {StoreContext} from '../context/Store';
import {Form, Button} from 'react-bootstrap'

export default function TimeSelector(props) {
  const store = useContext(StoreContext);
  const [times, setTimes] = useState([]);

  let selectedTime = useRef();

  useEffect(() => {
    if (props.guests.length > 1) {
      getEvents();
    }
  }, []);

  async function getEvents() {
    const jwt = store.getJwt();
    let userIds = [];
    props.guests.map(user => userIds.push(user.id))

    let response = await fetch("/api/event?ids=" + encodeURIComponent(JSON.stringify(userIds)), {
      method: 'GET',
      headers: {
        'Content-Type': 'application/json',
        'Authorization': 'Bearer ' + jwt
      }
    });

    if (response.status !== 200) return;
    response = await response.json();
    setTimes(response)
  }

  function submitTime(e) {
    e.preventDefault();
    props.callback(parseInt(selectedTime.current.value))
  }

  return (
    <div>
      <Form.Group>
        <Form.Label>Available times: </Form.Label>
        <Form.Control ref={selectedTime} as="select">
          {times ? times.map(time => <option key={time} value={time}>{new Date(time).toString()}</option>) : null}
        </Form.Control>
      </Form.Group>
      <Button onClick={submitTime}>Create Event</Button>
    </div>
  )
}

