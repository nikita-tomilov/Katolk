import React, {useEffect, useState} from "react";
import {getUserDialogList} from "../api/dialog";
import {DialogList} from "./DialogList";
import {DialogScreen} from "./DialogScreen";

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
    const [selected, setSelected] = useState<number | null>(null);

    useEffect(() => {
        if(!dialogs) getUserDialogList().then(setDialogs);
    });

    return (
        <div>
            {dialogs && !selected && <DialogList dialogsData={dialogs} onDialogSelect={setSelected}/> }
            {dialogs && selected && <DialogScreen dialogData={dialogs[selected]} onBack={() => setSelected(null)}/> }
        </div>
    )
}