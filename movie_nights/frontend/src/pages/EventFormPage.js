import React, {useState, useContext} from 'react'
import Tabs from 'react-bootstrap/Tabs'
import Tab from 'react-bootstrap/Tab'
import {Button} from 'react-bootstrap'
import {StoreContext} from '../context/Store';
import SearchMovie from '../components/SearchMovies'
import GuestSelector from '../components/GuestSelector'
import TimeSelector from '../components/TimeSelector'

export default function EventForm(props) {
  const store = useContext(StoreContext);
  const [tabKey, setTabKey] = useState('selectMovie');
  const [selectedMovie, setSelectedMovie] = useState(null);
  const [selectedGuests, setSelectedGuests] = useState([]);
  const [selectedTime, setSelectedTime] = useState(null);

  const movieSelectorCallback = (movieTitle) => {
    setSelectedMovie(movieTitle);
    setTabKey('selectGuests');
  };

  const guestSelectorCallback = (user) => {
    if (selectedGuests.includes(user)) {
      let newGuests = [...selectedGuests];
      let index = newGuests.indexOf(user);
      newGuests.splice(index, 1);
      setSelectedGuests(newGuests);
    } else {
      setSelectedGuests([...selectedGuests, user]);
    }
  };

  const timeSelectorCallback = async (time) => {
    setSelectedTime(time);
    let objectToPost = {
      creatorId: store.currentUser.id,
      usersToInvite: selectedGuests.map(guest => guest.id).filter(guest => guest !== store.currentUser.id),
      title: "You're invited!",
      description: `${selectedMovie} with ${store.currentUser.username}`,
      startDate: time,
      endDate: time + 10800000
    };

    const jwt = store.getJwt();
    const res = await fetch(`/api/event/create_event`, {
      method: 'POST',
      headers:
        {
          'Content-Type': 'application/json',
          'Authorization': 'Bearer ' + jwt
        },
      body: JSON.stringify(objectToPost)
    });
    if (res.status === 200) {
      alert("Event created!");
      return true
    } else {
      alert("Unable to create event.");
      return false;
    }
  };

  return (
    <div className="width">
      {store.currentUser && <div>
        <Tabs id="tabs" activeKey={tabKey} onSelect={k => setTabKey(k)}>
          <Tab eventKey="selectMovie" title="Select Movie">
            {selectedMovie &&
            <h5>Select Movie: {selectedMovie} </h5>
            }
            <SearchMovie movieSelector={true} callback={movieSelectorCallback}/>
          </Tab>
          <Tab eventKey="selectGuests" title="Select Guests">
            {selectedMovie ?
              <h5>Select guests</h5>
              :
              <h5>Please choose a movie.</h5>
            }

            <h5>Selected Guests: {selectedGuests.map(user => user.username + ", ")}</h5>

            <GuestSelector callback={guestSelectorCallback}/>
            <Button onClick={() => setTabKey('selectTime')}>Select time</Button>

          </Tab>
          <Tab eventKey="selectTime" title="Select Time">
            {tabKey === "selectTime" &&
            <TimeSelector guests={selectedGuests} callback={timeSelectorCallback}/>
            }
          </Tab>
        </Tabs>
      </div>
      }
    </div>
  )
}