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
package com.kleegroup.analyticaimpl.server.plugins.cubestore.h2;

import java.sql.Connection;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import kasper.kernel.util.Assertion;

import com.kleegroup.analytica.server.data.DataKey;
import com.kleegroup.analytica.server.data.DataType;
import com.kleegroup.analytica.server.data.TimeDimension;
import com.kleegroup.analytica.server.data.WhatDimension;
import com.kleegroup.analyticaimpl.server.cube.Cube;
import com.kleegroup.analyticaimpl.server.cube.CubeBuilder;
import com.kleegroup.analyticaimpl.server.cube.CubeKey;
import com.kleegroup.analyticaimpl.server.cube.MetaData;
import com.kleegroup.analyticaimpl.server.cube.Metric;
import com.kleegroup.analyticaimpl.server.cube.TimePosition;
import com.kleegroup.analyticaimpl.server.cube.WhatPosition;
import com.kleegroup.analyticaimpl.server.plugins.cubestore.h2.bean.CubeBuilderBean;
import com.kleegroup.analyticaimpl.server.plugins.cubestore.h2.bean.DataKeyBuilderBean;
import com.kleegroup.analyticaimpl.server.plugins.cubestore.h2.bean.LastProcessIdBuilderBean;
import com.kleegroup.analyticaimpl.server.plugins.cubestore.h2.bean.MetaDataBuilder;
import com.kleegroup.analyticaimpl.server.plugins.cubestore.h2.bean.MetricBuilder;
import com.kleegroup.analyticaimpl.server.plugins.cubestore.h2.bean.TimePositionBuilderBean;
import com.kleegroup.analyticaimpl.server.plugins.cubestore.h2.bean.WhatPositionBuilderBean;
import com.kleegroup.analyticaimpl.server.plugins.cubestore.h2.dao.DaoException;
import com.kleegroup.analyticaimpl.server.plugins.cubestore.h2.dao.H2DataBase;
import com.kleegroup.analyticaimpl.server.plugins.cubestore.h2.dao.PlainBean;
import com.kleegroup.analyticaimpl.server.plugins.cubestore.h2.dao.SimpleDAO;

/**
 * @author npiedeloup
 * @version $Id: CubeStatements.java,v 1.16 2013/01/14 16:35:20 npiedeloup Exp $
 */
final class CubeStatements {
	private static final String CREATE_DATABASE_SCRIPT;

