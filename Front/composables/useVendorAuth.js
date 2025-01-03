export function useVendorAuth() {
  const { isAnonymous } = useUserDetails();
  const router = useRouter();
  const route = useRoute();
  const isAuthModalOpen = ref(false);

  const checkVendorAccess = () => {
    if (isAnonymous()) {
      isAuthModalOpen.value = true;
      return false;
    }
    return true;
  };

  const closeAuthModal = () => {
    isAuthModalOpen.value = false;
  };

  const navigateToSignup = () => {
    router.push("/user/signup");
  };

  const navigateToSignin = () => {
    // 현재 경로를 returnURL로 포함
    return navigateTo(`/user/signin?returnURL=${encodeURIComponent(route.fullPath)}`, {
      replace: true,
      external: true,
    });
  };

  return {
    isAuthModalOpen,
    checkVendorAccess,
    closeAuthModal,
    navigateToSignup,
    navigateToSignin,
  };
}
