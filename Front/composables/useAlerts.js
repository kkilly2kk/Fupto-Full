const hasUnreadAlerts = ref(false);

export const useAlerts = () => {
  const alerts = ref([]);
  const isLoading = ref(false);
  const error = ref(null);

  const fetchAlerts = async () => {
    isLoading.value = true;
    error.value = null;
    try {
      const response = await use$Fetch(`/user/member/alerts`);
      if (response && Array.isArray(response.content)) {
        alerts.value = response.content;
        // 읽지 않은 알림 여부 업데이트
        hasUnreadAlerts.value = alerts.value.some((alert) => !alert.isRead);
      } else {
        alerts.value = Array.isArray(response) ? response : [];
      }
    } catch (err) {
      error.value = "알림을 불러오는데 실패했습니다.";
      console.error("Failed to fetch alerts:", err);
    } finally {
      isLoading.value = false;
    }
  };

  const addAlert = (newAlert) => {
    if (Array.isArray(alerts.value) && !alerts.value.some((alert) => alert.id === newAlert.id)) {
      alerts.value = [newAlert, ...alerts.value];
      console.log("New alert added:", newAlert);
    }
  };

  const markAsRead = async (alertId) => {
    try {
      await use$Fetch(`/user/member/alerts/${alertId}/read`, {
        method: "PATCH",
      });
      const alert = alerts.value.find((a) => a.id === alertId);
      if (alert) {
        alert.isRead = true;
        // 읽지 않은 알림 여부 업데이트
        hasUnreadAlerts.value = alerts.value.some((alert) => !alert.isRead);
      }
    } catch (err) {
      console.error("Failed to mark alert as read:", err);
    }
  };

  const markAllAsRead = async () => {
    try {
      await use$Fetch(`/user/member/readAll`, {
        method: "PATCH",
      });
      alerts.value.forEach((alert) => (alert.isRead = true));
      hasUnreadAlerts.value = false; // 모두 읽음으로 설정
    } catch (err) {
      console.error("Failed to mark all alerts as read:", err);
    }
  };

  return {
    alerts,
    isLoading,
    error,
    hasUnreadAlerts,
    addAlert,
    fetchAlerts,
    markAsRead,
    markAllAsRead,
  };
};
