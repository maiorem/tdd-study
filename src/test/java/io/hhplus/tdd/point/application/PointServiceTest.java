package io.hhplus.tdd.point.application;

import io.hhplus.tdd.common.exception.CanNotChargePointException;
import io.hhplus.tdd.common.exception.PointShortageException;
import io.hhplus.tdd.database.PointHistoryTable;
import io.hhplus.tdd.database.UserPointTable;
import io.hhplus.tdd.point.component.PointHistory;
import io.hhplus.tdd.point.component.TransactionType;
import io.hhplus.tdd.point.component.UserPoint;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
public class PointServiceTest {

    @InjectMocks
    private PointService pointService;

    @Mock
    private UserPointTable userPointTable;

    @Mock
    private PointHistoryTable pointHistoryTable;

    @BeforeEach
    public void setUp(){
        MockitoAnnotations.openMocks(this);
        userPointTable = new UserPointTable();
        pointHistoryTable = new PointHistoryTable();
        pointService = new PointService(userPointTable, pointHistoryTable);
    }

    @Nested
    @DisplayName("유저 포인트 조회 시")
    class GetUserPoint {

        @Test
        void 포인트_조회_성공(){
            //given
            userPointTable.insertOrUpdate(1L, 10000);

            //when
            UserPoint testPoint = pointService.getPointById(1L);

            //then
            assertEquals(10000, testPoint.point());
        }

        @Test
        void ID가_존재하지_않으면_빈_값_반환() {
            //given
            userPointTable.insertOrUpdate(1L, 10000);

            //when
            UserPoint testPoint = pointService.getPointById(2L);

            //then
            assertEquals(0, testPoint.point());
       }


    }

    @Nested
    @DisplayName("유저 포인트 내역 조회 시")
    class GetPointHistory {

        @Test
        void 포인트_내역_조회_성공() {
            //given
            pointHistoryTable.insert(1L, 1000, TransactionType.CHARGE, System.currentTimeMillis());
            pointHistoryTable.insert(1L, 100, TransactionType.CHARGE, System.currentTimeMillis());
            pointHistoryTable.insert(1L, 500, TransactionType.USE, System.currentTimeMillis());

            //when
            List<PointHistory> history = pointService.history(1L);

            //then
            assertEquals(3, history.size());
            assertEquals(500, history.get(2).amount());
            assertEquals(TransactionType.CHARGE, history.get(0).type());

        }

        @Test
        void ID가_존재하지_않으면_빈_값_반환() {
            //given
            pointHistoryTable.insert(1L, 1000, TransactionType.CHARGE, System.currentTimeMillis());
            pointHistoryTable.insert(1L, 100, TransactionType.CHARGE, System.currentTimeMillis());
            pointHistoryTable.insert(1L, 500, TransactionType.USE, System.currentTimeMillis());

            //when
            List<PointHistory> history = pointService.history(2L);

            //then
            assertEquals(0, history.size());

        }

    }

    @Nested
    @DisplayName("포인트 충전 시")
    class ChargePoint {

        @Test
        void 충전_포인트가_음수면_충전_실패() {
            //given
            userPointTable.insertOrUpdate(1L, 1000);

            //when
            CanNotChargePointException exception = assertThrows(CanNotChargePointException.class, () -> {
                pointService.charge(1L, -100);
            });

            //then
           assertEquals("충전 포인트는 0 이하가 될 수 없습니다.", exception.getMessage());

        }


        @Test
        void 포인트_충전_성공() {
            //given
            userPointTable.insertOrUpdate(1L, 1000);

            //when
            pointService.charge(1L, 20000);

            //then
            UserPoint userPoint = pointService.getPointById(1L);
            assertEquals(21000, userPoint.point());
        }

    }

    @Nested
    @DisplayName("포인트 사용 시")
    class UsePoint {


        @Test
        void 사용_금액이_잔고보다_많으면_사용_실패() {
            userPointTable.insertOrUpdate(1L, 1000);

            //when
            PointShortageException exception = assertThrows(PointShortageException.class, () -> {
                pointService.use(1L, 2000);
            });

            //then
            assertEquals("포인트가 부족합니다.", exception.getMessage());
        }

        @Test
        void 포인트_사용_성공() {
            //given
            userPointTable.insertOrUpdate(1L, 1000);

            //when
            pointService.use(1L, 600);

            //then
            UserPoint userPoint = pointService.getPointById(1L);
            assertEquals(400, userPoint.point());

        }
    }

}
