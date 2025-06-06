<script setup>
import { use$Fetch } from "~/composables/use$Fetch.js";

useHead({
  link: [{ rel: "stylesheet", href: "/css/my-board.css" }],
});

const userDetails = useUserDetails();
const boards = ref([]);
const config = useRuntimeConfig();

const formatDate = (dateString) => {
  if (!dateString) return "";

  const date = new Date(dateString);
  const ymd =
    date.getUTCFullYear() +
    "-" +
    String(date.getUTCMonth() + 1).padStart(2, "0") +
    "-" +
    String(date.getUTCDate()).padStart(2, "0");

  const time =
    String(date.getUTCHours()).padStart(2, "0") +
    ":" +
    String(date.getUTCMinutes()).padStart(2, "0") +
    ":" +
    String(date.getUTCSeconds()).padStart(2, "0");

  return `${ymd}  ${time}`;
};

const fetchBoards = async () => {
  try {
    const id = userDetails.id.value;
    const response = await use$Fetch(`/user/member/${id}/boards`, {
      method: "GET",
    });
    if (!response) {
      throw new Error("데이터 조회 실패");
    }
    boards.value = response.map((board) => ({
      ...board,
      showAlert: false,
      image: null,
      writerProfileImg: board.regMemberProfileImg
        ? `${config.public.apiBase}/${board.regMemberProfileImg}`
        : "/imgs/user/default-profile.jpg",
    }));

    await Promise.all(
      boards.value.map(async (board) => {
        if (board.img) {
          try {
            const imageResponse = await use$Fetch(`/user/member/${board.id}/boardimg`, {
              method: "GET",
              responseType: "blob",
            });
            board.image = URL.createObjectURL(imageResponse);
          } catch (imageError) {
            console.error(`이미지 로드 실패 (ID: ${board.id}):`, imageError);
            board.image = null;
          }
        }
      })
    );
  } catch (error) {
    console.error(error);
  }
};

onMounted(async () => {
  await fetchBoards();
});
</script>

<template>
  <div class="board-container">
    <div class="board-list">
      <div v-if="!boards.length" class="no-boards-message">내가 쓴 글이 없습니다.</div>

      <div class="board-item" v-for="item in boards" :key="item.id">
        <!-- 좌측 번호 영역 -->
        <div class="item-number">{{ item.id }}</div>

        <!-- 중앙 컨텐츠 영역 -->
        <div class="item-content">
          <div class="content-main">
            <nuxt-link :to="`/boards/${item.id}/detail`">
              <span class="title">
                {{ item.title }}
                <span v-if="item.commentCount > 0" class="comment-count">[{{ item.commentCount }}]</span>
              </span>
            </nuxt-link>
            <div class="content-info">
              <div class="user-info">
                <img
                  :src="item.writerProfileImg || '/imgs/user/default-profile.jpg'"
                  :alt="item.regMemberNickName"
                  class="writer-profile-img"
                />
                <span class="writer">{{ item.regMemberNickName }}</span>
              </div>
              <span class="date">{{ formatDate(item.createdAt) }}</span>
            </div>
          </div>
        </div>

        <!-- 우측 이미지 영역 -->
        <div class="item-image" v-if="item.img">
          <img :src="item.image || 'https://via.placeholder.com/70'" alt="게시글 이미지" class="product-img" />
        </div>
      </div>
    </div>
  </div>
</template>
