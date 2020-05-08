import React from 'react';
import {Card} from 'react-bootstrap'

const MovieSelector = (props) => {
  const selectMovie = () => {
    props.callback(props.Title)
  };

  return (
    <Card>
      <Card.Body>
        <Card.Title>{props.Title}</Card.Title>
        <button onClick={() => selectMovie()}>Select Movie</button>
      </Card.Body>
    </Card>
  )
};

export default MovieSelector;