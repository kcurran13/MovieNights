import React, {useState, useContext, useRef} from 'react';
import {StoreContext} from '../context/Store';
import {useHistory} from "react-router-dom";
import {Form} from 'react-bootstrap';

export default function LoginPage() {
  const store = useContext(StoreContext);
  const history = useHistory();
  const [invalidUsername, setInvalidUsername] = useState(false)
  const [invalidPassword, setInvalidPassword] = useState(false)
  const [loginError, setLoginError] = useState(false)

  let userName = useRef();
  let password = useRef();

  async function validate(e) {
    e.preventDefault();
    let valid = true;

    if (userName.current.value === '') {
      setInvalidUsername(true)
      valid = false;
    } else {
      setInvalidUsername(false)
    }

    if (password.current.value === '') {
      setInvalidPassword(true)
      valid = false;
    } else {
      setInvalidPassword(false)
    }

    if (valid) {
      if (await store.login({username: userName.current.value, password: password.current.value})) {
        history.push("/");
      } else {
        setLoginError(true)
      }
    }
  }

  return (
    <Form noValidate onSubmit={validate}>
      <h1 className="mb-4">Login here</h1>
      <Form.Group>
        <Form.Label>Username</Form.Label>
        <Form.Control required ref={userName} type="name" placeholder="Username"/>
        {invalidUsername &&
          <p className="register-form-error">Username required</p>
        }
      </Form.Group>
      <Form.Group>
        <Form.Label>Password</Form.Label>
        <Form.Control required ref={password} type="password" placeholder="Password" autoComplete="password"/>
        {invalidPassword &&
          <p className="register-form-error">Password required</p>
        }
      </Form.Group>
      <button className="control-button up" type="submit">Login</button>
      {loginError &&
        <p className="register-form-error">Unable to log in.</p>
      }
    </Form>
  )
}