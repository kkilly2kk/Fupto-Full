<script setup>
useHead({
  link: [{ rel: "stylesheet", href: "/css/board-list.css" }],
});

import { ref, onMounted } from "vue";
import { use$Fetch } from "~/composables/use$Fetch";
import { useRoute, useRouter } from "vue-router";

const route = useRoute();
const router = useRouter();

const boards = ref([]);
const totalElements = ref(0);
const totalPages = ref(0);
const currentPage = ref(1);
const pageSize = ref(10);

const searchForm = ref(null);
const noDataMessage = ref("");
const imageUrl = ref("");

const config = useRuntimeConfig();
const userDetails = useUserDetails();
const isAdmin = computed(() => {
  return userDetails.hasRole("ROLE_ADMIN");
});

const formData = ref({
  searchType: "title",
  searchKeyWord: "",
  boardCategoryName: route.query.category || "커뮤니티",
});

const handleCategoryClick = (categoryName) => {
  formData.value.boardCategoryName = categoryName;
  formData.value.searchType = "title";
  formData.value.searchKeyWord = "";
  currentPage.value = 1;

  router.replace({
    query: {
      ...route.query,
      category: categoryName,
    },
  });

  fetchBoards();
};

const handleWriteClick = () => {
  sessionStorage.setItem("selectedCategory", formData.value.boardCategoryName);
};

const fetchBoards = async () => {
  try {
    const params = new URLSearchParams({
      page: currentPage.value.toString(),
      size: pageSize.value.toString(),
    });
    if (formData.value.searchType) params.append("searchType", formData.value.searchType);
    if (formData.value.searchKeyWord) params.append("searchKeyWord", formData.value.searchKeyWord);
    if (formData.value.boardCategoryName) params.append("boardCategory", formData.value.boardCategoryName);
    params.append("active", true);

    const data = await use$Fetch(`/boards/list?${params.toString()}`);
    boards.value = data.boards;
    imageUrl.value = data.img ? (data.img.startsWith("/") ? data.img : "/" + data.img) : "";
    totalElements.value = data.totalElements;
    totalPages.value = data.totalPages;
    if (boards.value.length === 0) {
      noDataMessage.value = "게시글이 없습니다.";
    } else {
      noDataMessage.value = "";
    }
  } catch (error) {
    console.error("게시판 데이터를 가져오는 중 오류 발생:", error);
  }
};

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

const handleSearch = (event) => {
  event.preventDefault();
  currentPage.value = 1; // 검색 시 첫 페이지로 리셋
  fetchBoards();
};

const pageChange = (newPage) => {
  if (newPage < 1 || newPage > totalPages.value) return;
  currentPage.value = newPage;
  fetchBoards();
};

const visiblePages = computed(() => {
  const startPage = Math.floor((currentPage.value - 1) / 5) * 5 + 1;
  const endPage = Math.min(startPage + 4, totalPages.value);
  return Array.from({ length: endPage - startPage + 1 }, (_, i) => startPage + i);
});

onMounted(() => {
  const categoryFromUrl = route.query.category;
  if (categoryFromUrl) {
    formData.value.boardCategoryName = categoryFromUrl;
  } else {
    router.replace({
      query: {
        category: formData.value.boardCategoryName, // 기본값은 '커뮤니티'라 formData는 업데이트 필요 없음
      },
    });
  }
  fetchBoards();
});
</script>

<template>
  <main>
    <nav>
      <ul class="board_ul">
        <li>
          <button @click="handleCategoryClick('공지사항')" :class="{ active: formData.boardCategoryName === '공지사항' }">
            공지사항
          </button>
        </li>
        <li>
          <button @click="handleCategoryClick('커뮤니티')" :class="{ active: formData.boardCategoryName === '커뮤니티' }">
            커뮤니티
          </button>
        </li>

        <li>
          <button @click="handleCategoryClick('FAQ')" :class="{ active: formData.boardCategoryName === 'FAQ' }">FAQ</button>
        </li>
        <li>
          <button @click="handleCategoryClick('고객센터')" :class="{ active: formData.boardCategoryName === '고객센터' }">
            고객센터
          </button>
        </li>
      </ul>
    </nav>
    <div class="board">
      <table>
        <tbody>
          <tr v-for="board in boards" :key="board.id">
            <td class="num">
              <span>{{ board.id }}</span>
            </td>
            <td>
              <div class="board-item">
                <span class="board-title">
                  <nuxt-link :to="`/boards/${board.id}/detail`">
                    {{ board.title }}
                    <span v-if="board.commentCount > 0" class="comment-count">[{{ board.commentCount }}]</span>
                  </nuxt-link>
                </span>
                <div class="smalls">
                  <div class="user-info">
                    <img
                      :src="
                        board.regMemberProfileImg
                          ? `${config.public.apiBase}/${board.regMemberProfileImg}`
                          : '/imgs/user/default-profile.jpg'
                      "
                      :alt="board.regMemberNickName"
                      class="writer-profile-img"
                    />
                    <span class="writer">{{ board.regMemberNickName }}</span>
                  </div>
                  <span class="date">{{ formatDate(board.updateDate) }}</span>
                </div>
              </div>
            </td>
            <td class="product-img">
              <div class="d-flex align-items-center">
                <img v-if="board.img" :src="config.public.apiBase + '/' + board.img" class="product-img" />
                <!-- 이미지가 없을 경우, 빈 공간 표시되지 않음 -->
              </div>
            </td>

            <td class="comment"></td>
          </tr>
        </tbody>
      </table>

      <div class="write">
        <button
          v-if="(formData.boardCategoryName !== '공지사항' && formData.boardCategoryName !== 'FAQ') || isAdmin"
          class="write-btn"
          @click="handleWriteClick"
        >
          <nuxt-link to="/boards/reg">글쓰기</nuxt-link>
        </button>
      </div>

      <form ref="searchForm" @submit="handleSearch" class="searchBox">
        <select v-model="formData.searchType" name="sc" class="type">
          <option value="title">제목</option>
          <option value="regMemberNickName">작성자</option>
          <option value="contents">내용</option>
        </select>
        <input v-model="formData.searchKeyWord" type="text" name="ss" class="keyword" />
        <div class="text-center">
          <button type="submit" class="searchBtn" value="검색">검색</button>
        </div>
      </form>

      <div class="Pagination-container">
        <nav aria-label="Page navigation">
          <ul class="Pagination justify-content-center">
            <li class="Page-item" :class="{ disabled: currentPage === 1 }">
              <a class="Page-link" href="#" @click.prevent="pageChange(1)">&lt;&lt;</a>
            </li>
            <li class="Page-item" :class="{ disabled: currentPage === 1 }">
              <a class="Page-link" href="#" @click.prevent="pageChange(currentPage - 1)"><</a>
            </li>
            <li class="Page-item" v-for="page in visiblePages" :key="page" :class="{ active: currentPage === page }">
              <a class="Page-link" href="#" @click.prevent="pageChange(page)">{{ page }}</a>
            </li>
            <li class="Page-item" :class="{ disabled: currentPage === totalPages }">
              <a class="Page-link" href="#" @click.prevent="pageChange(currentPage + 1)">></a>
            </li>
            <li class="Page-item" :class="{ disabled: currentPage === totalPages }">
              <a class="Page-link" href="#" @click.prevent="pageChange(totalPages)">&gt;&gt;</a>
            </li>
          </ul>
        </nav>
      </div>
    </div>
  </main>
</template>
