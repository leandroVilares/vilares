package com.ml.logistica.romaneio.dao;

import com.ml.logistica.romaneio.entity.*;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;
import java.util.stream.Collectors;

@Repository
@Configuration
@ConfigurationProperties(prefix = "ml.logistica.api.romaneio.packingList")
public class PackingListDAO {

    private static Logger logger = LoggerFactory.getLogger(PackingListDAO.class);
    @Autowired
    private NamedParameterJdbcTemplate template;
    @Autowired
    private ResourceLoader resourceLoader;

    private List<QueryData> queries;

    public List<PackingList> findPackingList(PackingListFilter filter, Long packingListTypeId) {

        final List<PackingList> result = new ArrayList<>();
        if (packingListTypeId == null) {
            queries.forEach(query -> {
                Optional.ofNullable(findPackingList(query.getQuery(), filter)).ifPresent(result::addAll);
            });
        } else {
            result.addAll(findPackingList(getQueryById(packingListTypeId).getQuery(), filter));
        }

        return result;

    }

    private List<PackingList> findPackingList(String sql, PackingListFilter filter) {
        Map<String, Object> params = new HashMap<>();
        params.put("NROMANEIO", filter.getId());
        params.put("CODFIL", filter.getBranch());
        params.put("START_DATE", filter.getStartDate());
        params.put("END_DATE", filter.getEndDate());

        sql = parseQueryParameters(sql, params);
        logger.debug("Executing query:\n\n" + sql + "\n\nWith parameters: " + filter);
        return template.query(sql, params, rs -> {
            List<PackingList> result = new ArrayList<>();

            PackingList pl = null;

            buildRomaneioList(rs, result, pl);
            logger.debug("The query returned " + (result == null ? 0 : result.size()) + " iten(s)");
            return result;
        });
    }

    private void buildRomaneioList(ResultSet rs, List<PackingList> result, PackingList pl) throws SQLException {
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
                    pl.setType(getQueryByName(rs.getString("TIPO")).toPackingListType());
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
    }

    public QueryData getQueryById(Long typeId) {
        return queries.stream().filter(qd -> qd.getId().equals(typeId)).findFirst().orElse(null);
    }

    private String parseQueryParameters(String sql, Map<String, ?> params) {
        String result = sql;

        List<String> paramsToRemove = new ArrayList<>();

        for (Map.Entry<String, ?> es : params.entrySet()) {
            String k = es.getKey();
            Object v = es.getValue();
            if (v == null) {
                result = result.replaceAll("\\[\\s*:" + k + "\\s*\\]\\s*\\[.*?\\];", "");
                paramsToRemove.add(k);
            } else {
                result = result.replaceAll("\\[\\s*:" + k + "\\s*\\]\\s*\\[(.*?)\\];", "$1");
            }
        }

        paramsToRemove.forEach(params::remove);

        return result;
    }

    public QueryData getQueryByName(String name) {
        return queries.stream().filter(qd -> qd.getName().equals(name)).findFirst().orElse(null);
    }

    public PackingList getPackingList(Long branch, Long id) {
        return getPackingList(branch, id, null);
    }

    public PackingList getPackingList(Long branch, Long id, Long type) {
        PackingListFilter filter = new PackingListFilter();
        filter.setId(id);
        filter.setBranch(branch);

        if (Optional.ofNullable(type).isPresent()) {
            return findPackingList(getQueryById(type).getQuery(), filter).stream().findFirst().orElse(null);
        } else {
            for (QueryData qd : queries) {
                PackingList pl = findPackingList(getQueryById(qd.getId()).getQuery(), filter).stream().findFirst().orElse(null);
                if (pl != null) {
                    return pl;
                }
            }
            return null;
        }

    }

    public List<QueryData> getQueries() {
        return queries;
    }

    public void setQueries(List<QueryData> queries) {
        this.queries = queries;
    }

    public ResourceLoader getResourceLoader() {
        return resourceLoader;
    }

    public void setResourceLoader(ResourceLoader resourceLoader) {
        this.resourceLoader = resourceLoader;
    }

    public NamedParameterJdbcTemplate getTemplate() {
        return template;
    }

    public void setTemplate(NamedParameterJdbcTemplate template) {
        this.template = template;
    }

    @PostConstruct
    public void postConstruct() {

        if (queries != null) {
            queries.forEach(qData -> qData.setQuery(loadQueries(qData.getQuery())));
        }

    }

    private String loadQueries(String query) {
        query = StringUtils.trimToEmpty(query);
        if (query.startsWith("classpath:")) {
            try {
                Resource queryResource = resourceLoader.getResource(query);
                return IOUtils.readLines(queryResource.getInputStream()).stream()
                        .filter(line -> !line.matches("^\\s*--.*?"))
                        .collect(Collectors.joining("\n"));
            } catch (IOException e) {
                throw new DaoException(e);
            }
        } else {
            return query;
        }
    }

    public static class QueryData {
        private Long id;
        private String name;
        private String query;

        public String getQuery() {
            return query;
        }

        public void setQuery(String query) {
            this.query = query;
        }

        public PackingListType toPackingListType() {
            return PackingListType.newBuilder().id(getId()).name(getName()).build();
        }

        public Long getId() {
            return id;
        }

        public void setId(Long id) {
            this.id = id;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }
    }

}

