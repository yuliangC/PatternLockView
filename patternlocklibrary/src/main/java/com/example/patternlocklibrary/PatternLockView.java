package com.example.patternlocklibrary;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;

import java.util.ArrayList;
import java.util.List;

public class PatternLockView extends View {

    private int defaultColor, selectedColor, errorColor;
    private float pointRadius, lineWith;
    private Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private Point[][] points = new Point[3][3];
    private float mWidth;
    private float mTouchX, mTouchY;
    private ArrayList<Point> selectedPoints = new ArrayList<>();
    private boolean isDrawLine, isConnectPoint,isFinish;
    private int pswDigit,pointState;
    private String password="";
    private PatternLockListener lockListener;

    public PatternLockView(Context context) {
        this(context, null);
    }

    public PatternLockView(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, -1);
    }

    public PatternLockView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        TypedArray array = context.obtainStyledAttributes(attrs, R.styleable.MyPatternLockView, 0, 0);
        defaultColor = array.getColor(R.styleable.MyPatternLockView_default_color, Color.GREEN);
        selectedColor = array.getColor(R.styleable.MyPatternLockView_selected_color, Color.GRAY);
        errorColor = array.getColor(R.styleable.MyPatternLockView_error_color, Color.RED);
        pointRadius = array.getDimension(R.styleable.MyPatternLockView_point_radius, Util.dip2px(context, 20));
        lineWith = array.getDimension(R.styleable.MyPatternLockView_line_width, Util.dip2px(context, 5));
        pswDigit = array.getInteger(R.styleable.MyPatternLockView_password_digit, 5);
        array.recycle();
    }


    /**
     * 初始化所有点
     */
    private void initPoints() {
        int value=0;
        for (int i = 0; i < points.length; i++) {
            for (int j = 0; j < points[i].length; j++) {
                value++;
                points[i][j] = getPoint(i, j,String.valueOf(value));
            }
        }
    }


    @Override
    protected void onDraw(Canvas canvas) {
        if (points[0][0]==null){
            initPoints();
        }
        initSelectedPoints();
        drawAllPoints(canvas);
        paint.setStrokeWidth(lineWith);
        drawLines(canvas);
        drawConnectLines(canvas);
    }

    /**
     * 恢复view状态时会用到此方法，根据记住的密码来恢复点的状态
     * 为什么不记住点的坐标信息呢？因为横竖屏切换后view的宽高都发生了变化，重绘时点的坐标都发生了变化
     */
    private void initSelectedPoints() {
        if (TextUtils.isEmpty(password)){
            return;
        }
        if (selectedPoints.size()>0){
            return;
        }
        char[] chars=password.toCharArray();
        for (char c:chars){
            for (int i = 0; i < points.length; i++) {
                for (int j = 0; j < points[i].length; j++) {
                    if (points[i][j].getValue().equals(String.valueOf(c))){
                        points[i][j].setState(pointState);
                        selectedPoints.add(points[i][j]);
                    }
                }
            }
        }
    }


    /**
     * 画点与点之间的连线
     * @param canvas
     */
    private void drawConnectLines(Canvas canvas) {
        if (!isConnectPoint || selectedPoints.size() <= 0) {
            return;
        }
        Point point = selectedPoints.get(0);
        paint.setColor(getPaintColor(point.getState()));
        for (int i = 1; i < selectedPoints.size(); i++) {
            Point point1 = selectedPoints.get(i);
            if (point.getX() == point1.getX()) {
                float minY = Math.min(point.getY(), point1.getY());
                float maxY = Math.max(point.getY(), point1.getY());
                canvas.drawLine(point.getX(), minY + pointRadius, point1.getX(), maxY - pointRadius, paint);
            } else if (point.getY() == point1.getY()) {
                float minX = Math.min(point.getX(), point1.getX());
                float maxX = Math.max(point.getX(), point1.getX());
                canvas.drawLine(minX + pointRadius, point.getY(), maxX - pointRadius, point1.getY(), paint);
            } else if (point.getX() > point1.getX()) {
                float gap = (float) (Math.sqrt(2) / 2 * pointRadius);
                if (point.getY() > point1.getY()) {
                    canvas.drawLine(point.getX() - gap, point.getY() - gap, point1.getX() + gap, point1.getY() + gap, paint);
                } else {
                    canvas.drawLine(point.getX() - gap, point.getY() + gap, point1.getX() + gap, point1.getY() - gap, paint);
                }
            } else if (point.getX() < point1.getX()) {
                float gap = (float) (Math.sqrt(2) / 2 * pointRadius);
                if (point.getY() > point1.getY()) {
                    canvas.drawLine(point.getX() + gap, point.getY() - gap, point1.getX() - gap, point1.getY() + gap, paint);
                } else {
                    canvas.drawLine(point.getX() + gap, point.getY() + gap, point1.getX() - gap, point1.getY() - gap, paint);
                }
            }
            point = selectedPoints.get(i);
        }
    }

    /**
     * 画手指触摸处与最后一个选中点之间的连线
     * @param canvas
     */
    private void drawLines(Canvas canvas) {
        if (!isDrawLine) {
            return;
        }
        if (selectedPoints.size() <= 0) {
            return;
        }
        if (isFinish){
            return;
        }
        Point point = selectedPoints.get(selectedPoints.size() - 1);
        paint.setColor(getPaintColor(point.getState()));
        Point circlePoint = getCrossCirclePoint(point, mTouchX, mTouchY);
        canvas.drawLine(circlePoint.getX(), circlePoint.getY(), mTouchX, mTouchY, paint);
    }


    @Override
    public boolean onTouchEvent(MotionEvent event) {
        mTouchX = event.getX();
        mTouchY = event.getY();
        Point point = isSelectedPoint(mTouchX, mTouchY);
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                isFinish=false;
                resetPoints();
                if (point != null) {
                    selectedPoints.add(point);
                }
                break;
            case MotionEvent.ACTION_MOVE:
                if (selectedPoints.size() > 0 && (point== null)) {
                    isDrawLine = true;
                } else {
                    isDrawLine = false;
                }
                if (point != null) {
                    addSelectedPoint(point);
                    if (selectedPoints.size() >= 2 && selectedPoints.size() <= 5) {
                        isConnectPoint = true;
                    }
                }

                break;
            case MotionEvent.ACTION_UP:
                isFinish=true;
                if (selectedPoints.size()<=0){
                    return true;
                }
                for (Point point1:selectedPoints){
                    point1.setState(selectedPoints.size()==pswDigit?Point.STATE_SELECTED:Point.STATE_ERROR);
                    pointState=point1.getState();
                    password=password+point1.getValue();
                }
                if (lockListener!=null){
                    if (selectedPoints.size()==pswDigit){
                        lockListener.getPswSuccess(password);
                    }else {
                        lockListener.getPswError(password);
                    }
                }
                break;
        }
        invalidate();
        return true;
    }


    /**
     * 重置点的原始状态
     */
    private void resetPoints() {
        selectedPoints.clear();
        for (int i = 0; i < points.length; i++) {
            for (int j = 0; j < points[i].length; j++) {
                points[i][j].setState(Point.STATE_NORMAL);
            }
        }
        password="";
    }


    /**
     * @param point 被选中的点
     *          新增选中的点
     */
    private void addSelectedPoint(Point point) {
        if (selectedPoints.contains(point)) {
            return;
        }
        selectedPoints.add(point);
    }


    /**
     * 画出所有的点
     * @param canvas
     */
    private void drawAllPoints(Canvas canvas) {
        paint.setStyle(Paint.Style.FILL);
        for (int i = 0; i < points.length; i++) {
            for (int j = 0; j < points[i].length; j++) {
                Point point = points[i][j];
                paint.setColor(getPaintColor(points[i][j].getState()));
                canvas.drawCircle(point.getX(), point.getY(), pointRadius, paint);
            }
        }
    }


    /**获取从圆外一点与圆心的连线与圆的交点坐标
     * @param circlePoint   圆心坐标
     * @param outsidePointX  圆外一点横坐标
     * @param outsidePointY   圆外一点纵坐标
     * @return
     */
    private Point getCrossCirclePoint(Point circlePoint, float outsidePointX, float outsidePointY) {
        Point point = null;
        double distance = Math.pow(outsidePointX - circlePoint.getX(), 2) + Math.pow(outsidePointY - circlePoint.getY(), 2);
        float pointX = (float) (pointRadius * (outsidePointX - circlePoint.getX()) / Math.sqrt(distance) + circlePoint.getX());
        float pointY = (float) (pointRadius * (outsidePointY - circlePoint.getY()) / Math.sqrt(distance) + circlePoint.getY());
        point = new Point(pointX, pointY);
        return point;
    }


    /**
     * 判断点是否被选中
     * @param x 手指触摸的x坐标
     * @param y 手指触摸的y坐标
     * @return
     */
    private Point isSelectedPoint(float x, float y) {
        for (int i = 0; i < points.length; i++) {
            for (int j = 0; j < points[i].length; j++) {
                Point point1 = points[i][j];
                double distance = Math.pow(x - point1.getX(), 2) + Math.pow(y - point1.getY(), 2);
                if (Math.sqrt(distance) <= pointRadius) {
                    point1.setState(Point.STATE_SELECTED);
                    return point1;
                }
            }
        }
        return null;
    }


    /**
     * 初始化新点
     * @param i 行数
     * @param j 列数
     * @param value 点的数值
     * @return
     */
    private Point getPoint(int i, int j,String value) {
        float mSpace;
        float mAverageWidth;
        Point point = null;
        if (getMeasuredWidth() > getMeasuredHeight()) {
            mWidth = getMeasuredHeight() / 2;
            mSpace = (getMeasuredWidth() - mWidth) / 2;
            mAverageWidth = mWidth / 2;
            point = new Point(mSpace + mAverageWidth * j, mAverageWidth * (i + 1));
        } else {
            mWidth = getMeasuredWidth() / 2;
            mSpace = (getMeasuredHeight() - mWidth) / 2;
            mAverageWidth = mWidth / 2;
            point = new Point(mAverageWidth * (j + 1), mSpace + mAverageWidth * i);
        }
        point.setValue(value);
        return point;
    }


    /**
     * 根据点的状态返回画笔的颜色
     * @param state 点的状态
     * @return
     */
    private int getPaintColor(int state) {
        int color;
        switch (state) {
            case Point.STATE_SELECTED:
                color = selectedColor;
                break;
            case Point.STATE_ERROR:
                color = errorColor;
                break;
            default:
            case Point.STATE_NORMAL:
                color = defaultColor;
                break;
        }
        return color;
    }


    /**
     * 重置点的状态
     */
    public void clearPointState(){
        resetPoints();
        invalidate();
    }


    public void setLockListener(PatternLockListener lockListener) {
        this.lockListener = lockListener;
    }

    public void setDefaultColor(int defaultColor) {
        this.defaultColor = defaultColor;
    }

    public void setSelectedColor(int selectedColor) {
        this.selectedColor = selectedColor;
    }

    public void setErrorColor(int errorColor) {
        this.errorColor = errorColor;
    }

    public void setPointRadius(float pointRadius) {
        this.pointRadius = pointRadius;
    }

    public void setLineWith(float lineWith) {
        this.lineWith = lineWith;
    }

    public void setPswDigit(int pswDigit) {
        this.pswDigit = pswDigit;
    }



    @Nullable
    @Override
    protected Parcelable onSaveInstanceState() {
        super.onSaveInstanceState();
        Bundle bundle = new Bundle();
        bundle.putParcelable(PatternLockView.class.getSimpleName(),super.onSaveInstanceState());
        bundle.putInt("defaultColor", defaultColor);
        bundle.putInt("selectedColor", selectedColor);
        bundle.putInt("errorColor", errorColor);
        bundle.putFloat("pointRadius", pointRadius);
        bundle.putFloat("lineWith", lineWith);
        bundle.putFloat("mTouchX", mTouchX);
        bundle.putFloat("mTouchY", mTouchY);
        bundle.putInt("pswDigit", pswDigit);
        bundle.putBoolean("isDrawLine", isDrawLine);
        bundle.putBoolean("isConnectPoint", isConnectPoint);
        bundle.putBoolean("isFinish", isFinish);
        bundle.putString("password",password);
        bundle.putInt("pointState",pointState);
        return bundle;
    }


    @Override
    protected void onRestoreInstanceState(Parcelable state) {
        if (state instanceof Bundle){
            Bundle bundle= (Bundle) state;
            defaultColor=bundle.getInt("defaultColor");
            selectedColor=bundle.getInt("selectedColor");
            errorColor=bundle.getInt("errorColor");
            pswDigit=bundle.getInt("pswDigit");
            pointRadius=bundle.getFloat("pointRadius");
            lineWith=bundle.getFloat("lineWith");
            mTouchX=bundle.getFloat("mTouchX");
            mTouchY=bundle.getFloat("mTouchY");
            isDrawLine=bundle.getBoolean("isDrawLine");
            isConnectPoint=bundle.getBoolean("isConnectPoint");
            isFinish=bundle.getBoolean("isFinish");
            password=bundle.getString("password");
            pointState=bundle.getInt("pointState");
            super.onRestoreInstanceState(bundle.getParcelable(PatternLockView.class.getSimpleName()));
        }else {
            super.onRestoreInstanceState(state);
        }
    }




    public interface PatternLockListener{
        void getPswSuccess(String password);
        void getPswError(String password);
    }


}
