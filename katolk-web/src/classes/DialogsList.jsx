import React, {Component} from 'react';
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

  render() {
    return (
      <div className="DialogsList">
          {this.state.entries}
      </div>
    )
  }
}