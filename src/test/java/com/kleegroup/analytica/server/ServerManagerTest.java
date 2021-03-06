/**
 * 
 */
package com.kleegroup.analytica.server;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import javax.inject.Inject;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.kleegroup.analytica.core.KProcess;
import com.kleegroup.analytica.core.KProcessBuilder;
import com.kleegroup.analytica.hcube.HCubeManager;
import com.kleegroup.analytica.hcube.cube.HCounterType;
import com.kleegroup.analytica.hcube.cube.HCube;
import com.kleegroup.analytica.hcube.cube.HMetric;
import com.kleegroup.analytica.hcube.cube.HMetricKey;
import com.kleegroup.analytica.hcube.dimension.HTimeDimension;
import com.kleegroup.analytica.hcube.query.HQuery;
import com.kleegroup.analytica.hcube.query.HQueryBuilder;

import kasper.AbstractTestCaseJU4;

/**
 * @author Stephane TATCHUM
 *
 */
public class ServerManagerTest extends AbstractTestCaseJU4 {
	private static final HMetricKey MONTANT = new HMetricKey("MONTANT", false);
	private static final HMetricKey POIDS = new HMetricKey("POIDS", false);
	private static final HMetricKey DURATION = new HMetricKey(KProcess.DURATION, true);

	private static final String PROCESS_SERVICES = "SERVICES";
	private static final String PROCESS_SQL = "SQL";

	@Inject
	private ServerManager serverManager;

	private Date date;
	private final int price = 8;
	private int categoryLevel ;

	@Before
	public void init() throws ParseException {
		date = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.FRANCE).parse("08/05/2013 10:10");
	}
	
	
	private static void assertMetricEquals(HMetric metric, double count, double sum, double mean, double min, double max) {
		Assert.assertEquals(count, metric.get(HCounterType.count), 0);
		Assert.assertEquals(count, metric.getCount(), 0); //test accesseur rapide
		Assert.assertEquals(sum, metric.get(HCounterType.sum), 0);//test accesseur rapide 0);
		Assert.assertEquals(sum, metric.getSum(), 0);
		Assert.assertEquals(mean, metric.get(HCounterType.mean), 0);
		Assert.assertEquals(mean, metric.getMean(), 0);//test accesseur rapide
		Assert.assertEquals(min, metric.get(HCounterType.min), 0);
		Assert.assertEquals(max, metric.get(HCounterType.max), 0);
	}
	
	
	@Test
	public void testSimpleProcess() {
		final KProcess selectProcess1 = new KProcessBuilder(date, 100, PROCESS_SQL, "select article")//
		.incMeasure(MONTANT.id(), price)//
		.build();
		serverManager.push(selectProcess1);
		
		categoryLevel = 2;
		
		final HQuery daySqlQuery = new HQueryBuilder()//
				.on(HTimeDimension.Day)//
				.from(date)//
				.to(date)//
				.with("SQL")
				.build();

		List<HCube> cubes = serverManager.execute(daySqlQuery).getCubes();
		Assert.assertEquals(1, cubes.size());
		//
		HMetric montantMetric = cubes.get(0).getMetric(MONTANT);
		assertMetricEquals(montantMetric, 1, price * 1, price, price, price);
	}
	
}
