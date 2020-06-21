import React from "react";
import './Buttons.css';

export type DefaultButtonProps = React.HTMLAttributes<HTMLButtonElement>;

export const DefaultButton = (props: DefaultButtonProps): JSX.Element => {
    return <button {...props} className="btnWrapper" >{props.title}</button>;
}