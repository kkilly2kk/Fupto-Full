<script setup>
import { onMounted } from "vue";
import { useRoute, useRouter } from "vue-router";
import { jwtDecode } from "jwt-decode";

const route = useRoute();
const router = useRouter();
const userDetails = useUserDetails();
const returnURL = route.query.returnURL || "/";

onMounted(() => {
  try {
    const token = route.query.token;
    const userId = route.query.userId;

    if (token) {
      const userInfo = jwtDecode(token);

      userDetails.setAuthentication({
        id: userId,
        username: userInfo.username,
        email: userInfo.email,
        roles: userInfo.roles.map((role) => role.authority),
        token: token,
      });

      router.push(returnURL);
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
