package it.unibs.ingsoft.support;
/*
import org.junit.platform.engine.DiscoverySelector;
import org.junit.platform.launcher.Launcher;
import org.junit.platform.launcher.LauncherDiscoveryRequest;
import org.junit.platform.launcher.core.LauncherDiscoveryRequestBuilder;
import org.junit.platform.launcher.core.LauncherFactory;
import org.junit.platform.launcher.listeners.SummaryGeneratingListener;
import org.junit.platform.launcher.listeners.TestExecutionSummary;

import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;

import static org.junit.platform.engine.discovery.DiscoverySelectors.selectPackage;

public final class JUnitLauncher {

    private JUnitLauncher() {
    }

    public static void main(String[] args) {
        List<DiscoverySelector> selectors = new ArrayList<>();
        if (args.length == 0) {
            selectors.add(selectPackage("it.unibs.ingsoft"));
        } else {
            for (String arg : args) {
                selectors.add(selectPackage(arg));
            }
        }

        LauncherDiscoveryRequest request = LauncherDiscoveryRequestBuilder.request()
                .selectors(selectors)
                .build();

        SummaryGeneratingListener listener = new SummaryGeneratingListener();
        Launcher launcher = LauncherFactory.create();
        launcher.registerTestExecutionListeners(listener);
        launcher.execute(request);

        TestExecutionSummary summary = listener.getSummary();
        summary.printTo(new PrintWriter(System.out, true));
        summary.printFailuresTo(new PrintWriter(System.out, true));

        if (summary.getTestsFailedCount() > 0) {
            System.exit(1);
        }
    }
}
*/