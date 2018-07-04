package annotations;

import cli.Region;
import org.junit.Before;
import org.junit.Test;

import java.util.List;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.hamcrest.CoreMatchers.hasItem;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.AllOf.allOf;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.mock;

/**
 * Tests for the Annotation class.
 * Created by regiv on 30/05/2018.
 */
public class AnnotationTest {

    private AnnotationData.AnnotationDataBuilder builder;

    private Annotation createAnnotation() {
        return new Annotation(builder.build());
    }
    /**
     * Sets up a annotation builder for the tests.
     */
    @Before
    public void setUp() {
        builder = AnnotationData.builder();
        builder.sequenceID("standardTestID");
        builder.start(1);
        builder.end(1);
    }

    /**
     * Tests if adding a child actually adds it.
     */
    @Test
    public void testAddChild() {
        Annotation annotation = createAnnotation();
        Annotation annotation1 = mock(Annotation.class);
        annotation.addChild(annotation1);
        assertThat(annotation.getChildren().size()).isEqualTo(1);
        assertEquals(annotation1, annotation.getChildren().get(0));
    }

    /**
     * Tests if adding a parent actually adds it.
     */
    @Test
    public void testAddParent() {
        Annotation annotation = createAnnotation();
        Annotation annotation1 = mock(Annotation.class);
        annotation.addParent(annotation1);
        assertThat(annotation.getParents().size()).isEqualTo(1);
        assertEquals(annotation1, annotation.getParents().get(0));
    }


    /**
     * Tests if this method correctly gathers all lowest regions.
     */
    @Test
    public void testGetLowestRegions() {
        builder.start(1);
        builder.end(2);
        Annotation annotation = createAnnotation();
        setUp();
        builder.start(3);
        builder.end(4);
        Annotation annotation1 = createAnnotation();
        setUp();
        builder.start(5);
        builder.end(6);
        Annotation annotation2 = createAnnotation();
        setUp();
        builder.start(7);
        builder.end(8);
        Annotation annotation3 = createAnnotation();
        setUp();
        builder.start(9);
        builder.end(10);
        Annotation annotation4 = createAnnotation();
        setUp();
        builder.start(11);
        builder.end(12);
        Annotation annotation5 = createAnnotation();

        annotation.addChild(annotation1);
        annotation.addChild(annotation2);
        annotation1.addChild(annotation3);
        annotation1.addChild(annotation4);
        annotation2.addChild(annotation5);
        try {
            List<int[]> regions = annotation.getLowestRegions();
            assertThat(regions, allOf(
                    hasItem(new int[]{7, 8}),
                    hasItem(new int[]{9, 10}),
                    hasItem(new int[]{11, 12})));
            assertEquals(3, regions.size());
        } catch (Exception e) {
            fail();
        }
    }


    /**
     * Tests if this method throws an exception instead of going infinite.
     */
    @Test
    public void testGetLowestRegionsInfinite() {
        builder.start(1);
        builder.end(2);
        Annotation annotation = createAnnotation();
        setUp();
        builder.start(3);
        builder.end(4);
        Annotation annotation1 = createAnnotation();

        annotation.addChild(annotation1);
        annotation1.addChild(annotation);
        try {
            List<int[]> regions = annotation.getLowestRegions();
            fail();
        } catch (Exception e) {
            assertEquals("Annotation is it's own ancestor", e.getMessage());
        }
    }

