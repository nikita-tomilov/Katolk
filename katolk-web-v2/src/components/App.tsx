import React, {useState} from 'react';
import {LoginPage} from "./LoginPage";
import {HomePage} from "./HomePage";
import {getAuthService} from "../service/AuthService";

const authService = getAuthService();

function App() {
  const [isAuthorized, setIsAuthorized] = useState<boolean>(authService.isAuthorized());
  return isAuthorized ? <HomePage /> : <LoginPage onLogin={setIsAuthorized} />;
}

export default App;
