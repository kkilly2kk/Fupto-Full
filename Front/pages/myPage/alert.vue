<script setup>
import { onMounted } from "vue";
import { useAlerts } from "~/composables/useAlerts";

useHead({
  link: [{ rel: "stylesheet", href: "/css/myAlert.css" }],
});

const { alerts, isLoading, error, fetchAlerts, markAsRead, markAllAsRead } = useAlerts();

// 날짜 포맷팅
const formatDate = (dateString) => {
  if (!dateString) return "";

  const date = new Date(dateString);
  const ymd =
    date.getUTCFullYear() +
    "-" +
    String(date.getUTCMonth() + 1).padStart(2, "0") +
    "-" +
    String(date.getUTCDate()).padStart(2, "0");

  const time = String(date.getUTCHours()).padStart(2, "0") + ":" + String(date.getUTCMinutes()).padStart(2, "0");

  return `${ymd} ${time}`;
};

const handleReadAlert = async (alertId) => {
  await markAsRead(alertId);
};

const handleReadAllAlerts = async () => {
  await markAllAsRead();
};

const handleDeleteAlert = async (alertId) => {
  try {
    await use$Fetch(`/user/member/alerts/${alertId}/delete`, {
      method: "PATCH",
    });

    alerts.value = alerts.value.filter((alert) => alert.id !== alertId);
  } catch (err) {
    console.error("Failed to delete alert:", err);
  }
};

onMounted(() => {
  fetchAlerts();
});
</script>

<template>
  <div class="alert-container">
    <div class="alert-header">
      <h2 class="alert-title">알림 목록</h2>
      <button class="read-all-btn" @click="handleReadAllAlerts">모두 읽음</button>
    </div>

    <div v-if="isLoading" class="loading">로딩 중...</div>
    <div v-else-if="error" class="error">{{ error }}</div>
    <div v-else-if="alerts.length === 0" class="no-alerts">알림이 없습니다.</div>
    <div v-else class="alert-list">
      <div v-for="alert in alerts" :key="alert.id" class="alert-item" :class="{ 'is-read': alert.isRead }">
        <div class="alert-content" @click="handleReadAlert(alert.id)">
          <div class="alert-info">
            <div class="alert-info-left">
              <span class="alert-type">{{ alert.alertType === "PRICE_ALERT" ? "가격 알림" : "알림" }}</span>
              <span class="alert-date">{{ formatDate(alert.createDate) }}</span>
            </div>
            <button class="delete-alert-btn" @click.stop="handleDeleteAlert(alert.id)">×</button>
          </div>
          <p class="alert-message">{{ alert.message }}</p>
          <div class="alert-details">
            <span class="product-name">({{ alert.referBrandName }}) {{ alert.referName }}</span>
          </div>
        </div>
        <div class="alert-status" v-if="!alert.isRead"></div>
      </div>
    </div>
  </div>
</template>
