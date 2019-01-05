import React, {Component} from 'react';
import axios from 'axios';
import './App.css';

import Clock from './classes/clock.jsx';
import Toggle from './classes/toggle.jsx';
import LoginPasswordForm from './classes/loginpasswordform.jsx';
import Message from './classes/message.jsx';


class App extends Component {

  login(state) {
    // alert(state.login + ' ' + state.password);
    let url = '/api/user/me';

    let api = axios.create({
      auth: {
        username: state.login,
        password: state.password
      },
      withCredentials: true,
      headers: { "Access-Control-Allow-Origin": "*"}
    });

   api.get(url)
    .then(function(response) {
      console.log('Authenticated');
      console.log(response.data);
      alert('YOU ARE ' + response.data.username);
    })
    .catch(function(error) {
      alert('Error on Authentication: ' + error);
    });
  }

  render() {
    return (
      <div className="App">
        <Clock />
        <Message author="message author" text="message content" />
        <Toggle />
        <LoginPasswordForm callback={(e) => this.login(e)}/>
      </div>
    );
  }
}

export default App;
