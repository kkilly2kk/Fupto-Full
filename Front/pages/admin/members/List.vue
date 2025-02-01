<script setup>
import { ref, computed, onMounted } from "vue";

useHead({
  link: [{ rel: "stylesheet", href: "/css/admin/member-list.css" }],
});

const members = ref([]);
const isLoading = ref(false);
const totalPages = ref(0);
const currentPage = ref(1);
const pageSize = ref(10);
const config = useRuntimeConfig();

const filterData = ref({
  memberStatus: "active", // active, suspended, withdrawn
  role: "", // ROLE_USER, ROLE_ADMIN
  gender: "",
  searchType: "userId", // userId, nickname, email
  searchKeyword: "",
  dateType: "create_date",
  startDate: "",
  endDate: "",
});

const fetchMembers = async (page = 1) => {
  try {
    isLoading.value = true;
    const queryParams = {
      page: page,
      size: pageSize.value,
      memberStatus: filterData.value.memberStatus,
      role: filterData.value.role,
      gender: filterData.value.gender,
      searchType: filterData.value.searchType,
      searchKeyword: filterData.value.searchKeyword,
      dateType: filterData.value.dateType,
      startDate: filterData.value.startDate,
      endDate: filterData.value.endDate,
    };

    const response = await use$Fetch("/admin/members/search", {
      method: "GET",
      params: queryParams,
    });

    members.value = response.members;
    totalPages.value = response.totalPages;
    currentPage.value = page;
  } catch (error) {
    console.error("데이터 조회 오류:", error);
    alert("회원 목록을 불러오는데 실패했습니다.");
  } finally {
    isLoading.value = false;
  }
};

// Flatpickr 초기화
const startDatePicker = ref(null);
const endDatePicker = ref(null);
const startDateInput = ref(null);
const endDateInput = ref(null);

const initializeFlatpickr = () => {
  const config = {
    dateFormat: "Y-m-d",
    showMonths: 1,
    static: true,
    monthSelectorType: "static",
    onChange: (selectedDates, dateStr, instance) => {
      if (instance.element === startDateInput.value) {
        filterData.value.startDate = dateStr;
      } else if (instance.element === endDateInput.value) {
        filterData.value.endDate = dateStr;
      }
    },
  };

  if (startDateInput.value && endDateInput.value) {
    startDatePicker.value = flatpickr(startDateInput.value, config);
    endDatePicker.value = flatpickr(endDateInput.value, config);
  }
};

// 날짜 설정 함수
const setYesterday = () => {
  const yesterday = new Date();
  yesterday.setDate(yesterday.getDate() - 1);
  if (startDatePicker.value && endDatePicker.value) {
    startDatePicker.value.setDate(yesterday);
    endDatePicker.value.setDate(yesterday);
  }
};

const setToday = () => {
  const today = new Date();
  if (startDatePicker.value && endDatePicker.value) {
    startDatePicker.value.setDate(today);
    endDatePicker.value.setDate(today);
  }
};

const setThisWeek = () => {
  const today = new Date();
  const firstDay = new Date(today.setDate(today.getDate() - today.getDay()));
  const lastDay = new Date(today.setDate(today.getDate() - today.getDay() + 6));
  if (startDatePicker.value && endDatePicker.value) {
    startDatePicker.value.setDate(firstDay);
    endDatePicker.value.setDate(lastDay);
  }
};

const setThisMonth = () => {
  const today = new Date();
  const firstDay = new Date(today.getFullYear(), today.getMonth(), 1);
  const lastDay = new Date(today.getFullYear(), today.getMonth() + 1, 0);
  if (startDatePicker.value && endDatePicker.value) {
    startDatePicker.value.setDate(firstDay);
    endDatePicker.value.setDate(lastDay);
  }
};

// 회원 관리 함수들
const handleActiveChange = async (memberId, active) => {
  try {
    await use$Fetch(`/admin/members/${memberId}/active`, {
      method: "PATCH",
      params: { active: active },
    });
    await fetchMembers(currentPage.value);
  } catch (error) {
    console.error("상태 변경 실패:", error);
    alert("회원 상태 변경에 실패했습니다.");
  }
};

const handleRoleChange = async (memberId, newRole) => {
  try {
    await use$Fetch(`/admin/members/${memberId}/role`, {
      method: "PATCH",
      body: newRole,
    });
    await fetchMembers();
  } catch (error) {
    console.error("권한 변경 실패:", error);
    alert("회원 권한 변경에 실패했습니다.");
    await fetchMembers(); // 에러 시에도 목록 새로고침
  }
};

