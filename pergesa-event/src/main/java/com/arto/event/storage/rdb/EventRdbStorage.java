/**
 * Licensed to the Apache Software Foundation (ASF) under one or more contributor license agreements. See the NOTICE
 * file distributed with this work for additional information regarding copyright ownership. The ASF licenses this file
 * to You under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the
 * License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on
 * an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations under the License.
 */
package com.arto.event.storage.rdb;

import com.arto.event.common.Constants;
import com.arto.event.common.EventStatusEnum;
import com.arto.event.storage.EventInfo;
import com.arto.event.storage.EventStorage;
import com.arto.event.util.StringUtil;
import com.google.common.base.Strings;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.BeanPropertySqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 事件JDBC操作类
 *
 * Created by xiong.j on 2016/12/21.
 */
@Repository
public class EventRdbStorage implements EventStorage {

	private static final String EVENT_COLUMN = "ID, TAG, SYSTEM_ID, BUSINESS_ID, BUSINESS_TYPE, " +
			" EVENT_TYPE, STATUS, PAYLOAD, RETRIED_COUNT_D, RETRIED_COUNT_C, " +
			" NEXT_RETRY_TIME, MEMO, GMT_CREATED, GMT_MODIFIED ";

	private static final String EVENT_SQL = " SELECT " + EVENT_COLUMN + " FROM EVENT_STORAGE E ";

    @Autowired
	private NamedParameterJdbcTemplate npJdbcTemplate;

	@Override
	public EventInfo create(EventInfo eventInfo) throws SQLException {
		String sql = (" INSERT INTO EVENT_STORAGE(TAG, SYSTEM_ID, BUSINESS_ID, BUSINESS_TYPE, "
				+ " EVENT_TYPE, STATUS, PAYLOAD, RETRIED_COUNT_D, GMT_CREATED, GMT_MODIFIED) VALUES ("
				+ " :tag, :systemId, :businessId, :businessType, :eventType, :status, :payload, :defaultRetriedCount,"
				+ Constants.PG_DATE_SQL + "," + Constants.PG_DATE_SQL + " ) ");

		KeyHolder keyHolder = new GeneratedKeyHolder();
		npJdbcTemplate.update(sql, new BeanPropertySqlParameterSource(eventInfo), keyHolder);
		/*
		// 为何keyColumnNames设了后会在SQL中加上双引号?
		npJdbcTemplate.update(sql, new BeanPropertySqlParameterSource(eventInfo), keyHolder, new String[]{"ID", "GMT_MODIFIED"});
		*/
		eventInfo.setId((Long)keyHolder.getKeys().get("ID"));
		eventInfo.setGmtModified((Timestamp)keyHolder.getKeys().get("GMT_MODIFIED"));
		return eventInfo;
	}

	@Override
	public int update(EventInfo eventInfo) {
		StringBuilder builder = new StringBuilder();
		builder.append("UPDATE EVENT_STORAGE SET ");
		if (eventInfo.getStatus() != -1) {
			builder.append(" STATUS = :status, ");
		}
		if (eventInfo.getCurrentRetriedCount() != 0) {
			builder.append(" RETRIED_COUNT_C = :currentRetriedCount, ");
		}
		if (eventInfo.getNextRetryTime() != null) {
			builder.append(" NEXT_RETRY_TIME = :nextRetryTime, ");
		}
		builder.append(" GMT_MODIFIED = " + Constants.PG_DATE_SQL);
		builder.append(" WHERE ID = :id");
		return npJdbcTemplate.update(builder.toString(), new BeanPropertySqlParameterSource(eventInfo));
	}

	@Override
	public int optimisticUpdate(EventInfo eventInfo) {
		StringBuilder builder = new StringBuilder();
		builder.append("UPDATE EVENT_STORAGE SET ");
		if (eventInfo.getStatus() != -1) {
			builder.append(" STATUS = :status, ");
		}
		if (eventInfo.getCurrentRetriedCount() != 0) {
			builder.append(" RETRIED_COUNT_C = :currentRetriedCount, ");
		}
		if (eventInfo.getNextRetryTime() != null) {
			builder.append(" NEXT_RETRY_TIME = :nextRetryTime, ");
		}
		builder.append(" GMT_MODIFIED = " + Constants.PG_DATE_SQL);
		builder.append(" WHERE ID = :id ");
		builder.append(" AND GMT_MODIFIED = :gmtModified ");
		return npJdbcTemplate.update(builder.toString(), new BeanPropertySqlParameterSource(eventInfo));
	}

	@Override
	public int delete(EventInfo eventInfo) {
		StringBuilder builder = new StringBuilder();
		builder.append("DELETE FROM EVENT_STORAGE WHERE STATUS IN(2, 3) AND GMT_MODIFIED < :gmtModified ");
		return npJdbcTemplate.update(builder.toString(), new BeanPropertySqlParameterSource(eventInfo));
	}

