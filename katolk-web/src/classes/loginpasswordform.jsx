import React, {Component} from 'react';

export default class NameForm extends React.Component {
  constructor(props) {
    super(props);
    this.state = {login: '', password: '', callback: props.callback};

    this.handleChangeLogin = this.handleChangeLogin.bind(this);
    this.handleChangePassword = this.handleChangePassword.bind(this);
    this.handleSubmit = this.handleSubmit.bind(this);
  }

  handleChangeLogin(event) {
    this.setState({login: event.target.value});
  }

  handleChangePassword(event) {
    this.setState({password: event.target.value});
  }

  handleSubmit(event) {
    // alert('Login: ' + this.state.login + ' Password: ' + this.state.password);
    this.state.callback(this.state);
    event.preventDefault();
  }

  render() {
    return (
      <form onSubmit={this.handleSubmit}>
        <label>
          Login:
          <input type="text" value={this.state.login} onChange={this.handleChangeLogin} />
        </label>
        <br />
        <label>
          Password:
          <input type="password" value={this.state.password} onChange={this.handleChangePassword} />
        </label>
        <br />
        <input type="submit" value="Submit" />
      </form>
    );
  }
}