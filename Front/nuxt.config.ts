// https://nuxt.com/docs/api/configuration/nuxt-config
export default defineNuxtConfig({
  compatibilityDate: "2024-04-03",
  devtools: { enabled: true },
  runtimeConfig: {
    public: {
      apiBase: "http://localhost:8085/api/v1",
      frontendUrl: "http://localhost:3000",
    },
  },
  routeRules: {
    "/oauth2/callback": {
      ssr: false,
    },
  },
});