	@Override
	public EventInfo findById(long id) {
		String sql = EVENT_SQL + " WHERE ID = :id ";
		Map<String, Object> paramMap = new HashMap<String, Object>();
		paramMap.put("id", id);
		return npJdbcTemplate.queryForObject(sql, paramMap, new EventRowMapper());
		// return npJdbcTemplate.queryForObject(sql, (SqlParameterSource)EmptySqlParameterSource.INSTANCE, new EventRowMapper());
	}

	@Override
	public EventInfo lockById(long id) {
		String sql = EVENT_SQL + " WHERE ID = :id FOR UPDATE NOWAIT";
		Map<String, Object> paramMap = new HashMap<String, Object>();
		paramMap.put("id", id);
		return npJdbcTemplate.queryForObject(sql, paramMap, new EventRowMapper());
	}

	@Override
	public List<EventInfo> lock(EventInfo eventInfo) {
		return null;
	}

	@Override
	public List<EventInfo> find(EventInfo eventInfo) {
		StringBuilder builder = new StringBuilder();

		builder.append(EVENT_SQL + " WHERE 1=1");
		if (!Strings.isNullOrEmpty(eventInfo.getSystemId())) {
			builder.append(" AND SYSTEM_ID = :systemId");
		}
		if (eventInfo.getStatus() != 0) {
			builder.append(" AND STATUS = :status");
		}

		return npJdbcTemplate.query(builder.toString(), new BeanPropertySqlParameterSource(eventInfo), new EventRowMapper());
	}

	@Override
	public List<EventInfo> findSince(String systemId, List<Integer> tags, Timestamp recoveryDate, Timestamp delaySecond, int limit) {
		StringBuilder builder = new StringBuilder();

		builder.append(" SELECT " + EVENT_COLUMN + " FROM(");
		// 等待处理的数据, 默认延迟10分钟再执行
		builder.append(EVENT_SQL + " WHERE ");
		builder.append(" SYSTEM_ID = :systemId");
		builder.append(" AND TAG IN (").append(StringUtil.join(tags, ",")).append(")");
		builder.append(" AND STATUS = " + EventStatusEnum.WAIT.getCode());
		builder.append(" AND GMT_MODIFIED < :delaySecond");
		builder.append(" UNION ALL ");
		// 重试过1次以上的数据，按照重试时间取
		builder.append(EVENT_SQL + " WHERE ");
		builder.append(" SYSTEM_ID = :systemId" );
		builder.append(" AND TAG IN (").append(StringUtil.join(tags, ",")).append(")");
		builder.append(" AND GMT_MODIFIED > :maxRecoveryDate");
		builder.append(" AND STATUS = " + EventStatusEnum.PROCESSING.getCode());
		builder.append(" AND NEXT_RETRY_TIME < :currentDate");
		builder.append(" ) es LIMIT :limit");

		Map<String, Object> paramMap = new HashMap<String, Object>();
		paramMap.put("systemId", systemId);
		paramMap.put("delaySecond", delaySecond);
		paramMap.put("maxRecoveryDate", recoveryDate);
		paramMap.put("currentDate", new Timestamp(System.currentTimeMillis()));
		paramMap.put("limit", limit);

		return npJdbcTemplate.query(builder.toString(), paramMap, new EventRowMapper());
	}

	private class EventRowMapper implements RowMapper<EventInfo> {

		@Override
		public EventInfo mapRow(ResultSet rs, int i) throws SQLException {
			EventInfo eventInfo = new EventInfo();
			eventInfo.setId(rs.getLong("ID"));
			eventInfo.setTag(rs.getInt("TAG"));
			eventInfo.setSystemId(rs.getString("SYSTEM_ID"));
			eventInfo.setBusinessId(rs.getString("BUSINESS_ID"));
			eventInfo.setBusinessType(rs.getString("BUSINESS_TYPE"));
			eventInfo.setEventType(rs.getString("EVENT_TYPE"));
			eventInfo.setStatus(rs.getInt("STATUS"));
			eventInfo.setPayload(rs.getString("PAYLOAD"));
			eventInfo.setDefaultRetriedCount(rs.getInt("RETRIED_COUNT_D"));
			eventInfo.setCurrentRetriedCount(rs.getInt("RETRIED_COUNT_C"));
			eventInfo.setNextRetryTime(rs.getTimestamp("NEXT_RETRY_TIME"));
			eventInfo.setMemo(rs.getString("MEMO"));
			eventInfo.setGmtCreated(rs.getTimestamp("GMT_CREATED"));
			eventInfo.setGmtModified(rs.getTimestamp("GMT_MODIFIED"));
			return eventInfo;
		}
	}
}
