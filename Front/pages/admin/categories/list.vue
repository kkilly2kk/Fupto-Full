<script setup>
import { ref, onMounted } from "vue";
import { use$Fetch } from "~/composables/use$Fetch";

useHead({
  link: [{ rel: "stylesheet", href: "/css/admin/category-list.css" }],
});

const categories = ref([]);
const totalElements = ref(0);
const currentPage = ref(1);
const pageSize = ref(10);
const totalPages = ref(0);

// 날짜 관련 변수 추가
const startDatePicker = ref(null);
const endDatePicker = ref(null);
const startDateInput = ref(null);
const endDateInput = ref(null);
const searchForm = ref(null);

// 검색 폼 데이터
const formData = ref({
  name: "",
  level: "",
  dateType: "reg",
  startDate: "",
  endDate: "",
});

// Flatpickr 관련
const waitForFlatpickr = (callback) => {
  if (window.flatpickr) {
    callback();
  } else {
    setTimeout(() => waitForFlatpickr(callback), 100);
  }
};

const initializeFlatpickr = () => {
  const config = {
    dateFormat: "Y-m-d",
    showMonths: 1,
    static: true,
    monthSelectorType: "static",
  };

  startDatePicker.value = window.flatpickr(startDateInput.value, {
    ...config,
    onChange: (selectedDates) => {
      const selectedDate = selectedDates[0];
      if (selectedDate && endDatePicker.value) {
        endDatePicker.value.set("minDate", selectedDate);
      }
    },
  });

  endDatePicker.value = window.flatpickr(endDateInput.value, {
    ...config,
    onChange: (selectedDates) => {
      const selectedDate = selectedDates[0];
      if (selectedDate && startDatePicker.value) {
        startDatePicker.value.set("maxDate", selectedDate);
      }
    },
  });
};

// 날짜 설정 함수
const setYesterday = () => {
  const yesterday = new Date();
  yesterday.setDate(yesterday.getDate() - 1);
  if (startDatePicker.value && endDatePicker.value) {
    startDatePicker.value.setDate(yesterday);
    endDatePicker.value.setDate(yesterday);
    formData.value.startDate = startDatePicker.value.formatDate(yesterday, "Y-m-d");
    formData.value.endDate = endDatePicker.value.formatDate(yesterday, "Y-m-d");
  }
};

const setToday = () => {
  const today = new Date();
  if (startDatePicker.value && endDatePicker.value) {
    startDatePicker.value.setDate(today);
    endDatePicker.value.setDate(today);
    formData.value.startDate = startDatePicker.value.formatDate(today, "Y-m-d");
    formData.value.endDate = endDatePicker.value.formatDate(today, "Y-m-d");
  }
};

const setThisWeek = () => {
  const today = new Date();
  const firstDayOfWeek = new Date(today);
  firstDayOfWeek.setDate(today.getDate() - today.getDay());
  const lastDayOfWeek = new Date(firstDayOfWeek);
  lastDayOfWeek.setDate(firstDayOfWeek.getDate() + 6);

  if (startDatePicker.value && endDatePicker.value) {
    startDatePicker.value.setDate(firstDayOfWeek);
    endDatePicker.value.setDate(lastDayOfWeek);
    formData.value.startDate = startDatePicker.value.formatDate(firstDayOfWeek, "Y-m-d");
    formData.value.endDate = endDatePicker.value.formatDate(lastDayOfWeek, "Y-m-d");
  }
};

const setThisMonth = () => {
  const today = new Date();
  const firstDayOfMonth = new Date(today.getFullYear(), today.getMonth(), 1);
  const lastDayOfMonth = new Date(today.getFullYear(), today.getMonth() + 1, 0);

  if (startDatePicker.value && endDatePicker.value) {
    startDatePicker.value.setDate(firstDayOfMonth);
    endDatePicker.value.setDate(lastDayOfMonth);
    formData.value.startDate = startDatePicker.value.formatDate(firstDayOfMonth, "Y-m-d");
    formData.value.endDate = endDatePicker.value.formatDate(lastDayOfMonth, "Y-m-d");
  }
};

// 날짜 포맷팅
const formatDate = (dateString) => {
  if (!dateString) return "";
  return dateString.replace("T", " ").slice(0, 19);
};

// 체크박스 상태
const selectedItems = ref(new Set());
const selectAll = ref(false);

