package com.example.patternlocklibrary;

import android.os.Parcel;
import android.os.Parcelable;

public class Point implements Parcelable{
    private float x;
    private float y;
    private int state = STATE_NORMAL;
    private String value;


    public static final int STATE_NORMAL = 0;
    public static final int STATE_SELECTED = 1;
    public static final int STATE_ERROR = 2;

    public Point() {
    }

    public Point(float x, float y) {
        this.x = x;
        this.y = y;
    }

    protected Point(Parcel in) {
        x = in.readFloat();
        y = in.readFloat();
        state = in.readInt();
        value = in.readString();
    }

    public static final Creator<Point> CREATOR = new Creator<Point>() {
        @Override
        public Point createFromParcel(Parcel in) {
            return new Point(in);
        }

        @Override
        public Point[] newArray(int size) {
            return new Point[size];
        }
    };

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
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

    public int getState() {
        return state;
    }

    public void setState(int state) {
        this.state = state;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeFloat(x);
        parcel.writeFloat(y);
        parcel.writeInt(state);
        parcel.writeString(value);
    }
}
