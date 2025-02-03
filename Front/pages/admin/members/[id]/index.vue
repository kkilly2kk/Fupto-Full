<script setup>
import { ref, onMounted } from "vue";
import { use$Fetch } from "~/composables/use$Fetch";

useHead({
  link: [{ rel: "stylesheet", href: "/css/admin/member-detail.css" }],
});

const route = useRoute();
const member = ref(null);
const config = useRuntimeConfig();

// 프로필 이미지 URL 계산
const profileImageUrl = computed(() => {
  if (member.value?.profileImg) {
    return `${config.public.apiBase}/${member.value.profileImg}`;
  }
  return "/imgs/user/default-profile.jpg";
});

// 회원 정보 조회
const fetchMember = async () => {
  try {
    const id = route.params.id;
    const response = await use$Fetch(`/admin/members/${id}`);
    member.value = response;
  } catch (error) {
    console.error("회원 정보 조회 실패:", error);
    alert("회원 정보를 불러오는데 실패했습니다.");
  }
};

// 날짜 포맷팅
const formatDate = (date) => {
  if (!date) return "-";
  return new Date(date).toLocaleDateString("ko-KR", {
    year: "numeric",
    month: "2-digit",
    day: "2-digit",
    hour: "2-digit",
    minute: "2-digit",
  });
};

// 나이 계산
const calculateAge = (birthDate) => {
  if (!birthDate) return "-";
  const today = new Date();
  const birth = new Date(birthDate);
  let age = today.getFullYear() - birth.getFullYear();
  const monthDiff = today.getMonth() - birth.getMonth();

  if (monthDiff < 0 || (monthDiff === 0 && today.getDate() < birth.getDate())) {
    age--;
  }
  return age;
};

const handleDeleteMember = async () => {
  if (!confirm("정말로 이 회원을 삭제하시겠습니까? 이 작업은 되돌릴 수 없습니다.")) {
    return;
  }

  try {
    await use$Fetch(`/admin/members/${member.value.id}`, {
      method: "DELETE",
    });
    navigateTo("/admin/members/list");
  } catch (error) {
    console.error("회원 삭제 실패:", error);
    alert("회원 삭제에 실패했습니다.");
  }
};

const handleBackToList = () => {
  localStorage.setItem("fromMemberDetail", "true");
  navigateTo("/admin/members/list");
};

onMounted(() => {
  fetchMember();
});
</script>

<template>
  <main v-if="member">
    <h1 class="title">회원 상세 정보</h1>
    <ul class="breadcrumbs">
      <li><a href="#">FUPTO</a></li>
      <li class="divider">/</li>
      <li><a href="#">회원</a></li>
      <li class="divider">/</li>
      <li><a href="#" class="active">회원 상세</a></li>
    </ul>

    <div class="card-container">
      <!-- 기본 정보 -->
      <div class="card">
        <h2 class="section-title">기본 정보</h2>
        <table class="table">
          <tbody>
            <tr>
              <th>프로필<br />이미지</th>
              <td>
                <img :src="profileImageUrl" :alt="member.nickname" class="profile-img" />
              </td>
            </tr>
            <tr>
              <th>아이디</th>
              <td>{{ member.userId }}</td>
            </tr>
            <tr>
              <th>닉네임</th>
              <td>{{ member.nickname }}</td>
            </tr>
            <tr>
              <th>이메일</th>
              <td>{{ member.email }}</td>
            </tr>
            <tr>
              <th>가입 경로</th>
              <td>{{ member.provider || "FUPTO" }}</td>
            </tr>
            <tr>
              <th>성별</th>
              <td>{{ member.gender || "-" }}</td>
            </tr>
            <tr>
              <th>나이</th>
              <td>{{ calculateAge(member.birthDate) }}세 ({{ member.birthDate }})</td>
            </tr>
            <tr>
              <th>전화번호</th>
              <td>{{ member.tel || "-" }}</td>
            </tr>
          </tbody>
        </table>
      </div>

      <!-- 계정 및 활동 정보 -->
      <div class="card">
        <h2 class="section-title">계정 및 활동 정보</h2>
        <table class="table">
          <tbody>
            <tr>
              <th>계정 상태</th>
              <td>
                <div class="pl-switch" :class="{ 'switch-active': member.active }">
                  <span class="pl-slider round"></span>
                </div>
                <span class="status-text">{{ member.active ? "활성" : "정지" }}</span>
              </td>
            </tr>
            <tr>
              <th>회원 유형</th>
              <td class="member-role">
                {{ member.role === "ROLE_ADMIN" ? "관리자" : "일반 회원" }}
              </td>
            </tr>
            <tr>
              <th>회원 상태</th>
              <td>{{ member.state ? "정상" : "탈퇴" }}</td>
            </tr>
            <tr>
              <th>가입일</th>
              <td>{{ formatDate(member.createDate) }}</td>
            </tr>
            <tr>
              <th>최근 수정일</th>
              <td>{{ formatDate(member.updateDate) }}</td>
            </tr>
            <tr>
              <th>최근 로그인</th>
              <td>{{ formatDate(member.loginDate) }}</td>
            </tr>
            <tr>
              <th>게시글 수</th>
              <td>{{ member.boardCount || 0 }}</td>
            </tr>
            <tr>
              <th>찜 목록 수</th>
              <td>{{ member.favoriteCount || 0 }}</td>
            </tr>
            <tr>
              <td colspan="2">
                <div class="button-container">
                  <button class="btn btn-outline-danger" @click="handleDeleteMember">회원 삭제</button>
                  <button class="btn btn-outline-secondary" @click="handleBackToList">목록으로</button>
                </div>
              </td>
            </tr>
          </tbody>
        </table>
      </div>
    </div>
  </main>
</template>
