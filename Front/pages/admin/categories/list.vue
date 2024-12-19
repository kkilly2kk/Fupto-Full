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
  dateType: "reg", // 추가
  startDate: "", // 추가
  endDate: "", // 추가
});

// Flatpickr 관련 함수 추가
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

// 날짜 설정 함수들 추가
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

// 체크박스 상태
const selectedItems = ref(new Set());
const selectAll = ref(false);

// API 호출
const fetchCategories = async () => {
  try {
    const params = new URLSearchParams({
      page: currentPage.value.toString(),
      size: pageSize.value.toString(),
    });

    if (formData.value.name) params.append("name", formData.value.name);
    if (formData.value.level) params.append("level", formData.value.level);
    if (formData.value.dateType) params.append("dateType", formData.value.dateType);
    if (formData.value.startDate) params.append("startDate", formData.value.startDate);
    if (formData.value.endDate) params.append("endDate", formData.value.endDate);

    const data = await use$Fetch(`/admin/categories?${params.toString()}`);
    categories.value = data.categories;
    totalElements.value = data.totalElements;
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
            <button type="submit" class="btn btn-primary">검 색</button>
          </div>
        </form>
      </div>
    </div>

    <!-- 카테고리 목록 -->
    <div class="card">
      <div class="card-body">
        <div class="d-flex justify-content-between">
          <select class="select" v-model="pageSize" @change="pageSizeChange">
            <option :value="10">10</option>
            <option :value="20">20</option>
            <option :value="50">50</option>
          </select>
          <router-link to="/admin/categories/reg" class="btn btn-primary"> + Add Category </router-link>
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
                    marginLeft: (category.level - 1) * 20 + 'px',
                    fontWeight: category.level === 1 ? 'bold' : 'normal',
                  }"
                >
                  {{ category.name }}
                </span>
              </td>
              <td>{{ category.parentName || "-" }}</td>
              <td>{{ category.createDate }}</td>
              <td>{{ category.updateDate }}</td>
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
      </div>
    </div>
  </main>
</template>
