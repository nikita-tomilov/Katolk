import {getAuthService} from "../service/AuthService";
import {Method} from "../service/types";

export const customFetch = <ReqT, ResT>(
    url: string,
    method: Method,
    requestBody?: ReqT
): Promise<ResT> => {
    return fetch(url, {
        method,
        headers:
            method === Method.POST
                ? {
                    "Content-Type": "application/json;charset=utf-8",
                    Authorization: `Bearer ${getAuthService().getToken()}`,
                }
                : {
                    Authorization: `Bearer ${getAuthService().getToken()}`,
                },
        body: JSON.stringify(requestBody),
    }).then((data) => data.json());
};

export const customFetchForImages = <ReqT, ResT>(
    url: string,
    method: Method,
    requestBody?: any
): Promise<ResT> => {
    return fetch(url, {
        method,
        headers: {
            Authorization: `Bearer ${getAuthService().getToken()}`
        },
        body: requestBody,
    }).then((data) => data.json());
};