	static {
		final StringBuilder sql = new StringBuilder();
		sql.append("CREATE SEQUENCE SEQ_CUBE; ");
		sql.append("CREATE SEQUENCE SEQ_METRIC; ");
		sql.append("CREATE SEQUENCE SEQ_META_DATA; ");
		sql.append("CREATE TABLE CUBE(CUB_ID BIGINT PRIMARY KEY, TIME_POSITION TIMESTAMP, TID_CD VARCHAR2, WHAT_POSITION VARCHAR2, WHD_CD VARCHAR2); ");
		sql.append("CREATE TABLE METRIC(MTR_ID BIGINT PRIMARY KEY, CUB_ID BIGINT, NAME VARCHAR2, COUNT BIGINT, MIN DOUBLE, MAX DOUBLE, SUM DOUBLE, SQR_SUM DOUBLE, FOREIGN KEY(CUB_ID) REFERENCES CUBE(CUB_ID)); ");
		sql.append("CREATE TABLE META_DATA(MTA_ID BIGINT PRIMARY KEY, CUB_ID BIGINT, NAME VARCHAR2, VALUE VARCHAR2, FOREIGN KEY(CUB_ID) REFERENCES CUBE(CUB_ID)); ");
		sql.append("CREATE MEMORY TABLE TIME_DIMENSION(TID_CD VARCHAR2 PRIMARY KEY, TID_CD_PARENT VARCHAR2, FOREIGN KEY(TID_CD_PARENT) REFERENCES TIME_DIMENSION(TID_CD)); ");
		sql.append("CREATE MEMORY TABLE WHAT_DIMENSION(WHD_CD VARCHAR2 PRIMARY KEY, WHD_CD_PARENT VARCHAR2, FOREIGN KEY(WHD_CD_PARENT) REFERENCES WHAT_DIMENSION(WHD_CD)); ");
		sql.append("CREATE MEMORY TABLE LAST_PROCESS_ID(LAST_PROCESS_ID VARCHAR2 PRIMARY KEY); ");

		sql.append("CREATE INDEX IDX_CUB_TIME ON CUBE(TID_CD, TIME_POSITION); ");
		sql.append("CREATE INDEX IDX_CUB_WHAT ON CUBE(WHD_CD, WHAT_POSITION); ");
		sql.append("CREATE INDEX IDX_MTR_CUB_ID ON METRIC(CUB_ID); ");
		sql.append("CREATE INDEX IDX_MTR_NAME ON METRIC(NAME); ");
		sql.append("CREATE INDEX IDX_MTA_CUB_ID ON META_DATA(CUB_ID); ");
		sql.append("CREATE INDEX IDX_MTA_NAME ON META_DATA(NAME); ");
		sql.append("CREATE INDEX IDX_TDI_PARENT ON TIME_DIMENSION(TID_CD, TID_CD_PARENT); ");
		sql.append("CREATE INDEX IDX_WHD_PARENT ON WHAT_DIMENSION(WHD_CD, WHD_CD_PARENT); ");
		sql.append("CREATE UNIQUE INDEX IDX_CUB_UNIQUE ON CUBE(TID_CD, WHD_CD, TIME_POSITION, WHAT_POSITION); ");
		sql.append("SET DEFAULT_LOCK_TIMEOUT 5000; ");

		CREATE_DATABASE_SCRIPT = sql.toString();
	}
	//private static final String WHERE_PART_RANGED_CUBE;
	static {
		final StringBuilder sql = new StringBuilder();
		sql.append("and cub.TID_CD = ${timeDimension} ");
		sql.append("and cub.WHD_CD = ${whatDimension} ");
		sql.append("and cub.time_position >= ${timePositionMin} ");
		sql.append("and cub.time_position < ${timePositionMax} ");
		sql.append("and cub.what_position >= ${whatPositionMin} ");
		sql.append("and cub.what_position < ${whatPositionMax} ");
		//	WHERE_PART_RANGED_CUBE = sql.toString();
	}
	private static final String SELECT_CUBE;
	static {
		final StringBuilder sql = new StringBuilder();
		sql.append("select cub.cub_id, cub.time_position, cub.tid_cd, cub.what_position, cub.whd_cd ");
		sql.append("from CUBE cub ");
		sql.append("where cub.TID_CD = ${timeDimension} ");
		sql.append("and cub.WHD_CD = ${whatDimension} ");
		sql.append("and cub.time_position = ${timePosition} ");
		sql.append("and cub.what_position = ${whatPosition} ");
		SELECT_CUBE = sql.toString();
	}

	private static final String SELECT_CUBE_LOCKED;
	static {
		final StringBuilder sql = new StringBuilder();
		sql.append(SELECT_CUBE);
		sql.append("for update ");
		SELECT_CUBE_LOCKED = sql.toString();
	}

	private static final String SELECT_METRICS;
	static {
		final StringBuilder sql = new StringBuilder();
		sql.append("select mtr.name, mtr.count, mtr.min, mtr.max, mtr.sum, mtr.sqr_sum ");
		sql.append("from METRIC mtr ");
		sql.append("where mtr.cub_id = ${cubId}; ");
		SELECT_METRICS = sql.toString();
	}
	//	private static final String SELECT_RANGED_METRICS;
	//	static {
	//		final StringBuilder sql = new StringBuilder();
	//		sql.append("select mtr.name, mtr.count, mtr.min, mtr.max, mtr.sum, mtr.sqr_sum ");
	//		sql.append("from CUBE cub, METRIC mtr ");
	//		sql.append("where mtr.cub_id = cub.cub_id ");
	//		sql.append(WHERE_PART_RANGED_CUBE);
	//		sql.append("and mtr.name in (${metric_list})");
	//		sql.append("; ");
	//		SELECT_RANGED_METRICS = sql.toString();
	//	}

