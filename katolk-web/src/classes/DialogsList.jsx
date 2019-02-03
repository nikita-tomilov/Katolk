import React, {Component} from 'react';
import Websocket from 'react-websocket';
import DialogEntry from './DialogEntry.jsx';
import axios from "axios/index";

export default class DialogsList extends Component {
  constructor(props) {
    super();
    this.props = props;
    this.state = {entries: [], onDialogPicked: props.onDialogPicked}
  }

  componentDidMount() {
    this.updateDialogEntries()
  }

  updateDialogEntries() {
    let entries = [];
    let url = '/api/dialog/list';
    let api = axios.create({});

    var capture = this;

    api.get(url)
      .then(function(response) {
        response.data.forEach(function(dialog) {
          let dialogName = dialog.name;
          let latestMsgPreview = dialog.latestMessage;
          if (latestMsgPreview == null) {
            latestMsgPreview = "\<no messages\>";
          } else {
            latestMsgPreview = dialog.latestMessage.author + ": " + dialog.latestMessage.body;
          }

          entries.push(<DialogEntry
              name={dialogName}
              msg={latestMsgPreview}
              onclick={(e) => capture.onDialogPicked(dialog)}/>);
        });
        capture.setState({entries: entries})
      })
      .catch(function(error) {
        alert('Error on Get Dialog List: ' + error);
      });

    // entries.push(<DialogEntry name="Another Conference" msg="Another 228" />);

    return entries;
  }

  onDialogPicked(e) {
    this.state.onDialogPicked(e);
  }

  onWsOpened() {
    console.log("WS OPENED")
  }

  onWsClosed() {
    console.log("WS CLOSED")
  }

  onWsData(data) {
    console.log(data);
  }

  getWsUrl() {
    return "ws://localhost:3000/api/ws/websocket"
  }

  render() {
    return (
      <div className="DialogsList">
        {this.state.entries}
        <Websocket  url={this.getWsUrl()}
                    onMessage={this.onWsData.bind(this)}
                    onOpen={this.onWsOpened()}
                    onClose={this.onWsClosed()}
                    debug={true}/>
      </div>
    )
  }
}