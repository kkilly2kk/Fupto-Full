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

const emit = defineEmits(["update-comments"]);

const comments = ref([]);
const newComment = ref("");
const userDetails = useUserDetails();
const totalCommentCount = computed(() => {
  let count = 0;

  // 재귀적으로 모든 댓글을 카운트하는 함수
  const countComments = (commentList) => {
    commentList.forEach((comment) => {
      count++; // 현재 댓글 카운트
      if (comment.children && comment.children.length > 0) {
        countComments(comment.children); // 대댓글이 있으면 재귀적으로 카운트
      }
    });
  };

  countComments(comments.value);
  return count;
});

// 댓글 목록 조회
const fetchComments = async () => {
  try {
    const data = await use$Fetch(`/comments/board/${props.boardId}`);
    comments.value = data;
    emit("update-count", totalCommentCount.value);
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
  <div class="comments-inner-section">
    <!-- 댓글 작성 폼 -->
    <div v-if="userDetails.id" class="comment-compose">
      <textarea v-model="newComment" placeholder="댓글을 입력하세요" class="comment-compose-input"> </textarea>
      <button @click="submitComment" class="comment-submit-btn">댓글 작성</button>
    </div>

    <!-- 댓글 목록 -->
    <div class="comment-list">
      <comment-item
        v-for="comment in comments"
        :key="comment.id"
        :comment="comment"
        :user-id="userDetails.id.value"
        :board-id="boardId"
        @refresh="fetchComments"
      />
    </div>
  </div>
</template>
