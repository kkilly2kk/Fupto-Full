<script setup>
const userDetails = useUserDetails();
</script>

<template>
  <footer class="main-footer">
    <div class="content-footer-content">
      <h1 class="content-footer_title">Fupto</h1>
      <ul>
        <li><a href="#">소개</a></li>
        <li><a href="#">이용약관</a></li>
        <li><a href="#">개인정보처리방침</a></li>
        <li><a href="#">쇼핑몰정보</a></li>
        <ClientOnly>
          <li v-if="userDetails.hasRole('ROLE_ADMIN')">
            <nuxt-link to="/admin">관리자페이지</nuxt-link>
          </li>
        </ClientOnly>
      </ul>
      <span>CONTACT US fupto@gmail.com</span>
      <span>© 2024FUPTO</span>
    </div>
  </footer>
</template>
