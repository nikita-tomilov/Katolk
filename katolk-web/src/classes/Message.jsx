import React, {Component} from 'react';

export default class Message extends Component {
  constructor(props) {
    super();
    this.props = props;
  }

  getMessageType() {
    if (this.props.message.authorId === this.props.me.id) {
      return "MineMessage";
    }
    return "NotMineMessage";
  }

  render() {
    return (
      <div className={"Message " + this.getMessageType()}>
        <div className="MessageAuthor">
          {this.props.message.author}
        </div>
        <div className="MessageBody">
          {this.props.message.body}
        </div>
      </div>
    )
  }
}