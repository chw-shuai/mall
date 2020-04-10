package com.imooc.mall.service.impl;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class test {
    public static void main(String[] args) {
        BigDecimal num = BigDecimal.valueOf(1);
        Integer num2 = 2;
        System.out.println(num.multiply(BigDecimal.valueOf(num2)));

        Map map = new HashMap();
        map.put("1","a");
        map.put("2","b");
        map.put("3","c");

        Iterator<Map.Entry<Integer, Integer>> it=map.entrySet().iterator();
        while(it.hasNext()) {
            Map.Entry<Integer,Integer> entry=it.next();
          //  Integer key=entry.getKey();
          //  Integer value=entry.getValue();
            System.out.println(entry);
            System.out.println(entry.getKey()+" "+entry.getValue());
        }

    }
}
