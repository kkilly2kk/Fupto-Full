<script setup>
import { ref, onMounted } from "vue";
const userDetails = useUserDetails();
const route = useRoute();
const config = useRuntimeConfig();
const isClient = ref(false);

onMounted(() => {
  isClient.value = true;
});

const logoutHandler = async () => {
  userDetails.logout();
  await navigateTo("/", { replace: true });
  window.location.reload();
};

const isActiveLink = (path, gender) => {
  if (path === "products") {
    return route.path === `/${path}` && route.query.gender === gender;
  }
  return route.path.startsWith(`/${path}`);
};

const redirect = () => {
  window.location.href = `${config.public.frontendUrl}/boards/list`;
};
</script>

<template>
  <header class="main-header">
    <nav class="main-nav">
      <h1 class="main-nav_title"><NuxtLink to="/">FUPTO</NuxtLink></h1>
      <ul class="main-nav-list">
        <li class="main-nav_link">
          <NuxtLink to="/products?gender=2" :class="{ 'active-link': isActiveLink('products', '2') }">여성</NuxtLink>
          <NuxtLink to="/products?gender=1" :class="{ 'active-link': isActiveLink('products', '1') }">남성</NuxtLink>
        </li>
        <li class="main-nav_link">
          <NuxtLink to="/brands" :class="{ 'active-link': isActiveLink('brands') }">브랜드</NuxtLink>
          <NuxtLink to="/shoppingmalls" :class="{ 'active-link': isActiveLink('shoppingmalls') }">쇼핑몰</NuxtLink>
        </li>
        <li class="main-nav_link">
          <NuxtLink to="/boards/list" :class="{ 'active-link': isActiveLink('boards') }" @click="redirect">게시글</NuxtLink>
        </li>
        <li class="utility-nav-list">
          <!-- 클라이언트 사이드에서만 렌더링 -->
          <template v-if="isClient">
            <nuxt-link v-if="!userDetails.isAnonymous()" to="/myPage" :class="{ 'active-link': isActiveLink('myPage') }">
              my
            </nuxt-link>
            <NuxtLink v-if="userDetails.isAnonymous()" :to="`/user/signin?returnURL=${encodeURIComponent(route.fullPath)}`">
              로그인
            </NuxtLink>
            <nuxt-link v-else @click="logoutHandler" to="#">로그아웃</nuxt-link>
          </template>
        </li>
      </ul>
    </nav>
  </header>
</template>
