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
      if (alert) alert.isRead = true;
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
    } catch (err) {
      console.error("Failed to mark all alerts as read:", err);
    }
  };

  const hasUnreadAlerts = computed(() => {
    return alerts.value.some((alert) => !alert.isRead);
  });

  return {
    alerts,
    isLoading,
    error,
    addAlert,
    fetchAlerts,
    markAsRead,
    markAllAsRead,
    hasUnreadAlerts,
  };
};
