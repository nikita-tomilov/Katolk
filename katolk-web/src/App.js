import React, {Component} from 'react';
import axios from 'axios';
import './App.css';

import Clock from './classes/clock.jsx';
import Toggle from './classes/toggle.jsx';
import UserAuth from './classes/UserAuth.jsx';
import DialogsList from './classes/DialogsList.jsx';
import Dialog from './classes/Dialog.jsx';

class App extends Component {

  constructor(props) {
    super(props);
    this.state = {
      authorized: false,
      dialogPicked: null,
      me: null
    }
  }

  login(state) {
    this.setState({authorized: state.authorized, dialogPicked: null, me: state.user})
  }

  changeDialogPicked(dialog) {
    // console.log(dialog);
    this.setState({dialogPicked: dialog});
  }

  render() {
    return (
      <div className="App">
        <Clock />
        <Toggle />
        <UserAuth callback={(e) => this.login(e)}/>
          {this.state.authorized && <DialogsList onDialogPicked={(e) => this.changeDialogPicked(e)} />}
          {this.state.authorized && <Dialog dialogPicked={this.state.dialogPicked} me={this.state.me}/>
          }
      </div>
    );
  }
}

export default App;
