<script setup>
import { ref, onMounted } from "vue";
import { use$Fetch } from "~/composables/use$Fetch";

useHead({
  link: [{ rel: "stylesheet", href: "/css/admin/report.css" }],
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

// 회원 상태 관리 함수들
const handleActiveChange = async () => {
  try {
    await use$Fetch(`/admin/members/${member.value.id}/active`, {
      method: "PATCH",
      params: { active: !member.value.active },
    });
    await fetchMember();
  } catch (error) {
    console.error("상태 변경 실패:", error);
    alert("회원 상태 변경에 실패했습니다.");
  }
};

const handleRoleChange = async (event) => {
  try {
    await use$Fetch(`/admin/members/${member.value.id}/role`, {
      method: "PATCH",
      body: event.target.value,
    });
    await fetchMember();
  } catch (error) {
    console.error("권한 변경 실패:", error);
    alert("회원 권한 변경에 실패했습니다.");
  }
};

const handleDeleteMember = async () => {
  if (!confirm("정말로 이 회원을 삭제하시겠습니까? 이 작업은 되돌릴 수 없습니다.")) {
    return;
  }

  try {
    await use$Fetch(`/admin/members/${member.value.id}`, {
      method: "DELETE",
    });
    navigateTo("/admin/members");
  } catch (error) {
    console.error("회원 삭제 실패:", error);
    alert("회원 삭제에 실패했습니다.");
  }
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

    <div class="data">
      <!-- 기본 정보 -->
      <div class="content-data">
        <div class="card">
          <div class="card-body">
            <h2 class="section-title">기본 정보</h2>
            <table class="report member">
              <tbody>
                <tr>
                  <th>프로필 이미지</th>
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
                  <td>{{ calculateAge(member.birthDate) }}</td>
                </tr>
                <tr>
                  <th>전화번호</th>
                  <td>{{ member.tel || "-" }}</td>
                </tr>
              </tbody>
            </table>
          </div>
        </div>
      </div>

      <!-- 계정 상태 및 관리 -->
      <div class="content-data">
        <div class="card">
          <div class="card-body">
            <h2 class="section-title">계정 상태 및 관리</h2>
            <table class="report member">
              <tbody>
                <tr>
                  <th>계정 상태</th>
                  <td>
                    <label class="pl-switch">
                      <input type="checkbox" v-model="member.active" @change="handleActiveChange" :disabled="!member.state" />
                      <span class="pl-slider round"></span>
                    </label>
                    {{ member.active ? "활성" : "정지" }}
                  </td>
                </tr>
                <tr>
                  <th>회원 유형</th>
                  <td>
                    <select
                      class="role-select"
                      v-model="member.role"
                      @change="handleRoleChange"
                      :disabled="!member.state || !member.active"
                    >
                      <option value="ROLE_USER">일반회원</option>
                      <option value="ROLE_ADMIN">관리자</option>
                    </select>
                  </td>
                </tr>
                <tr>
                  <th>회원 상태</th>
                  <td>{{ member.state ? "정상" : "탈퇴" }}</td>
                </tr>
              </tbody>
            </table>
          </div>
        </div>
      </div>

      <!-- 활동 정보 -->
      <div class="content-data">
        <div class="card">
          <div class="card-body">
            <h2 class="section-title">활동 정보</h2>
            <table class="report member">
              <tbody>
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
              </tbody>
            </table>
          </div>
        </div>
      </div>

      <!-- 관리 버튼 -->
      <div class="content-data">
        <div class="card">
          <div class="card-body text-right">
            <button class="btn btn-outline-danger" @click="handleDeleteMember">회원 삭제</button>
            <button class="btn btn-outline-secondary ml-2" @click="$router.push('/admin/members')">목록으로</button>
          </div>
        </div>
      </div>
    </div>
  </main>
</template>

<style scoped>
.section-title {
  font-size: 1.2rem;
  font-weight: 500;
  margin-bottom: 1rem;
  padding-bottom: 0.5rem;
  border-bottom: 2px solid #f0f0f0;
}

.profile-img {
  width: 100px;
  height: 100px;
  border-radius: 50%;
  object-fit: cover;
}

.role-select {
  padding: 4px 8px;
  border: 1px solid #dee2e6;
  border-radius: 4px;
  background-color: #fff;
}

.role-select:disabled {
  background-color: #e9ecef;
  cursor: not-allowed;
}

.pl-switch {
  position: relative;
  display: inline-block;
  width: 35px;
  height: 20px;
  margin-right: 10px;
}

.pl-switch input {
  opacity: 0;
  width: 0;
  height: 0;
}

.pl-slider {
  position: absolute;
  cursor: pointer;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background-color: #ccc;
  transition: 0.4s;
  border-radius: 34px;
}

.pl-slider:before {
  position: absolute;
  content: "";
  height: 14px;
  width: 14px;
  left: 3px;
  bottom: 3px;
  background-color: white;
  transition: 0.4s;
  border-radius: 50%;
}

input:checked + .pl-slider {
  background-color: #2196f3;
}

input:checked + .pl-slider:before {
  transform: translateX(15px);
}

input:disabled + .pl-slider {
  background-color: #e9ecef;
  cursor: not-allowed;
}

.text-right {
  text-align: right;
}

.ml-2 {
  margin-left: 0.5rem;
}
</style>
