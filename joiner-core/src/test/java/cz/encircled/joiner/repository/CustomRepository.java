package cz.encircled.joiner.repository;

import cz.encircled.joiner.QRepository;
import cz.encircled.joiner.model.AbstractEntity;

/**
 * @author Kisel on 21.01.2016.
 */
public interface CustomRepository<T extends AbstractEntity> extends QRepository<T> {


}
