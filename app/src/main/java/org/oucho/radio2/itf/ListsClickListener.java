package org.oucho.radio2.itf;


public interface ListsClickListener {
    void onHeaderClick();
    void onPlayableItemClick(PlayableItem item);
    void onPlayableItemMenuClick(PlayableItem item, int menuId);
    void onCategoryClick(Object item);
    void onCategoryMenuClick(Object item, int menuId);
}
