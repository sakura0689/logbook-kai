package logbook.bean;

import java.util.List;
import java.util.Objects;
import java.util.function.Predicate;

public interface TestAllPredicate<T> extends Predicate<T> {
    @Override
    default public TestAllPredicate<T> and(Predicate<? super T> other) {
        Objects.requireNonNull(other);
        return (t) -> {
            boolean result1 = test(t);
            boolean result2 = other.test(t);
            return result1 && result2;
        };
    }

    @Override
    default public TestAllPredicate<T> or(Predicate<? super T> other) {
        Objects.requireNonNull(other);
        return (t) -> {
            boolean result1 = test(t);
            boolean result2 = other.test(t);
            return result1 || result2;
        };
    }

    /**
     * 条件(論理演算)
     * @return 論理演算を行う Predicate
     */
    default Predicate<T> processOperator(String operator, List<? extends TestAllPredicate<T>> conditions) {
        Predicate<T> predicate = null;
        for (TestAllPredicate<T> condition : conditions) {
            if (predicate == null) {
                predicate = condition;
            } else {
                predicate = operator.endsWith("AND")
                        ? predicate.and(condition)
                        : predicate.or(condition);
            }
        }
        if ("NAND".equals(operator) || "NOR".equals(operator)) {
            predicate = predicate.negate();
        }
        return predicate;
    }

    default String toStringOperator(String operator, String description) {
        StringBuilder sb = new StringBuilder(64);
        switch (operator) {
        case "AND":
            sb.append("次の条件を全て満たす");
            break;
        case "OR":
            sb.append("次の条件のいずれか少なくとも1つを満たす");
            break;
        case "NAND":
            sb.append("次の条件のいずれか少なくとも1つを満たさない");
            break;
        case "NOR":
            sb.append("次の条件を全て満たさない");
            break;
        default:
            break;
        }
        if (description != null) {
            sb.append("(").append(description).append(")");
        }
        return sb.toString();
    }
}