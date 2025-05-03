package fr.anisekai.wireless.utils;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.BinaryOperator;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collector;

/**
 * A custom {@link Collector} implementation that collects elements into a {@link Map}, using the element itself as the key and a
 * value derived from a mapping function.
 *
 * @param <T>
 *         The type of input elements.
 * @param <K>
 *         The type of values to associate with each key.
 */
public class MapCollector<T, K> implements Collector<T, Map<T, K>, Map<T, K>> {

    private final Function<T, K> valueMapper;

    /**
     * Create a new {@code MapCollector} with the given value mapping function.
     *
     * @param valueMapper
     *         A function that computes the value for each key based on the input element.
     */
    public MapCollector(Function<T, K> valueMapper) {

        this.valueMapper = valueMapper;
    }

    @Override
    public Supplier<Map<T, K>> supplier() {

        return HashMap::new;
    }

    @Override
    public BiConsumer<Map<T, K>, T> accumulator() {

        return (map, item) -> map.put(item, this.valueMapper.apply(item));
    }

    @Override
    public BinaryOperator<Map<T, K>> combiner() {

        return (left, right) -> {
            left.putAll(right);
            return left;
        };
    }

    @Override
    public Function<Map<T, K>, Map<T, K>> finisher() {

        return item -> item;
    }

    @Override
    public Set<Characteristics> characteristics() {

        return Set.of(Characteristics.UNORDERED);
    }

}
