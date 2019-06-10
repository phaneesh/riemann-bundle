package com.codehale.metrics.riemann;

import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.riemann.DropWizardRiemannReporter;
import com.codahale.metrics.riemann.Riemann;
import io.dropwizard.riemann.RiemannConfig;
import io.riemann.riemann.client.EventDSL;
import io.riemann.riemann.client.IRiemannClient;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.mockito.Mockito;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

/**
 * @author koushik
 */
public class DropwizardRiemannReporterTest {

    private static final MetricRegistry metricRegistry = new MetricRegistry();
    private static final String DC = "dc";
    private static final String METRIC = "m1_rate";
    private static final String COMPONENT = "component";


    private RiemannConfig getRiemannConfig(){
        return RiemannConfig.builder()
                    .host("localhost")
                    .port(8080)
                    .prefix("prefix")
                    .pollingInterval(30)
                    .build();
    }


    private DropWizardRiemannReporter.Builder getRiemannReporterBuilder(
            RiemannConfig riemannConfig, String dc){
        DropWizardRiemannReporter.Builder builder = DropWizardRiemannReporter.forRegistry(metricRegistry)
                .tags(riemannConfig.getTags())
                .prefixedWith(riemannConfig.getPrefix())
                .useSeparator(".")
                .localHost(riemannConfig.getHost())
                .convertDurationsTo(TimeUnit.MILLISECONDS).convertRatesTo(TimeUnit.SECONDS);
        if(dc != null){
            builder.withDc(dc);
        }
        return builder;
    }

    private Riemann getRiemannInstance(RiemannConfig riemannConfig) throws IOException{
        return new Riemann(riemannConfig.getHost(), riemannConfig.getPort());
    }

    @Test
    public void testMetricBuildingWithoutDC() throws IOException {
        RiemannConfig riemannConfig = getRiemannConfig();
        DropWizardRiemannReporter.Builder builder = getRiemannReporterBuilder(riemannConfig, null);
        DropWizardRiemannReporter riemannReporter = builder.build(getRiemannInstance(riemannConfig));
        DropWizardRiemannReporter.EventClosure eventClosure = riemannReporter.newEvent(
                METRIC, System.currentTimeMillis(), "timer");
        EventDSL eventDSL = eventClosure.name(COMPONENT);
        Assert.assertFalse(eventDSL.builder.getService().contains(DC));
    }

    @Test
    public void testMetricBuildingWithDC() throws IOException{
        RiemannConfig riemannConfig = getRiemannConfig();
        DropWizardRiemannReporter.Builder builder = getRiemannReporterBuilder(riemannConfig, DC);
        DropWizardRiemannReporter riemannReporter = builder.build(getRiemannInstance(riemannConfig));
        DropWizardRiemannReporter.EventClosure eventClosure = riemannReporter.newEvent(
                METRIC, System.currentTimeMillis(), "timer");
        EventDSL eventDSL = eventClosure.name(COMPONENT);
        Assert.assertTrue(eventDSL.builder.getService().contains(DC));
    }
}
