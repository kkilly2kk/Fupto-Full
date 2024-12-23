<script setup>
import { ref, onMounted } from "vue";

const props = defineProps({
  boardId: {
    type: Number,
    required: true,
  },
});

useHead({
  link: [{ rel: "stylesheet", href: "/css/comment.css" }],
});

const comments = ref([]);
const newComment = ref("");
const replyContent = ref("");
const editingComment = ref(null);
const editContent = ref("");
const replyingTo = ref(null);

const userDetails = useUserDetails();

// 댓글 목록 조회
const fetchComments = async () => {
  try {
    console.log("게시글 ID:", props.boardId);
    const data = await use$Fetch(`/comments/board/${props.boardId}`);
    console.log("받은 댓글:", data);
    comments.value = data;
  } catch (error) {
    console.error("댓글 조회 실패:", error);
  }
};

// 댓글 작성
const submitComment = async () => {
  if (!newComment.value.trim()) return;

  try {
    const response = await use$Fetch("/comments", {
      method: "POST",
      body: {
        boardId: props.boardId,
        memberId: userDetails.id.value,
        content: newComment.value,
        parentId: null,
      },
    });

    console.log("댓글 작성 응답:", response);
    newComment.value = "";
    await fetchComments(); // 댓글 목록 새로고침
  } catch (error) {
    console.error("댓글 작성 실패:", error);
  }
};

// 답글 작성
const submitReply = async (parentId) => {
  if (!replyContent.value.trim()) return;

  try {
    await use$Fetch("/comments", {
      method: "POST",
      body: {
        boardId: props.boardId,
        memberId: userDetails.id.value,
        content: replyContent.value,
        parentId: parentId,
      },
    });
    replyContent.value = "";
    replyingTo.value = null;
    await fetchComments();
  } catch (error) {
    console.error("답글 작성 실패:", error);
  }
};

// 댓글 수정
const updateComment = async (commentId) => {
  if (!editContent.value.trim()) return;

  try {
    await use$Fetch(`/comments/${commentId}`, {
      method: "PATCH",
      body: {
        memberId: userDetails.id.value,
        content: editContent.value,
      },
    });
    editingComment.value = null;
    editContent.value = "";
    await fetchComments();
  } catch (error) {
    console.error("댓글 수정 실패:", error);
  }
};

// 댓글 삭제
const deleteComment = async (commentId) => {
  if (!confirm("댓글을 삭제하시겠습니까?")) return;

  try {
    await use$Fetch(`/comments/${commentId}?memberId=${userDetails.id.value}`, {
      method: "DELETE",
    });
    await fetchComments();
  } catch (error) {
    console.error("댓글 삭제 실패:", error);
  }
};

// 수정 모드 시작
const startEdit = (comment) => {
  editingComment.value = comment.id;
  editContent.value = comment.content;
};

// 답글 모드 시작
const startReply = (commentId) => {
  replyingTo.value = commentId;
  replyContent.value = "";
};

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

onMounted(() => {
  fetchComments();
});
</script>

<template>
  <div class="comments-section">
    <!-- 댓글 작성 폼 -->
    <div class="comment-form" v-if="userDetails.id">
      <textarea v-model="newComment" placeholder="댓글을 입력하세요" class="comment-input"></textarea>
      <button @click="submitComment" class="submit-btn">댓글 작성</button>
    </div>

    <!-- 댓글 목록 -->
    <div class="comments-list">
      <template v-for="comment in comments" :key="comment.id">
        <!-- 댓글 -->
        <div class="comment" :class="{ reply: comment.parentId }">
          <!-- 대댓글 화살표 -->
          <div v-if="comment.parentId" class="reply-arrow"></div>

          <div class="comment-content">
            <div class="comment-header">
              <span class="author">{{ comment.member.nickname }}</span>
              <span class="date">{{ formatDate(comment.createDate) }}</span>
            </div>

            <!-- 수정 모드 -->
            <div v-if="editingComment === comment.id">
              <textarea v-model="editContent" class="edit-input"></textarea>
              <div class="button-group">
                <button @click="updateComment(comment.id)">수정완료</button>
                <button @click="editingComment = null">취소</button>
              </div>
            </div>

            <!-- 일반 모드 -->
            <div v-else>
              <p class="text">{{ comment.content }}</p>
              <div class="button-group">
                <button @click="startReply(comment.id)">답글</button>
                <template v-if="userDetails.id === comment.member.id">
                  <button @click="startEdit(comment)">수정</button>
                  <button @click="deleteComment(comment.id)">삭제</button>
                </template>
              </div>
            </div>

            <!-- 답글 작성 폼 -->
            <div v-if="replyingTo === comment.id" class="reply-form">
              <textarea v-model="replyContent" placeholder="답글을 입력하세요" class="reply-input"></textarea>
              <div class="button-group">
                <button @click="submitReply(comment.id)">답글작성</button>
                <button @click="replyingTo = null">취소</button>
              </div>
            </div>
          </div>
        </div>
      </template>
    </div>
  </div>
</template>
