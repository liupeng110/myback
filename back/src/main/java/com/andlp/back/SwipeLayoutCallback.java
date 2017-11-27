package com.andlp.back;

public interface SwipeLayoutCallback {
    
    public abstract SwipeBackLayout getSwipeBackLayout();//返回与此活动关联的SwipeBackLayout
    public abstract void setSwipeBackEnable(boolean enable);
    public abstract void scrollToFinishActivity();//滚动contentView并完成活动

}
