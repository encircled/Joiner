package cz.encircled.joiner.spring;

import cz.encircled.joiner.core.JoinerRepository;
import cz.encircled.joiner.query.JoinerQuery;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

/**
 * Entity repository with spring-data integration
 *
 * @author Kisel on 31.10.2016.
 */
public interface SpringJoinerRepository<T> extends JoinerRepository<T> {

    /**
     * This "find" method support spring {@link Pageable} and converts result to spring {@link Page}
     * <p>
     *     {@link PageableFeature} is used
     * </p>
     *
     * @see PageableFeature
     * @param request query request
     * @param <R> return type
     * @return find result converted to spring page
     */
    <R, U extends T> Page<R> findPage(JoinerQuery<U, R> request, Pageable pageable);

}
