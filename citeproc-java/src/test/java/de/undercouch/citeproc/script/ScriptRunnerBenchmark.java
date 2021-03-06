package de.undercouch.citeproc.script;

import com.carrotsearch.junitbenchmarks.BenchmarkOptions;
import com.carrotsearch.junitbenchmarks.BenchmarkRule;
import de.undercouch.citeproc.CSL;
import de.undercouch.citeproc.bibtex.AbstractBibTeXTest;
import de.undercouch.citeproc.bibtex.BibTeXItemDataProvider;
import de.undercouch.citeproc.output.Citation;
import de.undercouch.citeproc.script.ScriptRunnerFactory.RunnerType;
import org.jbibtex.BibTeXDatabase;
import org.jbibtex.Key;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestRule;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;

/**
 * Tests the performance of different {@link ScriptRunner} implementations
 * @author Michel Kraemer
 */
public class ScriptRunnerBenchmark extends AbstractBibTeXTest {
    /**
     * Configures this benchmark test
     */
    @Rule
    public TestRule benchmarkRun = new BenchmarkRule();

    private static BibTeXDatabase db;
    private static BibTeXItemDataProvider sys = new BibTeXItemDataProvider();
    private static List<Integer> rnds = new ArrayList<>();

    /**
     * Set up this test
     * @throws Exception if something goes wrong
     */
    @BeforeClass
    public static void setUp() throws Exception {
        db = loadUnixDatabase();
        sys.addDatabase(db);

        for (int i = 0; i < 10; ++i) {
            int j = (int)(Math.random() * db.getEntries().size());
            rnds.add(j);
        }
    }

    private void runTest() throws Exception {
        try (CSL citeproc = new CSL(sys, "ieee")) {
            citeproc.setOutputFormat("text");

            List<Key> keys = new ArrayList<>(db.getEntries().keySet());
            List<String> result = new ArrayList<>();

            for (Integer r : rnds) {
                Key k = keys.get(r);
                List<Citation> cs = citeproc.makeCitation(k.getValue());
                for (Citation c : cs) {
                    while (result.size() <= c.getIndex()) {
                        result.add("");
                    }
                    result.set(c.getIndex(), c.getText());
                }
            }

            int c = 0;
            for (Integer r : rnds) {
                Key k = keys.get(r);
                List<Citation> cs = citeproc.makeCitation(k.getValue());
                assertEquals(1, cs.size());
                String nc = cs.get(0).getText();
                String pc = result.get(c);
                assertEquals(nc, pc);
                ++c;
            }
        }
    }

    /**
     * Tests the JRE script runner
     * @throws Exception if something goes wrong
     */
    @BenchmarkOptions(benchmarkRounds = 10, warmupRounds = 1)
    @Test
    @Ignore("Should not be called together with other tests because it sets " +
            "CSL.sharedRunner, which cannot be reset")
    public void jre() throws Exception {
        RunnerType prev = ScriptRunnerFactory.setRunnerType(RunnerType.JRE);
        try {
            runTest();
        } finally {
            ScriptRunnerFactory.setRunnerType(prev);
        }
    }

    /**
     * Tests the GraalVM JavaScript script runner
     * @throws Exception if something goes wrong
     */
    @BenchmarkOptions(benchmarkRounds = 10, warmupRounds = 1)
    @Test
    @Ignore("Should not be called together with other tests because it sets " +
            "CSL.sharedRunner, which cannot be reset")
    public void graaljs() throws Exception {
        RunnerType prev = ScriptRunnerFactory.setRunnerType(RunnerType.GRAALJS);
        try {
            runTest();
        } finally {
            ScriptRunnerFactory.setRunnerType(prev);
        }
    }
}
