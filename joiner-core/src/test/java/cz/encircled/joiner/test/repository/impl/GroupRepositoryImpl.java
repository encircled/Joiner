package cz.encircled.joiner.test.repository.impl;

import com.mysema.query.types.EntityPath;
import cz.encircled.joiner.test.model.Group;
import cz.encircled.joiner.test.model.QGroup;
import cz.encircled.joiner.test.repository.GroupRepository;
import cz.encircled.joiner.test.repository.SpringJoinerRepository;
import org.springframework.stereotype.Repository;

/**
 * @author Kisel on 25.01.2016.
 */
@Repository
public class GroupRepositoryImpl extends SpringJoinerRepository<Group> implements GroupRepository {

    @Override
    protected EntityPath<Group> getRootEntityPath() {
        return QGroup.group;
    }

}
