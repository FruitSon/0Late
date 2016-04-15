package zhenma.hackthon;

import android.graphics.Color;

import com.google.api.client.util.DateTime;

import java.util.Date;
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
