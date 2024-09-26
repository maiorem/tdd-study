package io.hhplus.tdd.point.application;

import io.hhplus.tdd.common.exception.CanNotChargePointException;
import io.hhplus.tdd.common.exception.PointShortageException;
import io.hhplus.tdd.database.PointHistoryTable;
import io.hhplus.tdd.database.UserPointTable;
import io.hhplus.tdd.point.component.PointHistory;
import io.hhplus.tdd.point.component.TransactionType;
import io.hhplus.tdd.point.component.UserPoint;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class PointService {

    private final UserPointTable userPointTable;
    private final PointHistoryTable pointHistoryTable;

    public UserPoint getPointById(long id) {
        return userPointTable.selectById(id);
    }

    public List<PointHistory> history(long id) {
        return pointHistoryTable.selectAllByUserId(id);
    }

    public UserPoint charge(long id, long amount) {
        if (amount < 0) throw new CanNotChargePointException("충전 포인트는 0 이하가 될 수 없습니다.");

        UserPoint userPoint = userPointTable.selectById(id);
        long point = userPoint.point();
        userPointTable.insertOrUpdate(id, point+amount);
        pointHistoryTable.insert(id, amount, TransactionType.CHARGE, System.currentTimeMillis());
        return userPointTable.selectById(id);
    }

    public UserPoint use(long id, long amount) {
        UserPoint userPoint = userPointTable.selectById(id);
        long point = userPoint.point();

        if (point < amount) throw new PointShortageException("포인트가 부족합니다.");

        userPointTable.insertOrUpdate(id, point-amount);
        pointHistoryTable.insert(id, amount, TransactionType.USE, System.currentTimeMillis());
        return userPointTable.selectById(id);
    }





}
