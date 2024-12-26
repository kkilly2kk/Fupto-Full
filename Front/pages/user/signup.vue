<script setup>
import { ref, reactive } from "vue";
import { useRouter } from "vue-router";
import { useAuthFetch } from "~/composables/useAuthFetch.js";

const router = useRouter();

const email = ref("");
const name = ref("");
const userId = ref("");
const phone = ref("");
const password = ref("");
const confirmPassword = ref("");
const gender = ref("");
const agreement = ref(false);
const birthDate = ref("");

const errors = reactive({
  email: "",
  name: "",
  userId: "",
  phone: "",
  password: "",
  confirmPassword: "",
  gender: "",
  agreement: "",
});

const validateEmail = () => {
  if (!email.value) return;

  const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
  if (!emailRegex.test(email.value)) {
    errors.email = "유효한 이메일 주소를 입력해주세요.";
  } else {
    errors.email = "";
  }
};

const validateName = () => {
  if (!name.value) return;

  const nameRegex = /^[가-힣a-zA-Z]{2,20}$/;
  if (!nameRegex.test(name.value)) {
    errors.name = "이름은 2~20자의 한글 또는 영문만 가능합니다.";
  } else {
    errors.name = "";
  }
};

const validateUserId = () => {
  if (!userId.value) return;

  const userIdRegex = /^[a-z0-9]{4,20}$/;
  if (!userIdRegex.test(userId.value)) {
    errors.userId = "아이디는 4~20자의 영문 소문자, 숫자만 가능합니다.";
  } else {
    errors.userId = "";
  }
};

const validatePhone = () => {
  if (!phone.value) return;

  if (!phone.value.match(/^\d{10,11}$/)) {
    errors.phone = "'-' 없이 숫자 10~11자리를 입력해주세요.";
  } else {
    errors.phone = "";
  }
};

const validatePassword = () => {
  if (!password.value) return;

  const passwordRegex = /^(?=.*[A-Za-z])(?=.*\d)(?=.*[@$!%*#?&])[A-Za-z\d@$!%*#?&]{8,16}$/;
  if (!passwordRegex.test(password.value)) {
    errors.password = "영문, 숫자, 특수문자를 포함하여 8~16자로 입력해주세요.";
  } else {
    errors.password = "";
  }
};

const validateConfirmPassword = () => {
  if (!confirmPassword.value) return;

  if (password.value !== confirmPassword.value) {
    errors.confirmPassword = "비밀번호가 일치하지 않습니다.";
  } else {
    errors.confirmPassword = "";
  }
};

const handleSubmit = async () => {
  validateEmail();
  validateName();
  validateUserId();
  validatePhone();
  validatePassword();
  validateConfirmPassword();

  if (!gender.value) {
    errors.gender = "성별을 선택해주세요.";
  } else {
    errors.gender = "";
  }

  if (!agreement.value) {
    errors.agreement = "이용약관에 동의해주세요.";
  } else {
    errors.agreement = "";
  }

  if (
    !errors.email &&
    !errors.name &&
    !errors.userId &&
    !errors.phone &&
    !errors.password &&
    !errors.confirmPassword &&
    !errors.gender &&
    !errors.agreement
  ) {
    try {
      const requestBody = {
        userId: userId.value,
        nickname: name.value,
        password: password.value,
        birthDate: birthDate.value,
        gender: gender.value === "male" ? "남성" : "여성",
        tel: phone.value,
        email: email.value,
      };

      const { data, error } = await useAuthFetch("/auth/signup", {
        method: "POST",
        body: requestBody,
      });
      if (error.value) {
        console.error("Registration error:", error.value);
        alert(`회원가입 실패: ${error.value.message || "알 수 없는 오류가 발생했습니다."}`);
      } else {
        console.log("Response data:", data.value);
        alert("회원가입이 완료되었습니다!");
        router.push("/user/signin");
      }
    } catch (error) {
      console.error("Unexpected error:", error);
      alert("회원가입 중 예기치 않은 오류가 발생했습니다. 다시 시도해주세요.");
    }
  }
};
</script>

<template>
  <div class="join-container">
    <h1 class="join-title">회원 가입을 위해 정보를 입력해주세요.</h1>
    <form class="join-form" @submit.prevent="handleSubmit">
      <div class="form-group">
        <label for="userId">*아이디</label>
        <input
          type="text"
          id="userId"
          v-model="userId"
          @input="validateUserId"
          @blur="validateUserId"
          placeholder="영문 소문자, 숫자 4~20자"
        />
        <p class="validation-error" v-if="errors.userId">{{ errors.userId }}</p>
      </div>

      <div class="form-group">
        <label for="name">*이름</label>
        <input
          type="text"
          id="name"
          v-model="name"
          @input="validateName"
          @blur="validateName"
          placeholder="한글 또는 영문 2~20자"
        />
        <p class="validation-error" v-if="errors.name">{{ errors.name }}</p>
      </div>

      <div class="form-group">
        <label for="email">*이메일</label>
        <input
          type="email"
          id="email"
          v-model="email"
          @input="validateEmail"
          @blur="validateEmail"
          placeholder="예: email@example.com"
        />
        <p class="validation-error" v-if="errors.email">{{ errors.email }}</p>
      </div>

      <div class="form-group phone-group">
        <label for="phone">*전화번호</label>
        <div class="phone-input-group">
          <input
            type="tel"
            id="phone"
            v-model="phone"
            @input="validatePhone"
            @blur="validatePhone"
            placeholder="'-' 없이 숫자만 입력"
          />
          <button type="button" class="phone-certify-button">인증</button>
        </div>
        <p class="validation-error" v-if="errors.phone">{{ errors.phone }}</p>
      </div>

      <div class="form-group">
        <label for="password">*비밀번호</label>
        <input
          type="password"
          id="password"
          v-model="password"
          @input="validatePassword"
          @blur="validatePassword"
          placeholder="영문, 숫자, 특수문자 조합 8~16자"
        />
        <p class="validation-error" v-if="errors.password">{{ errors.password }}</p>
      </div>

      <div class="form-group">
        <label for="confirm-password">*비밀번호 확인</label>
        <input
          type="password"
          id="confirm-password"
          v-model="confirmPassword"
          @input="validateConfirmPassword"
          @blur="validateConfirmPassword"
          placeholder="비밀번호를 다시 입력해주세요"
        />
        <p class="validation-error" v-if="errors.confirmPassword">{{ errors.confirmPassword }}</p>
      </div>

      <div class="form-group">
        <label>*성별</label>
        <div class="gender-group">
          <input type="radio" id="male" name="gender" value="male" v-model="gender" />
          <label for="male" class="gender-label male">남자</label>

          <input type="radio" id="female" name="gender" value="female" v-model="gender" />
          <label for="female" class="gender-label female">여자</label>
        </div>
        <p class="validation-error" v-if="errors.gender">{{ errors.gender }}</p>
      </div>

      <div class="form-group">
        <input type="checkbox" id="agreement" v-model="agreement" />
        <label for="agreement">이용약관 개인정보 수집 및 이용, 마케팅 활용 선택에 모두 동의합니다.</label>
        <p class="validation-error" v-if="errors.agreement">{{ errors.agreement }}</p>
      </div>

      <button type="submit" class="submit-button">가입하기</button>
    </form>
  </div>
</template>

<style scoped>
@import url("@/public/css/user-join.css");
</style>
