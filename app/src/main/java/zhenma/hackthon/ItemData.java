package zhenma.hackthon;

import com.google.api.client.util.DateTime;
/**
 * Created by xuehanyu on 4/13/16.
 */
public class ItemData {
    int color;
    public int icon;
    public String title;
    public DateTime time;

    public ItemData(int color, int icon, String title, DateTime time) {
        this.color = color;
        this.icon = icon;
        this.title = title;
        this.time = time;
    }
}
