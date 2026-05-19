package com.auction.server.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

class AuctionRuntimeStateTest {

    private AuctionRuntimeState state;

    @BeforeEach
    void setUp() {
        // Khởi tạo một đối tượng state mới tinh trước mỗi bài test
        state = new AuctionRuntimeState();
    }

    @Test
    @DisplayName("Khởi tạo: Các giá trị mặc định phải chính xác (0, false, rỗng)")
    void initialState_IsCorrect() {
        assertEquals(0L, state.getTotalExtendedSeconds(), "Tổng thời gian gia hạn mặc định phải là 0");
        assertFalse(state.isEntryLocked(), "Trạng thái khóa mặc định phải là false");
        assertTrue(state.getParticipants().isEmpty(), "Danh sách participant mặc định phải rỗng");
    }

    @Test
    @DisplayName("Gia hạn thời gian: Phải cộng dồn chính xác số giây qua nhiều lần gọi")
    void extendBySeconds_AccumulatesCorrectly() {
        // Lần 1: Cộng thêm 30 giây
        state.extendBySeconds(30);
        assertEquals(30L, state.getTotalExtendedSeconds());

        // Lần 2: Cộng thêm 15 giây nữa (Tổng phải là 45)
        state.extendBySeconds(15);
        assertEquals(45L, state.getTotalExtendedSeconds());
    }

    @Test
    @DisplayName("Khóa phòng: Bật cờ khóa thành công và bỏ qua các lần gọi sau")
    void lockEntry_LocksSuccessfullyAndIgnoresSubsequentCalls() {
        // Gọi khóa lần đầu
        LocalDateTime firstLockTime = LocalDateTime.now();
        state.lockEntry(firstLockTime);

        assertTrue(state.isEntryLocked(), "Cờ khóa phải được bật thành true");

        // Gọi khóa lần 2 với mốc thời gian khác (để test logic nhánh if (!entryLocked) không bị lỗi)
        state.lockEntry(LocalDateTime.now().plusMinutes(5));
        assertTrue(state.isEntryLocked(), "Cờ khóa vẫn phải giữ nguyên là true");
    }

    @Test
    @DisplayName("Thêm Participant: Lưu ID hợp lệ và kiểm tra tồn tại chính xác")
    void addParticipant_ValidId_AddsSuccessfully() {
        state.addParticipant("U001");
        state.addParticipant("U002");

        assertTrue(state.hasParticipant("U001"), "Phải tìm thấy U001 trong danh sách");
        assertTrue(state.hasParticipant("U002"), "Phải tìm thấy U002 trong danh sách");
        assertFalse(state.hasParticipant("U003"), "U003 chưa được thêm nên phải trả về false");
        assertEquals(2, state.getParticipants().size(), "Kích thước danh sách phải đúng bằng 2");
    }

    @Test
    @DisplayName("Thêm Participant: Chặn và bỏ qua các ID null hoặc rỗng")
    void addParticipant_NullOrBlankId_Ignores() {
        // Cố tình truyền các giá trị rác
        state.addParticipant(null);
        state.addParticipant("");
        state.addParticipant("   ");

        // Danh sách vẫn phải trống trơn
        assertTrue(state.getParticipants().isEmpty(), "Không được phép lưu ID null hoặc khoảng trắng");
        assertFalse(state.hasParticipant(null));
    }
}