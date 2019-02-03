import React, {Component} from 'react';
import Message from './Message.jsx';
import axios from "axios/index";

export default class Dialog extends Component {
  constructor(props) {
    super();
    this.props = props;
    this.state = {messages: [], dialogPicked: props.dialogPicked, me: this.props.me}
  }

  componentDidMount() {
  }

  componentWillReceiveProps(props) {
    console.log("Props updated");
    this.updateDialogEntries(props.dialogPicked);
    this.setState({dialogPicked: props.dialogPicked});
  }

  updateDialogEntries(dialogPicked) {
    let messages = [];
    let url = '/api/dialog/messages/' + dialogPicked.id;
    let api = axios.create({});

    var capture = this;

    api.get(url)
      .then(function(response) {
        console.log(response.data);
        response.data.forEach(function(message) {

          console.log(message);
          messages.push(<Message
            message={message}
            me={capture.props.me}
            key={message.id}/>);
        });
        if (messages.length === 0) {
          messages.push(<Message
            message={{author: "", authorId: 0, body: "No messages in this dialog"}}
            me={capture.props.me}/>);
        }
        capture.setState({messages: messages})
      })
      .catch(function(error) {
        alert('Error on Get Messages List: ' + error);
      });
  }

  dialogPicked(e) {
    this.state.dialogPicked(e);
  }

  render() {
    if (this.state.dialogPicked == null) {
      return (
        <div className="Dialog">
          Please, pick the dialog from the list
        </div>
      )
    }
    return (
      <div className="Dialog">
        <div className="DialogHeader">
          Dialog {this.state.dialogPicked.name}
        </div>
        {this.state.messages}
      </div>
    )
  }
}