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

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import kasper.kernel.util.Assertion;

import com.kleegroup.analytica.hcube.dimension.HCategoryDictionary;
import com.kleegroup.analytica.hcube.dimension.HCategoryPosition;

/**
 * Selection de cat�gories permettant de d�finir un ensemble de positions sur un niveau donn�. 
 * exemple : 
 * - toutes les cat�gories racine (niveau 0) : new HCategorySelection() 
 * - toutes les sous-cat�gories de SQL niveau 1 : HCategorySelection(1, new HCategoryPosition("SQL", "select"));
 * 
 * @author npiedeloup, pchretien, statchum
 */
final class HCategorySelection {
	private final HCategoryPosition selectedCategoryPosition;
	private final boolean children;
	private final Set<HCategoryPosition> categoryPositions;
//	private int categoryLevel;

	

	HCategorySelection(final HCategoryDictionary categoryDictionary, final HCategoryPosition categoryPosition, boolean children) {
		Assertion.notNull(categoryDictionary);
		Assertion.notNull(categoryPosition);
		
		//		Assertion.precondition(categoryLevel >= 0, "Le level de la cat�gorie commence � 0.");
//		// ---------------------------------------------------------------------
//		categoryPositions = computeAllCategoryPositions(categories, categoryLevel, Arrays.asList(categoryPosition));
		this.children = children;
		this.selectedCategoryPosition = categoryPosition;
		if (children){
			categoryPositions = categoryDictionary.getAllCategories(categoryPosition);
		}else{
			categoryPositions = Collections.singleton(categoryPosition);
		}
	}

	Set<HCategoryPosition> getAllCategoryPositions() {
		return categoryPositions;
	}

//	public int getCategoryLevel() {
//		return categoryLevel;
//	}

	
//	/**
//	 * @return Cat�gorie courante
//	 */
//	HCategoryPosition getCategory();
//
//	/**
//	 * @return Liste des cat�gories filles
//	 */
//	List<HCategoryPosition> getSubCategories();
	
	@Override
	public String toString() {
		return  "categories = " +  ( children ? "children of ":"") +  selectedCategoryPosition;
	}
}