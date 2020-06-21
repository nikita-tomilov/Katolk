import React, {useCallback, useState} from "react";
import './LoginPage.css';
import {Button, TextField} from "@material-ui/core";
import {getAuthService} from "../service/AuthService";

export type LoginPageProps = {
    setToken: (token: string) => void;
}

const authService = getAuthService();

export const LoginPage = (props: LoginPageProps): JSX.Element => {
    const { setToken } = props;
    const [authMessage, setAuthMessage] = useState<string | null>(null);
    const [name, setName] = useState("");
    const [password, setPassword] = useState("");
    const onNameChange = useCallback((event) => {
        if (event.currentTarget && event.currentTarget.value)
            setName(event.currentTarget.value);
    }, []);

    const onPasswordChange = useCallback((event) => {
        if (event.currentTarget && event.currentTarget.value)
            setPassword(event.currentTarget.value);
    }, []);

    const onSignIn = useCallback(() => {
        authService.login(name, password).then((success: boolean) => {
            if(success) setToken(authService.getToken());
            else setAuthMessage(authService.getAuthErrorMessage());
        })
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
            {authMessage && <div className="error">{authMessage}</div>}
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