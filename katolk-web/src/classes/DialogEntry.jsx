import React, {Component} from 'react';

export default class DialogEntry extends Component {

  constructor(props) {
    super();
    this.props = props;
    this.state = {onclick: props.onclick};
  }

  componentWillReceiveProps(props) {
    console.log("Props of DialogEntry updated");
  }

  render() {
    return (
      <div className="DialogEntry"
           onClick={this.state.onclick}>
        <div className="DialogEntryName">
          {this.props.name}
        </div>
        <div className="DialogEntryMsgPreview">
          {this.props.msg}
        </div>
      </div>
    )
  }
}