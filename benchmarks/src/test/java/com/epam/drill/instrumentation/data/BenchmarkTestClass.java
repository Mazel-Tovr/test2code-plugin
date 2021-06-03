package com.epam.drill.instrumentation.data;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class BenchmarkTestClass implements Runnable {
    @Override
    public void run() {
        BenchmarkTestClass f = new BenchmarkTestClass(20113L, name, 312, new ArrayList<>(), new HashMap<>(), true);
        long start = System.nanoTime();
        f.gettersAndSettersInvoker();
        System.out.println("Getters and setters" + (System.nanoTime() - start));
        start = System.nanoTime();
        f.ifCleanInvoker();
        System.out.println("Clear if " + (System.nanoTime() - start));
        start = System.nanoTime();
        f.ifGettersInvoker();
        System.out.println("Dirty if" + (System.nanoTime() - start));
//        start = System.nanoTime();
//        System.out.println("" + (System.nanoTime() - start));
        start = System.nanoTime();
        f.gettersAndSettersInvoker();
        System.out.println("Getters and setters Second time " + (System.nanoTime() - start));
        start = System.nanoTime();
    }

    public BenchmarkTestClass() {
    }

    private Long id;

    private String name;

    private int count;

    private List<Object> objectList;

    private HashMap<String, Integer> stringIntegerHashMap;

    private boolean flag;


    public BenchmarkTestClass(Long id, String name, int count, List<Object> objectList, HashMap<String, Integer> stringIntegerHashMap, boolean flag) {
        this.id = id;
        this.name = name;
        this.count = count;
        this.objectList = objectList;
        this.stringIntegerHashMap = stringIntegerHashMap;
        this.flag = flag;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getCount() {
        return count;
    }

    public void setCount(int count) {
        this.count = count;
    }

    public HashMap<String, Integer> getStringIntegerHashMap() {
        return stringIntegerHashMap;
    }

    public void setStringIntegerHashMap(HashMap<String, Integer> stringIntegerHashMap) {
        this.stringIntegerHashMap = stringIntegerHashMap;
    }

    public boolean isFlag() {
        return flag;
    }

    public void setFlag(boolean flag) {
        this.flag = flag;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public List<Object> getObjectList() {
        return objectList;
    }

    public void setObjectList(List<Object> objectList) {
        this.objectList = objectList;
    }

    public void gettersAndSettersInvoker() {
        getId();
        getName();
        getCount();
        isFlag();
        getStringIntegerHashMap();
        getObjectList();

        setId(123123123L);
        setName("Sashenbka");
        setCount(4);
        setFlag(false);
        setObjectList(new ArrayList<>());
        setStringIntegerHashMap(new HashMap<>());

    }

    public void ifCleanInvoker() {
        for (int i = 0; i < 100; i += 10) {
            int sum = i;
            if (flag) {
                long var1 = (id % 33) * 21;
                if (var1 % 5 > 0) {
                    sum += var1;
                    id = id + sum;
                    if (id / 2 * 84 > 24123) {
                        var1 = sum * id + count;
                        sum += var1;
                        stringIntegerHashMap.put(name, sum);
                    } else {
                        stringIntegerHashMap.put(name, sum);
                    }
                } else {
                    var1 = (id % 55) * 12;
                    if (var1 % 9 > 0) {
                        sum += var1;
                        id = id + sum;
                        if (id * 841 / 2 > 24123) {
                            var1 = sum * id + count;
                            sum += var1;
                            stringIntegerHashMap.put(name, sum);
                        } else {
                            stringIntegerHashMap.put(name, sum);
                        }
                    }

                }
            } else {
                long var1 = (id * 21) % 2;
                if (var1 % 10 > 0) {
                    sum += var1;
                    id = id + sum;
                    if (id / 2 * 84 > 2413) {
                        var1 = sum * id + count;
                        sum += var1;
                        stringIntegerHashMap.put(name, sum);
                    } else {
                        stringIntegerHashMap.put(name, sum);
                    }
                } else {
                    var1 = (id % 55) * 12;
                    if (var1 % 9 > 0) {
                        sum += var1;
                        id = id + sum;
                        if (id * 841 / 2 > 2413) {
                            var1 = sum * id + count;
                            sum += var1;
                            stringIntegerHashMap.put(name, sum);
                        } else {
                            stringIntegerHashMap.put(name, sum);
                        }
                    }
                }
            }
        }
    }


    public void ifGettersInvoker() {
        for (int i = 0; i < 100; i += 10) {
            int sum = i;
            if (isFlag()) {
                long var1 = (getId() % 33) * 21;
                if (var1 % 5 > 0) {
                    sum += var1;
                    setId(getId() + sum);
                    if (getId() / 2 * 84 > 24123) {
                        var1 = sum * getId() + getId();
                        sum += var1;
                        getStringIntegerHashMap().put(name, sum);
                    } else {
                        getStringIntegerHashMap().put(name, sum);
                    }
                } else {
                    var1 = (getId() % 55) * 12;
                    if (var1 % 9 > 0) {
                        sum += var1;
                        setId(getId() + sum);
                        if (getId() * 841 / 2 > 24123) {
                            var1 = sum * getId() + getCount();
                            sum += var1;
                            getStringIntegerHashMap().put(name, sum);
                        } else {
                            getStringIntegerHashMap().put(name, sum);
                        }
                    }
                }
            } else {
                long var1 = (getId() * 21) % 2;
                if (var1 % 10 > 0) {
                    sum += var1;
                    setId(getId() + sum);
                    if (getId() / 2 * 84 > 2413) {
                        var1 = sum * getId() + getCount();
                        sum += var1;
                        getStringIntegerHashMap().put(name, sum);
                    } else {
                        getStringIntegerHashMap().put(name, sum);
                    }
                } else {
                    var1 = (getId() % 55) * 12;
                    if (var1 % 9 > 0) {
                        sum += var1;
                        setId(getId() + sum);
                        if (getId() * 841 / 2 > 2413) {
                            var1 = sum * getId() + getCount();
                            sum += var1;
                            getStringIntegerHashMap().put(name, sum);
                        } else {
                            getStringIntegerHashMap().put(name, sum);
                        }
                    }
                }
            }
        }
    }

}
