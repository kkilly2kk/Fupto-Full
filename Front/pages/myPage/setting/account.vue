<script setup>
import { ref, computed } from "vue";
import { use$Fetch } from "@/composables/use$Fetch";
import useUserDetails from "~/composables/useUserDetails";

useHead({
  link: [{ rel: "stylesheet", href: "/css/myAccount.css" }],
});

const userDetails = useUserDetails();
const username = computed(() => userDetails.username.value);
const id = computed(() => userDetails.id.value);
const currentPassword = ref("");
const newPassword = ref("");
const confirmPassword = ref("");
const nickname = ref("");
const email = ref("");
const birthDate = ref("");
const phone = ref("");
const gender = ref("");

const getMember = async () => {
  try {
    if (id.value) {
      const response = await use$Fetch(`/user/member/${id.value}`, {
        method: "GET",
      });
      const data = response;
      if (data) {
        nickname.value = data.nickname;
        email.value = data.email;
        phone.value = data.tel;
        gender.value = data.gender;
        birthDate.value = data.birthDate ? new Date(data.birthDate).toISOString().split("T")[0] : "";
      }
    }
  } catch (error) {
    console.error("Failed to load user:", error);
  }
};

const updateMember = async () => {
  try {
    const response = await use$Fetch("/user/member/edit", {
      method: "PUT",
      body: {
        password: currentPassword.value,
        newPassword: newPassword.value,
        confirmPassword: confirmPassword.value,
        nickname: nickname.value,
        email: email.value,
        birthDate: birthDate.value,
        tel: phone.value,
      },
    });

    if (response) {
      alert("회원정보가 수정되었습니다.");
    }
  } catch (error) {
    if (error.response?.status === 401) {
      alert("현재 비밀번호가 일치하지 않습니다.");
    } else {
      alert("회원정보 수정에 실패했습니다.");
    }
  }
};

onMounted(() => {
  getMember();
});
</script>

<template>
  <div class="edit-member-container">
    <div class="member-info">
      <h2>회원 정보 수정</h2>

      <!-- 기본 정보 섹션 -->
      <section class="info-section">
        <h3>기본 정보</h3>
        <div class="info-grid">
          <div class="info-item">
            <label>아이디</label>
            <input type="text" v-model="username" disabled class="readonly-input" />
          </div>

          <div class="info-item">
            <label>성별</label>
            <input type="text" :value="gender" disabled class="readonly-input" />
          </div>

          <div class="info-item">
            <label>닉네임</label>
            <input type="text" v-model="nickname" />
          </div>

          <div class="info-item">
            <label>이메일</label>
            <input type="email" v-model="email" />
          </div>

          <div class="info-item">
            <label>전화번호</label>
            <input type="tel" v-model="phone" placeholder="'-' 없이 숫자만 입력" />
          </div>

          <div class="info-item">
            <label>생년월일</label>
            <input type="date" v-model="birthDate" />
          </div>
        </div>
      </section>

      <!-- 비밀번호 변경 섹션 -->
      <section class="password-section">
        <h3>비밀번호 변경</h3>
        <div class="password-grid">
          <div class="info-item">
            <label>* 현재 비밀번호</label>
            <input type="password" v-model="currentPassword" placeholder="현재 비밀번호를 입력하세요" />
          </div>

          <div class="info-item">
            <label>새로운 비밀번호</label>
            <input type="password" v-model="newPassword" placeholder="새 비밀번호" />
          </div>

          <div class="info-item">
            <label>새로운 비밀번호 확인</label>
            <input type="password" v-model="confirmPassword" placeholder="새 비밀번호 확인" />
          </div>
        </div>
      </section>

      <div class="button-group">
        <button @click="updateMember" class="update-btn">정보 수정</button>
      </div>
    </div>
  </div>
</template>
