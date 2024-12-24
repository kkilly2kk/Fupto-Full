<script setup>
import { ref } from "vue";

useHead({
  link: [{ rel: "stylesheet", href: "/css/comment-item.css" }],
});

const props = defineProps({
  comment: {
    type: Object,
    required: true,
  },
  userId: {
    type: [Number, String],
    required: true,
  },
  boardId: {
    type: Number,
    required: true,
  },
});

const emit = defineEmits(["refresh"]);

const replyContent = ref("");
const editContent = ref("");
const isEditing = ref(false);
const isReplying = ref(false);

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

  return `${ymd} ${time}`;
};

// 답글 작성
const submitReply = async () => {
  if (!replyContent.value.trim()) return;

  const requestData = {
    boardId: props.boardId,
    memberId: props.userId,
    content: replyContent.value,
    parentId: props.comment.id,
  };

  try {
    await use$Fetch("/comments", {
      method: "POST",
      body: requestData,
    });
    replyContent.value = "";
    isReplying.value = false;
    emit("refresh");
  } catch (error) {
    console.error("답글 작성 실패:", error);
    console.error("Request Data was:", requestData);
  }
};

// 댓글 수정
const submitEdit = async () => {
  if (!editContent.value.trim()) return;

  try {
    await use$Fetch(`/comments/${props.comment.id}`, {
      method: "PATCH",
      body: {
        memberId: props.userId,
        content: editContent.value,
      },
    });
    isEditing.value = false;
    emit("refresh");
  } catch (error) {
    console.error("댓글 수정 실패:", error);
  }
};

// 댓글 삭제
const deleteComment = async () => {
  if (!confirm("댓글을 삭제하시겠습니까?")) return;

  try {
    await use$Fetch(`/comments/${props.comment.id}?memberId=${props.userId}`, {
      method: "DELETE",
    });
    emit("refresh");
  } catch (error) {
    console.error("댓글 삭제 실패:", error);
  }
};

// 수정 모드 시작
const startEdit = () => {
  isEditing.value = true;
  editContent.value = props.comment.content;
};

// 답글 모드 시작
const startReply = () => {
  isReplying.value = true;
  replyContent.value = "";
};
</script>

<template>
  <div class="comment-wrapper">
    <div class="comment" :class="{ reply: comment.parentId }">
      <div v-if="comment.parentId" class="reply-line"></div>
      <div class="comment-content">
        <div class="comment-header">
          <div class="user-info">
            <span class="author">{{ comment.member.nickname }}</span>
            <span class="date">{{ formatDate(comment.createDate) }}</span>
            <div class="button-group">
              <button @click="startReply">답글</button>
              <!-- userId를 숫자로 변환하여 비교 -->
              <template v-if="Number(userId) === comment.member.id">
                <button @click="startEdit">수정</button>
                <button @click="deleteComment">삭제</button>
              </template>
            </div>
          </div>
        </div>

        <!-- 수정 모드 -->
        <div v-if="isEditing" class="comment-body">
          <textarea v-model="editContent" class="edit-input"></textarea>
          <div class="edit-buttons">
            <button @click="submitEdit">수정완료</button>
            <button @click="isEditing = false">취소</button>
          </div>
        </div>

        <!-- 일반 모드 -->
        <div v-else class="comment-body">
          <p class="text">{{ comment.content }}</p>
        </div>

        <!-- 답글 작성 폼 -->
        <div v-if="isReplying" class="reply-form">
          <textarea v-model="replyContent" placeholder="답글을 입력하세요" class="reply-input"></textarea>
          <div class="reply-buttons">
            <button @click="submitReply">답글작성</button>
            <button @click="isReplying = false">취소</button>
          </div>
        </div>
      </div>
    </div>

    <!-- 대댓글 목록 -->
    <div v-if="comment.children && comment.children.length > 0" class="replies">
      <comment-item
        v-for="child in comment.children"
        :key="child.id"
        :comment="child"
        :user-id="userId"
        :board-id="boardId"
        @refresh="$emit('refresh')"
      />
    </div>
  </div>
</template>
