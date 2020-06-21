import React from "react";
import {UserDialogData} from "./HomePage";

export type DialogScreen = {
    dialogData: UserDialogData;
    onBack: () => void;
}

export const DialogScreen = (props: DialogScreen):JSX.Element => {
    return <div>DialogScreen</div>;
}