package com.ml.logistica.romaneio.dao;

import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import javax.annotation.PostConstruct;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.ResultSetExtractor;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

import com.ml.logistica.romaneio.entity.Branch;
import com.ml.logistica.romaneio.entity.Order;
import com.ml.logistica.romaneio.entity.PackingList;
import com.ml.logistica.romaneio.entity.PackingListDetail;
import com.ml.logistica.romaneio.entity.PackingListType;

@Repository
@Configuration
@ConfigurationProperties(prefix = "ml.logistica.api.romaneio.packingListDAO")
public class PackingListDAO {

	private static Logger logger = LoggerFactory.getLogger(PackingListDAO.class);

	@Autowired
	private NamedParameterJdbcTemplate template;

	@Autowired
	private ResourceLoader resourceLoader;

	private String findTraditionalPackingListSQL;

	private String findCourrierPackingListSQL;

	@PostConstruct
	public void postConstruct() {
		findTraditionalPackingListSQL = StringUtils.trimToEmpty(findTraditionalPackingListSQL);
		findCourrierPackingListSQL = StringUtils.trimToEmpty(findCourrierPackingListSQL);

		if (findTraditionalPackingListSQL.startsWith("classpath:")) {
			findTraditionalPackingListSQL = loadQueries(findTraditionalPackingListSQL);
		}
		if (findCourrierPackingListSQL.startsWith("classpath:")) {
			findCourrierPackingListSQL = loadQueries(findCourrierPackingListSQL);
		}

	}

	private String loadQueries(String path) {
		try {
			Resource queryResource = resourceLoader.getResource(path);
			return IOUtils.readLines(queryResource.getInputStream()).stream().filter(line -> {
				return !line.matches("^\\s*--.*?");
			}).collect(Collectors.joining("\n"));
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	private List<PackingList> findPackingList(String sql, PackingListFilter filter) {
		Map<String, Object> params = new HashMap<>();
		params.put("NROMANEIO", filter.getId());
		params.put("CODFIL", filter.getBranch());
		params.put("START_DATE", filter.getStartDate());
		params.put("END_DATE", filter.getEndDate());

		sql = parseQueryParameters(sql, params);
		logger.debug("Executing query:\n\n" + sql + "\n\nWith parameters: " + filter);
		return template.query(sql, params, new ResultSetExtractor<List<PackingList>>() {
			@Override
			public List<PackingList> extractData(ResultSet rs) throws SQLException, DataAccessException {
				List<PackingList> result = new ArrayList<>();

				PackingList pl = null;

				while (rs.next()) {
					Long nromaneio = rs.getLong("NROMANEIO");
					// Se mudar o romaneio, iniciamos um novo.
					if (pl == null || !pl.getId().equals(nromaneio)) {
						pl = new PackingList();
						result.add(pl);

						pl.setId(nromaneio);
						pl.setBranch(new Branch(rs.getLong("CODFIL")));
						String type = rs.getString("TIPO");
						if (StringUtils.trimToNull(type) != null) {
							pl.setType(PackingListType.valueOf(rs.getString("TIPO")));
						}
						Date date = rs.getDate("DATA_ROMANEIO");
						if (date != null) {
							Calendar cal = Calendar.getInstance();
							cal.setTime(rs.getDate("DATA_ROMANEIO"));
							pl.setCreatedAt(cal);
						}
					}
					// Adicionamos os detalhes do romaneio
					PackingListDetail plDetail = new PackingListDetail();
					pl.getDetails().add(plDetail);
					plDetail.setOrder(new Order(rs.getLong("NUMPEDVEN")));
					plDetail.setBatch(rs.getLong("NLOTE"));
					// --

				}
				logger.debug("The query returned " + (result == null ? 0 : result.size()) + " iten(s)");
				return result;
			}
		});
	}

	public List<PackingList> findTraditionalPackingList(PackingListFilter filter) {
		return findPackingList(findTraditionalPackingListSQL, filter);
	}

	public NamedParameterJdbcTemplate getTemplate() {
		return template;
	}

	public void setTemplate(NamedParameterJdbcTemplate template) {
		this.template = template;
	}

	public PackingList getPackingList(Long branch, Long id) {
		return getPackingList(branch, id, null);
	}

	public PackingList getPackingList(Long branch, Long id, PackingListType type) {
		PackingListFilter filter = new PackingListFilter();
		filter.setId(id);
		filter.setBranch(branch);
		List<PackingList> result = null;
		if (type == null || PackingListType.Traditional.equals(type)) {
			result = findTraditionalPackingList(filter);
		}
		if ((type == null && CollectionUtils.isEmpty(result)) || PackingListType.Courrier.equals(type)) {
			result = findCourrierPackingList(filter);
		}
		return CollectionUtils.isNotEmpty(result) ? result.get(0) : null;
	}

	private String parseQueryParameters(String sql, Map<String, ?> params) {
		String result = sql;

		for (String k : params.keySet()) {
			Object v = params.get(k);
			if (v == null) {
				result = result.replaceAll("\\[\\s*:" + k + "\\s*\\]\\s*\\[.*?\\];", "");
			} else {
				result = result.replaceAll("\\[\\s*:" + k + "\\s*\\]\\s*\\[(.*?)\\];", "$1");
			}
		}

		return result;
	}

	public List<PackingList> findCourrierPackingList(PackingListFilter filter) {
		return findPackingList(findCourrierPackingListSQL, filter);
	}

	public ResourceLoader getResourceLoader() {
		return resourceLoader;
	}

	public void setResourceLoader(ResourceLoader resourceLoader) {
		this.resourceLoader = resourceLoader;
	}

	public String getFindTraditionalPackingListSQL() {
		return findTraditionalPackingListSQL;
	}

	public void setFindTraditionalPackingListSQL(String findTraditionalPackingListSQL) {
		this.findTraditionalPackingListSQL = findTraditionalPackingListSQL;
	}

	public String getFindCourrierPackingListSQL() {
		return findCourrierPackingListSQL;
	}

	public void setFindCourrierPackingListSQL(String findCourrierPackingListSQL) {
		this.findCourrierPackingListSQL = findCourrierPackingListSQL;
	}

}
