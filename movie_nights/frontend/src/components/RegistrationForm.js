import React, {useRef, useState, useContext} from 'react'
import {Form} from 'react-bootstrap'
import {useHistory} from "react-router-dom";
import {StoreContext} from '../context/Store';

export default function RegisterPage(props) {
  const history = useHistory();
  const store = useContext(StoreContext);
  const userName = useRef();
  const password = useRef();
  const password2 = useRef();
  const [newUserId, setNewUserId] = useState(null);
  const [googleError, setGoogleError] = useState(false)
  const [userNameError, setUserNameError] = useState(false)
  const [matchingPasswordError, setMatchingPasswordError] = useState(false)
  const CLIENT_ID = "522663004580-2953u1b8ieso5htlegbe57prqdp336ab.apps.googleusercontent.com";

  function googleConnect(userId) {
    window.gapi.load('auth2', function () {
      try {
        window.auth2 = window.gapi.auth2.init({
          client_id: CLIENT_ID,
          scope: "https://www.googleapis.com/auth/calendar.events"
        });
        window.auth2.grantOfflineAccess().then(res => login(res, userId)).catch(error => errorCallback());
      } catch (error) {
        setGoogleError(true);
      }
    });
  }

  function errorCallback(){
      setGoogleError(true);
  }

  async function login(authResult, userId) {
    if (authResult['code']) {
      let result = await fetch('/api/users/storeauthcode', {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json; charset=utf-8',
          'X-Requested-With': 'XMLHttpRequest',
        },
        body: JSON.stringify({
          code: authResult['code'],
          userId: userId
        })

      });

      if (result.status === 200) {
        if (await store.login({username: userName.current.value, password: password.current.value})) {
          history.push("/");
        } else {
          console.log("Error: ", userName.current.value, password.current.value)
        }
      } else {
          console.log("Error: ", result.status)
      }
    } else {
      errorCallback("Error signing in to Google.");
    }
  }

  async function validate(e) {
    e.preventDefault();
    let valid = true;

    if (userName.current.value === '') {
      setUserNameError(true)
      valid = false;
    } else {
      setUserNameError(false)
    }

    if (password.current.value !== password2.current.value) {
      setMatchingPasswordError(true)
      valid = false;
    } else {
      setMatchingPasswordError(false);
    }

    if (valid) {
      await register(userName.current.value, password.current.value)
    }
  }

  async function register(userName, password) {
    let data = {
      username: userName, password
    };

    let response = await fetch('/api/users', {
      method: 'POST',
      body: JSON.stringify(data),
      headers: {'Content-Type': 'application/json'}
    });

    if (response.status === 200) {
      let user = await response.json();
      setNewUserId(user.id)
      googleConnect(user.id);
    } else {
      console.log("Error in response")
    }
  }

  return (
    <Form noValidate onSubmit={validate}>
      <h1 className="mb-4">Register here</h1>
      <Form.Group>
        <Form.Label>Username</Form.Label>
        <Form.Control readOnly={googleError} required ref={userName} type="name" placeholder="Username"/>
        {userNameError &&
          <p className="register-form-error">Username required</p>
        }
      </Form.Group>

      <Form.Group>
        <Form.Label>Password</Form.Label>
        <Form.Control readOnly={googleError} required ref={password} type="password" placeholder="Password" autoComplete="password"/>
      </Form.Group>

      <Form.Group>
        <Form.Label>Repeat password</Form.Label>
        <Form.Control readOnly={googleError} required ref={password2} type="password" placeholder="Repeat password" autoComplete="repeat-password"/>
        {matchingPasswordError &&
        <p className="register-form-error">Passwords must match</p>
        }
      </Form.Group>

      {googleError ?
        <>
          <p className="register-form-error">Access to Google failed - please try again</p>
          <button className="control-button up" onClick={googleConnect(newUserId)}>Try again</button>
        </>
        :
        <button className="control-button up" type="submit">Register</button>
      }
    </Form>
  )
}