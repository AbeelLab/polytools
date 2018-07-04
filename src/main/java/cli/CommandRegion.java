package cli;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

/**
 * A region from the command-line.
 * This is currently always positively aligned.
 */
@EqualsAndHashCode
public class CommandRegion implements Region {

    @Getter
    private final int start;
    @Getter
    @Setter
    private int end;

    /**
     * Creats a new region from start up until end.
     *
     * @param start The start position
     * @param end   The end position.
     */
    public CommandRegion(int start, int end) {
        this.start = start;
        this.end = end;
    }

    /**
     * @inheritDocs
     */
    @Override
    public boolean getStrandedness() {
        return true;
    }

    /**
     * @inheritDocs
     */
    @Override
    public String getType() {
        //Region has no type if passed through the command line.
        return null;
    }

    /**
     * @inheritDocs
     */
    @Override
    public String getName() {
        return null;
    }
}
