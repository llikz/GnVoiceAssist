package com.gionee.gnvoiceassist.message.model.render;

import java.util.List;

/**
 * Created by liyingheng on 11/7/17.
 */

public class ListRenderEntity extends RenderEntity {

    //列表
    private List<ListItemModel> list;

    public List<ListItemModel> getList() {
        return list;
    }

    public void setList(List<ListItemModel> list) {
        this.list = list;
    }
}