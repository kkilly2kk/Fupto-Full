export function use$Fetch(url, options = {}) {
  const { token, refreshToken, refreshAccessToken } = useUserDetails();
  const config = useRuntimeConfig();

  options.headers = {
    ...options.headers,
    ...(token.value && { Authorization: `Bearer ${token.value}` }),
  };
  const fullUrl = `${config.public.apiBase}${url}`;
  console.log("요청 URL:", fullUrl);
  console.log(`Bearer ${token.value}`);

  return $fetch(`${config.public.apiBase}${url}`, options).catch(async (error) => {
    if (error.status === 401 && refreshToken.value) {
      try {
        await refreshAccessToken();
        // 새 토큰으로 원래 요청 재시도
        options.headers.Authorization = `Bearer ${token.value}`;
        return $fetch(`${config.public.apiBase}${url}`, options);
      } catch {
        // 리프레시 실패시 로그아웃
        navigateTo("/user/signin");
      }
    }
    throw error;
  });
}