    /**
     * Tests if this method correctly gathers all regions.
     */
    @Test
    public void testGetAllRegions() {
        builder.start(1);
        builder.end(2);
        Annotation annotation = createAnnotation();
        setUp();
        builder.start(3);
        builder.end(4);
        Annotation annotation1 = createAnnotation();
        setUp();
        builder.start(5);
        builder.end(6);
        Annotation annotation2 = createAnnotation();
        setUp();
        builder.start(7);
        builder.end(8);
        Annotation annotation3 = createAnnotation();
        setUp();
        builder.start(9);
        builder.end(10);
        Annotation annotation4 = createAnnotation();
        setUp();
        builder.start(11);
        builder.end(12);
        Annotation annotation5 = createAnnotation();

        annotation.addChild(annotation1);
        annotation.addChild(annotation2);
        annotation1.addChild(annotation3);
        annotation1.addChild(annotation4);
        annotation2.addChild(annotation5);
        try {
            List<Region> regions = annotation.getAllRegions();
            List<int[]> regionList = regions.stream().map(
                region -> new int[]{region.getStart(), region.getEnd()}).collect(Collectors.toList());
            assertThat(regionList, allOf(
                    hasItem(new int[]{1, 2}),
                    hasItem(new int[]{3, 4}),
                    hasItem(new int[]{5, 6}),
                    hasItem(new int[]{7, 8}),
                    hasItem(new int[]{9, 10}),
                    hasItem(new int[]{11, 12})));
            assertEquals(6, regionList.size());
        } catch (Exception e) {
            fail();
        }
    }

    /**
     * Tests if this method throws an exception instead of going infinite.
     */
    @Test
    public void testGetAllRegionsInfinite() {
        builder.start(1);
        builder.end(2);
        Annotation annotation = createAnnotation();
        setUp();
        builder.start(3);
        builder.end(4);
        Annotation annotation1 = createAnnotation();

        annotation.addChild(annotation1);
        annotation1.addChild(annotation);
        try {
            Object regions = annotation.getAllRegions();
            fail();
        } catch (Exception e) {
            assertEquals("Annotation is it's own ancestor", e.getMessage());
        }
    }

    /**
     * Checks if the child an grandchild are succesors of a parent works.
     */
    @Test
    public void getChilderen() {
        builder.start(1).end(2).name("Parent");
        Annotation parent = createAnnotation();
        setUp();
        builder.start(3).end(4);
        Annotation child = createAnnotation();
        setUp();
        builder.start(5).end(6);
        Annotation grandChild = createAnnotation();
        setUp();

        parent.addChild(child);
        child.addParent(parent);

        grandChild.addParent(child);
        child.addChild(grandChild);

        try {
            assertThat(child.isSuccessor("Parent")).isTrue();
            assertThat(grandChild.isSuccessor("Parent")).isTrue();
        } catch (Annotation.RecursionException e) {
            fail();
        }
    }


    /**
     * Checks if it returns falls when it is not a child.
     */
    @Test
    public void notAChild() {
        builder.start(1).end(2).name("Parent");
        Annotation parent = createAnnotation();
        setUp();
        builder.start(3).end(4);
        Annotation child = createAnnotation();
        setUp();
        builder.start(5).end(6);
        Annotation grandChild = createAnnotation();
        setUp();

        parent.addChild(child);
        child.addParent(parent);

        grandChild.addParent(child);
        child.addChild(grandChild);

        try {
            assertThat(child.isSuccessor("Friend")).isFalse();
            assertThat(grandChild.isSuccessor("Friend")).isFalse();
        } catch (Annotation.RecursionException e) {
            fail();
        }
    }

    /**
     * Checks if the if an infinite loop returns false on child.
     */
    @Test
    public void notAChildInfinite() {
        builder.start(1).end(2).name("Parent");
        Annotation parent = createAnnotation();
        setUp();
        builder.start(3).end(4);
        Annotation child = createAnnotation();
        setUp();
        builder.start(5).end(6);
        Annotation grandChild = createAnnotation();
        setUp();

        parent.addChild(child);
        child.addParent(parent);

        grandChild.addParent(child);
        child.addChild(grandChild);

        grandChild.addChild(parent);
        parent.addParent(grandChild);

        assertThatThrownBy(() -> child.isSuccessor("Friend")).isInstanceOf(Annotation.RecursionException.class);
        assertThatThrownBy(() -> grandChild.isSuccessor("Friend")).isInstanceOf(Annotation.RecursionException.class);
    }

}
