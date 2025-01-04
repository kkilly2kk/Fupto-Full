<script setup>
useHead({
  link: [{ rel: "stylesheet", href: "/css/myLayout.css" }],
});

const userDetails = useUserDetails();
const member = ref({
  id: null,
  email: null,
  username: null,
});

import { onMounted } from "vue";
import { useAlerts } from "~/composables/useAlerts";
import { useSSE } from "~/composables/useSSE";

const { fetchUnreadAlerts } = useAlerts();
const { connectSSE } = useSSE();

onMounted(() => {
  // 클라이언트에서 member 데이터 설정
  member.value = {
    id: userDetails.id.value,
    email: userDetails.email.value,
    username: userDetails.username.value,
  };
  fetchUnreadAlerts();
  connectSSE();
});
</script>

<template>
  <main class="mainLayout">
    <aside class="profileContainer">
      <div class="profile">
        <div>
          <img src="" alt="" />
        </div>
        <ul class="info">
          <li class="info-name">{{ member.username }}</li>
          <li class="info-email">{{ member.email }}</li>
          <li>
            <nuxt-link to="/myPage/setting/account"><button>프로필관리</button></nuxt-link>
          </li>
        </ul>
      </div>
      <nav class="profileNav">
        <ul>
          <li>
            <nuxt-link to="/myPage">
              <span><img src="/imgs/icon/board.svg" /></span>
              <span>내 활동</span>
            </nuxt-link>
          </li>
          <li>
            <nuxt-link to="/myPage/favorite">
              <span><img src="/imgs/icon/favorite.svg" /></span>
              <span>찜 목록</span>
            </nuxt-link>
          </li>
          <li>
            <nuxt-link to="/myPage/alert">
              <span><img src="/imgs/icon/alerts.svg" /></span>
              <span>알림</span>
            </nuxt-link>
          </li>
        </ul>
      </nav>
    </aside>
    <nuxt-page />
  </main>
</template>
