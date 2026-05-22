package com.auction.client.network.dispatcher;

import com.auction.common.dto.Message;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.mockito.Mockito.*;

class AuctionMessageDispatcherTest {

    private AuctionMessageDispatcher dispatcher;
    private MessageRouteResult mockRouteResult;
    private Message mockMessage;

    @BeforeEach
    void setUp() {
        dispatcher = new AuctionMessageDispatcher();

        // Tuyệt kỹ RETURNS_DEEP_STUBS: Giúp Mockito tự động tạo mock cho cái Handler
        // nằm bên trong RouteResult mà chúng ta không cần khai báo rõ tên Class Handler đó.
        mockRouteResult = mock(MessageRouteResult.class, RETURNS_DEEP_STUBS);
        mockMessage = new Message("TEST_ACTION", "user_1", "Mock Data");
    }

    @Test
    @DisplayName("Nhánh 1: RouteResult bị null -> Bỏ qua, không văng lỗi")
    void testDispatch_NullRouteResult_DoesNothing() {
        assertDoesNotThrow(() -> dispatcher.dispatch(null),
                "Hệ thống không được crash khi Dispatcher nhận vào một kết quả null");
    }

    @Test
    @DisplayName("Nhánh 2: RouteResult không chứa Message -> Bỏ qua, không văng lỗi")
    void testDispatch_NullMessage_DoesNothing() {
        when(mockRouteResult.getMessage()).thenReturn(null);

        assertDoesNotThrow(() -> dispatcher.dispatch(mockRouteResult));
    }

    @Test
    @DisplayName("Nhánh 3: Message hợp lệ nhưng không có Handler xử lý -> Không chạy hàm handle")
    void testDispatch_NoHandler_DoesNothing() throws Exception {
        when(mockRouteResult.getMessage()).thenReturn(mockMessage);
        when(mockRouteResult.hasHandler()).thenReturn(false);

        assertDoesNotThrow(() -> dispatcher.dispatch(mockRouteResult));

        // Xác thực hàm handle của Handler KHÔNG HỀ bị gọi (never)
        verify(mockRouteResult.getHandler(), never()).handle(any());
    }

    @Test
    @DisplayName("Nhánh 4 (Happy Path): Dữ liệu đầy đủ -> Gọi hàm handle thành công")
    void testDispatch_ValidResult_CallsHandle() throws Exception {
        when(mockRouteResult.getMessage()).thenReturn(mockMessage);
        when(mockRouteResult.hasHandler()).thenReturn(true);

        dispatcher.dispatch(mockRouteResult);

        // Xác thực Handler lấy từ Result đã được gọi hàm handle đúng 1 lần (times 1)
        verify(mockRouteResult.getHandler(), times(1)).handle(mockMessage);
    }

    @Test
    @DisplayName("Nhánh 5 (Bắt lỗi): Handler bị crash -> Dispatcher bắt được lỗi, ứng dụng vẫn sống")
    void testDispatch_HandlerThrowsException_CatchesSafely() throws Exception {
        when(mockRouteResult.getMessage()).thenReturn(mockMessage);
        when(mockRouteResult.hasHandler()).thenReturn(true);

        // === ĐOẠN CODE "GIẢI CỨU" MOCKITO ===
        // Bước 1: Lấy cái Handler giả (được tạo ngầm bởi Deep Stubs) ra và gán vào một biến
        var mockHandler = mockRouteResult.getHandler();

        // Bước 2: Ép biến này ném lỗi. Lúc này Mockito sẽ hiểu ngay lập tức!
        doThrow(new RuntimeException("Giả lập lỗi bất ngờ từ thuật toán bên trong"))
                .when(mockHandler).handle(any());
        // ===================================

        // Xác thực lệnh try-catch đã phát huy tác dụng: Nuốt trọn lỗi và không ném ra ngoài
        assertDoesNotThrow(() -> dispatcher.dispatch(mockRouteResult),
                "Dispatcher phải chặn được lỗi từ Handler, chỉ ghi log chứ không được làm crash UI");
    }
}