package io.hhplus.tdd;

import io.hhplus.tdd.database.PointHistoryTable;
import io.hhplus.tdd.database.UserPointTable;
import io.hhplus.tdd.point.PointService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
public class PointServiceTest {

    private PointService pointService;

    @Mock
    private UserPointTable userPointTable;

    @Mock
    private PointHistoryTable pointHistoryTable;

    @BeforeEach
    public void setUp(){

    }

    @Test
    public void 최소_충전_포인트_미달() {

    }


    @Test
    public void 충전_가능_포인트_초과() {

    }

    @Test
    public void 포인트_충전() {

    }

    @Test
    public void 사용_금액이_잔고보다_많으면_사용_실패() {

    }

    @Test
    public void 사용_가능_잔고_초과() {

    }

    @Test
    public void 포인트_사용() {

    }



}
