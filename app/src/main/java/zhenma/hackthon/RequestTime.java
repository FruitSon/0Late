package zhenma.hackthon;

import android.os.Handler;
import android.os.Message;

/**
 * Created by dnalwqer on 4/16/16.
 */
class myThread extends Thread{
    private String start, end, mode;
    private Handler handler;

    public myThread(String start, String end, String mode) {
        this.start = start;
        this.end = end;
        this.mode = mode;
    }

    public void setHandler(Handler handler) {
        this.handler = handler;
    }
    public void run(){
        UtilHelper httpclient = new UtilHelper();
        String xml;
        String n_start = start.replace(" ", "+");
        String n_end = end.replace(" ", "+");
        System.out.println(n_start + " " + n_end);
        String url = "https://maps.googleapis.com/maps/api/distancematrix/xml?mode="+mode+"&origins="+n_start+"&destinations="+n_end+"&key=AIzaSyCOGAfDGxrDHfvjvCyHubzdS5NntrY5W3o";
        while(true){
            try {
                xml = httpclient.getXML(url);
//                System.out.println(xml);
                Message msg = handler.obtainMessage();
                msg.obj = xml;
                handler.sendMessage(msg);
                Thread.sleep(300*1000);
            } catch (Exception e1) {
                e1.printStackTrace();
            }
        }
    }
}