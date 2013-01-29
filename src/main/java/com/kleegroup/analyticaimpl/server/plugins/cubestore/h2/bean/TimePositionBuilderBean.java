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
package com.kleegroup.analyticaimpl.server.plugins.cubestore.h2.bean;

import java.util.Date;

import com.kleegroup.analytica.server.data.TimeDimension;
import com.kleegroup.analyticaimpl.server.cube.TimePosition;

/**
 * Metric d'un cube.
 * Une metric poss�de un type et une valeur.
 *  
 * @author npiedeloup, pchretien
 * @version $Id: TimePositionBuilderBean.java,v 1.2 2012/03/22 09:16:40 npiedeloup Exp $
 */
public final class TimePositionBuilderBean {
	private Date time;
	private TimeDimension timeDimension;

	/**
	 * @return Build de metric
	 */
	public TimePosition buildTimePosition() {
		return new TimePosition(time, timeDimension);
	}

	public final void setTidCd(final String tidCd) {
		timeDimension = TimeDimension.valueOf(tidCd);
	}

	public final void setTimePosition(final Date timePosition) {
		time = timePosition;
	}
}