// API 호출
const fetchCategories = async () => {
  try {
    const params = new URLSearchParams({
      page: (currentPage.value - 1).toString(),
      size: pageSize.value.toString(),
      sort: "id",
    });

    if (formData.value.name) params.append("name", formData.value.name);
    if (formData.value.level) params.append("level", formData.value.level);
    if (formData.value.dateType) params.append("dateType", formData.value.dateType);
    if (formData.value.startDate) {
      const date = new Date(formData.value.startDate);
      params.append("startDate", date.toISOString());
    }
    if (formData.value.endDate) {
      const date = new Date(formData.value.endDate + "T23:59:59");
      params.append("endDate", date.toISOString());
    }

    console.log("Search params:", {
      startDate: formData.value.startDate,
      endDate: formData.value.endDate,
    });

    const data = await use$Fetch(`/admin/categories?${params.toString()}`);
    categories.value = data.categories;
    totalElements.value = data.totalElements;
    totalPages.value = data.totalPages;
  } catch (error) {
    console.error("Error fetching categories:", error);
  }
};

// 삭제 처리
const handleDelete = async (categoryId) => {
  if (confirm("이 카테고리를 삭제하시겠습니까?")) {
    try {
      await use$Fetch(`/admin/categories/${categoryId}`, {
        method: "DELETE",
      });
      alert("카테고리가 삭제되었습니다.");
      await fetchCategories();
    } catch (error) {
      console.error("Error deleting category:", error);
      alert("카테고리 삭제에 실패했습니다.");
    }
  }
};

// 일괄 삭제 처리
const handleBulkDelete = async () => {
  if (selectedItems.value.size === 0) {
    alert("삭제할 카테고리를 선택해주세요.");
    return;
  }

  if (!confirm("선택한 카테고리를 모두 삭제하시겠습니까?")) {
    return;
  }

  try {
    const deletePromises = Array.from(selectedItems.value).map((id) =>
      use$Fetch(`/admin/categories/${id}`, {
        method: "DELETE",
      })
    );

    await Promise.all(deletePromises);
    alert("선택한 카테고리가 삭제되었습니다.");
    selectedItems.value.clear();
    selectAll.value = false;
    await fetchCategories();
  } catch (error) {
    console.error("Error deleting categories:", error);
    alert("카테고리 삭제 중 오류가 발생했습니다.");
  }
};

// 체크박스 핸들러
const handleSelectAll = (event) => {
  const checked = event.target.checked;
  if (checked) {
    selectedItems.value = new Set(categories.value.map((cat) => cat.id));
  } else {
    selectedItems.value.clear();
  }
  selectAll.value = checked;
};

const handleSelectItem = (event, categoryId) => {
  if (selectedItems.value.has(categoryId)) {
    selectedItems.value.delete(categoryId);
  } else {
    selectedItems.value.add(categoryId);
  }
  selectAll.value = selectedItems.value.size === categories.value.length;
};

// 검색 핸들러
const handleSearch = (event) => {
  event.preventDefault();
  currentPage.value = 1;
  fetchCategories();
};

// 페이지 크기 변경
const pageSizeChange = (event) => {
  pageSize.value = parseInt(event.target.value);
  currentPage.value = 1;
  fetchCategories();
};

// 페이지 변경 핸들러
const pageChange = (newPage) => {
  if (newPage < 1 || newPage > totalPages.value) return;
  currentPage.value = newPage;
  fetchCategories();
};

// 페이지 번호 계산
const visiblePages = computed(() => {
  const startPage = Math.floor((currentPage.value - 1) / 5) * 5 + 1;
  const endPage = Math.min(startPage + 4, totalPages.value);
  return Array.from({ length: endPage - startPage + 1 }, (_, i) => startPage + i);
});

// 초기화
onMounted(() => {
  fetchCategories();
  waitForFlatpickr(() => {
    initializeFlatpickr();
  });
});
</script>