	//	private static final String SELECT_GROUPED_RANGED_METRICS;
	//	static {
	//		final StringBuilder sql = new StringBuilder();
	//		sql.append("select mtr.name, sum(mtr.count), min(mtr.min), max(mtr.max), sum(mtr.sum), sum(mtr.sqr_sum) ");
	//		sql.append("from CUBE cub, METRIC mtr ");
	//		sql.append("where mtr.cub_id = cub.cub_id ");
	//		sql.append(WHERE_PART_RANGED_CUBE);
	//		sql.append("and mtr.name in (${metric_list})");
	//		sql.append("group by mtr.name; ");
	//		SELECT_GROUPED_RANGED_METRICS = sql.toString();
	//	}
	private static final String SELECT_METADATAS;
	static {
		final StringBuilder sql = new StringBuilder();
		sql.append("select mta.name, mta.value ");
		sql.append("from META_DATA mta ");
		sql.append("where mta.cub_id = ${cubId}; ");
		SELECT_METADATAS = sql.toString();
	}
	//	private static final String SELECT_RANGED_METADATAS;
	//	static {
	//		final StringBuilder sql = new StringBuilder();
	//		sql.append("select mta.name, mta.value ");
	//		sql.append("from CUBE cub, META_DATA mta ");
	//		sql.append("where mta.cub_id = cub.cub_id ");
	//		sql.append(WHERE_PART_RANGED_CUBE);
	//		sql.append("and mta.name in (${metadata_list})");
	//		sql.append("; ");
	//		SELECT_RANGED_METADATAS = sql.toString();
	//	}

	private final H2DataBase h2DataBase;

	public CubeStatements(final H2DataBase h2DataBase) {
		Assertion.notNull(h2DataBase);
		//---------------------------------------------------------------------
		this.h2DataBase = h2DataBase;
	}

	void createDataBase() throws DaoException {
		h2DataBase.createDb();
		executeAutoTx(CREATE_DATABASE_SCRIPT);
	}

	//	private static List<MetricBuilderBean> loadMetrics(final Date timeMin, final Date timeMax, final TimeDimension timeDimension, final String whatMin, final String whatMax,
	//			final WhatDimension whatDimension, final Set<String> metricNames, final Connection connection) throws DaoException {
	//		final PlainBean params = new PlainBean();
	//		params.set("time_dimension", timeDimension);
	//		params.set("what_dimension", whatDimension);
	//		params.set("time_position_min", timeMin);
	//		params.set("time_position_max", timeMax);
	//		params.set("what_position_min", whatMin);
	//		params.set("what_position_max", whatMax);
	//		final String preparedSql = addMultiValuedField("metric_list", metricNames, params, SELECT_RANGED_METRICS);
	//
	//		return SimpleDAO.executeQueryList(preparedSql, params, MetricBuilderBean.class, connection);
	//	}
	//
	//	private static List<MetaDataBuilderBean> loadMetaDatas(final Date timeMin, final Date timeMax, final TimeDimension timeDimension, final String whatMin, final String whatMax,
	//			final WhatDimension whatDimension, final Set<String> metaDataNames, final Connection connection) throws DaoException {
	//		final PlainBean params = new PlainBean();
	//		params.set("time_dimension", timeDimension);
	//		params.set("what_dimension", whatDimension);
	//		params.set("time_position_min", timeMin);
	//		params.set("time_position_max", timeMax);
	//		params.set("what_position_min", whatMin);
	//		params.set("what_position_max", whatMax);
	//		final String preparedSql = addMultiValuedField("metadata_list", metaDataNames, params, SELECT_RANGED_METADATAS);
	//		return SimpleDAO.executeQueryList(preparedSql, params, MetaDataBuilderBean.class, connection);
	//	}

