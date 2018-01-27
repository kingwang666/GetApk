package com.wang.baseadapter.util;




import com.wang.baseadapter.widget.SwipeItemView;

import java.util.List;


public interface SwipeItemMangerInterface {

    void openItem(int position);

    void closeItem(int position);

    void closeAllExcept(SwipeItemView layout);
    
    void closeAllItems();

    List<Integer> getOpenItems();

    List<SwipeItemView> getOpenLayouts();

    void removeShownLayouts(SwipeItemView layout);

    boolean isOpen(int position);

    SwipeItemView.Mode getMode();

    void setMode(SwipeItemView.Mode mode);

}
