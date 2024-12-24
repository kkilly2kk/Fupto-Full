<script setup>
import { ref, onMounted } from "vue";

useHead({
  link: [{ rel: "stylesheet", href: "/css/comment.css" }],
});

const props = defineProps({
  boardId: {
    type: Number,
    required: true,
  },
});

const comments = ref([]);
const newComment = ref("");
const userDetails = useUserDetails();

// 댓글 목록 조회
const fetchComments = async () => {
  try {
    console.log("게시글 ID:", props.boardId);
    const data = await use$Fetch(`/comments/board/${props.boardId}`);
    console.log("받은 댓글 전체:", data);
    comments.value = data;
  } catch (error) {
    console.error("댓글 조회 실패:", error);
  }
};

// 댓글 작성
const submitComment = async () => {
  if (!newComment.value.trim()) return;

  try {
    await use$Fetch("/comments", {
      method: "POST",
      body: {
        boardId: props.boardId,
        memberId: userDetails.id.value,
        content: newComment.value,
        parentId: null,
      },
    });
    newComment.value = "";
    await fetchComments();
  } catch (error) {
    console.error("댓글 작성 실패:", error);
  }
};

onMounted(() => {
  fetchComments();
});
</script>

<template>
  <div class="comments-section">
    <!-- 댓글 작성 폼 -->
    <div class="comment-form" v-if="userDetails.id">
      <textarea v-model="newComment" placeholder="댓글을 입력하세요" class="comment-input"> </textarea>
      <button @click="submitComment" class="submit-btn">댓글 작성</button>
    </div>

    <!-- 댓글 목록 -->
    <div class="comments-list">
      <comment-item
        v-for="comment in comments"
        :key="comment.id"
        :comment="comment"
        :user-id="userDetails.id"
        @refresh="fetchComments"
      />
    </div>
  </div>
</template>