	public Cube loadCube(final CubeKey cubeKey, final Connection connection) throws DaoException {
		long time = System.currentTimeMillis();
		final PlainBean params = new PlainBean();
		params.set("timeDimension", cubeKey.getTimePosition().getDimension().name());
		params.set("whatDimension", cubeKey.getWhatPosition().getDimension().name());
		params.set("timePosition", cubeKey.getTimePosition().getValue());
		params.set("whatPosition", cubeKey.getWhatPosition().getValue());
		final List<CubeBuilderBean> resultOption = SimpleDAO.executeQueryList(SELECT_CUBE, params, CubeBuilderBean.class, connection);
		Assertion.invariant(resultOption.size() <= 1, "Trop de cubes retourn�s"); //normalement assurer par une contrainte BDD d'unicit�
		if (resultOption.size() == 0) {
			return null;
		}
		final CubeBuilderBean result = resultOption.get(0);

		if (System.currentTimeMillis() - time > 1000) {
			System.out.println("load lent (" + (System.currentTimeMillis() - time) + " ms)======================================================");
			System.out.println("explain select cub.cub_id, cub.time_position, cub.tid_cd, cub.what_position, cub.whd_cd ");
			System.out.println("from CUBE cub");
			System.out.println("where cub.TID_CD = '" + cubeKey.getTimePosition().getDimension().name() + "'");
			System.out.println("and cub.WHD_CD = '" + cubeKey.getWhatPosition().getDimension().name() + "'");
			final SimpleDateFormat sdf = new SimpleDateFormat("yyyy-dd-MM hh:mm:ss");
			System.out.println("and cub.time_position = '" + sdf.format(cubeKey.getTimePosition().getValue()) + ".0	'");
			System.out.println("and cub.what_position = '" + cubeKey.getWhatPosition().getValue() + "'");
		}

		time = System.currentTimeMillis();
		//On reutilise le result comme entr�e des autres requetes 
		final List<MetricBuilder> metricBuilders = SimpleDAO.executeQueryList(SELECT_METRICS, result, MetricBuilder.class, connection);
		if (System.currentTimeMillis() - time > 1000) {
			System.out.println("load METRICS lent (" + (System.currentTimeMillis() - time) + " ms)  cubId:" + result.getCubId() + "======================================================");
		}

		time = System.currentTimeMillis();
		final List<MetaDataBuilder> metaDataBuilders = SimpleDAO.executeQueryList(SELECT_METADATAS, result, MetaDataBuilder.class, connection);
		if (System.currentTimeMillis() - time > 1000) {
			System.out.println("load SELECT_METADATAS lent (" + (System.currentTimeMillis() - time) + " ms)  cubId:" + result.getCubId() + "======================================================");
		}
		final CubeBuilder cubeBuilder = new CubeBuilder(cubeKey);
		for (final MetricBuilder metricBuilder : metricBuilders) {
			cubeBuilder.withMetric(metricBuilder.build());
		}
		for (final MetaDataBuilder metaDataBuilder : metaDataBuilders) {
			cubeBuilder.withMetaData(metaDataBuilder.build());
		}

		return cubeBuilder.build();
	}

	public List<Cube> loadCubes(final TimeDimension timeDimension, final WhatDimension whatDimension, final Date timeMin, final Date timeMax, final List<String> whatPrefixes, final Set<String> metricsNames, final Set<String> metaDataNames, final boolean aggregateTime, final boolean aggregateWhat, final Connection connection) throws DaoException {
		Assertion.precondition(whatPrefixes.size() >= 1, "Il faut au moins 1 pr�fix de whatPosition.");
		//---------------------------------------------------------------------
		final PlainBean params = new PlainBean();
		params.set("timeDimension", timeDimension.name());
		params.set("whatDimension", whatDimension.name());
		params.set("timePositionMin", timeMin);
		params.set("timePositionMax", timeMax);
		final StringBuilder agregateFields = new StringBuilder();
		if (!aggregateTime) {
			agregateFields.append(", cub.time_position");
		}
		if (!aggregateTime) {
			agregateFields.append(", cub.what_position");
		}

		final StringBuilder cubesSql = new StringBuilder();

		cubesSql.append("select cub.cub_id, cub.time_position, cub.tid_cd, cub.what_position, cub.whd_cd ");
		cubesSql.append("from CUBE cub ");
		cubesSql.append("where 1=1 ");
		appendCubeWherePart(whatPrefixes, params, cubesSql);
		cubesSql.append("order by cub.time_position, cub.what_position; ");
		final List<CubeBuilderBean> cubeInfos = SimpleDAO.executeQueryList(cubesSql.toString(), params, CubeBuilderBean.class, connection);

		final List<MetricBuilder> metricBuilders;
		if (!metricsNames.isEmpty()) {
			final StringBuilder metricsSql = new StringBuilder();
			metricsSql.append("select mtr.cub_id, mtr.name, sum(mtr.count) count, min(mtr.min) min, max(mtr.max) max, sum(mtr.sum) sum, sum(mtr.sqr_sum) sqr_sum ");
			metricsSql.append("from CUBE cub, METRIC mtr ");
			metricsSql.append("where mtr.cub_id = cub.cub_id ");
			appendCubeWherePart(whatPrefixes, params, metricsSql);
			metricsSql.append("and mtr.name in (${metricList}) ");
			metricsSql.append("group by mtr.cub_id, mtr.name; ");
			final String preparedMetricsSql = addMultiValuedField("metricList", metricsNames, params, metricsSql.toString());
			metricBuilders = SimpleDAO.executeQueryList(preparedMetricsSql, params, MetricBuilder.class, connection);
		} else {
			metricBuilders = Collections.emptyList();
		}
		final List<MetaDataBuilder> metaDataBuilders;
		if (!metaDataNames.isEmpty()) {
			final StringBuilder metaDatasSql = new StringBuilder();
			metaDatasSql.append("select distinct mta.cub_id, mta.name, mta.value ");
			metaDatasSql.append("from CUBE cub, META_DATA mta ");
			metaDatasSql.append("where mta.cub_id = cub.cub_id ");
			appendCubeWherePart(whatPrefixes, params, metaDatasSql);
			metaDatasSql.append("and mta.name in (${metaDataList}) ");
			metaDatasSql.append("order by mta.cub_id, mta.name; ");
			final String preparedMetaDatasSql = addMultiValuedField("metaDataList", metaDataNames, params, metaDatasSql.toString());
			metaDataBuilders = SimpleDAO.executeQueryList(preparedMetaDatasSql, params, MetaDataBuilder.class, connection);
		} else {
			metaDataBuilders = Collections.emptyList();
		}
		final Map<Long, CubeBuilder> cubeBuilderIndex = new LinkedHashMap<Long, CubeBuilder>();
		for (final CubeBuilderBean cubeInfo : cubeInfos) {
			final CubeBuilder cubeBuilder = cubeInfo.build();
			cubeBuilderIndex.put(cubeInfo.getCubId(), cubeBuilder);
		}
		CubeBuilder cubeBuilder;
		for (final MetricBuilder metricBuilder : metricBuilders) {
			cubeBuilder = cubeBuilderIndex.get(metricBuilder.getCubId());
			cubeBuilder.withMetric(metricBuilder.build());
		}
		for (final MetaDataBuilder metaDataBuilder : metaDataBuilders) {
			cubeBuilder = cubeBuilderIndex.get(metaDataBuilder.getCubId());
			cubeBuilder.withMetaData(metaDataBuilder.build());
		}
		final List<Cube> cubes = new ArrayList<Cube>();
		for (final CubeBuilder cubeBuilderFinal : cubeBuilderIndex.values()) {
			cubes.add(cubeBuilderFinal.build());
		}
		return cubes;
	}

