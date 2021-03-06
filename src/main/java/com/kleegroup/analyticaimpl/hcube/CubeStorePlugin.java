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
package com.kleegroup.analyticaimpl.hcube;

import java.util.List;
import java.util.Map;

import kasper.kernel.manager.Plugin;

import com.kleegroup.analytica.hcube.cube.HCube;
import com.kleegroup.analytica.hcube.dimension.HCategoryPosition;
import com.kleegroup.analytica.hcube.query.HQuery;

/**
 * Plugin g�rant le stockage des cubes.
 * @author npiedeloup
 * @version $Id: CubeStorePlugin.java,v 1.1 2012/03/22 09:16:40 npiedeloup Exp $
 */
public interface CubeStorePlugin extends Plugin {
	/**
	 * Enregistre un cube.
	 * Celui-ci sera merg� avec les autres cubes d�j� enregistr�s.
	 * @param cube Cube.
	 */
	void merge(HCube cube);

	/**
	 * Liste des cubes, regroup�s par s�rie index�e par ma cat�gorie correspondant � une requ�te.
	 * @param query Requ�te
	 * @return S�ries des cubes 
	 */
	Map<HCategoryPosition, List<HCube>>  findAll(HQuery query);
}
