package annotations;

import cli.Region;
import lombok.Getter;

import java.util.ArrayList;
import java.util.List;

/**
 * A data class containing all the information of an annotation as specified by GFF3.
 * Also contains the children of this annotation.
 * Can recursively generate the region this annotation and its children has.
 * Created by regiv on 25/05/2018.
 */
public class Annotation implements Region {

    /**
     * Indicates the children of the feature.
     * A feature may have multiple children.
     */
    @Getter
    private List<Annotation> children;

    /**
     * Indicates the parent of the feature.
     * A feature may have multiple parents.
     */
    @Getter
    private List<Annotation> parents;

    @Getter
    private AnnotationData data;

    /**
     * A boolean to detect if the recursive region function is looping.
     * This happens if an Annotation is its own ancestor.
     */
    private boolean infinityDetector = false;

    /**
     * Constructs an annotation object, given the parsed data from an annotation file.
     * @param data Parsed data from an annotation file.
     */
    Annotation(AnnotationData data) {
        this.children = new ArrayList<>();
        this.parents = new ArrayList<>();
        this.data = data;
    }

    /**
     * Adds a child Annotation to this Annotation.
     * Note that this does not add this Annotation as a parent to the other Annotation.
     *
     * @param child The Annotation to be used as child for this annotation.
     */
    void addChild(Annotation child) {
        children.add(child);
    }

    /**
     * Adds a parent Annotation to this Annotation.
     * Note that this does not add this Annotation as a child to the other Annotation.
     *
     * @param parent The Annotation to be used as parent for this annotation.
     */
    void addParent(Annotation parent) {
        parents.add(parent);
    }

    /**
     * Gets all annotations that are descendants of this Annotation.
     *
     * @return A list with this Annotation and its descendants.
     * @throws RecursionException If this Annotation is it's own ancestor.
     */
    List<Annotation> getDescendants() throws RecursionException {
        List<Annotation> foundChildren = new ArrayList<>();
        getDescendants(foundChildren);
        return foundChildren;
    }

    /**
     * Recursive helper function to getDescendants().
     *
     * @param foundChildren The accumulator for this recursive function.
     * @throws RecursionException If this Annotation is it's own ancestor.
     */
    private void getDescendants(List<Annotation> foundChildren) throws RecursionException {
        if (infinityDetector) {
            throw new RecursionException("Annotation is it's own ancestor");
        }
        infinityDetector = true;
        foundChildren.add(this);
        for (Annotation child : this.children) {
            child.getDescendants(foundChildren);
        }
        infinityDetector = false;
    }

    /**
     * Gets the regions of all annotations that do not have children, and are descendants of this Annotation.
     * Returns this Annotation's region if it does not have children
     *
     * @return The regions of this Annotation's descendants without children,
     * or this Annotation's region if it doesn't have children.
     * @throws RecursionException If this Annotation is it's own ancestor.
     */
    List<int[]> getLowestRegions() throws RecursionException {
        List<int[]> regions = new ArrayList<>();
        getLowestRegions(regions);
        return regions;
    }

    /**
     * Recursive helper function to getLowestRegions().
     *
     * @param regions The accumulator for this recursive function.
     * @throws RecursionException If this Annotation is it's own ancestor.
     */
    private void getLowestRegions(List<int[]> regions) throws RecursionException {
        if (infinityDetector) {
            throw new RecursionException("Annotation is it's own ancestor");
        }
        infinityDetector = true;

        if (children.isEmpty()) {
            regions.add(new int[]{data.getStart(), data.getEnd()});
        } else {
            for (Annotation annotation : children) {
                annotation.getLowestRegions(regions);
            }
        }
        infinityDetector = false;
    }

    /**
     * Gets all regions of annotations that are descendants of this Annotation.
     *
     * @return A list with regions of this Annotation and its descendants.
     * @throws RecursionException If this Annotation is it's own ancestor.
     */
    List<Region> getAllRegions() throws RecursionException {
        List<Region> foundChildren = new ArrayList<>();
        getAllRegionsRecursive(foundChildren);
        return foundChildren;
    }

    /**
     * Recursive helper function to getAllRegions().
     *
     * @param foundChildren The accumulator for this recursive function.
     * @throws RecursionException If this Annotation is it's own ancestor.
     */
    private void getAllRegionsRecursive(List<Region> foundChildren) throws RecursionException {
        if (infinityDetector) {
            throw new RecursionException("Annotation is it's own ancestor");
        }
        infinityDetector = true;
        foundChildren.add(this);
        for (Annotation child : this.children) {
            child.getAllRegionsRecursive(foundChildren);
        }
        infinityDetector = false;
    }

    /**
     * Returns the start point of this region.
     *
     * @return the start point of this region.
     */
    @Override
    public int getStart() {
        return data.getStart();
    }

    /**
     * Returns the end point of this region.
     *
     * @return the end point of this region.
     */
    @Override
    public int getEnd() {
        return data.getEnd();
    }

    @Override
    public void setEnd(int end) {
    }

    @Override
    public boolean getStrandedness() {
        return data.isSense() || !data.isStranded();
    }

    /**
     * The type of region this is.
     *
     * @return the type of region.
     */
    @Override
    public String getType() {
        return data.getType();
    }

    /**
     * A colliquial name for this region.
     *
     * @return the name for this region.
     */
    @Override
    public String getName() {
        return data.getName();
    }

    /**
     * Checks whether this annotation is a successor, (child grandchild, grandgrandchild etc.) of an annotation
     * with the given parentName.
     *
     * @param parentName The parentname.
     * @return If it is a successor or if all parents have a circular reference.
     * @throws RecursionException If an Annotation is it's own succesor.
     */
    boolean isSuccessor(String parentName) throws RecursionException {
        //If this is called it means infinity detector was not set to false so it is a self loop.
        if (infinityDetector) {
            throw new RecursionException("Annotation is it's own ancestor");
        }

        infinityDetector = true;
        for (Annotation parent : getParents()) {
            if ((parent.getName() != null && parent.getName().equals(parentName)) || parent.isSuccessor(parentName)) {
                infinityDetector = false;
                return true;
            }
        }

        infinityDetector = false;
        return false;
    }

    /**
     * An exception thrown when an infinite recursion would occur.
     */
    public static class RecursionException extends Exception {
        private RecursionException(String message) {
            super(message);
        }
    }

}
