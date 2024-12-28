<script setup>
useHead({
  link: [{ rel: "stylesheet", href: "/css/board-detail.css" }],
});

import { use$Fetch } from "~/composables/use$Fetch";

const board = ref({
  title: "",
  contents: "",
  regMemberNickName: "",
  regMemberId: "",
  boardCategoryName: "",
  fileUpload: null,
  active: "",
});

const config = useRuntimeConfig();
const route = useRoute();
const router = useRouter();
const boardId = route.params.id;

const userDetails = useUserDetails();
const imageUrl = ref("");
const dropdownVisible = ref(false);
const showModal = ref(false);
const comments = ref([]);
const commentCount = ref(0);

const updateCommentCount = (count) => {
  commentCount.value = count;
};

const getImageUrl = (url) => {
  if (!url) return "";
  if (url.startsWith("data:")) {
    // 새로 업로드된 이미지의 경우 (data URL)
    return url;
  }
  // 서버에서 가져온 이미지 URL의 경우
  return `${config.public.apiBase}${url}`;
};

const loadBoardData = async () => {
  try {
    const data = await use$Fetch(`/boards/${boardId}/edit`);

    console.log("Received data:", data);

    if (!data) {
      throw new Error("게시글 데이터를 받지 못했습니다.");
    }

    board.value = {
      title: data.title,
      contents: data.contents,
      boardCategoryName: data.boardCategoryName,
      regMemberNickName: data.regMemberNickName,
      regMemberId: data.regMemberId,
      createDate: data.createDate,
      updateDate: data.updateDate,
      active: data.active,
      img: data.img,
    };
    console.log(board);
    imageUrl.value = data.img ? (data.img.startsWith("/") ? data.img : "/" + data.img) : "";
  } catch (error) {
    console.error("Error loading board:", error);
    alert(`게시글을 불러오는데 실패했습니다: ${error.message}`);
  }
};

// 날짜 형식을 'yyyy-mm-dd'로 포맷팅하는 함수
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

const isMatchUser = computed(() => {
  return userDetails.id.value == board.value.regMemberId;
});

// 드롭다운 메뉴 토글
const toggleDropdown = () => {
  if (isMatchUser.value) {
    dropdownVisible.value = !dropdownVisible.value; // isMatchUser가 true일 때만 드롭다운 토글
  } else {
    alert("권한이 없습니다.");
  }
};

const closeDropdown = (event) => {
  // 메뉴 버튼과 메뉴 외부를 클릭했을 때 드롭다운을 닫는다
  if (!event.target.closest(".actions-menu") && dropdownVisible.value) {
    dropdownVisible.value = false;
  }
};

// 수정 버튼 클릭 시 이동
const handleEdit = () => {
  router.push(`/boards/${boardId}/edit`); // 수정 페이지로 이동
};

const handleDelete = async () => {
  try {
    const hasComments = await use$Fetch(`/boards/check-comments/${boardId}`);

    if (hasComments) {
      alert("댓글이 있는 게시글은 삭제할 수 없습니다.");
      return;
    }

    // 삭제 확인 메시지
    const isConfirmed = confirm("게시물을 삭제하시겠습니까?");

    if (isConfirmed) {
      const formData = new FormData();
      formData.append("active", false);

      await use$Fetch(`/boards/${boardId}/inactive`, {
        method: "PATCH",
        body: formData,
      });

      alert("게시글이 삭제되었습니다!");
      router.push("/boards/list");
    }
  } catch (error) {
    console.error("Error:", error);
    alert(`게시글 삭제에 실패했습니다: ${error.message}`);
  }
};

const closeModal = () => {
  showModal.value = false;
  window.location.href = "http://localhost:3000/boards/list";
};

onMounted(async () => {
  await loadBoardData();
  document.addEventListener("click", closeDropdown); // 외부 클릭 시 드롭다운 닫기
});

onBeforeUnmount(() => {
  document.removeEventListener("click", closeDropdown);
});
</script>

<template>
  <main class="board-container">
    <article class="board-detail">
      <section class="header">
        <div class="filter-tag" @click="closeModal">
          <span>{{ board.boardCategoryName }}</span>
        </div>
        <h1 class="post-title">{{ board.title }}</h1>

        <div class="user-section">
          <div class="user-avatar"></div>
          <div class="user-meta">
            <span class="user-name">{{ board.regMemberNickName }}</span>
            <span class="post-date">{{ formatDate(board.updateDate) }}</span>
          </div>
          <div class="actions-menu">
            <button class="menu-trigger" v-show="isMatchUser" @click="toggleDropdown">
              <img src="/public/imgs/icon/3dot.svg" alt="Menu" />
            </button>
            <div v-if="dropdownVisible" class="menu-dropdown">
              <button @click="handleEdit">수정</button>
              <button @click="handleDelete">삭제</button>
            </div>
          </div>
        </div>
      </section>

      <section class="body">
        <div class="image-preview" id="imagePreview">
          <img v-if="imageUrl" :src="getImageUrl(imageUrl)" alt="미리보기 이미지" />
        </div>

        <div class="post-content">{{ board.contents }}</div>
      </section>

      <section class="board-comments-section">
        <span class="comments-title">
          댓글
          <span class="comments-count" v-if="commentCount > 0">{{ commentCount }}</span>
        </span>
        <Comment :board-id="Number(route.params.id)" @update-count="updateCommentCount" />
      </section>
    </article>
  </main>
</template>