	public List<Cube> loadCubes(final TimeDimension timeDimension, final WhatDimension whatDimension, final Date timeMin, final Date timeMax, final Connection connection) throws DaoException {
		final PlainBean params = new PlainBean();
		params.set("timeDimension", timeDimension.name());
		params.set("whatDimension", whatDimension.name());
		params.set("timePositionMin", timeMin);
		params.set("timePositionMax", timeMax);

		final StringBuilder cubesSql = new StringBuilder();

		cubesSql.append("select cub.cub_id, cub.time_position, cub.tid_cd, cub.what_position, cub.whd_cd ");
		cubesSql.append("from CUBE cub ");
		cubesSql.append("where 1=1 ");
		appendCubeWherePart(Collections.<String> emptyList(), params, cubesSql);
		cubesSql.append(" order by cub.time_position, cub.what_position; ");
		final List<CubeBuilderBean> cubeInfos = SimpleDAO.executeQueryList(cubesSql.toString(), params, CubeBuilderBean.class, connection);

		final StringBuilder metricsSql = new StringBuilder();
		metricsSql.append("select mtr.cub_id, mtr.name, sum(mtr.count) count, min(mtr.min) min, max(mtr.max) max, sum(mtr.sum) sum, sum(mtr.sqr_sum) sqr_sum ");
		metricsSql.append("from CUBE cub, METRIC mtr ");
		metricsSql.append("where mtr.cub_id = cub.cub_id ");
		appendCubeWherePart(Collections.<String> emptyList(), params, metricsSql);
		metricsSql.append(" group by mtr.cub_id, mtr.name; ");
		final List<MetricBuilder> metricBuilders = SimpleDAO.executeQueryList(metricsSql.toString(), params, MetricBuilder.class, connection);

		final StringBuilder metaDatasSql = new StringBuilder();
		metaDatasSql.append("select distinct mta.cub_id, mta.name, mta.value ");
		metaDatasSql.append("from CUBE cub, META_DATA mta ");
		metaDatasSql.append("where mta.cub_id = cub.cub_id ");
		appendCubeWherePart(Collections.<String> emptyList(), params, metaDatasSql);
		metaDatasSql.append(" order by mta.cub_id, mta.name; ");
		final List<MetaDataBuilder> metaDataBuilders = SimpleDAO.executeQueryList(metaDatasSql.toString(), params, MetaDataBuilder.class, connection);

		final Map<Long, CubeBuilder> cubeBuilderIndex = new LinkedHashMap<Long, CubeBuilder>();
		for (final CubeBuilderBean cubeInfo : cubeInfos) {
			final CubeBuilder cubeBuilder = cubeInfo.build();
			cubeBuilderIndex.put(cubeInfo.getCubId(), cubeBuilder);
		}
		CubeBuilder cubeBuilder;
		for (final MetricBuilder metricBuilder : metricBuilders) {
			cubeBuilder = cubeBuilderIndex.get(metricBuilder.getCubId());
			cubeBuilder.withMetric(metricBuilder.build());
		}
		for (final MetaDataBuilder metaDataBuilder : metaDataBuilders) {
			cubeBuilder = cubeBuilderIndex.get(metaDataBuilder.getCubId());
			cubeBuilder.withMetaData(metaDataBuilder.build());
		}
		final List<Cube> cubes = new ArrayList<Cube>();
		for (final CubeBuilder cubeBuilderFinal : cubeBuilderIndex.values()) {
			cubes.add(cubeBuilderFinal.build());
		}
		return cubes;
	}

