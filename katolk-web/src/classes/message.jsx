import React, {Component} from 'react';

export default class Message extends Component {
  constructor(props) {
    super();
    this.props = props;
  }
  render() {
    return (
      <div className="Message">
        <div className="MessageAuthor">
          {this.props.author}
        </div>
        <div className="MessageBody">
          {this.props.text}
        </div>
      </div>
    )
  }
}