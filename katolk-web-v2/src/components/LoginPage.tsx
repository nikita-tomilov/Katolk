import React, {useCallback} from "react";
import {DefaultButton} from "./basic/Buttons";
import './LoginPage.css';

export type LoginPageProps = {
    setToken: (token: string) => void;
}

export const LoginPage = (props: LoginPageProps): JSX.Element => {
    const { setToken } = props;

    const clickHandler = useCallback(() => {
        setToken('true');
    }, [setToken]);

    return (
        <div className="loginPageWrapper">
            <div className="btnContainer">
                <DefaultButton onClick={clickHandler} title={'Login'} />
            </div>
        </div>
    )
}