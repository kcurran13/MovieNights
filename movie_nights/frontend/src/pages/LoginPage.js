import React, {Component} from 'react';
import RegisterForm from '../components/RegistrationForm';
import SignInForm from '../components/LoginForm';

class LoginPage extends Component {
  render() {
    return (
      <div className="width">
        <div>
          <SignInForm/>
        </div>

        <div>
          <RegisterForm/>
        </div>
      </div>
    )
  }
}

export default LoginPage;