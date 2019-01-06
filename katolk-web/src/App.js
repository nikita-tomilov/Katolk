import React, {Component} from 'react';
import axios from 'axios';
import './App.css';

import Clock from './classes/clock.jsx';
import Toggle from './classes/toggle.jsx';
import UserAuth from './classes/UserAuth.jsx';
import Message from './classes/message.jsx';


class App extends Component {

  login(state) {
    // alert(state.login + ' ' + state.password);

  }

  render() {
    return (
      <div className="App">
        <Clock />
        <Message author="message author" text="message content" />
        <Toggle />
        <UserAuth callback={(e) => this.login(e)}/>
      </div>
    );
  }
}

export default App;
