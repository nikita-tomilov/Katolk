import React, {useState} from 'react';
import {LoginPage} from "./LoginPage";
import {HomePage} from "./HomePage";

function App() {
  const [token, setToken] = useState<string | null>(null);
  return token ? <HomePage /> : <LoginPage setToken={setToken} />;
}

export default App;
