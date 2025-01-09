export const useSSE = () => {
  const userDetails = useUserDetails();
  const config = useRuntimeConfig();
  const { addAlert, fetchUnreadAlerts } = useAlerts();
  let eventSource = null;
  let reconnectTimeout = null;
  const maxReconnectAttempts = 5;
  let reconnectAttempts = 0;

  const connectSSE = () => {
    if (eventSource) return;

    const token = userDetails.token.value;
    const url = `${config.public.apiBase}/user/member/subscribe?token=${token}`;
    // console.log("Connecting to SSE with URL:", url);

    eventSource = new EventSource(url, { withCredentials: true });

    // 일반 메시지
    eventSource.onmessage = async (event) => {
      // console.log("Received message:", event.data);
      try {
        const newAlert = JSON.parse(event.data);
        addAlert(newAlert);
      } catch (error) {
        console.error("Error parsing SSE message:", error);
      }
    };

    // 특정 이벤트 타입 리스너 추가
    eventSource.addEventListener("PRICE_ALERT", async (event) => {
      // console.log("Received price alert:", event.data);
      try {
        const newAlert = JSON.parse(event.data);
        addAlert(newAlert);
      } catch (error) {
        console.error("Error parsing price alert:", error);
      }
    });

    // 연결 성공
    eventSource.onopen = () => {
      console.log("SSE connection established");
      reconnectAttempts = 0; // 연결 성공시 카운트 리셋
    };

    // 에러 처리
    eventSource.onerror = (error) => {
      console.error("SSE error:", error);
      eventSource?.close();
      eventSource = null;

      // 재연결 로직
      if (reconnectAttempts < maxReconnectAttempts) {
        reconnectTimeout = setTimeout(() => {
          console.log(`Attempting to reconnect SSE... (${reconnectAttempts + 1}/${maxReconnectAttempts})`);
          reconnectAttempts++;
          connectSSE();
        }, 5000 * Math.pow(2, reconnectAttempts)); // 지수 백오프
      }
    };
  };

  const disconnectSSE = () => {
    if (eventSource) {
      eventSource.close();
      eventSource = null;
    }
    if (reconnectTimeout) {
      clearTimeout(reconnectTimeout);
    }
    console.log("SSE connection closed");
  };

  onUnmounted(() => {
    disconnectSSE();
  });

  return {
    connectSSE,
    disconnectSSE,
  };
};
