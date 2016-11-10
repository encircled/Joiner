package cz.encircled.joiner.spring;

import cz.encircled.joiner.core.JoinerRepository;
import cz.encircled.joiner.query.JoinerQuery;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

/**
 * @author Kisel on 31.10.2016.
 */
public interface SpringJoinerRepository<T> extends JoinerRepository<T> {

    /**
     *
     * @param request query request
     * @param <R> return type
     * @return spring pageable result
     */
    <R, U extends T> Page<R> findPage(JoinerQuery<U, R> request, Pageable pageable);

}
