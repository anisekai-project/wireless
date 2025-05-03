package fr.anisekai.wireless.utils;

import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.function.BiConsumer;
import java.util.function.BinaryOperator;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collector;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Utility class to handle {@link Map} related methods.
 */
@SuppressWarnings("ClassCanBeRecord")
public final class MapUtils {

    private static class MappingCollector<T, K, V> implements Collector<T, Map<K, V>, Map<K, V>> {

        private final Function<T, K> keyMapper;
        private final Function<T, V> valueMapper;

        public MappingCollector(Function<T, K> keyMapper, Function<T, V> valueMapper) {

            this.keyMapper   = keyMapper;
            this.valueMapper = valueMapper;
        }

        @Override
        public Supplier<Map<K, V>> supplier() {

            return HashMap::new;
        }

        @Override
        public Function<Map<K, V>, Map<K, V>> finisher() {

            return item -> item;
        }

        @Override
        public Set<Characteristics> characteristics() {

            return Set.of(Characteristics.UNORDERED);
        }

        @Override
        public BiConsumer<Map<K, V>, T> accumulator() {

            return (map, item) -> map.put(this.keyMapper.apply(item), this.valueMapper.apply(item));
        }

        @Override
        public BinaryOperator<Map<K, V>> combiner() {

            return (left, right) -> {
                left.putAll(right);
                return left;
            };
        }

    }

    private static class GroupingCollector<T, K, V> implements Collector<T, Map<K, List<V>>, Map<K, List<V>>> {

        private final Function<T, K> keyMapper;
        private final Function<T, V> valueMapper;

        public GroupingCollector(Function<T, K> keyMapper, Function<T, V> valueMapper) {

            this.keyMapper   = keyMapper;
            this.valueMapper = valueMapper;
        }

        @Override
        public Supplier<Map<K, List<V>>> supplier() {

            return HashMap::new;
        }

        @Override
        public Function<Map<K, List<V>>, Map<K, List<V>>> finisher() {

            return item -> item;
        }

        @Override
        public Set<Characteristics> characteristics() {

            return Set.of(Characteristics.UNORDERED);
        }

        @Override
        public BiConsumer<Map<K, List<V>>, T> accumulator() {

            return (map, item) -> {
                K key   = this.keyMapper.apply(item);
                V value = this.valueMapper.apply(item);

                if (map.containsKey(key)) {
                    map.get(key).add(value);
                } else {
                    List<V> values = new ArrayList<>();
                    values.add(value);
                    map.put(key, values);
                }
            };
        }

        @Override
        public BinaryOperator<Map<K, List<V>>> combiner() {

            return (left, right) -> {
                for (K key : right.keySet()) {
                    if (left.containsKey(key)) {
                        left.get(key).addAll(right.get(key));
                    } else {
                        left.put(key, right.get(key));
                    }
                }
                return left;
            };
        }

    }

    private MapUtils() {}

    /**
     * Retrieve a {@link Collector} allowing to map any value of choice to an object, where the object would be the
     * key.
     *
     * @param valueMapper
     *         The function that will be used to extract the value of an object, used as value for the map.
     * @param <T>
     *         Type of the input object
     * @param <V>
     *         Type of the value for the map
     *
     * @return A collector to use in {@link Stream}
     */
    public static <T, V> Collector<T, Map<T, V>, Map<T, V>> map(Function<T, V> valueMapper) {

        return new MappingCollector<>(item -> item, valueMapper);
    }

    /**
     * Retrieve a {@link Collector} allowing to map any value of choice to another value.
     *
     * @param keyMapper
     *         The function that will be used to extract the value of an object, used as key for the map.
     * @param valueMapper
     *         The function that will be used to extract the value of an object, used as value for the map.
     * @param <T>
     *         Type of the input object
     * @param <K>
     *         Type of the key for the map
     * @param <V>
     *         Type of the value for the map
     *
     * @return A collector to use in {@link Stream}
     */
    public static <T, K, V> Collector<T, Map<K, V>, Map<K, V>> map(Function<T, K> keyMapper, Function<T, V> valueMapper) {

        return new MappingCollector<>(keyMapper, valueMapper);
    }

    /**
     * Retrieve a {@link Collector} allowing to group any set of values to an object.
     *
     * @param valueMapper
     *         The function that will be used to extract the value of an object, used as value item for the map.
     * @param <T>
     *         Type of the input object
     * @param <V>
     *         Type of the value for the grouping list
     *
     * @return A collector to use in {@link Stream}
     */
    public static <T, V> Collector<T, Map<T, List<V>>, Map<T, List<V>>> group(Function<T, V> valueMapper) {

        return new GroupingCollector<>(item -> item, valueMapper);
    }

    /**
     * Retrieve a {@link Collector} allowing to group any set of values to another value.
     *
     * @param keyMapper
     *         The function that will be used to extract the value of an object, used as key for the map.
     * @param valueMapper
     *         The function that will be used to extract the value of an object, used as value item for the map.
     * @param <T>
     *         Type of the input object
     * @param <K>
     *         Type of the key for the map
     * @param <V>
     *         Type of the value for the grouping list
     *
     * @return A collector to use in {@link Stream}
     */
    public static <T, K, V> Collector<T, Map<K, List<V>>, Map<K, List<V>>> group(Function<T, K> keyMapper, Function<T, V> valueMapper) {

        return new GroupingCollector<>(keyMapper, valueMapper);
    }


    /**
     * Creates a {@link Map} from a {@link Collection} by grouping elements based on a provided key extractor function.
     * <p>
     * This method is particularly useful when you have a collection of objects with common properties, and you want to
     * group them based on those properties.
     *
     * @param collection
     *         The {@link Collection} of objects from which the {@link Map} will be generated
     * @param classifier
     *         The {@link Function} to extract the key for each object
     * @param <K>
     *         The type of the keys in the resulting map
     * @param <V>
     *         The type of the objects in the collection
     *
     * @return a {@link Map} associating each object's property to a list of objects with that property
     *
     * @deprecated Recommended to use {@link #group(Function)} or {@link #group(Function, Function)} as it is more
     *         flexible.
     */
    @Deprecated
    public static <K, V> Map<K, List<V>> groupBy(@NotNull Collection<V> collection, Function<? super V, ? extends K> classifier) {

        return collection.stream().collect(Collectors.groupingBy(classifier, HashMap::new, Collectors.toList()));
    }

    /**
     * Creates a {@link Map} from a {@link Collection} by mapping each element to a key-value pair using a provided key
     * extractor function.
     * <p>
     * This method is particularly useful when you have a collection of objects with different property values, and you
     * want to map them directly to those properties.
     *
     * @param collection
     *         The {@link Collection} of objects from which the {@link Map} will be generated
     * @param classifier
     *         The {@link Function} to extract the key for each object
     * @param <K>
     *         The type of the keys in the resulting map
     * @param <V>
     *         The type of the objects in the collection
     *
     * @return a {@link Map} associating each object's property directly to the object
     *
     * @deprecated Recommended to use {@link #map(Function)} or {@link #map(Function, Function)} as it is more flexible.
     */
    @Deprecated
    public static <K, V> Map<K, V> mapBy(@NotNull Collection<V> collection, Function<? super V, ? extends K> classifier) {

        return collection.stream().collect(Collectors.toMap(classifier, item -> item));
    }

}
