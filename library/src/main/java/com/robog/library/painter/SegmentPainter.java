package com.robog.library.painter;

import android.graphics.Canvas;
import android.util.Log;

import com.robog.library.Action;
import com.robog.library.PixelPoint;
import com.robog.library.PixelShape;
import com.robog.library.Util;

/**
 * @Author: yuxingdong
 * @Time: 2018/2/9
 */

public class SegmentPainter extends AbsPainter {

    private static final String TAG = "SegmentPainter";

    private final PixelShape mPixelShape;

    private final int mDuration;

    private final boolean mClose;

    public SegmentPainter() {
        this(null, 0, false);
    }

    public SegmentPainter(Painter painter) {
        this(painter.getShape(), painter.duration(), painter.close());
    }

    public SegmentPainter(PixelShape pixelShape, int duration, boolean close) {
        mPixelShape = pixelShape;
        mDuration = duration;
        mClose = close;
    }

    @Override
    public PixelShape getShape() {
        return mPixelShape;
    }

    @Override
    public int duration() {
        return mDuration;
    }

    @Override
    public boolean close() {
        return mClose;
    }

    @Override
    public void completeDraw(Canvas canvas) {
        draw(canvas, true);
    }

    @Override
    protected void realDraw(Canvas canvas) {
        draw(canvas, false);
    }

    private void draw(Canvas canvas, boolean complete) {

        for (int i = 0; i < pointList.size(); i++) {
            PixelPoint current = pointList.get(i);
            PixelPoint next;
            if (i < pointList.size() - 1) {
                next = pointList.get(i + 1);
            } else {
                if (!close()) return;
                next = pointList.get(0);
            }
            // 如果是完整画完当前笔，或者当前点已完整绘制，则当前点起始点与下一点起始点相连
            if (complete || current.isPathFinish()) {

                canvas.drawLine(current.getStartX(), current.getStartY(),
                        next.getStartX(), next.getStartY(), paint);
            } else {
                // 否则，当前点起始点与当前点所到点相连
                canvas.drawLine(current.getStartX(), current.getStartY(),
                        current.getEndX(), current.getEndY(), paint);
            }

        }
    }

    @Override
    public void performDraw(Action action) {

        // 总路程
        float distance = Util.calDistance(pointList, close());

        for (int i = 0; i < pointList.size(); i++) {

            PixelPoint current = pointList.get(i);
            PixelPoint next;

            if (i < pointList.size() - 1) {
                next = pointList.get(i + 1);
            } else {
                next = pointList.get(0);
            }
            // 当前线段占总路径百分比
            float fraction = Util.calFraction(distance, current, next);
            float segment = duration() * fraction / INTERVAL;

            float startX = current.getStartX();
            float startY = current.getStartY();

            float endX = next.getStartX();
            float endY = next.getStartY();

            float deltaX = endX - startX;
            float deltaY = endY - startY;

            float dis = (float) Math.sqrt(deltaX * deltaX + deltaY * deltaY);
            // 单位时间移动长度
            float div = dis / segment;
            float degree = (float) Math.atan2(deltaY, deltaX);

            while (!current.isPathFinish() && !isStop) {

                float moveX = (float) (current.getEndX() + div * Math.cos(degree));
                float moveY = (float) (current.getEndY() + div * Math.sin(degree));

                current.setEndX(moveX);
                current.setEndY(moveY);

                float moveDis = Util.getDis(current.getEndX(), current.getEndY(),
                        current.getStartX(), current.getStartY());

                // 如果当前点离目标点小于单位时间长度，则认为当前线段已完成
                if (Math.abs(dis - moveDis) < 2 * div) {
                    current.setEndX(endX);
                    current.setEndY(endY);
                    current.setPathFinish(true);
                }
                // 更新界面
                action.update(this);
                try {
                    Thread.sleep(INTERVAL);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }

        }
    }
}