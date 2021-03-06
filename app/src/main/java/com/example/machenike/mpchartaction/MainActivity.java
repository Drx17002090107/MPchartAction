package com.example.machenike.mpchartaction;


import android.content.Intent;
import android.graphics.Color;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.LimitLine;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;



import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

import okhttp3.Call;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class MainActivity extends AppCompatActivity {
    public static final MediaType JSON = MediaType.parse("application/json;charset=utf-8");
    private MyLIneChartSet lineDataSet;
    private int counter = 0;//计数器
    private TextView textView;
    private float max = 0, min = 1000;
    private float[] data = new float[1000];
    private Timer timer;
    String response1 = "";
    String res = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        textView = (TextView) findViewById(R.id.show);
        final LineChart lineChart = (LineChart) findViewById(R.id.linechart);
        lineChart.getXAxis().setPosition(XAxis.XAxisPosition.BOTTOM);//设置X轴位置
        lineChart.getXAxis().setTextSize(20);//设置x轴的字体大小


        final List<String> xList = new ArrayList<>();
        final List<Entry> yList = new ArrayList<>();


        Button button = (Button) findViewById(R.id.button);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(MainActivity.this, Main2Activity.class));
                overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);//淡入淡出
            }
        });

        this.timer = new Timer();
        timer.schedule(new TimerTask(){
                public void run() {
////                    Map map = new HashMap();
////                    map.put("aa", "co2");
////                    OkHttpClient client=new OkHttpClient();
////                    String url="http://10.0.3.2:8080/ss";
////                    Request request=new Request.Builder()
////                            .url(url)
////                            .post(RequestBody.create(JSON,new JSONObject(map).toString()))
////                            .build();
//
//                    JSONObject jo=new JSONObject();
//                    Response response=null;
//                    try {
//                        response = client.newCall(request).execute();
//
//                        if (response.isSuccessful()) {
//                            String jsondata = response.body().string();
//                            jo = new JSONObject(jsondata);
////                            Toast.makeText(MainActivity.this,jo.getString("id"),Toast.LENGTH_SHORT).show();
//
//                        } else {
//                            //TODO
//                        }
//                    } catch (IOException e) {
//                        e.printStackTrace();
//                    } catch (JSONException e) {
//                        e.printStackTrace();

                    HttpUtil.sendOkHttpRequest("http://10.0.3.2:8080/ss", new okhttp3.Callback() {
                        @Override
                        public void onFailure(Call call, IOException e) {

                        }

                        @Override
                        public void onResponse(Call call, Response response) throws IOException {
                            response1 = response.body().string();
                            Log.d("response", "onResponse: ");
                        }
                    });
                    JSONObject jo = null;
                    try {
                        jo = new JSONObject(response1);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                    if (jo!=null){
                        final String pm25 = parseJSON(jo);
                        Log.d("ss: ", pm25);

                    runOnUiThread(new Runnable() {
                            @Override
                            public void run() {

                                if (counter == 0) {
                                    lineDataSet = new MyLIneChartSet(yList, "pm25");
                                    //设置点的颜色
                                    lineDataSet.setCircleColors(new int[]{
                                            MainActivity.this.getResources().getColor(R.color.abc_hint_foreground_material_dark),
                                            MainActivity.this.getResources().getColor(R.color.abc_hint_foreground_material_light),
                                    });
                                    //设置线的颜色
                                    lineDataSet.setColor(Color.BLUE);

                                    xList.add("" + new SimpleDateFormat("hh:mm:ss").format(new Date()));
                                    yList.add(new Entry((Float.parseFloat(pm25)), counter));

                                    LineData data = new LineData(xList, lineDataSet);
                                    lineChart.setData(data);
                                } else {
                                    lineChart.getLineData().addEntry(new Entry(Float.parseFloat(pm25), counter), 0);
                                    lineChart.getLineData().addXValue("" + new SimpleDateFormat("hh:mm:ss").format(new Date()));
                                }
                                counter++;
                                lineChart.getLineData().notifyDataChanged();
                                lineChart.notifyDataSetChanged();
                                //设置x轴显示的最多最少数量
                                lineChart.setVisibleXRangeMaximum(10);
                                lineChart.setVisibleXRangeMinimum(5);
                                if (counter > 10) {
                                    lineChart.moveViewToX(counter - 10);//移动坐标轴，把counter-10这个点当作原点
                                }
                                lineChart.invalidate();
                                //预警线
                                YAxis leftAxis=lineChart.getAxisLeft();
                                LimitLine ll=new LimitLine(200f,"Critical Blood Pressure");
                                ll.setLineColor(Color.RED);
                                ll.setLineWidth(2f);
                                leftAxis.addLimitLine(ll);

                                data[counter] = Float.parseFloat(pm25);

                                if (counter <= 30) {
                                    if (Float.parseFloat(pm25) > max) {
                                        max = Float.parseFloat(pm25);
                                    }
                                    if (Float.parseFloat(pm25) < min) {
                                        min = Float.parseFloat(pm25);
                                    }
                                } else {
                                    for (int i = (counter - 30); i < counter; i++) {
                                        if (data[i] > max) {
                                            max = data[i];
                                        }
                                        if (data[i] < min) {
                                            min = data[i];
                                        }
                                    }
                                }
                                textView.setText("max:" + max + "  min:" + min);
                            }
                        });
                    }
                }

            }, 500, 500);


    }

    private String parseJSON(JSONObject jsonData) {
        String result = "";
        String pm25 = null;
        try {
            pm25 = jsonData.getString("SS");
            res = jsonData.getString("RESULT");
        } catch (JSONException e) {
            e.printStackTrace();
        }
        result = result + pm25 + " ";

        return result;
    }
    @Override
    protected void onDestroy() {
        super.onDestroy();
        timer.cancel();
    }
}
