export default () => {
  const id = useState("id", () => null); // Vue의 ref 객체를 반환
  const username = useState("username", () => null);
  const email = useState("email", () => null);
  const provider = useState("provider", () => null);
  const roles = useState("roles", () => []);
  const token = useState("token", () => []);

  const isAnonymous = () => {
    if (process.server) return true;
    return username.value === null;
  };

  const setAuthentication = (loginInfo) => {
    id.value = loginInfo.id;
    username.value = loginInfo.username;
    email.value = loginInfo.email;
    provider.value = loginInfo.provider;
    roles.value = loginInfo.roles;
    token.value = loginInfo.token;

    if (process.client) {
      localStorage.setItem("id", loginInfo.id);
      localStorage.setItem("username", loginInfo.username);
      localStorage.setItem("email", loginInfo.email);
      localStorage.setItem("provider", loginInfo.provider);
      localStorage.setItem("roles", JSON.stringify(loginInfo.roles)); //[]->"[]"
      localStorage.setItem("token", loginInfo.token);
    }
  };

  const loadUserFromStorage = async () => {
    if (process.client) {
      if (!localStorage.getItem("username")) return;

      id.value = localStorage.getItem("id");
      username.value = localStorage.getItem("username");
      email.value = localStorage.getItem("email");
      provider.value = localStorage.getItem("provider");
      roles.value = JSON.parse(localStorage.getItem("roles")); //[ROLE_ADMIN]
      token.value = localStorage.getItem("token");

      // await new Promise(resolve => setTimeout(resolve, 10));
    }
  };

  const logout = () => {
    id.value = null;
    username.value = null;
    email.value = null;
    provider.value = null;
    roles.value = [];
    token.value = null;

    if (process.client) {
      localStorage.removeItem("id");
      localStorage.removeItem("username");
      localStorage.removeItem("email");
      localStorage.removeItem("provider");
      localStorage.removeItem("roles");
      localStorage.removeItem("token");
    }
  };

  const hasRole = (role) => roles.value.includes(role);

  return {
    id,
    username,
    email,
    provider,
    roles,
    token,
    isAnonymous,
    setAuthentication,
    hasRole,
    logout,
    loadUserFromStorage,
  };
};
