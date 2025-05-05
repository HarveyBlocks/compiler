package org.harvey.compiler.type.generic.link;

import lombok.Getter;
import org.harvey.compiler.common.collecction.Pair;
import org.harvey.compiler.exception.self.CompilerException;
import org.harvey.compiler.io.source.SourcePositionSupplier;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;

/**
 * TODO
 *
 * @author <a href="mailto:harvey.blocks@outlook.com">Harvey Blocks</a>
 * @version 1.0
 * @date 2025-04-14 12:45
 */
@Getter
public class ParameterizedTypeLink<PATH extends SourcePositionSupplier> {
    // pre same list.first

    private final List<Pair<PATH, List<ParameterizedTypeLink<PATH>>>> list;


    public ParameterizedTypeLink(List<Pair<PATH, List<ParameterizedTypeLink<PATH>>>> list) {
        this.list = list;
    }

    public static <RAW extends SourcePositionSupplier> ParameterizedTypeLink<RAW> empty() {
        return new ParameterizedTypeLink<>(null);
    }

    public static <PATH extends SourcePositionSupplier> ParameterizedTypeLink<PATH> toTree(
            List<? extends Sequential<PATH>> sequential) {
        if (sequential.isEmpty()) {
            return ParameterizedTypeLink.empty();
        }
        ListIterator<? extends Sequential<PATH>> iterator = sequential.listIterator();
        LinkedList<Pair<int[], ParameterizedTypeLink<PATH>>> queue = new LinkedList<>();
        Sequential<PATH> first = iterator.next();
        ParameterizedTypeLink<PATH> root = first.to();
        queue.addLast(new Pair<>(toSizeArray(first.list), root));
        while (!queue.isEmpty()) {
            Pair<int[], ParameterizedTypeLink<PATH>> peek = queue.removeFirst();
            ParameterizedTypeLink<PATH> cur = peek.getValue();
            int[] sizes = peek.getKey();
            for (int i = 0; i < sizes.length; i++) {
                int size = sizes[i];
                List<ParameterizedTypeLink<PATH>> children = cur.list.get(i).getValue();
                for (int j = 0; j < size; j++) {
                    if (!iterator.hasNext()) {
                        throw new CompilerException("illegal sequential");
                    }
                    Sequential<PATH> next = iterator.next();
                    ParameterizedTypeLink<PATH> link = next.to();
                    queue.addLast(new Pair<>(toSizeArray(next.list), link));
                    children.add(link);
                }
            }
        }
        return root;
    }

    private static <RAW> int[] toSizeArray(List<Pair<RAW, Integer>> pairs) {
        int[] sizes = new int[pairs.size()];
        for (int i = 0; i < pairs.size(); i++) {
            sizes[i] = pairs.get(i).getValue();
        }
        return sizes;
    }

    /**
     * <pre>{@code
     * pre (pre.raw[0],pre.raw[1],...){
     *          (pre.raw[0].generic[0], pre.raw[0].generic[1], ...),
     *          (pre.raw[1].generic[0], pre.raw[1].generic[1], ...),
     *      ...
     * }
     * pre.raw[0].generic[0].pre
     * }</pre>
     */
    public List<Sequential<PATH>> toSequential() {
        LinkedList<ParameterizedTypeLink<PATH>> queue = new LinkedList<>();
        queue.addLast(this);
        List<Sequential<PATH>> result = new ArrayList<>();
        while (!queue.isEmpty()) {
            ParameterizedTypeLink<PATH> first = queue.removeFirst();
            result.add(new Sequential<>(first));
            for (Pair<PATH, List<ParameterizedTypeLink<PATH>>> pair : first.list) {
                List<ParameterizedTypeLink<PATH>> children = pair.getValue();
                for (ParameterizedTypeLink<PATH> link : children) {
                    queue.addLast(link);
                }
            }
        }
        return result;
    }

    @Getter
    public static class Sequential<RAW extends SourcePositionSupplier> {

        private final List<Pair<RAW, Integer>> list;

        public Sequential(ParameterizedTypeLink<RAW> link) {
            list = new ArrayList<>(link.list.size());
            for (Pair<RAW, List<ParameterizedTypeLink<RAW>>> pair : link.list) {
                List<ParameterizedTypeLink<RAW>> children = pair.getValue();
                this.list.add(new Pair<>(pair.getKey(), children.size()));
            }
        }

        public Sequential(List<Pair<RAW, Integer>> list) {
            this.list = list;
        }

        private ParameterizedTypeLink<RAW> to() {
            List<Pair<RAW, List<ParameterizedTypeLink<RAW>>>> pairs = new ArrayList<>(this.list.size());
            for (Pair<RAW, Integer> each : this.list) {
                pairs.add(new Pair<>(each.getKey(), new ArrayList<>(each.getValue())));
            }
            return new ParameterizedTypeLink<>(pairs);
        }
    }
}

