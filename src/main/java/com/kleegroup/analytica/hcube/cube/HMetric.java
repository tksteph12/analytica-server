/**
 * Analytica - beta version - Systems Monitoring Tool
 *
 * Copyright (C) 2013, KleeGroup, direction.technique@kleegroup.com (http://www.kleegroup.com)
 * KleeGroup, Centre d'affaire la Boursidi�re - BP 159 - 92357 Le Plessis Robinson Cedex - France
 *
 * This program is free software; you can redistribute it and/or modify it under the terms
 * of the GNU General Public License as published by the Free Software Foundation;
 * either version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with this program;
 * if not, see <http://www.gnu.org/licenses>
 */
package com.kleegroup.analytica.hcube.cube;

import java.util.Map;

import kasper.kernel.util.Assertion;

/**
 * Metric.
 * La m�tric est le r�sultat issue de l'aggr�gation 
 *  - de mesures 
 *  - de metric
 * 
 * Une metric est identifi�e par son nom. 
 * Des metrics ne peuvent �tre aggr�g�s ensemble 
 * que si elles concernent la m�me entit� c'est � dire poss�de un m�me nom.
 * 
 * @author npiedeloup, pchretien
 * @version $Id: Metric.java,v 1.5 2013/01/14 16:35:20 npiedeloup Exp $
 */
public final class HMetric {
	private final HMetricKey metricKey;

	private final long count;
	private final double min;
	private final double max;
	private final double sum;
	private final double sqrSum;
	private final Map<Double, Long> clusteredValues;

	HMetric(final HMetricKey metricKey, final long count, final double min, final double max, final double sum, final double sqrSum, Map<Double, Long> clusteredValues) {
		Assertion.notNull(metricKey);
		//Assertion.precondition(metricKey.isClustered() ^ clusteredValues != null, "la metric {0} cluster doit avoir des donn�es clusteris�es", metricKey);
		//---------------------------------------------------------------------
		this.metricKey = metricKey;
		this.count = count;
		this.min = min;
		this.max = max;
		this.sum = sum;
		this.sqrSum = sqrSum;
		//---------------------------------------------------------------------
		this.clusteredValues = clusteredValues;
	}

	/**
	 * @return Nom de metric
	 */
	public HMetricKey getKey() {
		return metricKey;
	}

	/**
	 * @return Ecart type
	 */
	private double getStandardDeviation() {
		if (count > 1) {
			// formule non exacte puisque qu'on ne conna�t pas toutes les valeurs, mais estimation suffisante
			// rq : �cart type (ou sigma) se dit standard deviation en anglais
			return Math.round(100 * Math.sqrt((sqrSum - sum * sum / count) / ((double) count - 1))) / 100d;
		}
		return Double.NaN;
	}

	public double get(final HCounterType dataType) {
		Assertion.notNull(dataType);
		//---------------------------------------------------------------------
		switch (dataType) {
			case count:
				return count;
			case max:
				return max;
			case min:
				return min;
			case sum:
				return sum;
			case mean:
				return getMean();
			case sqrSum:
				return sqrSum;
			case stdDev:
				return getStandardDeviation();
			default:
				throw new IllegalArgumentException("Fonction inconnue : " + dataType);
		}
	}

	public long getCount() {
		return count;
	}

	public double getSum() {
		return sum;
	}

	/**
	 * @return Moyenne
	 */
	public double getMean() {
		if (count > 0) {
			return sum / count;
		}
		return Double.NaN;
	}

	public Map<Double, Long> getClusteredValues() {
		return clusteredValues;
	}

	public String toString() {
		return metricKey + "= {count:" + count + ", mean:" + getMean() + (clusteredValues == null ? " " : ", clustered:" + clusteredValues) + "}";
	}

}