const handleRoleSelectChange = (event, member) => {
  const select = event.target;
  const originalValue = member.role;
  const newValue = select.value;

  const confirmMessage =
    newValue === "ROLE_ADMIN" ? "정말로 관리자 권한으로 변경하시겠습니까?" : "정말로 일반회원 권한으로 변경하시겠습니까?";

  if (!window.confirm(confirmMessage)) {
    select.value = originalValue;
    return;
  }

  handleRoleChange(member.id, newValue);
};

const handleDeleteMember = async (memberId) => {
  if (!confirm("정말로 이 회원을 삭제하시겠습니까? 이 작업은 되돌릴 수 없습니다.")) {
    return;
  }

  try {
    await use$Fetch(`/admin/members/${memberId}`, {
      method: "DELETE",
    });
    await fetchMembers(currentPage.value);
  } catch (error) {
    console.error("회원 삭제 실패:", error);
    alert("회원 삭제에 실패했습니다.");
  }
};

// 페이지네이션 함수들
const getPageNumbers = computed(() => {
  const pageCount = Math.min(10, totalPages.value);
  let start = Math.max(1, currentPage.value - Math.floor(pageCount / 2));
  let end = Math.min(start + pageCount - 1, totalPages.value);

  if (end - start + 1 < pageCount) {
    start = Math.max(1, end - pageCount + 1);
  }

  return Array.from({ length: end - start + 1 }, (_, i) => start + i);
});

const pageChange = (newPage) => {
  fetchMembers(newPage);
};

const pageSizeChange = (event) => {
  pageSize.value = parseInt(event.target.value);
  currentPage.value = 1;
  fetchMembers(1);
};

// 검색 핸들러
const handleSearch = (event) => {
  event.preventDefault();
  currentPage.value = 1;
  fetchMembers(1);
};

// 날짜 포맷팅 함수
const formatDate = (dateString) => {
  if (!dateString) return "";

  const date = new Date(dateString);

  const ymd = `${date.getUTCFullYear()}-${String(date.getUTCMonth() + 1).padStart(2, "0")}-${String(date.getUTCDate()).padStart(
    2,
    "0"
  )}`;

  const time = `${String(date.getUTCHours()).padStart(2, "0")}:${String(date.getUTCMinutes()).padStart(2, "0")}:${String(
    date.getUTCSeconds()
  ).padStart(2, "0")}`;

  return [ymd, time];
};

onMounted(() => {
  fetchMembers();
  initializeFlatpickr();
});
</script>

