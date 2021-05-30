package cz.encircled.joiner.core;

import com.querydsl.core.types.EntityPath;
import cz.encircled.joiner.exception.AliasMissingException;
import cz.encircled.joiner.exception.JoinerException;
import cz.encircled.joiner.model.QAddress;
import cz.encircled.joiner.model.QGroup;
import cz.encircled.joiner.model.QUser;
import cz.encircled.joiner.query.Q;
import cz.encircled.joiner.query.join.J;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * @author Kisel on 26.01.2016.
 */
public class FailTest extends AbstractTest {

    @Test
    public void testNullInput() {
        assertThrows(IllegalArgumentException.class, () -> {
            joiner.find(null);
        });
    }

    @Test
    public void testNullProjection() {
        assertThrows(IllegalArgumentException.class, () -> {
            joiner.find(Q.select((EntityPath<?>[]) null).from(QAddress.address));
        });
    }

    @Test
    public void testNullRequest() {
        assertThrows(IllegalArgumentException.class, () -> {
            joiner.find(null);
        });
    }

    @Test
    public void predicateNoAliasTest() {
        assertThrows(AliasMissingException.class, () -> {
            joiner.find(Q.from(QUser.user1).where(QAddress.address.name.eq("user1street1")));
        });
    }

    @Test
    public void testRightJoinFetch_Eclipse() {
        if (isEclipse()) {
            assertThrows(JoinerException.class, () -> {
                joiner.find(Q.from(QGroup.group).joins(J.left(QUser.user1).right()));
            });
        }
    }

    @Test
    public void testGroupByNoAlias() {
        assertThrows(AliasMissingException.class, () -> {
            joiner.find(
                    Q.select(QAddress.address.id.avg())
                            .from(QAddress.address).groupBy(QGroup.group.name)
            );
        }, "Alias group1 is not present in joins!");
    }

    @Test
    public void testGroupByHavingNoAlias() {
        assertThrows(AliasMissingException.class, () -> {
            joiner.find(
                    Q.select(QAddress.address.id.avg()).from(QAddress.address)
                            .groupBy(QAddress.address.user)
                            .having(QGroup.group.id.count().gt(2))
            );
        }, "Alias group1 is not present in joins!");
    }

}
