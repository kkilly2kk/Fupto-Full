<script setup>
import { jwtDecode } from "jwt-decode";

const route = useRoute();
const config = useRuntimeConfig();
const userDetails = useUserDetails();
const returnURL = route.query.returnURL || "/";

const username = ref("");
const password = ref("");

// returnURL을 저장하는 함수
const saveReturnUrl = () => {
  if (process.client) {
    sessionStorage.setItem("oauth2_return_url", returnURL);
  }
};

const localLoginHandler = async () => {
  try {
    const response = await use$Fetch("/auth/signin", {
      method: "POST",
      body: {
        username: username.value,
        password: password.value,
      },
    });

    let userInfo = jwtDecode(response.token);

    userDetails.setAuthentication({
      id: response.userId,
      username: userInfo.username,
      email: userInfo.email,
      provider: response.provider,
      roles: userInfo.roles.map((role) => role.authority),
      token: response.token,
      refreshToken: response.refreshToken,
    });

    return navigateTo(returnURL);
  } catch (error) {
    console.error("Login failed:", error);
    alert(error.data || "계정이 일치하지 않습니다.");
  }
};
</script>

<template>
  <main>
    <div class="login-container">
      <div class="login-box">
        <h1>로그인</h1>
        <form @submit.prevent="localLoginHandler">
          <div class="input-group">
            <label for="username">아이디</label>
            <input class="textbox" v-model="username" type="text" id="username" name="username" required />
          </div>
          <div class="input-group">
            <label for="password">비밀번호</label>
            <input class="textbox" v-model="password" type="password" id="password" name="password" required />
          </div>
          <button type="submit" class="login-button">로그인</button>
        </form>

        <div class="social-login-section">
          <div class="divider">
            <span>SNS 계정으로 로그인하기</span>
          </div>
          <div class="social-buttons">
            <a @click="saveReturnUrl" :href="`${config.public.apiBase}/oauth2/authorization/google`" class="social-button">
              <img src="/imgs/icon/google.png" alt="Google" />
            </a>
            <a @click="saveReturnUrl" :href="`${config.public.apiBase}/oauth2/authorization/kakao`" class="social-button">
              <img src="/imgs/icon/kakao.png" alt="Kakao" />
            </a>
            <a @click="saveReturnUrl" :href="`${config.public.apiBase}/oauth2/authorization/naver`" class="social-button">
              <img src="/imgs/icon/naver.png" alt="Naver" />
            </a>
          </div>
        </div>

        <div class="login-links">
          <a href="#">아이디/비밀번호 찾기</a>
          <nuxt-link to="/user/signup">회원가입</nuxt-link>
        </div>
      </div>
    </div>
  </main>
</template>

<style scoped>
@import url("@/public/css/sign-in.css");
</style>