<template>
  <main>
    <h1 class="title">회원 목록</h1>
    <ul class="breadcrumbs">
      <li><a href="#">FUPTO</a></li>
      <li class="divider">/</li>
      <li><a href="#">회원</a></li>
      <li class="divider">/</li>
      <li><a href="#" class="active">회원 목록</a></li>
    </ul>

    <!-- 검색 필터 -->
    <div class="card">
      <div class="card-body">
        <form @submit="handleSearch">
          <table class="table table-filter">
            <tbody>
              <tr>
                <th>날짜</th>
                <td colspan="3">
                  <select v-model="filterData.dateType" class="select">
                    <option value="create_date">가입일</option>
                    <option value="update_date">수정일</option>
                    <option value="login_date">접속일</option>
                  </select>
                  <input ref="startDateInput" type="text" class="input-text date" readonly v-model="filterData.startDate" />
                  <button type="button" class="btn-calendar" @click="() => startDatePicker?.open()">&#x1F4C5;</button>
                  ~
                  <input ref="endDateInput" type="text" class="input-text date" readonly v-model="filterData.endDate" />
                  <button type="button" class="btn-calendar mr-2" @click="() => endDatePicker?.open()">&#x1F4C5;</button>
                  <button type="button" class="btn btn-primary mr-2" @click="setYesterday">어제</button>
                  <button type="button" class="btn btn-primary mr-2" @click="setToday">오늘</button>
                  <button type="button" class="btn btn-primary mr-2" @click="setThisWeek">이번 주</button>
                  <button type="button" class="btn btn-primary" @click="setThisMonth">이번 달</button>
                </td>
              </tr>
              <tr>
                <th>계정상태</th>
                <td>
                  <select v-model="filterData.memberStatus" class="select">
                    <option value="active">정상</option>
                    <option value="suspended">정지</option>
                    <option value="withdrawn">탈퇴</option>
                  </select>
                </td>
                <th>회원유형</th>
                <td>
                  <select v-model="filterData.role" class="select">
                    <option value="">전체</option>
                    <option value="ROLE_USER">일반회원</option>
                    <option value="ROLE_ADMIN">관리자</option>
                  </select>
                </td>
              </tr>
              <tr>
                <th>성별</th>
                <td colspan="3">
                  <input class="mr-2" type="radio" id="gender-all" v-model="filterData.gender" value="" checked />
                  <label for="gender-all">전체</label>
                  <input class="mr-2" type="radio" id="gender-male" v-model="filterData.gender" value="남성" />
                  <label for="gender-male">남성</label>
                  <input class="mr-2" type="radio" id="gender-female" v-model="filterData.gender" value="여성" />
                  <label for="gender-female">여성</label>
                </td>
              </tr>
              <tr>
                <th>검색어</th>
                <td colspan="3">
                  <select v-model="filterData.searchType" class="select">
                    <option value="userId">아이디</option>
                    <option value="nickname">닉네임</option>
                    <option value="email">이메일</option>
                  </select>
                  <input type="text" v-model="filterData.searchKeyword" class="input-text" placeholder="검색어를 입력하세요" />
                </td>
              </tr>
            </tbody>
          </table>
          <div class="text-center">
            <button type="submit" class="btn btn-primary">검색</button>
          </div>
        </form>
      </div>
    </div>

    <!-- 회원 목록 -->
    <div class="card">
      <div class="card-body">
        <div class="d-flex">
          <select class="select mr-2" v-model="pageSize" @change="pageSizeChange">
            <option :value="10">10</option>
            <option :value="25">25</option>
            <option :value="50">50</option>
            <option :value="100">100</option>
          </select>
        </div>

        <table class="table member-list-table">
          <thead>
            <tr>
              <th>프로필</th>
              <th>아이디</th>
              <th>닉네임</th>
              <th>이메일</th>
              <th>가입일</th>
              <th>최근접속</th>
              <th>회원유형</th>
              <th>상태</th>
              <th>관리</th>
            </tr>
          </thead>
          <tbody>
            <tr v-for="member in members" :key="member.id">
              <td>
                <img
                  :src="member.profileImg ? `${config.public.apiBase}/${member.profileImg}` : '/imgs/user/default-profile.jpg'"
                  :alt="member.nickname"
                  class="profile-img"
                />
              </td>
              <td>
                <NuxtLink :to="`/admin/members/${member.id}`" class="member-link">
                  {{ member.userId }}
                </NuxtLink>
              </td>
              <td>
                <NuxtLink :to="`/admin/members/${member.id}`" class="member-link">
                  {{ member.nickname }}
                </NuxtLink>
              </td>
              <td>
                <NuxtLink :to="`/admin/members/${member.id}`" class="member-link">
                  {{ member.email }}
                </NuxtLink>
              </td>
              <td>
                <div>{{ formatDate(member.createDate)[0] }}</div>
                <div>{{ formatDate(member.createDate)[1] }}</div>
              </td>
              <td>
                <div>{{ formatDate(member.loginDate)[0] }}</div>
                <div>{{ formatDate(member.loginDate)[1] }}</div>
              </td>
              <td>
                <select
                  class="role-select"
                  :value="member.role"
                  @change="(event) => handleRoleSelectChange(event, member)"
                  :disabled="!member.state || !member.active"
                >
                  <option value="ROLE_USER">일반회원</option>
                  <option value="ROLE_ADMIN">관리자</option>
                </select>
              </td>
              <td>
                <label class="pl-switch">
                  <input
                    type="checkbox"
                    v-model="member.active"
                    @change="handleActiveChange(member.id, member.active)"
                    :disabled="!member.state"
                  />
                  <span class="pl-slider round"></span>
                </label>
              </td>
              <td>
                <button class="btn btn-outline-danger btn-sm" @click="handleDeleteMember(member.id)">
                  <i class="bx bx-trash"></i>
                </button>
              </td>
            </tr>
          </tbody>
        </table>

        <!-- 페이지네이션 -->
        <div class="pagination-container mt-4">
          <ul class="pagination justify-content-center">
            <li class="page-item" :class="{ disabled: currentPage === 1 }">
              <button class="page-link" @click="pageChange(1)">&lt;&lt;</button>
            </li>
            <li class="page-item" :class="{ disabled: currentPage === 1 }">
              <button class="page-link" @click="pageChange(currentPage - 1)">이전</button>
            </li>
            <li v-for="pageNum in getPageNumbers" :key="pageNum" class="page-item" :class="{ active: pageNum === currentPage }">
              <button class="page-link" @click="pageChange(pageNum)">{{ pageNum }}</button>
            </li>
            <li class="page-item" :class="{ disabled: currentPage === totalPages }">
              <button class="page-link" @click="pageChange(currentPage + 1)">다음</button>
            </li>
            <li class="page-item" :class="{ disabled: currentPage === totalPages }">
              <button class="page-link" @click="pageChange(totalPages)">&gt;&gt;</button>
            </li>
          </ul>
        </div>
      </div>
    </div>
  </main>
</template>
