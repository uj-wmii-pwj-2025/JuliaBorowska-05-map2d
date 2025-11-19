package uj.wmii.pwj.map2d;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

public class Map2DImplementation<R, C, V> implements Map2D<R, C, V>{
    private final Map<R, Map<C, V>> data = new HashMap<>();
    private int size = 0;

    public Map2DImplementation() {}

    @Override
    public int size() {
        return size;
    }

    @Override
    public V put(R row, C col, V val) {
        if (row==null || col==null) throw new NullPointerException();

        Map<C, V> innerMap = data.computeIfAbsent(row, k -> new HashMap<>());
        if (!innerMap.containsKey(col)) size++;

        return innerMap.put(col, val);
    }

    @Override
    public V getOrDefault(R row, C col, V defaultVal) {
        Map<C, V> innerMap = data.get(row);
        if (innerMap==null) return defaultVal;

        return innerMap.getOrDefault(col, defaultVal);
    }

    @Override
    public V get(R row, C col) {
        return getOrDefault(row, col, null);
    }

    @Override
    public V remove(R row, C col) {
        Map<C, V> innerMap = data.get(row);
        if (innerMap==null || !innerMap.containsKey(col)) return null;

        V prev = innerMap.remove(col);
        size--;
        if (innerMap.isEmpty()) data.remove(row);

        return prev;
    }

    @Override
    public boolean isEmpty() {
        return size==0;
    }

    @Override
    public boolean nonEmpty() {
        return size>0;
    }

    @Override
    public void clear() {
        data.clear();
        size = 0;
    }

    @Override
    public Map<C, V> rowView(R row) {
        Map<C, V> innerMap = data.get(row);
        if (innerMap==null) return Collections.emptyMap();

        return Collections.unmodifiableMap(new HashMap<>(innerMap));
    }

    @Override
    public Map<R, V> columnView(C col) {
        Map<R, V> result = new HashMap<>();

        for (R row : data.keySet()) {
            Map<C, V> innerMap = data.get(row);
            if(innerMap.containsKey(col)) result.put(row, innerMap.get(col));
        }

        return Collections.unmodifiableMap(result);
    }

    @Override
    public boolean containsValue(V val) {
        for(R row : data.keySet()) {
            Map<C, V> innerMap = data.get(row);
            if (innerMap.containsValue(val)) return true;
        }

        return false;
    }

    @Override
    public boolean containsKey(R row, C col) {
        Map<C, V> innerMap = data.get(row);
        return innerMap!=null && innerMap.containsKey(col);
    }

    @Override
    public boolean containsRow(R row) {
        Map<C, V> innerMap = data.get(row);
        return innerMap!=null && !innerMap.isEmpty();
    }

    @Override
    public boolean containsColumn(C col) {
        for(R row : data.keySet()) {
            Map<C, V> innerMap = data.get(row);
            if (innerMap.containsKey(col)) return true;
        }

        return false;
    }

    @Override
    public Map<R, Map<C,V>> rowMapView() {
        if (data.isEmpty()) return Collections.emptyMap();

        Map<R, Map<C, V>> result = new HashMap<>();

        for(R row : data.keySet()) {
            Map<C, V> innerCopy = Collections.unmodifiableMap(new HashMap<>(data.get(row)));
            result.put(row, innerCopy);
        }

        return Collections.unmodifiableMap(result);
    }

    @Override
    public Map<C, Map<R,V>> columnMapView() {
        if (data.isEmpty()) return Collections.emptyMap();

        Map<C, Map<R, V>> temp = new HashMap<>();
        Map<C, Map<R, V>> result = new HashMap<>();

        for(R row : data.keySet()) {
            Map<C, V> innerMap = data.get(row);

            for(C col : innerMap.keySet()) {
                V val = innerMap.get(col);
                temp.computeIfAbsent(col, c -> new HashMap<>())
                        .put(row, val);
            }
        }

        for(C col : temp.keySet()) {
            Map<R, V> innerMap = temp.get(col);
            result.put(col, Collections.unmodifiableMap(innerMap));
        }

        return Collections.unmodifiableMap(result);
    }

    @Override
    public Map2D<R, C, V> fillMapFromRow(Map<? super C, ? super V> target, R row) {
        Map<C, V> innerMap = data.get(row);
        if (innerMap==null) return this;

        for(C col : innerMap.keySet()) {
            target.put(col, innerMap.get(col));
        }

        return this;
    }

    @Override
    public Map2D<R, C, V> fillMapFromColumn(Map<? super R, ? super V> target, C col) {
        for (R row : data.keySet()) {
            Map<C, V> innerMap = data.get(row);
            if (innerMap.containsKey(col)) {
                target.put(row, innerMap.get(col));
            }
        }

        return this;
    }

    @Override
    public Map2D<R, C, V>  putAll(Map2D<? extends R, ? extends C, ? extends V> source) {
        Map<? extends R, ? extends Map<? extends C, ? extends V>> rows = source.rowMapView();

        for(R row : rows.keySet()) {
            Map<? extends C, ? extends V> innerMap = rows.get(row);

            for (C col : innerMap.keySet()) {
                put(row, col, innerMap.get(col));
            }
        }

        return this;
    }

    @Override
    public Map2D<R, C, V>  putAllToRow(Map<? extends C, ? extends V> source, R row) {
        for (C col : source.keySet()) {
            put(row, col, source.get(col));
        }

        return this;
    }

    @Override
    public Map2D<R, C, V>  putAllToColumn(Map<? extends R, ? extends V> source, C col) {
        for (R row : source.keySet()) {
            put(row, col, source.get(row));
        }

        return this;
    }

    @Override
    public <R2, C2, V2> Map2D<R2, C2, V2> copyWithConversion(
            Function<? super R, ? extends R2> rowFunction,
            Function<? super C, ? extends C2> columnFunction,
            Function<? super V, ? extends V2> valueFunction) {
        Map2D<R2, C2, V2> result = Map2D.createInstance();

        for (R row : data.keySet()) {
            Map <C, V> innerMap = data.get(row);

            for (C col : innerMap.keySet()) {
                V val = innerMap.get(col);

                R2 newRow = rowFunction.apply(row);
                C2 newCol = columnFunction.apply(col);
                V2 newVal = valueFunction.apply(val);

                result.put(newRow, newCol, newVal);
            }
        }

        return result;
    }
}
