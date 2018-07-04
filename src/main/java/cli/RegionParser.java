package cli;

import java.util.ArrayList;
import java.util.List;

/**
 * Parses Regions from the command line.
 * Created by regiv on 12/06/2018.
 */
public class RegionParser {

    /**
     * Determine the region.
     *
     * @param regionStrings An array of strings containing the regions.
     * @return The regions.
     */
    public List<Region> parseRegions(String[] regionStrings) {
        List<Region> regions = new ArrayList<Region>();
        for (String region : regionStrings) {
            regions.add(parseRegion(region));
        }
        return regions;
    }


    /**
     * Interpret a region string to an integer array containing 2 elements.
     *
     * @param region the region string to parse.
     * @return an integer array with start pos and end pos.
     */
    public Region parseRegion(String region) {
        if (region.matches("\\d+")) {
            int start = Integer.parseInt(region);
            int end = start;
            return new CommandRegion(start, end);
        }

        if (!region.matches("\\d+-(\\d+)?")) {
            throw new IllegalArgumentException("Specified region is not valid: " + region);
        }

        String[] args = region.split("-");
        int start = Integer.parseInt(args[0]);
        int end = -1;
        if (args.length == 2) {
            end = Integer.parseInt(args[1]);
            if (start > end) {
                throw new IllegalArgumentException("Start position has to be smaller than "
                        + "or equal to end position: " + start + "-" + end);
            }
        }
        return new CommandRegion(start, end);
    }
}
