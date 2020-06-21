import React, {useEffect, useState} from "react";
import {getUserDialogList} from "../api/dialog";

export type MessageData = {
    id: number;
    author: string;
    authorId: number;
    body: string;
    date: number;
    dialogueID: number;
    wasRead: boolean;
};

export type UserDialogData = {
    id: number;
    creator: number;
    name: string;
    unreadCount: number;
    participants: any[];
    latestMessage: MessageData;
}

export const HomePage = ():JSX.Element => {
    const [dialogs, setDialogs] = useState<UserDialogData[] | null>(null);

    useEffect(() => {
        if(!dialogs) getUserDialogList().then(setDialogs);
    });

    return (
        <div>
            Home
        </div>
    )
}