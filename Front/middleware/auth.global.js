import useUserDetails from "~/composables/useUserDetails.js";

export default defineNuxtRouteMiddleware(async (to, from) => {
  const userDetails = useUserDetails();
  const isRefresh = () => {
    const navEntries = performance.getEntriesByType("navigation");
    return navEntries.length > 0 && navEntries[0].type === "reload";
  };

  //페이지 로드시 로컬 스토리지에서 사용자 정보를 로드
  await userDetails.loadUserFromStorage();

  if (process.client) {
    if (to.path.startsWith("/admin")) {
      // 새로고침이 아닌 경우에만 권한 체크
      if (!isRefresh()) {
        if (!userDetails.hasRole("ROLE_ADMIN")) {
          return navigateTo("/error403");
        }
        if (to.path !== "/admin/signin" && userDetails.isAnonymous()) {
          return navigateTo(`/admin/signin?returnURL=${to.fullPath}`);
        }
      } else {
        // 새로고침인 경우, 사용자 정보가 로드될 때까지 짧게 대기
        await new Promise((resolve) => setTimeout(resolve, 50));

        // 대기 후 다시 권한 체크
        if (!userDetails.hasRole("ROLE_ADMIN")) {
          return navigateTo("/error403");
        }
      }
    }

    // boards 페이지 접근 제어
    if (to.path.startsWith("/boards")) {
      if (to.path !== "/user/signin" && to.path !== "/user/signup") {
        if (userDetails.isAnonymous()) {
          return navigateTo(`/user/signin?returnURL=${encodeURIComponent(to.fullPath)}`, {
            replace: true,
            external: true,
          });
        }
      }
    }

    // myPage 접근 제어
    if (to.path.startsWith("/myPage")) {
      if (to.path !== "/user/signin" && to.path !== "/user/signup") {
        if (userDetails.isAnonymous()) {
          return navigateTo(`/user/signin?returnURL=${encodeURIComponent(to.fullPath)}`, {
            replace: true,
            external: true,
          });
        }
      }
    }

    // admin/members/list 페이지에서 회원 상세 페이지로 이동할 때, 이전 페이지 정보를 저장
    if (
      from.path.includes("/admin/members/") &&
      from.path.includes("/admin/members/list") === false &&
      to.path === "/admin/members/list"
    ) {
      localStorage.setItem("fromMemberDetail", "true");
    }
  }
});
