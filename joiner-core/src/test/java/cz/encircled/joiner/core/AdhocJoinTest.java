package cz.encircled.joiner.core;

import com.querydsl.core.Tuple;
import cz.encircled.joiner.model.QGroup;
import cz.encircled.joiner.model.QUser;
import cz.encircled.joiner.model.QWithSelfReference;
import cz.encircled.joiner.query.Q;
import cz.encircled.joiner.query.join.J;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

public abstract class AdhocJoinTest extends AbstractTest {

    @Test
    public void adhocJoinTupleProject() {
        List<Tuple> result = joiner.find(Q.select(QUser.user1.name, QWithSelfReference.withSelfReference.name)
                .from(QUser.user1)
                .joins(
                        J.left(QWithSelfReference.withSelfReference)
                                .unmapped()
                                .on(QWithSelfReference.withSelfReference.name.eq("refWithParent")))
        );

        assertFalse(result.isEmpty());
        for (Tuple tuple : result) {
            assertEquals("refWithParent", tuple.get(QWithSelfReference.withSelfReference.name));
        }
    }

    @Test
    public void adhocJoinTupleProjectNativeQuery() {
        List<UserGroupDto> result = joiner.find(Q.select(UserGroupDto.class, QUser.user1.name, QWithSelfReference.withSelfReference.name)
                .from(QUser.user1)
                .nativeQuery(true)
                .joins(
                        J.left(QWithSelfReference.withSelfReference)
                                .unmapped()
                                .on(QWithSelfReference.withSelfReference.name.eq("refWithParent")))
        );

        assertFalse(result.isEmpty());
        for (UserGroupDto tuple : result) {
            assertEquals("refWithParent", tuple.groupName);
        }
    }

    @Test
    public void adhocJoinProjectionToDto() {
        List<UserGroupDto> result = joiner.find(Q.select(UserGroupDto.class, QUser.user1.name, QGroup.group.name)
                .from(QUser.user1)
                .joins(
                        J.left(QGroup.group).unmapped().on(QGroup.group.name.eq("group1")))
        );

        assertFalse(result.isEmpty());
        for (UserGroupDto dto : result) {
            assertEquals("group1", dto.groupName);
        }
    }

    public static class UserGroupDto {
        private String userName;
        private String groupName;

        public UserGroupDto(String userName, String groupName) {
            this.userName = userName;
            this.groupName = groupName;
        }

    }

}
