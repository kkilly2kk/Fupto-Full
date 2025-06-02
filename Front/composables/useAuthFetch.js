export function useAuthFetch(url, options = {}) {
  const { token, refreshAccessToken } = useUserDetails();
  const config = useRuntimeConfig();

  options.headers = {
    ...options.headers,
    ...(token.value && { Authorization: `Bearer ${token.value}` }),
  };

  options.credentials = "include"; // 쿠키를 포함한 요청

  const fullUrl = `${config.public.apiBase}${url}`;
  console.log("요청 URL:", fullUrl);
  console.log(`Bearer ${token.value}`);

  return useFetch(`${config.public.apiBase}${url}`, {
    ...options,
    async onResponseError({ response }) {
      if (response.status === 401) {
        try {
          await refreshAccessToken();
          // 토큰 리프레시 후 새 토큰으로 헤더 업데이트
          options.headers.Authorization = `Bearer ${token.value}`;
          // 원래 요청 재시도
          return useFetch(`${config.public.apiBase}${url}`, options);
        } catch {
          navigateTo("/user/signin");
        }
      }
    },
  });
}
