import React, {useState} from 'react';
import {LoginPage} from "./LoginPage";
import {HomePage} from "./HomePage";

function App() {
  const [token, setToken] = useState<string | null>(null);
  return token ? <HomePage token={token} /> : <LoginPage setToken={setToken} />;
}

export default App;