	private void appendCubeWherePart(final List<String> whatPrefixes, final PlainBean params, final StringBuilder sql) {
		sql.append("and cub.TID_CD = ${timeDimension} ");
		sql.append("and cub.WHD_CD = ${whatDimension} ");
		sql.append("and cub.time_position >= ${timePositionMin} ");
		sql.append("and cub.time_position < ${timePositionMax} ");

		if (!whatPrefixes.isEmpty()) {
			sql.append("and (");
			String sep = "";
			for (int i = 0; i < whatPrefixes.size(); i++) {
				final String whatPrefixName = "whatPrefix" + i;
				final String whatPrefixValue = whatPrefixes.get(i);
				sql.append(sep);
				sql.append("cub.what_position like ${");
				sql.append(whatPrefixName);
				sql.append("} || '%' ");
				params.set(whatPrefixName, whatPrefixValue);
				sep = " or ";
			}
			sql.append(") ");
		}
	}

	public void saveCube(final Cube cube, final Connection connection) throws DaoException {
		final PlainBean params = new PlainBean();
		params.set("timeDimension", cube.getKey().getTimePosition().getDimension().name());
		params.set("whatDimension", cube.getKey().getWhatPosition().getDimension().name());
		params.set("timePosition", cube.getKey().getTimePosition().getValue());
		params.set("whatPosition", cube.getKey().getWhatPosition().getValue());

		final List<CubeBuilderBean> result = SimpleDAO.executeQueryList(SELECT_CUBE_LOCKED, params, CubeBuilderBean.class, connection);
		final Long cubId;
		if (result.size() == 0) {
			//insert
			SimpleDAO.executeSQL("INSERT INTO CUBE (CUB_ID, TIME_POSITION, TID_CD, WHAT_POSITION, WHD_CD) VALUES(NEXTVAL('SEQ_CUBE'), ${timePosition}, " + "${timeDimension}, ${whatPosition}, ${whatDimension}); @generated(cubId)", params, connection);
			cubId = params.get("cubId");
		} else {
			//update : il n'y a pas a toucher au cube lui m�me
			Assertion.postcondition(result.size() == 1, "Trop de resultat ({1}) pour {0}", cube.getKey(), result.size());
			cubId = result.get(0).getCubId();
		}

		saveMetrics(cube.getMetrics(), cubId, connection);
		saveMetaDatas(cube.getMetaDatas(), cubId, connection);
	}

