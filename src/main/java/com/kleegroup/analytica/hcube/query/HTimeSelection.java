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
package com.kleegroup.analytica.hcube.query;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import kasper.kernel.exception.KRuntimeException;
import kasper.kernel.util.Assertion;

import com.kleegroup.analytica.hcube.dimension.HTimeDimension;
import com.kleegroup.analytica.hcube.dimension.HTimePosition;

/**
 * Selection temporelle permettant de d�finir un ensemble de positions sur un niveau temporel donn�.
 * exemple : 
 *  - tous les jours du 15 septembre 2000 au 15 novembre 2000.
 *  - toutes les ann�es de 1914 � 1918 
 * @author npiedeloup, pchretien
 */
final class HTimeSelection {
	private final HTimePosition minTimePosition;
	private final HTimePosition maxTimePosition;

	//	private final TimeDimension dimension;

	HTimeSelection(final HTimeDimension dimension, final Date minDate, final Date maxDate) {
		Assertion.notNull(minDate);
		Assertion.notNull(maxDate);
		Assertion.precondition(minDate.equals(maxDate) || minDate.before(maxDate), "la date min doit �tre inf�rieure � la date max");
		Assertion.notNull(dimension);
		//---------------------------------------------------------------------
		this.minTimePosition = new HTimePosition(minDate, dimension);
		this.maxTimePosition = new HTimePosition(maxDate, dimension);
		//	this.dimension = dimension;
	}

	List<HTimePosition> getAllTimePositions() {
		List<HTimePosition> timePositions = new ArrayList<HTimePosition>();
		//On pr�pare les bornes de temps
		int loops = 0;
		HTimePosition currentTimePosition = minTimePosition;
		do {
			timePositions.add(currentTimePosition);
			//---------------
			currentTimePosition = currentTimePosition.next();
			loops++;
			if (loops > 1000) {
				throw new KRuntimeException("Segment temporel trop grand : plus de 1000 positions");
			}
		} while (currentTimePosition.getValue().before(maxTimePosition.getValue()));

		return timePositions;
	}

	/** {@inheritDoc} */
	public String toString() {
		return "from:" + minTimePosition + " to:" + maxTimePosition;
	}
}
