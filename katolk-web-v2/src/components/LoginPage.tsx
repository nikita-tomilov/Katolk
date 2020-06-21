import React, {useCallback, useState} from "react";
import './LoginPage.css';
import {Button, TextField} from "@material-ui/core";

export type LoginPageProps = {
    setToken: (token: string) => void;
}

export const LoginPage = (props: LoginPageProps): JSX.Element => {
    const { setToken } = props;
    const [name, setName] = useState("");
    const [password, setPassword] = useState("");
    const [authFailMessage, setAuthFailMessage] = useState<string | null>(null);
    const onNameChange = useCallback((event) => {
        if (event.currentTarget && event.currentTarget.value)
            setName(event.currentTarget.value);
    }, []);

    const onPasswordChange = useCallback((event) => {
        if (event.currentTarget && event.currentTarget.value)
            setPassword(event.currentTarget.value);
    }, []);

    const onSignIn = useCallback(() => {
        setAuthFailMessage('FAILED');
    }, [name, password, setToken]);

    const onKeyPress = useCallback(
        (e: React.KeyboardEvent) => {
            if (e.which === 13) {
                onSignIn();
            }
        },
        [onSignIn]
    );

    return (
        <div className="SignInForm">
                <TextField
                    variant="outlined"
                    margin="normal"
                    required
                    fullWidth
                    id="login"
                    label="Login"
                    name="login"
                    autoComplete="login"
                    autoFocus
                    value={name}
                    onChange={onNameChange}
                    onKeyPress={onKeyPress}
                />
                <TextField
                    variant="outlined"
                    margin="normal"
                    required
                    fullWidth
                    name="password"
                    label="Password"
                    type="password"
                    id="password"
                    autoComplete="current-password"
                    value={password}
                    onChange={onPasswordChange}
                    onKeyDown={onKeyPress}
                />
                {authFailMessage && <div className="error">{authFailMessage}</div>}
                <Button
                    type="submit"
                    fullWidth
                    variant="contained"
                    color="secondary"
                    onClick={onSignIn}
                >
                    Sign In
                </Button>
            </div>
    )
}