	private void saveMetrics(final Collection<Metric> metrics, final long cubId, final Connection connection) throws DaoException {
		//if (auction.getAucId() == null) {
		final PlainBean params = new PlainBean();
		params.set("cubId", cubId);
		SimpleDAO.executeSQL("DELETE FROM METRIC WHERE CUB_ID = ${cubId}; ", params, connection);

		for (final Metric metric : metrics) {
			params.set("name", metric.getName());
			params.set(Metric.F_COUNT, metric.get(Metric.F_COUNT));
			params.set(Metric.F_MIN, metric.get(Metric.F_MIN));
			params.set(Metric.F_MAX, metric.get(Metric.F_MAX));
			params.set(Metric.F_SUM, metric.get(Metric.F_SUM));
			params.set(Metric.F_SQR_SUM, metric.get(Metric.F_SQR_SUM));
			SimpleDAO.executeSQL("INSERT INTO METRIC (MTR_ID, CUB_ID, NAME, COUNT, MIN, MAX, SUM, SQR_SUM) VALUES(NEXTVAL('SEQ_METRIC'), ${cubId}, " + "${name}, ${count}, ${min}, ${max}, ${sum}, ${sqrSum}); @generated(mtrId)", params, connection);
		}
		//		} else {
		//			SimpleDAO.executeSQL("UPDATE AUCTION SET START_INDEX = ${startIndex}," + " END_INDEX = ${endIndex}," + " TOTAL_INDEX = ${totalIndex},"
		//					+ " UPDATE_TIME = CURRENT_TIMESTAMP()" + " WHERE AUC_ID = ${aucId}", auction, connection);
		//
		//		}
	}

	private void saveMetaDatas(final Collection<MetaData> metaDatas, final long cubId, final Connection connection) throws DaoException {
		//if (auction.getAucId() == null) {
		final PlainBean params = new PlainBean();
		params.set("cubId", cubId);
		SimpleDAO.executeSQL("DELETE FROM META_DATA WHERE CUB_ID = ${cubId}; ", params, connection);
		for (final MetaData metaData : metaDatas) {
			params.set("name", metaData.getName());
			params.set("value", metaData.getValue());
			SimpleDAO.executeSQL("INSERT INTO META_DATA (MTA_ID, CUB_ID, NAME, VALUE) VALUES(NEXTVAL('SEQ_META_DATA'), ${cubId}, ${name}, ${value}); @generated(mtaId)", params, connection);
		}
		//		} else {
		//			SimpleDAO.executeSQL("UPDATE AUCTION SET START_INDEX = ${startIndex}," + " END_INDEX = ${endIndex}," + " TOTAL_INDEX = ${totalIndex},"
		//					+ " UPDATE_TIME = CURRENT_TIMESTAMP()" + " WHERE AUC_ID = ${aucId}", auction, connection);
		//
		//		}
	}

	private static String addMultiValuedField(final String fieldName, final Set<String> fieldValues, final PlainBean params, final String oldSql) {
		final StringBuilder sb = new StringBuilder();
		String sep = "";
		int i = 0;
		for (final String fieldValue : fieldValues) {
			final String newFieldName = fieldName + i++;
			sb.append(sep).append("${").append(newFieldName).append("}");
			params.set(newFieldName, fieldValue);
			sep = ",";
		}
		return oldSql.replace("${" + fieldName + "}", sb.toString());
	}

	public List<TimePosition> loadTimePositions(final TimeDimension timeDimension, final Date timeMin, final Date timeMax, final Connection connection) throws DaoException {
		final PlainBean params = new PlainBean();
		params.set("timeDimension", timeDimension.name());
		params.set("whatDimension", WhatDimension.Global);
		params.set("timePositionMin", timeMin);
		params.set("timePositionMax", timeMax);

		final StringBuilder cubesSql = new StringBuilder();

		cubesSql.append("select distinct cub.time_position, cub.tid_cd ");
		cubesSql.append("from CUBE cub ");
		cubesSql.append("where 1=1 ");
		appendCubeWherePart(Collections.<String> emptyList(), params, cubesSql);
		cubesSql.append("order by cub.time_position; ");
		final List<TimePositionBuilderBean> timePositionBuilders = SimpleDAO.executeQueryList(cubesSql.toString(), params, TimePositionBuilderBean.class, connection);

		final List<TimePosition> timePositions = new ArrayList<TimePosition>();
		for (final TimePositionBuilderBean timePositionBuilder : timePositionBuilders) {
			timePositions.add(timePositionBuilder.buildTimePosition());
		}
		return timePositions;
	}

