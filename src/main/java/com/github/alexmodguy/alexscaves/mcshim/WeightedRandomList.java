package com.github.alexmodguy.alexscaves.mcshim;
import net.minecraft.util.random.*;

import net.minecraft.util.RandomSource;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class WeightedRandomList<E> {
    private final WeightedList<E> delegate;

    private WeightedRandomList(WeightedList<E> delegate) {
        this.delegate = delegate;
    }

    public static <E> WeightedRandomList<E> from(WeightedList<E> delegate) {
        return new WeightedRandomList<>(delegate);
    }

    public static <E> WeightedRandomList<E> create(List<E> entries) {
        return new WeightedRandomList<>(WeightedList.of(
                entries.stream().map(entry -> new Weighted<>(entry, 1)).collect(Collectors.toList())));
    }

    public boolean isEmpty() {
        return delegate.isEmpty();
    }

    public Optional<E> getRandom(RandomSource randomSource) {
        return delegate.getRandom(randomSource);
    }

    public List<E> unwrap() {
        return delegate.unwrap().stream().map(Weighted::value).toList();
    }
}
