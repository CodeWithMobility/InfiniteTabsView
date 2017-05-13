package com.mobiledev.infinitetabs;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.support.annotation.Nullable;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;

/**
 * Created by Manu on 5/12/2017.
 */

public class InfiniteTabView extends RecyclerView {

    private static int NORMAL_COLOR = Color.GRAY;
    private static int SELECTED_COLOR = Color.RED;
    private static int NORMAL_TEXT_COLOR = Color.BLACK;
    private static int SELECTED_TEXT_COLOR = Color.WHITE;


    public InfiniteTabView(Context context) {
        super(context);
        init(null);
    }

    public InfiniteTabView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init(attrs);
    }

    public InfiniteTabView(Context context, @Nullable AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init(attrs);
    }

    private void init(AttributeSet attrs) {
        if (attrs != null) {
            TypedArray a = getContext().obtainStyledAttributes(attrs, R.styleable.CustomColor);
            String selectedColor = a.getString(R.styleable.CustomColor_selectedcolorCode);
            String unSelectedColor = a.getString(R.styleable.CustomColor_unSelectedcolorCode);
            String textSelectedColor = a.getString(R.styleable.CustomColor_textUnSelectedColorCode);
            String textUnSelectedColor = a.getString(R.styleable.CustomColor_textUnSelectedColorCode);
            if (selectedColor != null) {
                setSelectedColor(Color.parseColor(selectedColor));
            }
            if (unSelectedColor != null) {
               setUnSelectedColor(Color.parseColor(unSelectedColor));
              }
            if (textSelectedColor != null) {
                setTextSelectedColor(Color.parseColor(textSelectedColor));
            }
            if (textUnSelectedColor != null) {
                setTextUnSelectedColor(Color.parseColor(textUnSelectedColor));
            }
            a.recycle();
        }
    }

    public static int getSelectedColor() {
        return SELECTED_COLOR;
    }

    public  void setSelectedColor(int color) {
        SELECTED_COLOR = color;
    }

    public static int getUnSelectedColor() {
        return NORMAL_COLOR;
    }

    public  void setUnSelectedColor(int color) {
        NORMAL_COLOR = color;
    }


    public static int getTextSelectedColor(){
        return SELECTED_TEXT_COLOR;
    }

    public static void setTextSelectedColor(int color){
        SELECTED_TEXT_COLOR = color;
    }

    public static int getTextUnSelectedColor(){
        return NORMAL_TEXT_COLOR;
    }

    public static void setTextUnSelectedColor(int color){
        NORMAL_TEXT_COLOR = color;
    }
}
