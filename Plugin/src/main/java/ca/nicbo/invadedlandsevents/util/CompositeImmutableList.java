package ca.nicbo.invadedlandsevents.util;

import ca.nicbo.invadedlandsevents.api.util.Validate;

import java.util.AbstractList;
import java.util.ArrayList;
import java.util.List;

/**
 * Combine 2 lists into an immutable list.
 *
 * @author Kevin K
 * @author Nicbo
 */
public class CompositeImmutableList<E> extends AbstractList<E> {
    private final List<? extends E> list1;
    private final List<? extends E> list2;

    public CompositeImmutableList(List<? extends E> list1, List<? extends E> list2) {
        Validate.checkArgumentNotNull(list1, "list1");
        Validate.checkArgumentNotNull(list2, "list2");

        // Create local copies
        this.list1 = new ArrayList<>(list1);
        this.list2 = new ArrayList<>(list2);
    }

    @Override
    public E get(int index) {
        if (index < list1.size()) {
            return list1.get(index);
        }

        return list2.get(index - list1.size());
    }

    @Override
    public int size() {
        return list1.size() + list2.size();
    }
}
