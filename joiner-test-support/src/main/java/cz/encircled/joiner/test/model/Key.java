package cz.encircled.joiner.test.model;

import cz.encircled.joiner.eclipse.InheritanceJoiningCustomizer;
import org.eclipse.persistence.annotations.Customizer;

import javax.persistence.Entity;
import javax.persistence.Table;

/**
 * Created by Kisel on 28.01.2016.
 */
@Entity
@Table(name = "test_key")
@Customizer(InheritanceJoiningCustomizer.class)
public class Key extends AbstractEntity {

}
