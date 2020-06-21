import React, {useCallback} from "react";
import {UserDialogData} from "./HomePage";
import './DialogList.css';

export type DialogListProps = {
    dialogsData: UserDialogData[];
    onDialogSelect: (dialogId: number) => void;
}

export const DialogList = (props: DialogListProps): JSX.Element => {
    const { dialogsData, onDialogSelect } = props;
    return <div className="dialogsContainer">{
        dialogsData.map(dialog => {
            return <DialogListItem data={dialog} onSelect={onDialogSelect}/>;
        })
    }</div>;
}

const DialogListItem = (props: { data: UserDialogData, onSelect: (id: number) => void }): JSX.Element => {
    const { data, onSelect } = props;
    const clickHandler = useCallback((event) => {
        onSelect(data.id);
    }, [onSelect]);
    return <div onClick={clickHandler} className="dialogListItem">{data.latestMessage.body}</div>;
}