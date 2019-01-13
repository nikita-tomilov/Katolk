import React, {Component} from 'react';
import axios from 'axios';
import Cookies from './Cookies.jsx'

export default class UserAuth extends React.Component {
  constructor(props) {
    super(props);
    this.state = {login: '', password: '', callback: props.callback,
      authorized: false, user: null, api: null};

    this.handleChangeLogin = this.handleChangeLogin.bind(this);
    this.handleChangePassword = this.handleChangePassword.bind(this);
    this.handleSubmit = this.handleSubmit.bind(this);
    this.handleLogout = this.handleLogout.bind(this);
  }

  componentDidMount() {
    if (Cookies.getCookie("login") !== null) {
      this.setState({login: Cookies.getCookie("login")})
    }
    if ((Cookies.getCookie("javacookie") !== null) &&
      (Cookies.getCookie("javacookie") !== "")){
      this.setState({password: Cookies.getCookie("password")});
      this.performAuthorize(Cookies.getCookie("login"), "whatever", true);
    }
  }

  handleChangeLogin(event) {
    this.setState({login: event.target.value});
  }

  handleChangePassword(event) {
    this.setState({password: event.target.value});
  }

  performAuthorize(login, password, silent) {
    // alert('Login: ' + login + ' Password: ' + password);
    let url = '/api/user/me';

    let api = axios.create({
      auth: {
        username: login,
        password: password
      },
      withCredentials: true,
      headers: { "Access-Control-Allow-Origin": "*"}
    });

    var capture = this;

    api.get(url)
      .then(function(response) {
        console.log('Authenticated');
        console.log(response.data);
        capture.setState(
          {authorized: true, user: response.data,
            api: axios.create({
              auth: {}
            })
          });
        Cookies.setCookie("login", login);
        Cookies.setCookie("javacookie", "ok");
        capture.state.callback(capture.state);
        // alert('YOU ARE ' + response.data.username);
      })
      .catch(function(error) {
        if (!silent) {
          alert('Error on Authentication: ' + error);
        }
        capture.setState(
          {authorized: false, user: null, api: null});
        capture.state.callback(capture.state);
      });
  }

  handleSubmit(event) {
    this.performAuthorize(this.state.login, this.state.password, false);
    event.preventDefault();
  }

  handleLogout() {
    var capture = this;

    this.state.api.get("/api/user/logout")
      .then(function(response) {
        capture.setState({authorized: false,
          user: null, api: null, password: ''});
        Cookies.setCookie("javacookie", "");
        capture.state.callback(capture.state);
      });
  }

  renderIfAuthorized() {
    return (
      <div id="UserAuth">
        <p>Welcome back, {this.state.user.username}</p>
        <button onClick={this.handleLogout}>Logout</button>
      </div>
    )
  }

  renderIfNotAuthorized() {
    return (
      <div id="UserAuth">
        <form onSubmit={this.handleSubmit}>
          <label>
            Login: <br />
            <input type="text" value={this.state.login} onChange={this.handleChangeLogin} />
          </label>
          <br />
          <label>
            Password: <br />
            <input type="password" value={this.state.password} onChange={this.handleChangePassword} />
          </label>
          <br />
          <input type="submit" value="Login" />
        </form>
      </div>
    );
  }

  render() {
    if (this.state.authorized) return this.renderIfAuthorized();
    return this.renderIfNotAuthorized()
  }
}