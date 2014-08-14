package net.bingyan.campass.view;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PathMeasure;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

import net.bingyan.campass.R;

import java.util.ArrayList;

/**
 * Created by ant on 14-8-12.
 */
public class LineGraphView extends View {

    Line line;
    boolean lineChanged = false;

    Bitmap progressMark;

    int paddingTop = 0;

    Bitmap baseGraphImage;

    private int graphFillColor = Color.parseColor("#FAFFAB");

    PathMeasure pm;
    float[] mark = {0, 0};
    float[] tan = {0, 0};

    OnLineGraphBarChangeListener mBarChangeListener;


    public LineGraphView(Context context) {
        super(context, null);
    }

    public LineGraphView(Context context, AttributeSet attrs) {
        super(context, attrs);
        progressMark = BitmapFactory.decodeResource(context.getResources(),
                R.drawable.elec_seek_dot);
        paddingTop = progressMark.getHeight() + 10;
    }


    public void setGraphFillColor(int color) {
        graphFillColor = color;
    }

    public void setLine(Line line) {
        this.line = line;
        lineChanged = true;
        postInvalidate();
    }

    public void setOnLineGraphBarChangeListener(OnLineGraphBarChangeListener mBarChangeListener) {
        this.mBarChangeListener = mBarChangeListener;
    }

    public interface OnLineGraphBarChangeListener {
        public void onBarChanged(float percentX, float percentY);
    }

    private int getGraphWidth() {
        return getWidth();
    }

    private int getGraphHeight() {
        return getHeight() - paddingTop;
    }


    private LinePoint getGraphPoint(LinePoint p) {

        // Y 轴从0开始， X 轴从 最小值开始
        float yPercent = p.getY() / line.getMaxY();
        float xPercent = (p.getX() - line.getMinX()) / (line.getMaxX() - line.getMinX());

        float XPixels = (xPercent * getGraphWidth());
        float YPixels = getHeight() - (getGraphHeight() * yPercent);

        return new LinePoint(XPixels, YPixels);
    }

    private LinePoint getCtrlPoint(LinePoint point1, LinePoint point2, float k) {
        float x1 = point1.getX();
        float y1 = point1.getY();
        float x2 = point2.getX();
        float y2 = point2.getY();
        float x, y;
        float hy = getGraphHeight();
        float ly = 0;

        if (k <= 0) {
            if (y2 >= y1) {
                x = (x1 + x2) / 2;
                y = (x - x1) * k + y1;
                if (y < ly) {
                    y = ly;
                    x = x1 + (y - y1) / k;
                }
            } else if ((y2 - y1) / (x2 - x1) >= k) {
                y = y2;
                x = x1 + (y - y1) / k;
            } else {
                x = (x1 + x2) / 2;
                y = (x - x1) * k + y1;
            }
        } else {
            if (y2 < y1) {
                x = (x1 + x2) / 2;
                y = (x - x1) * k + y1;
                if (y > hy) {
                    y = hy;
                    x = x1 + (y - y1) / k;
                }
            } else if ((y2 - y1) / (x2 - x1) >= k) {
                x = (x1 + x2) / 2;
                y = (x - x1) * k + y1;
            } else {
                y = y2;
                x = x1 + (y - y1) / k;
            }
        }

        LinePoint point = new LinePoint(x, y);
        return point;
    }

