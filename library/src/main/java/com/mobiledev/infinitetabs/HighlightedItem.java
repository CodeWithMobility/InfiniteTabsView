package com.mobiledev.infinitetabs;

import android.view.View;

import static com.mobiledev.infinitetabs.InfiniteTabView.getSelectedColor;
import static com.mobiledev.infinitetabs.InfiniteTabView.getUnSelectedColor;

/**
 * Created by mdev3 on 4/11/17.
 */

public class HighlightedItem implements TabLayoutManager.ItemSelectionHighlightListener {
    @Override
    public void highlightItem(TabLayoutManager layoutManager, View item, float fraction) {
        item.setPivotX(item.getWidth() / 2.f);
        item.setPivotY(item.getHeight() / 2.0f);
        //   float scale = 1 - 0.2f * Math.abs(fraction);

        if (fraction == 0.0) {
            item.setBackgroundColor(getSelectedColor());
        } else {
            item.setBackgroundColor(getUnSelectedColor());
        }
    }
}
