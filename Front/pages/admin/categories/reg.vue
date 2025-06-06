<script setup>
import { ref, watch } from "vue";
import { use$Fetch } from "~/composables/use$Fetch";

useHead({
  link: [{ rel: "stylesheet", href: "/css/admin/category-reg.css" }],
});

const category = ref({
  name: "",
  level: "",
  parentId: null,
});

const intermediateParents = ref([]);
const selectedIntermediateId = ref(null);
const availableParents = ref([]);
const errors = ref({});

// 1차 카테고리 목록 가져오기
const fetchFirstLevelCategories = async () => {
  try {
    const response = await use$Fetch("/admin/categories/parents/1");
    availableParents.value = response;
  } catch (error) {
    console.error("Error fetching first level categories:", error);
  }
};

// 2차 카테고리 목록 가져오기
const fetchIntermediateCategories = async (parentId) => {
  if (!parentId) {
    intermediateParents.value = [];
    return;
  }

  try {
    const response = await use$Fetch(`/admin/categories/children/${parentId}`);
    intermediateParents.value = response;
  } catch (error) {
    console.error("Error fetching intermediate categories:", error);
  }
};

// 레벨 변경 시 상위 카테고리 목록 갱신
watch(
  () => category.value.level,
  (newLevel) => {
    category.value.parentId = null;
    fetchParentCategories(newLevel);
  }
);

// 유효성 검사
const validateForm = () => {
  errors.value = {};

  if (!category.value.name?.trim()) {
    errors.value.name = "카테고리명을 입력하세요.";
  }

  if (!category.value.level) {
    errors.value.level = "레벨을 선택하세요.";
  }

  if (category.value.level === "2" && !category.value.parentId) {
    errors.value.parentId = "1차 카테고리를 선택하세요.";
  }

  if (category.value.level === "3") {
    if (!category.value.parentId) {
      errors.value.parentId = "1차 카테고리를 선택하세요.";
    }
    if (!selectedIntermediateId.value) {
      errors.value.intermediateId = "2차 카테고리를 선택하세요.";
    }
  }

  return Object.keys(errors.value).length === 0;
};

// 폼 제출
const handleSubmit = async () => {
  if (!validateForm()) {
    alert("필수 항목을 모두 입력하세요.");
    return;
  }

  try {
    await use$Fetch("/admin/categories", {
      method: "POST",
      body: {
        name: category.value.name,
        level: parseInt(category.value.level),
        parentId: category.value.level === "3" ? selectedIntermediateId.value : category.value.parentId,
      },
    });

    alert("카테고리가 등록되었습니다.");
    navigateTo("/admin/categories/list");
  } catch (error) {
    console.error("Error creating category:", error);
    alert("카테고리 등록에 실패했습니다.");
  }
};

// 취소
const handleCancel = () => {
  navigateTo("/admin/categories/list");
};

watch(
  () => category.value.level,
  (newLevel) => {
    category.value.parentId = null;
    selectedIntermediateId.value = null;
    intermediateParents.value = [];

    if (newLevel === "2" || newLevel === "3") {
      fetchFirstLevelCategories();
    } else {
      availableParents.value = [];
    }
  }
);

watch(
  () => category.value.parentId,
  (newParentId) => {
    if (category.value.level === "3") {
      selectedIntermediateId.value = null;
      fetchIntermediateCategories(newParentId);
    }
  }
);
</script>

<template>
  <main>
    <h1 class="title">카테고리 등록</h1>
    <ul class="breadcrumbs">
      <li><a href="#">FUPTO</a></li>
      <li class="divider">/</li>
      <li><a href="#">카테고리</a></li>
      <li class="divider">/</li>
      <li><a href="#" class="active">카테고리 등록</a></li>
    </ul>

    <div class="container">
      <div class="card">
        <div class="card-body">
          <form @submit.prevent="handleSubmit">
            <div class="form-group">
              <label>
                <span>* 등록할 카테고리 레벨</span>
                <select v-model="category.level" :class="{ error: errors.level }">
                  <option value="">선택하세요</option>
                  <option value="1">1차 카테고리</option>
                  <option value="2">2차 카테고리</option>
                  <option value="3">3차 카테고리</option>
                </select>
                <span v-if="errors.level" class="error-message">
                  {{ errors.level }}
                </span>
              </label>
            </div>

            <div class="form-group" v-if="category.level !== '1'">
              <label>
                <span>* 1차 카테고리</span>
                <select v-model="category.parentId" :class="{ error: errors.parentId }">
                  <option value="">선택하세요</option>
                  <option v-for="parent in availableParents" :key="parent.id" :value="parent.id">
                    {{ parent.name }}
                  </option>
                </select>
                <span v-if="errors.parentId" class="error-message">
                  {{ errors.parentId }}
                </span>
              </label>
            </div>

            <div class="form-group" v-if="category.level === '3'">
              <label>
                <span>* 2차 카테고리</span>
                <select v-model="selectedIntermediateId" :class="{ error: errors.intermediateId }" :disabled="!category.parentId">
                  <option value="">선택하세요</option>
                  <option v-for="cat in intermediateParents" :key="cat.id" :value="cat.id">
                    {{ cat.name }}
                  </option>
                </select>
                <span v-if="errors.intermediateId" class="error-message">
                  {{ errors.intermediateId }}
                </span>
              </label>
            </div>

            <div class="form-group">
              <label>
                <span>* 카테고리명</span>
                <input type="text" v-model="category.name" :class="{ error: errors.name }" placeholder="카테고리명 입력" />
                <span v-if="errors.name" class="error-message">
                  {{ errors.name }}
                </span>
              </label>
            </div>

            <div class="button-group">
              <button type="submit" class="btn btn-primary">등록</button>
              <button type="button" class="btn btn-secondary" @click="handleCancel">취소</button>
            </div>
          </form>
        </div>
      </div>
    </div>
  </main>
</template>
