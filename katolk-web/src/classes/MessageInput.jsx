import React, {Component} from 'react';
import axios from "axios/index";

export default class MessageInput extends Component {

  constructor(props) {
    super();
    this.state = {value: '', dialogueID: props.dialogueID};
  }

  send(text) {
    let url = '/api/dialog/messages/send';
    let api = axios.create({});

    var capture = this;
    let data = {
      body: text,
      dialogueID: this.state.dialogueID
    };
    console.log(data);
    api.post(url, data)
      .then(function(response) {
        console.log(response)
      }
    );
  }

  onChange(event) {
    this.setState({value: event.target.value});
  }

  onKeyPress(key) {
    if (key.key === 'Enter') {
      if (!((key.ctrlKey === true) || (key.shiftKey === true))) {
        key.preventDefault();
        this.send(this.state.value);
        this.setState({value: ""})
      }
    }
  }

  render() {
    return (
        <textarea
            value={this.state.value}
            onChange={this.onChange.bind(this)}
            onKeyPress={this.onKeyPress.bind(this)}
        />
    );
  }

}