<template>
  <main>
    <h1 class="title">카테고리 목록</h1>
    <ul class="breadcrumbs">
      <li><a href="#">FUPTO</a></li>
      <li class="divider">/</li>
      <li><a href="#">카테고리</a></li>
      <li class="divider">/</li>
      <li><a href="#" class="active">카테고리 목록</a></li>
    </ul>

    <!-- 검색 폼 -->
    <div class="card">
      <div class="card-body">
        <form @submit="handleSearch">
          <table class="table">
            <tbody>
              <tr>
                <th>카테고리명</th>
                <td>
                  <input type="text" v-model="formData.name" class="input-text" />
                </td>
              </tr>
              <tr>
                <th>레벨</th>
                <td>
                  <select v-model="formData.level" class="select">
                    <option value="">전체</option>
                    <option value="1">1차 카테고리</option>
                    <option value="2">2차 카테고리</option>
                    <option value="3">3차 카테고리</option>
                  </select>
                </td>
              </tr>
              <tr>
                <th>날짜</th>
                <td>
                  <select v-model="formData.dateType" class="select">
                    <option value="reg" selected>등록일</option>
                    <option value="mod">수정일</option>
                  </select>
                  <input ref="startDateInput" v-model="formData.startDate" type="text" class="input-text date" readonly />
                  <button type="button" class="btn-calendar" @click="() => startDatePicker?.open()">&#x1F4C5;</button>
                  ~
                  <input ref="endDateInput" v-model="formData.endDate" type="text" class="input-text date" readonly />
                  <button type="button" class="btn-calendar mr-2" @click="() => endDatePicker?.open()">&#x1F4C5;</button>
                  <button type="button" class="btn btn-primary mr-2" @click="setYesterday">어제</button>
                  <button type="button" class="btn btn-primary mr-2" @click="setToday">오늘</button>
                  <button type="button" class="btn btn-primary mr-2" @click="setThisWeek">이번 주</button>
                  <button type="button" class="btn btn-primary" @click="setThisMonth">이번 달</button>
                </td>
              </tr>
            </tbody>
          </table>
          <div class="text-center">
            <button type="submit" class="btn btn-primary btn-search">검 색</button>
          </div>
        </form>
      </div>
    </div>

    <!-- 카테고리 목록 -->
    <div class="card">
      <div class="card-body">
        <div class="d-flex justify-content-between">
          <div>
            <button class="btn btn-outline-danger" @click="handleBulkDelete">선택 삭제</button>
          </div>
          <div class="d-flex align-items-center">
            <select class="select mr-2" v-model="pageSize" @change="pageSizeChange">
              <option :value="10">10</option>
              <option :value="20">20</option>
              <option :value="50">50</option>
            </select>
            <router-link to="/admin/categories/reg" class="btn btn-primary">+ Add Category</router-link>
          </div>
        </div>

        <table class="table category-list-table">
          <thead>
            <tr>
              <th>
                <input type="checkbox" :checked="selectAll" @change="handleSelectAll" class="pl-checkbox" />
              </th>
              <th>ID</th>
              <th>레벨</th>
              <th>카테고리명</th>
              <th>상위 카테고리</th>
              <th>등록일</th>
              <th>수정일</th>
              <th>관리</th>
            </tr>
          </thead>
          <tbody>
            <tr v-for="category in categories" :key="category.id">
              <td>
                <input
                  type="checkbox"
                  :checked="selectedItems.has(category.id)"
                  @change="(e) => handleSelectItem(e, category.id)"
                  class="pl-checkbox"
                />
              </td>
              <td>{{ category.id }}</td>
              <td>
                <span class="badge-pill" :class="'level-' + category.level"> Level {{ category.level }} </span>
              </td>
              <td>
                <span
                  :style="{
                    fontWeight: category.level === 1 ? 'bold' : 'normal',
                  }"
                >
                  {{ category.name }}
                </span>
              </td>
              <td>
                <span class="badge-pill badge-light text-md">{{ category.parentName || "-" }}</span>
              </td>
              <td>{{ formatDate(category.createDate) }}</td>
              <td>{{ formatDate(category.updateDate) }}</td>
              <td>
                <NuxtLink :to="`/admin/categories/${category.id}/edit`" class="btn btn-outline-secondary btn-sm">
                  <i class="bx bxs-pencil"></i>
                </NuxtLink>
                <button class="btn btn-outline-danger btn-sm" @click="() => handleDelete(category.id)">
                  <i class="bx bx-trash"></i>
                </button>
              </td>
            </tr>
          </tbody>
        </table>

        <div class="pagination-container">
          <nav aria-label="Page navigation">
            <ul class="pagination justify-content-center">
              <li class="page-item" :class="{ disabled: currentPage === 1 }">
                <a class="page-link" href="#" @click.prevent="pageChange(1)">&lt;&lt;</a>
              </li>
              <li class="page-item" :class="{ disabled: currentPage === 1 }">
                <a class="page-link" href="#" @click.prevent="pageChange(currentPage - 1)">이전</a>
              </li>
              <li class="page-item" v-for="page in visiblePages" :key="page" :class="{ active: currentPage === page }">
                <a class="page-link" href="#" @click.prevent="pageChange(page)">{{ page }}</a>
              </li>
              <li class="page-item" :class="{ disabled: currentPage === totalPages }">
                <a class="page-link" href="#" @click.prevent="pageChange(currentPage + 1)">다음</a>
              </li>
              <li class="page-item" :class="{ disabled: currentPage === totalPages }">
                <a class="page-link" href="#" @click.prevent="pageChange(totalPages)">&gt;&gt;</a>
              </li>
            </ul>
          </nav>
        </div>
      </div>
    </div>
  </main>
</template>