    private void initBaseGraphImage() {
        Path path = new Path();

        baseGraphImage = Bitmap.createBitmap(getWidth(), getHeight(),
                Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(baseGraphImage);

        LinePoint lastPoint = new LinePoint();
        LinePoint midPoint = new LinePoint();
        LinePoint newPoint;
        LinePoint ctrlPoint;

        int count = 0;
        float k = 0; // 斜率
        float startYPixels = 0; // 填充起点
        // 计算 PATH
        for (LinePoint p : line.getPoints()) {
            if (count == 0) {
                lastPoint = getGraphPoint(p);
                startYPixels = lastPoint.getY();
                path.moveTo(lastPoint.getX(), lastPoint.getY());
            } else if (count == 1) {
                midPoint = getGraphPoint(p);
            } else {
                newPoint = getGraphPoint(p);

                if ((newPoint.getY() >= midPoint.getY() && midPoint.getY() >= lastPoint.getY()) // 递增
                        || (newPoint.getY() <= midPoint.getY() && midPoint.getY() <= lastPoint.getY()) // 递减
                        || (newPoint.getY() >= midPoint.getY() && midPoint.getY() <= lastPoint.getY() && k != 0) //  凹
                        || (newPoint.getY() <= midPoint.getY() && midPoint.getY() >= lastPoint.getY() && k != 0)) { //凸

                    ctrlPoint = getCtrlPoint(lastPoint, midPoint, k);
                    k = (midPoint.getY() - ctrlPoint.getY()) / (midPoint.getX() - ctrlPoint.getX());

                    path.quadTo(ctrlPoint.getX(), ctrlPoint.getY(), midPoint.getX(), midPoint.getY());
                } else {
                    LinePoint ctrPoint1 = new LinePoint(
                            (lastPoint.getX() + midPoint.getX()) / 2, lastPoint.getY());
                    LinePoint ctrPoint2 = new LinePoint(
                            (lastPoint.getX() + midPoint.getX()) / 2, midPoint.getY());

                    path.cubicTo(ctrPoint1.getX(), ctrPoint1.getY(),
                            ctrPoint2.getX(), ctrPoint2.getY(),
                            midPoint.getX(), midPoint.getY());
                }
                if (count == line.getSize() - 1) {
                    ctrlPoint = getCtrlPoint(midPoint, newPoint, k);

                    path.quadTo(ctrlPoint.getX(), ctrlPoint.getY(),
                            newPoint.getX(), newPoint.getY());
                }
                lastPoint.setX(midPoint.getX());
                lastPoint.setY(midPoint.getY());
                midPoint.setX(newPoint.getX());
                midPoint.setY(newPoint.getY());
            }
            count++;
        }


        Paint paint = new Paint();
        paint.setAntiAlias(true);

        // 填充
        paint.setColor(graphFillColor);
        paint.setStyle(Paint.Style.FILL);

        Path pathFill = new Path(path);
        pathFill.lineTo(midPoint.getX(), getHeight()); // 右下
        pathFill.lineTo(0, getHeight());  //左下
        pathFill.lineTo(0, startYPixels);  //左上
        canvas.drawPath(pathFill, paint);

        //曲线
        paint.setStyle(Paint.Style.STROKE);
        paint.setColor(line.getColor());
        paint.setStrokeWidth(line.getStrokeWidth());
        canvas.drawPath(path, paint);

        pm = new PathMeasure();
        pm.setPath(path, false);
        pm.getPosTan(10, mark, tan);
    }

    public void onDraw(Canvas ca) {

        if (line == null) {
            return;
        }

        if (lineChanged) {
            initBaseGraphImage();
            lineChanged = false;
        }

        ca.drawBitmap(baseGraphImage, 0, 0, null);
        //ca.drawLine(mark[0], mark[1], mark[0], getHeight(), linePaint);
        ca.drawBitmap(progressMark, mark[0] - progressMark.getWidth() / 2,
                mark[1] - progressMark.getHeight() / 2, null);
        if (mBarChangeListener != null) {
            mBarChangeListener.onBarChanged((mark[0] / getGraphWidth()) * (line.getMaxX() - line.getMinX()) + line.getMinX(),
                    (1 - (mark[1] - paddingTop) / getGraphHeight()) * line.getMaxY());
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {

        if (line == null) {
            return true;
        }

        if (event.getAction() == MotionEvent.ACTION_DOWN
                || event.getAction() == MotionEvent.ACTION_UP
                || event.getAction() == MotionEvent.ACTION_MOVE) {

            float w = 5;
            for (int i = 0; i < pm.getLength(); i += w) {
                pm.getPosTan(i, mark, tan);
                if (mark[0] - w / 2.0 <= event.getX()
                        && event.getX() <= mark[0] + w / 2.0) {
                    postInvalidate();
                    break;
                }
            }
        }

        return true;
    }

    public static class LinePoint {

        private float x = 0;
        private float y = 0;

        public LinePoint(float x, float y) {
            super();
            this.x = x;
            this.y = y;
        }

        public LinePoint() {
        }

        public float getX() {
            return x;
        }

        public void setX(float x) {
            this.x = x;
        }

        public float getY() {
            return y;
        }

        public void setY(float y) {
            this.y = y;
        }

    }

    public static class Line {

        private ArrayList<LinePoint> points = new ArrayList<LinePoint>();
        private int color = Color.parseColor("#FBCE00"); //?
        private int strokeWidth = 5;
        private float minY = Float.MAX_VALUE, minX = Float.MAX_VALUE;
        private float maxY = Float.MIN_VALUE, maxX = Float.MIN_VALUE;

        public int getColor() {
            return color;
        }

        public void setColor(int color) {
            this.color = color;
        }

        public int getStrokeWidth() {
            return strokeWidth;
        }

        public float getMinY() {
            return minY;
        }

        public float getMinX() {
            return minX;
        }

        public float getMaxY() {
            return maxY;
        }

        public float getMaxX() {
            return maxX;
        }

        public ArrayList<LinePoint> getPoints() {
            return points;
        }

        public void setPoints(ArrayList<LinePoint> points) {
            this.points = points;

            for (LinePoint point : points) {
                if (point.getY() > maxY)
                    maxY = point.getY();
                if (point.getY() < minY)
                    minY = point.getY();
                if (point.getX() > maxX)
                    maxX = point.getX();
                if (point.getX() < minX)
                    minX = point.getX();
            }

        }

        public void addPoint(LinePoint point) {
            points.add(point);

            if (point.getY() > maxY)
                maxY = point.getY();
            if (point.getY() < minY)
                minY = point.getY();
            if (point.getX() > maxX)
                maxX = point.getX();
            if (point.getX() < minX)
                minX = point.getX();
        }

        public LinePoint getPoint(int index) {
            return points.get(index);
        }

        public int getSize() {
            return points.size();
        }
    }
}
