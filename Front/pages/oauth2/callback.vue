<script setup>
import { onMounted } from "vue";
import { useRoute, useRouter } from "vue-router";
import { jwtDecode } from "jwt-decode";

const route = useRoute();
const router = useRouter();
const userDetails = useUserDetails();

onMounted(() => {
  try {
    const token = route.query.token;
    const userId = route.query.userId;
    const provider = route.query.provider;

    // 세션 스토리지에서 returnURL 가져오기
    const savedReturnURL = process.client ? sessionStorage.getItem("oauth2_return_url") || "/" : "/";

    if (token) {
      const userInfo = jwtDecode(token);

      userDetails.setAuthentication({
        id: userId,
        username: userInfo.username,
        email: userInfo.email,
        roles: userInfo.roles.map((role) => role.authority),
        token: token,
        provider: provider,
      });

      // 저장된 returnURL로 이동
      router.push(savedReturnURL);
      // 사용 후 바로 삭제
      if (process.client) {
        sessionStorage.removeItem("oauth2_return_url");
      }
    } else {
      console.error("토큰이 없습니다.");
      router.push("/signin");
    }
  } catch (error) {
    console.error("OAuth 처리 중 오류:", error);
    router.push("/signin");
  }
});
</script>
