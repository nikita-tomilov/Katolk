import React, {useCallback} from "react";

export type LoginPageProps = {
    setAuthorized: (isAuthorized: boolean) => void;
}

export const LoginPage = (props: LoginPageProps): JSX.Element => {
    const { setAuthorized } = props;

    const clickHandler = useCallback(() => {
        setAuthorized(true);
    }, [setAuthorized]);

    return (
        <div onClick={clickHandler}>
            Login
        </div>
    )
}