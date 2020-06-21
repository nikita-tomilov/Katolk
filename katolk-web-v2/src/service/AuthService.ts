import { getToken} from "../api/auth";

export const tokenStorageName = "token";

class AuthServiceClass {
    token: string = '';
    authError: string | null = null;

    constructor() {
        this.getUserTokenFromStorage();
    }

    public login = (username: string, password: string): Promise<boolean> => {
        return getToken(`grant_type=password&username=${username}&password=${password}`)
            .then((responseToken: string) => {
                this.updateTokenValue(responseToken);
                return true;
            })
            .catch((errorMessage: string) => {
                this.setAuthErrorMessage(errorMessage);
                return false;
            })
    };

    public logout = (): void => {
        this.removeUserToken();
        this.updateTokenValue(null);
    };

    public getToken = (): string => {
        return this.token;
    }

    public getAuthErrorMessage = (): string | null => {
        return this.authError;
    }

    public isAuthorized = (): boolean => {
        return this.token.length > 0;
    }

    private setAuthErrorMessage = (message: string | null) => {
        this.authError = message;
    }

    private updateTokenValue = (token: string | null): void => {
        if(token) {
            this.token = token;
            this.setUserTokenToStorage(token);
        }
        else this.removeUserToken();
    };

    private removeUserToken = (): void => {
        localStorage.removeItem(tokenStorageName);
    };

    private setUserTokenToStorage = (token: string): void => {
        localStorage.setItem(tokenStorageName, token);
    };

    private getUserTokenFromStorage = (): void => {
        const token = localStorage.getItem(tokenStorageName);
        if (token && token !== "undefined") this.updateTokenValue(token);
    };
}

const authServiceClassInstance: AuthServiceClass = new AuthServiceClass();

export const getAuthService = () => {
    return authServiceClassInstance;
};
