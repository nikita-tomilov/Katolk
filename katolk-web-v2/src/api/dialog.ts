import {customFetch} from "./customFetch";
import {Method} from "../service/types";
import {getUrl} from "./url";
import {UserDialogData} from "../components/HomePage";

export const getUserDialogList = (): Promise<UserDialogData[]> => {
    return customFetch<{}, UserDialogData[]>(`${getUrl()}/dialog/list`, Method.GET);
};
