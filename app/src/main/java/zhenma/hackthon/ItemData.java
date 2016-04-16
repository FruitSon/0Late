package zhenma.hackthon;

/**
 * Created by xuehanyu on 4/13/16.
 */
public class ItemData {
    int color;
    public int icon;
    public int walk;
    public int drive;
    public int bus;
    public String id;
    public String title;
    public String startTime;   //StartTime
    public String date;

    public ItemData(int color, int icon, String title, String startTime, int walk, int drive, int bus, String id, String date) {
        this.color = color;
        this.icon = icon;
        this.title = title;
        this.startTime = startTime;
        this.walk = walk;
        this.drive = drive;
        this.bus = bus;
        this.id = id;
        this.date = date;
    }
}