	public List<WhatPosition> loadWhatPositions(final TimeDimension timeDimension, final WhatDimension whatDimension, final Date timeMin, final Date timeMax, final List<String> whatPrefixes, final Connection connection) throws DaoException {
		final PlainBean params = new PlainBean();
		params.set("timeDimension", timeDimension.name());
		params.set("whatDimension", whatDimension.name());
		params.set("timePositionMin", timeMin);
		params.set("timePositionMax", timeMax);

		final StringBuilder cubesSql = new StringBuilder();

		cubesSql.append("select distinct cub.what_position, cub.whd_cd ");
		cubesSql.append("from CUBE cub ");
		cubesSql.append("where 1=1 ");
		appendCubeWherePart(whatPrefixes, params, cubesSql);
		cubesSql.append("order by cub.what_position; ");
		final List<WhatPositionBuilderBean> whatPositionBuilders = SimpleDAO.executeQueryList(cubesSql.toString(), params, WhatPositionBuilderBean.class, connection);

		final List<WhatPosition> whatPositions = new ArrayList<WhatPosition>();
		for (final WhatPositionBuilderBean whatPositionBuilder : whatPositionBuilders) {
			whatPositions.add(whatPositionBuilder.buildWhatPosition());
		}
		return whatPositions;
	}

	public List<DataKey> loadDataKeys(final TimeDimension timeDimension, final WhatDimension whatDimension, final Date timeMin, final Date timeMax, final List<String> whatPrefixes, final Connection connection) throws DaoException {
		final PlainBean params = new PlainBean();
		params.set("timeDimension", timeDimension.name());
		params.set("whatDimension", whatDimension.name());
		params.set("timePositionMin", timeMin);
		params.set("timePositionMax", timeMax);

		final StringBuilder metricsSql = new StringBuilder();
		metricsSql.append("select distinct mtr.name ");
		metricsSql.append("from CUBE cub, METRIC mtr ");
		metricsSql.append("where mtr.cub_id = cub.cub_id ");
		appendCubeWherePart(whatPrefixes, params, metricsSql);
		metricsSql.append(" order by mtr.name; ");
		final List<DataKeyBuilderBean> metricsBuilders = SimpleDAO.executeQueryList(metricsSql.toString(), params, DataKeyBuilderBean.class, connection);

		final StringBuilder metaDatasSql = new StringBuilder();
		metaDatasSql.append("select distinct mta.name ");
		metaDatasSql.append("from CUBE cub, META_DATA mta ");
		metaDatasSql.append("where mta.cub_id = cub.cub_id ");
		appendCubeWherePart(whatPrefixes, params, metaDatasSql);
		metaDatasSql.append(" order by mta.name; ");
		final List<DataKeyBuilderBean> metaDatasBuilders = SimpleDAO.executeQueryList(metaDatasSql.toString(), params, DataKeyBuilderBean.class, connection);

		final List<DataKey> dataKeys = new ArrayList<DataKey>();
		for (final DataKeyBuilderBean dataKeyBuilder : metricsBuilders) {
			dataKeys.add(dataKeyBuilder.build(DataType.count));
			dataKeys.add(dataKeyBuilder.build(DataType.max));
			dataKeys.add(dataKeyBuilder.build(DataType.min));
			dataKeys.add(dataKeyBuilder.build(DataType.mean));
			dataKeys.add(dataKeyBuilder.build(DataType.stdDev));
		}
		for (final DataKeyBuilderBean dataKeyBuilder : metaDatasBuilders) {
			dataKeys.add(dataKeyBuilder.build(DataType.metaData));
		}

		return dataKeys;
	}

	private void executeAutoTx(final String sql) throws DaoException {
		final Connection conn = h2DataBase.getConnection();
		try {
			SimpleDAO.executeSQL(sql, conn);
			h2DataBase.commit(conn);
		} finally {
			h2DataBase.close(conn);
		}
	}

	public void saveLastProcessIdStored(final String lastProcessIdStored, final Connection connection) throws DaoException {
		final PlainBean params = new PlainBean();
		params.set("lastProcessId", lastProcessIdStored);
		SimpleDAO.executeSQL("DELETE FROM LAST_PROCESS_ID; ", connection);
		SimpleDAO.executeSQL("INSERT INTO LAST_PROCESS_ID (LAST_PROCESS_ID) VALUES (${lastProcessId});", params, connection);
	}

	public String loadLastProcessIdStored(final Connection connection) throws DaoException {
		final String query = "SELECT LAST_PROCESS_ID FROM LAST_PROCESS_ID;";
		final List<LastProcessIdBuilderBean> lastProcessIdBuilder = SimpleDAO.executeQueryList(query, LastProcessIdBuilderBean.class, connection);
		if (lastProcessIdBuilder.isEmpty()) {
			return null;
		}
		return lastProcessIdBuilder.get(0).getLastProcessId();
	}
}