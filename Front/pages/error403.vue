<script setup>
import { useRouter } from "vue-router";
const router = useRouter();

const goToHome = () => {
  router.push("/");
};

definePageMeta({
  layout: false,
});
</script>

<template>
  <div class="error-container">
    <h1 class="error-title">403</h1>
    <p class="error-message">접속 권한이 없습니다</p>
    <p class="error-description">관리자 권한이 필요한 페이지입니다</p>
    <button @click="goToHome" class="home-button">홈으로 돌아가기</button>
  </div>
</template>

<style scoped>
.error-container {
  min-height: 60vh;
  display: flex;
  flex-direction: column;
  justify-content: center;
  align-items: center;
  padding: 2rem;
  margin: auto;
}

.error-title {
  font-size: 6rem;
  font-weight: 200;
  color: #333;
  margin-bottom: 1rem;
}

.error-message {
  font-size: 1.5rem;
  color: #333;
  margin-bottom: 0.5rem;
}

.error-description {
  color: #666;
  margin-bottom: 2rem;
}

.home-button {
  padding: 0.75rem 2rem;
  font-size: 1rem;
  background-color: #1d70f5;
  color: white;
  border: none;
  cursor: pointer;
  transition: background-color 0.2s;
}

.home-button:hover {
  background-color: #3a71ca;
}
</style>
