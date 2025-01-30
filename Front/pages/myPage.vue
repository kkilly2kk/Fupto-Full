<script setup>
import { onMounted, ref } from "vue";
const { connectSSE } = useSSE();
const { hasUnreadAlerts, fetchAlerts } = useAlerts();

useHead({
  link: [{ rel: "stylesheet", href: "/css/my-page.css" }],
});

const userDetails = useUserDetails();
const config = useRuntimeConfig();
const member = ref({
  id: null,
  email: null,
  username: null,
  profileImg: null,
});

const profileImageUrl = computed(() => {
  if (member.value.profileImg) {
    return `${config.public.apiBase}/${member.value.profileImg}`;
  }
  return "/imgs/user/default-profile.jpg";
});

const handleImageUpload = async (event) => {
  const file = event.target.files[0];
  if (!file) return;

  const formData = new FormData();
  formData.append("file", file);

  try {
    await use$Fetch(`/user/member/${member.value.id}/profile-image`, {
      method: "POST",
      body: formData,
    });
    // 프로필 정보 다시 불러오기
    const response = await use$Fetch(`/user/member/${member.value.id}`);
    if (response && response.profileImg) {
      member.value.profileImg = response.profileImg;
    }
  } catch (error) {
    console.error("Image upload failed:", error);
    alert("이미지 업로드에 실패했습니다.");
  }
};

const deleteImage = async () => {
  try {
    await use$Fetch(`/user/member/${member.value.id}/profile-image`, {
      method: "DELETE",
    });
    member.value.profileImg = null;
  } catch (error) {
    console.error("Image delete failed:", error);
    alert("이미지 삭제에 실패했습니다.");
  }
};

onMounted(async () => {
  // 클라이언트에서 member 데이터 설정
  member.value = {
    id: userDetails.id.value,
    email: userDetails.email.value,
    username: userDetails.username.value,
  };

  //프로필 정보 (이미지 포함) 가져오기
  try {
    const response = await use$Fetch(`/user/member/${member.value.id}`);
    if (response && response.profileImg) {
      member.value.profileImg = response.profileImg;
    }
  } catch (error) {
    console.error("Failed to fetch profile info:", error);
  }
  await fetchAlerts();
  connectSSE();
});
</script>

<template>
  <main class="mainLayout">
    <aside class="profileContainer">
      <div class="profile">
        <div class="profile-image-container">
          <img :src="profileImageUrl" :alt="member.username" />
          <div class="profile-image-controls">
            <label for="imageUpload" class="edit-btn">
              <span>편집</span>
              <input type="file" id="imageUpload" accept="image/*" @change="handleImageUpload" class="hidden" />
            </label>
            <button v-if="member.profileImg" @click="deleteImage" class="delete-btn">삭제</button>
          </div>
        </div>
        <ul class="info">
          <li class="info-name">{{ member.username }}</li>
          <li class="info-email">{{ member.email }}</li>
          <li>
            <nuxt-link to="/myPage/setting/account"><button>프로필관리</button></nuxt-link>
          </li>
        </ul>
      </div>
      <nav class="profileNav">
        <ul>
          <li>
            <nuxt-link to="/myPage" exact-active-class="router-link-active">
              <span><img src="/imgs/icon/board.svg" /></span>
              <span>내 활동</span>
            </nuxt-link>
          </li>
          <li>
            <nuxt-link to="/myPage/favorite" exact-active-class="router-link-active">
              <span><img src="/imgs/icon/favorite.svg" /></span>
              <span>찜 목록</span>
            </nuxt-link>
          </li>
          <li>
            <nuxt-link to="/myPage/alert" exact-active-class="router-link-active">
              <span class="alert-menu">
                <img src="/imgs/icon/alerts.svg" />
                <div v-if="hasUnreadAlerts" class="unread-dot"></div>
              </span>
              <span>알림</span>
            </nuxt-link>
          </li>
        </ul>
      </nav>
    </aside>
    <nuxt-page />
  </main>
</template>
