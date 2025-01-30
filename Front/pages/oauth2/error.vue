<script setup>
const route = useRoute();
const errorMessage = route.query.error;

onMounted(() => {
  if (errorMessage) {
    alert(errorMessage);
    // 로그인 페이지로 리다이렉트
    navigateTo("/user/signin");
  }
});
</script>
