import { route } from './url'
import {Method} from "../service/types";

const request = require('request')
const user = 'oauth2-client'
const pass = 'oauth2-client-password'

export const getToken = (body: string): any => {
    return new Promise((resolve, reject) => {
        request(
            {
                url: `${route}/oauth/token`,
                method: Method.POST,
                headers: { 'Content-Type': 'application/x-www-form-urlencoded' },
                body,
                auth: {
                    user,
                    pass,
                },
                strictSSL: false,
            },
            (error: any, response: any, body: any) => {
                const ans = JSON.parse(body)
                if(ans.error) {
                    reject(ans.error);
                }

                const token = ans.access_token;
                resolve(token);
            }
        );
    });
};
