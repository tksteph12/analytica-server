/**
 * Analytica - beta version - Systems Monitoring Tool
 *
 * Copyright (C) 2013, KleeGroup, direction.technique@kleegroup.com (http://www.kleegroup.com)
 * KleeGroup, Centre d'affaire la Boursidière - BP 159 - 92357 Le Plessis Robinson Cedex - France
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
package com.kleegroup.analytica.hcube.dimension;

import java.text.SimpleDateFormat;
import java.util.Date;

import kasper.kernel.exception.KRuntimeException;
import kasper.kernel.lang.DateBuilder;

import com.kleegroup.analytica.hcube.HKey;

/**
 * @author npiedeloup
 * @version $Id: TimePosition.java,v 1.2 2012/04/17 09:11:15 pchretien Exp $
 */
public final class HTimePosition extends HKey implements HPosition<HTimePosition> {
	private final HTimeDimension dimension;
	private final Date date;

	public HTimePosition(final Date date, final HTimeDimension timeDimension) {
		super("time:[" + timeDimension.name() + "]" + new SimpleDateFormat(timeDimension.getPattern()).format(date));
		//---------------------------------------------------------------------
		this.dimension = timeDimension;
		this.date = timeDimension.reduce(date);
	}

	/** {@inheritDoc} */
	public HTimePosition drillUp() {
		final HTimeDimension upTimeDimension = dimension.drillUp();
		return upTimeDimension != null ? new HTimePosition(date, upTimeDimension) : null;
	}

	/** {@inheritDoc} */
	public HTimeDimension getDimension() {
		return dimension;
	}

	public Date getValue() {
		return date;
	}

	public HTimePosition next() {
		final Date nextDate;
		switch (dimension) {
			case Year:
				nextDate = new DateBuilder(date).addYears(1).toDateTime();
				break;
			case Month:
				nextDate = new DateBuilder(date).addMonths(1).toDateTime();
				break;
			case Day:
				nextDate = new DateBuilder(date).addDays(1).toDateTime();
				break;
			case Hour:
				nextDate = new DateBuilder(date).addHours(1).toDateTime();
				break;
			case Minute:
				nextDate = new DateBuilder(date).addMinutes(1).toDateTime();
				break;
			default:
				throw new KRuntimeException("TimeDimension inconnu");
		}
		return new HTimePosition(nextDate, dimension);
	}
}
