<script setup>
import { ref, reactive } from "vue";
import { useRouter } from "vue-router";
import debounce from "lodash/debounce";

const router = useRouter();

const email = ref("");
const nickname = ref("");
const userId = ref("");
const phone = ref("");
const password = ref("");
const confirmPassword = ref("");
const gender = ref("");
const birthDate = ref("");
const agreement = ref(false);

const validationStates = reactive({
  userId: null,
  nickname: null,
  email: null,
});

const validationMessages = reactive({
  userId: "",
  nickname: "",
  email: "",
});

const errors = reactive({
  email: "",
  nickname: "",
  userId: "",
  phone: "",
  password: "",
  confirmPassword: "",
  gender: "",
  agreement: "",
  birthDate: "",
});

const checking = reactive({
  userId: false,
  nickname: false,
  email: false,
});

const checkDuplicate = async (field, value) => {
  if (!value) return;

  checking[field] = true;
  try {
    const data = await use$Fetch(`/auth/check/${field}/${value}`);

    if (data.exists) {
      validationStates[field] = "invalid";
      validationMessages[field] = `이미 사용 중인 ${
        field === "userId" ? "아이디" : field === "nickname" ? "닉네임" : "이메일"
      }입니다.`;
    } else {
      validationStates[field] = "valid";
      validationMessages[field] = `사용 가능한 ${
        field === "userId" ? "아이디" : field === "nickname" ? "닉네임" : "이메일"
      }입니다.`;
    }
  } catch (error) {
    validationStates[field] = "invalid";
    validationMessages[field] = "서버 오류가 발생했습니다.";
  } finally {
    checking[field] = false;
  }
};

const debouncedCheckUserId = debounce(() => checkDuplicate("userId", userId.value), 500);
const debouncedCheckNickname = debounce(() => checkDuplicate("nickname", nickname.value), 500);
const debouncedCheckEmail = debounce(() => checkDuplicate("email", email.value), 500);

const validateUserId = () => {
  if (!userId.value) return;

  const userIdRegex = /^[a-z0-9]{4,20}$/;
  if (!userIdRegex.test(userId.value)) {
    validationStates.userId = "invalid";
    validationMessages.userId = "아이디는 4~20자의 영문 소문자, 숫자만 가능합니다.";
  } else {
    debouncedCheckUserId();
  }
};

const validateNickname = () => {
  if (!nickname.value) return;

  const nicknameRegex = /^[가-힣a-zA-Z]{2,20}$/;
  if (!nicknameRegex.test(nickname.value)) {
    validationStates.nickname = "invalid";
    validationMessages.nickname = "닉네임은 2~20자의 한글 또는 영문만 가능합니다.";
  } else {
    debouncedCheckNickname();
  }
};

const validateEmail = () => {
  if (!email.value) return;

  const emailRegex = /^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\.[a-zA-Z]{2,}$/;
  if (!emailRegex.test(email.value)) {
    validationStates.email = "invalid";
    validationMessages.email = "유효한 이메일 주소를 입력해주세요.";
    return;
  }

  // 도메인 부분 확인
  const domain = email.value.split("@")[1];
  if (!domain.includes(".") || domain.split(".").pop().length < 2) {
    validationStates.email = "invalid";
    validationMessages.email = "유효한 이메일 주소를 입력해주세요.";
    return;
  }

  debouncedCheckEmail();
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

const validateBirthDate = () => {
  if (!birthDate.value) {
    errors.birthDate = "생년월일을 선택해주세요.";
  } else {
    const selectedDate = new Date(birthDate.value);
    const today = new Date();

    if (selectedDate > today) {
      errors.birthDate = "미래의 날짜는 선택할 수 없습니다.";
    } else if (selectedDate.getFullYear() < 1900) {
      errors.birthDate = "유효하지 않은 생년월일입니다.";
    } else {
      errors.birthDate = "";
    }
  }
};

const handleSubmit = async () => {
  validateEmail();
  validateNickname();
  validateUserId();
  validatePhone();
  validatePassword();
  validateConfirmPassword();
  validateBirthDate();

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
    !errors.nickname &&
    !errors.userId &&
    !errors.phone &&
    !errors.password &&
    !errors.confirmPassword &&
    !errors.gender &&
    !errors.agreement &&
    !errors.birthDate
  ) {
    try {
      const requestBody = {
        userId: userId.value,
        nickname: nickname.value,
        password: password.value,
        birthDate: birthDate.value,
        gender: gender.value === "male" ? "남성" : "여성",
        tel: phone.value,
        email: email.value,
      };

      const response = await use$Fetch("/auth/signup", {
        method: "POST",
        body: requestBody,
      });

      console.log("Response data:", response);
      alert("회원가입이 완료되었습니다!");
      router.push("/user/joincomplete");
    } catch (error) {
      console.error("Registration error:", error);
      alert(`회원가입 실패: ${error.message || "알 수 없는 오류가 발생했습니다."}`);
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
        <p v-if="checking.userId" class="validation-checking">확인 중...</p>
        <p
          v-else-if="validationStates.userId"
          :class="['validation-message', validationStates.userId === 'valid' ? 'success' : 'error']"
        >
          {{ validationMessages.userId }}
        </p>
      </div>

      <div class="form-group">
        <label for="nickname">*닉네임</label>
        <input
          type="text"
          id="nickname"
          :value="nickname"
          @input="
            (e) => {
              nickname = e.target.value;
              validateNickname();
            }
          "
          @blur="validateNickname"
          placeholder="한글 또는 영문 2~20자"
        />
        <p v-if="checking.nickname" class="validation-checking">확인 중...</p>
        <p
          v-else-if="validationStates.nickname"
          :class="['validation-message', validationStates.nickname === 'valid' ? 'success' : 'error']"
        >
          {{ validationMessages.nickname }}
        </p>
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
        <p v-if="checking.email" class="validation-checking">확인 중...</p>
        <p
          v-else-if="validationStates.email"
          :class="['validation-message', validationStates.email === 'valid' ? 'success' : 'error']"
        >
          {{ validationMessages.email }}
        </p>
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
        <label for="birthDate">*생년월일</label>
        <input type="date" id="birthDate" v-model="birthDate" @input="validateBirthDate" @blur="validateBirthDate" required />
        <p class="validation-error" v-if="errors.birthDate">{{ errors.birthDate }}</p>
      </div>

      <div class="form-group">
        <label>*성별</label>
        <div class="gender-group">
          <input type="radio" id="male" name="gender" value="male" v-model="gender" />
          <label for="male" class="gender-label male">남성</label>

          <input type="radio" id="female" name="gender" value="female" v-model="gender" />
          <label for="female" class="gender-label female">여성</label>
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
@import url("@/public/css/sign-up.css");
</style>
