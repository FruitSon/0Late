package zhenma.hackthon;

import android.graphics.Color;
import java.util.Date;
/**
 * Created by xuehanyu on 4/13/16.
 */
public class ItemData {
    int color;
    public int icon;
    public String title;
    public Date time;

    public ItemData(int color, int icon, String title, Date time) {
        this.color = color;
        this.icon = icon;
        this.title = title;
        this.time = time;
    }

    public ItemData(int icon, String title) {
        this(Color.DKGRAY, icon, title, new Date());
    }
}
