package com.github.leblancjs.htp.java.collections;

import java.util.Collection;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.Spliterator;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;
import java.util.function.IntFunction;
import java.util.function.Predicate;
import java.util.function.UnaryOperator;
import java.util.stream.Stream;

import static java.util.Objects.requireNonNull;

/**
 * An {@code AtomicList} stores an immutable list in an {@link AtomicReference}
 * to make it thread safe.
 *
 * @param <E> the type of elements in the list
 */
public class AtomicList<E> implements List<E> {
    private final UnaryOperator<List<E>> generator;
    private final AtomicReference<List<E>> elements;

    public AtomicList() {
        this(List::copyOf, List.of());
    }

    public AtomicList(UnaryOperator<List<E>> generator) {
        this(generator, List.of());
    }

    public AtomicList(UnaryOperator<List<E>> generator, List<E> elements) {
        this.generator = requireNonNull(generator);
        this.elements = new AtomicReference<>(List.copyOf(elements));
    }

    private List<E> createDraft(List<E> elements) {
        return this.generator.apply(elements);
    }

    private List<E> commitDraft(List<E> elements) {
        return List.copyOf(elements);
    }

    @Override
    public int size() {
        return this.elements.get().size();
    }

    @Override
    public boolean isEmpty() {
        return this.elements.get().isEmpty();
    }

    @Override
    public boolean contains(Object element) {
        return this.elements.get().contains(element);
    }

    @Override
    public Iterator<E> iterator() {
        return this.elements.get().iterator();
    }

    @Override
    public Object[] toArray() {
        return this.elements.get().toArray();
    }

    @Override
    public <T> T[] toArray(T[] array) {
        return this.elements.get().toArray(array);
    }

    @Override
    public <T> T[] toArray(IntFunction<T[]> generator) {
        return this.elements.get().toArray(generator);
    }

    @Override
    public boolean add(E element) {
        var added = new AtomicBoolean();

        this.elements.getAndUpdate(elements -> {
            var draft = createDraft(elements);
            added.set(draft.add(element));
            return commitDraft(draft);
        });

        return added.get();
    }

    @Override
    public boolean remove(Object element) {
        var removed = new AtomicBoolean();

        this.elements.getAndUpdate(elements -> {
            var draft = createDraft(elements);
            removed.set(draft.remove(element));
            return commitDraft(draft);
        });

        return removed.get();
    }

    @Override
    public boolean containsAll(Collection<?> collection) {
        return this.elements.get().containsAll(collection);
    }

    @Override
    public boolean addAll(Collection<? extends E> collection) {
        var addedAll = new AtomicBoolean();

        this.elements.getAndUpdate(elements -> {
            var draft = createDraft(elements);
            addedAll.set(draft.addAll(collection));
            return commitDraft(draft);
        });

        return addedAll.get();
    }

    @Override
    public boolean addAll(int index, Collection<? extends E> collection) {
        var addedAll = new AtomicBoolean();

        this.elements.getAndUpdate(elements -> {
            var draft = createDraft(elements);
            addedAll.set(draft.addAll(index, collection));
            return commitDraft(draft);
        });

        return addedAll.get();
    }

    @Override
    public boolean removeAll(Collection<?> collection) {
        var removedAll = new AtomicBoolean();

        this.elements.getAndUpdate(elements -> {
            var draft = createDraft(elements);
            removedAll.set(draft.removeAll(collection));
            return commitDraft(draft);
        });

        return removedAll.get();
    }

    @Override
    public boolean retainAll(Collection<?> collection) {
        var retainedAll = new AtomicBoolean();

        this.elements.getAndUpdate(elements -> {
            var draft = createDraft(elements);
            retainedAll.set(draft.retainAll(collection));
            return commitDraft(draft);
        });

        return retainedAll.get();
    }

    @Override
    public void clear() {
        this.elements.getAndUpdate(elements -> {
            var draft = createDraft(elements);
            draft.clear();
            return commitDraft(draft);
        });
    }

    @Override
    public E get(int index) {
        return this.elements.get().get(index);
    }

    @Override
    public E set(int index, E element) {
        var previousValue = new AtomicReference<E>();

        this.elements.getAndUpdate(elements ->
        {
            var draft = createDraft(elements);
            previousValue.set(draft.set(index, element));
            return commitDraft(draft);
        });

        return previousValue.get();
    }

    @Override
    public void add(int index, E element) {
        this.elements.getAndUpdate(elements ->
        {
            var draft = createDraft(elements);
            draft.add(index, element);
            return commitDraft(draft);
        });
    }

    @Override
    public E remove(int index) {
        var valueRemoved = new AtomicReference<E>();

        this.elements.getAndUpdate(elements ->
        {
            var draft = createDraft(elements);
            valueRemoved.set(draft.remove(index));
            return commitDraft(draft);
        });

        return valueRemoved.get();
    }

    @Override
    public boolean removeIf(Predicate<? super E> filter) {
        var removed = new AtomicBoolean();

        this.elements.getAndUpdate(elements -> {
            var draft = createDraft(elements);
            removed.set(draft.removeIf(filter));
            return commitDraft(draft);
        });

        return removed.get();
    }

    @Override
    public int indexOf(Object element) {
        return this.elements.get().indexOf(element);
    }

    @Override
    public int lastIndexOf(Object element) {
        return this.elements.get().indexOf(element);
    }

    @Override
    public ListIterator<E> listIterator() {
        return this.elements.get().listIterator();
    }

    @Override
    public ListIterator<E> listIterator(int index) {
        return this.elements.get().listIterator(index);
    }

    @Override
    public List<E> subList(int fromIndex, int toIndex) {
        return this.elements.get().subList(fromIndex, toIndex);
    }

    @Override
    public Spliterator<E> spliterator() {
        return this.elements.get().spliterator();
    }

    @Override
    public void replaceAll(UnaryOperator<E> operator) {
        this.elements.getAndUpdate(elements ->
        {
            var draft = createDraft(elements);
            draft.replaceAll(operator);
            return commitDraft(elements);
        });
    }

    @Override
    public void sort(Comparator<? super E> comparator) {
        this.elements.getAndUpdate(elements ->
        {
            var draft = createDraft(elements);
            draft.sort(comparator);
            return commitDraft(draft);
        });
    }

    @Override
    public void forEach(Consumer<? super E> action) {
        this.elements.get().forEach(action);
    }

    @Override
    public Stream<E> stream() {
        return this.elements.get().stream();
    }

    @Override
    public Stream<E> parallelStream() {
        return this.elements.get().parallelStream();
    }
}
