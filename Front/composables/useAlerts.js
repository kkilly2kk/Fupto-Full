export const useAlerts = () => {
  const alerts = ref([]);
  const isLoading = ref(false);
  const error = ref(null);
  const config = useRuntimeConfig();

  const fetchUnreadAlerts = async () => {
    isLoading.value = true;
    error.value = null;
    try {
      // useAuthFetch 대신 use$Fetch 사용
      const response = await use$Fetch(`/user/member/unreadAlerts`);
      console.log("Raw response:", response);

      // response.content가 배열인지 확인
      if (response && Array.isArray(response.content)) {
        alerts.value = response.content;
      } else {
        alerts.value = Array.isArray(response) ? response : [];
      }

      console.log("Final alerts value:", alerts.value);
    } catch (err) {
      error.value = "알림을 불러오는데 실패했습니다.";
      console.error("Failed to fetch unread alerts:", err);
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
    console.log("Attempting to mark as read:", alertId);
    console.log("Current alerts:", alerts.value);

    if (!alertId) {
      console.error("Invalid alert ID:", alertId);
      return;
    }
    try {
      await use$Fetch(`/user/member/alerts/${alertId}/read`, {
        method: "PATCH",
      });
      if (Array.isArray(alerts.value)) {
        const alert = alerts.value.find((a) => a.id === alertId);
        console.log("Found alert:", alert);
        if (alert) alert.isRead = true;
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
      if (Array.isArray(alerts.value)) {
        alerts.value.forEach((alert) => (alert.isRead = true));
      }
    } catch (err) {
      console.error("Failed to mark all alerts as read:", err);
    }
  };

  return {
    alerts,
    isLoading,
    error,
    addAlert,
    fetchUnreadAlerts,
    markAsRead,
    markAllAsRead,
  };
};
