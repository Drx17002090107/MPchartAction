package com.example.machenike.mpchartaction;

import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineDataSet;

import java.util.List;

public class MyLIneChartSet extends LineDataSet {
    public MyLIneChartSet(List<Entry> yVals, String label) {
        super(yVals, label);
    }

    @Override
    public int getCircleColor(int index) {
        float a=getEntryForXIndex(index).getVal();
        //判断是否超标 ，若超标则将节点变成红色
        if(a>200){
            return getCircleColors().get(0);
        }
        else {
            return getCircleColors().get(1);
        }
    }

}
