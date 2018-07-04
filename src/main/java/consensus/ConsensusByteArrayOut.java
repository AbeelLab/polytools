package consensus;

import consensus.encoders.IupacEncoder;
import general.FormattingOutputStream;

import java.io.IOException;
import java.io.OutputStream;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * A byte array output stream that you can modify the buffer of.
 * Also has an easy apply method for consensus sequence generation.
 */
public class ConsensusByteArrayOut {
    private OutputStream stream;

    /**
     * Basic constructor.
     *
     * @param stream the stream to write to.
     */
    ConsensusByteArrayOut(OutputStream stream) {
        this.stream = stream;
    }

    /**
     * Writes the consensuses to the underlying output stream.
     * @param consensusList The consensuses to write.
     * @param encoder       The encoder to use.
     * @param stats         The statistics tracker.
     * @return              The amount of nucleotides written.
     * @throws IOException  If the underlying stream throws an exception.
     */
    public int writeConsensus(List<VariantMerger.Consensus> consensusList,
                               IupacEncoder encoder,
                               ConsensusGenerator.ConsensusStatistics stats)
            throws IOException {

        AtomicInteger delOpenBrackets = new AtomicInteger(0);
        AtomicInteger insOpenBrackets = new AtomicInteger(0);

        int lastCount = 0;
        int heteroDeletionLevel = 0;
        int heteroInsertionLevel = 0;
        int lastOpenType = 0; //0 = nothing, 1 = insertion, 2 = deletion
        for (VariantMerger.Consensus consensus : consensusList) {
            lastOpenType = writeBracketsBefore(delOpenBrackets, insOpenBrackets,
                    heteroDeletionLevel, heteroInsertionLevel, lastOpenType, consensus);
            if (!consensus.isDeleted() || consensus.getHeteroLevel() > 0) {
                int consensusSize = consensus.getConsensus().size();
                byte[] bytes;
                lastCount++;
                if (consensusSize > 0) {
                    if (consensus.getHeteroLevel() > 0) {
                        bytes = new byte[consensusSize + 1];
                        bytes[consensusSize] = consensus.getRef();
                        stats.addHeteroNucleotides(1);
                    } else {
                        bytes = new byte[consensusSize];
                        stats.addAlternativeNucleotides(1);
                    }

                    for (int i = 0; i < consensusSize; i++) {
                        bytes[i] = consensus.getConsensus().get(i);
                    }
                    write(encoder.encode(bytes));
                } else {
                    bytes = new byte[]{consensus.getRef()};
                    stats.addReferenceNucleotides(1);
                    write(encoder.encode(bytes), true);
                }
            }
            heteroDeletionLevel = consensus.isDeleted() ? consensus.getHeteroLevel() : 0;
            heteroInsertionLevel = consensus.getRelativePosition() > 0 ? consensus.getHeteroLevel() : 0;
        }

        writeBracketsAfter(delOpenBrackets, insOpenBrackets,
                heteroDeletionLevel, heteroInsertionLevel, lastOpenType);
        return lastCount;
    }

    private void writeBracketsAfter(AtomicInteger delOpenBrackets, AtomicInteger insOpenBrackets,
                                    int heteroDeletionLevel, int heteroInsertionLevel,
                                    int lastOpenType) throws IOException {
        if (heteroDeletionLevel != 0) {
            if (lastOpenType == 1) {
                lastOpenType = -1;
            } else {
                lastOpenType = 0;
                write(']');
                delOpenBrackets.decrementAndGet();
            }
        }
        if (heteroInsertionLevel != 0) {
            write(')');
            insOpenBrackets.decrementAndGet();
        }
        if (lastOpenType == -1) {
            write(']');
            delOpenBrackets.decrementAndGet();
        }

        while (delOpenBrackets.getAndDecrement() > 0) {
            write(']');
        }
        while (insOpenBrackets.getAndDecrement() > 0) {
            write(')');
        }
    }

    private int writeBracketsBefore(AtomicInteger del, AtomicInteger ins,
                                    int heteroDeletionLevel, int heteroInsertionLevel,
                                    int lastOpenType, VariantMerger.Consensus consensus) throws IOException {
        if (heteroDeletionLevel < consensus.getHeteroLevel() && consensus.isDeleted()
                && !(consensus.getRelativePosition() == 1 && heteroInsertionLevel < consensus.getHeteroLevel())) {
            lastOpenType = 2;
            write('[');
            del.incrementAndGet();
        }
        if (consensus.getRelativePosition() == 1 && heteroInsertionLevel < consensus.getHeteroLevel()) {
            lastOpenType = 1;
            write('(');
            ins.incrementAndGet();
        }
        if (heteroDeletionLevel > consensus.getHeteroLevel() && !(consensus.getRelativePosition() == 0
                && heteroInsertionLevel > consensus.getHeteroLevel())) {
            if (lastOpenType == 1) {
                lastOpenType = -1;
            } else {
                write(']');
                del.decrementAndGet();
            }
        }
        if (consensus.getRelativePosition() == 0 && heteroInsertionLevel > consensus.getHeteroLevel()) {
            write(')');
            ins.decrementAndGet();
        }
        if (lastOpenType == -1) {
            write(']');
            del.decrementAndGet();
        }
        return lastOpenType;
    }

    private void write(byte b, boolean ref) throws IOException {
        if (ref) {
            stream.write(b);
            return;
        }
        //alt:
        if (stream instanceof FormattingOutputStream) {
            //writeAlt in color method
            ((FormattingOutputStream) stream).writeAlt(new byte[]{b}, 0, 1);
        } else {
            stream.write(b);
        }
    }

    private void write(char b) throws IOException {
        write((byte) b, false);
    }

    private void write(byte b) throws IOException {
        write(b, false);
    }
}
