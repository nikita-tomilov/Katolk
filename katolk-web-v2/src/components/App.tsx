import React, {useState} from 'react';
import {LoginPage} from "./LoginPage";
import {HomePage} from "./HomePage";

function App() {
  const [authorised, setAuthorized] = useState<boolean>(false);
  return authorised ? <HomePage /> : <LoginPage setAuthorized={setAuthorized} />;
}

export default App;
